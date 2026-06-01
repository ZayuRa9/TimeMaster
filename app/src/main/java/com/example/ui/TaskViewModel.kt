package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ChecklistItem
import com.example.data.TaskEntity
import com.example.data.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TaskRepository
    private val prefs = application.getSharedPreferences("timemaster_prefs", android.content.Context.MODE_PRIVATE)

    init {
        val database = AppDatabase.getDatabase(application)
        repository = TaskRepository(application, database.taskDao())
    }

    val allTasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Calendar & Navigation State
    private val _selectedDate = MutableStateFlow(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _currentTab = MutableStateFlow(0) // 0: Input Form & Entry Table, 1: Grid Calendar views
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    private val _calendarViewMode = MutableStateFlow("WEEKLY") // "DAILY", "WEEKLY", "MONTHLY"
    val calendarViewMode: StateFlow<String> = _calendarViewMode.asStateFlow()

    // Partner Space State
    private val _isPartnerConnected = MutableStateFlow(prefs.getBoolean("is_partner_connected", false))
    val isPartnerConnected: StateFlow<Boolean> = _isPartnerConnected.asStateFlow()

    private val _partnerName = MutableStateFlow(prefs.getString("partner_name", "") ?: "")
    val partnerName: StateFlow<String> = _partnerName.asStateFlow()

    // Observe partner tasks directly from Room!
    val partnerTasks: StateFlow<List<TaskEntity>> = repository.partnerTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Passcodes and invitation letters (thư ngỏ)
    private val _myConnectionCode = MutableStateFlow("")
    val myConnectionCode: StateFlow<String> = _myConnectionCode.asStateFlow()

    private val _sentProposalCode = MutableStateFlow(prefs.getString("sent_proposal_code", "") ?: "")
    val sentProposalCode: StateFlow<String> = _sentProposalCode.asStateFlow()

    private val _isProposalSent = MutableStateFlow(prefs.getBoolean("is_proposal_sent", false))
    val isProposalSent: StateFlow<Boolean> = _isProposalSent.asStateFlow()

    private val _incomingProposalSender = MutableStateFlow<String?>(prefs.getString("incoming_proposal_sender", null))
    val incomingProposalSender: StateFlow<String?> = _incomingProposalSender.asStateFlow()

    private val _incomingProposalCode = MutableStateFlow<String?>(prefs.getString("incoming_proposal_code", null))
    val incomingProposalCode: StateFlow<String?> = _incomingProposalCode.asStateFlow()

    init {
        // Generate and persist constant personal connection code
        var code = prefs.getString("my_connection_code", "") ?: ""
        if (code.isBlank()) {
            val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
            val randomSuffix = (1..4).map { chars.random() }.joinToString("")
            code = "ME-$randomSuffix"
            prefs.edit().putString("my_connection_code", code).apply()
        }
        _myConnectionCode.value = code
    }

    private fun savePrefs() {
        prefs.edit().apply {
            putBoolean("is_partner_connected", _isPartnerConnected.value)
            putString("partner_name", _partnerName.value)
            putString("sent_proposal_code", _sentProposalCode.value)
            putBoolean("is_proposal_sent", _isProposalSent.value)
            putString("incoming_proposal_sender", _incomingProposalSender.value)
            putString("incoming_proposal_code", _incomingProposalCode.value)
            apply()
        }
    }

    fun connectPartner(name: String) {
        _partnerName.value = name.ifBlank { "Bạn bè" }
        _isPartnerConnected.value = true
        _isProposalSent.value = false
        _sentProposalCode.value = ""
        savePrefs()
    }

    fun sendConnectionProposal(code: String, name: String) {
        _sentProposalCode.value = code
        _partnerName.value = name.ifBlank { "Bạn bè" }
        _isProposalSent.value = true
        savePrefs()
    }

    fun acceptIncomingProposal() {
        val name = _incomingProposalSender.value ?: "Bạn bè"
        _partnerName.value = name
        _isPartnerConnected.value = true
        _incomingProposalSender.value = null
        _incomingProposalCode.value = null
        savePrefs()
    }

    fun declineIncomingProposal() {
        _incomingProposalSender.value = null
        _incomingProposalCode.value = null
        savePrefs()
    }

    fun simulatePartnerAcceptance() {
        if (_isProposalSent.value) {
            _isPartnerConnected.value = true
            _isProposalSent.value = false
            savePrefs()
        }
    }

    fun disconnectPartner() {
        _isPartnerConnected.value = false
        _isProposalSent.value = false
        _sentProposalCode.value = ""
        _incomingProposalSender.value = null
        _incomingProposalCode.value = null
        savePrefs()
    }

    fun addPartnerTask(
        date: String,
        departureTime: String,
        arrivalTime: String,
        content: String,
        checklistItems: List<ChecklistItem>,
        recurringType: String,
        colorHex: String,
        driveFileUri: String? = null,
        driveFileName: String? = null
    ) {
        viewModelScope.launch {
            val newTask = TaskEntity(
                date = date,
                departureTime = departureTime,
                arrivalTime = arrivalTime,
                content = content,
                checklistRaw = ChecklistItem.serialize(checklistItems),
                recurringType = recurringType,
                colorHex = colorHex,
                driveFileUri = driveFileUri,
                driveFileName = driveFileName,
                isPartnerTask = true
            )
            repository.insertTask(newTask)
        }
    }

    fun deletePartnerTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun togglePartnerChecklistItem(task: TaskEntity, itemId: String) {
        viewModelScope.launch {
            val list = ChecklistItem.deserialize(task.checklistRaw)
            val updatedList = list.map { item ->
                if (item.id == itemId) item.copy(isChecked = !item.isChecked) else item
            }
            val updatedTask = task.copy(checklistRaw = ChecklistItem.serialize(updatedList))
            repository.updateTask(updatedTask)
        }
    }

    fun selectDate(date: String) {
        _selectedDate.value = date
    }

    fun setTab(tabIndex: Int) {
        _currentTab.value = tabIndex
    }

    fun setCalendarViewMode(mode: String) {
        _calendarViewMode.value = mode
    }

    // Task manipulation
    fun addTask(
        date: String,
        departureTime: String,
        arrivalTime: String,
        content: String,
        checklistItems: List<ChecklistItem>,
        recurringType: String,
        colorHex: String,
        driveFileUri: String? = null,
        driveFileName: String? = null
    ) {
        viewModelScope.launch {
            val newTask = TaskEntity(
                date = date,
                departureTime = departureTime,
                arrivalTime = arrivalTime,
                content = content,
                checklistRaw = ChecklistItem.serialize(checklistItems),
                recurringType = recurringType,
                colorHex = colorHex,
                driveFileUri = driveFileUri,
                driveFileName = driveFileName,
                isPartnerTask = false
            )
            repository.insertTask(newTask)
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun toggleChecklistItem(task: TaskEntity, itemId: String) {
        viewModelScope.launch {
            val list = ChecklistItem.deserialize(task.checklistRaw)
            val updatedList = list.map { item ->
                if (item.id == itemId) {
                    item.copy(isChecked = !item.isChecked)
                } else {
                    item
                }
            }
            val updatedTask = task.copy(checklistRaw = ChecklistItem.serialize(updatedList))
            repository.updateTask(updatedTask)
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return TaskViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
