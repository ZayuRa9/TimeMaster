package com.example.ui

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ChecklistItem
import com.example.data.TaskEntity
import com.example.util.AppLauncherUtils
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: TaskViewModel) {
    val context = LocalContext.current
    val allTasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val calendarViewMode by viewModel.calendarViewMode.collectAsStateWithLifecycle()

    val isPartnerConnected by viewModel.isPartnerConnected.collectAsStateWithLifecycle()
    val partnerName by viewModel.partnerName.collectAsStateWithLifecycle()
    val partnerTasks by viewModel.partnerTasks.collectAsStateWithLifecycle()

    val myConnectionCode by viewModel.myConnectionCode.collectAsStateWithLifecycle()
    val sentProposalCode by viewModel.sentProposalCode.collectAsStateWithLifecycle()
    val isProposalSent by viewModel.isProposalSent.collectAsStateWithLifecycle()
    val incomingProposalSender by viewModel.incomingProposalSender.collectAsStateWithLifecycle()
    val incomingProposalCode by viewModel.incomingProposalCode.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var showAddPartnerDialog by remember { mutableStateOf(false) }

    val headerDateStr = remember {
        val sdf = SimpleDateFormat("EEEE, 'Ng' d 'Tháng' M", Locale("vi", "VN"))
        sdf.format(Date())
            .replace("Ng ", "Ngày ")
            .replace("Monday", "Thứ Hai")
            .replace("Tuesday", "Thứ Ba")
            .replace("Wednesday", "Thứ Tư")
            .replace("Thursday", "Thứ Năm")
            .replace("Friday", "Thứ Sáu")
            .replace("Saturday", "Thứ Bảy")
            .replace("Sunday", "Chủ Nhật")
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .statusBarsPadding()
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 4.dp)
            ) {
                ThreeDBlock(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color(0xFF0284C7), // Bright Ocean-breeze blue
                    borderColor = Color(0xFF0C4A6E),     // Deep ocean outline text
                    shadowColor = Color(0xFF0C4A6E).copy(alpha = 0.2f),
                    cornerRadius = 16.dp,
                    borderWidth = 2.dp,
                    shadowOffset = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "TimeMaster Pro",
                                fontWeight = FontWeight.Black,
                                fontSize = 23.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                headerDateStr,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE0F2FE)
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { 
                                    viewModel.setTab(1) // Navigate directly to calendar view
                                },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.25f))
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CalendarMonth,
                                    contentDescription = "Lịch Trình",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            
                            IconButton(
                                onClick = { 
                                    showAddDialog = true
                                },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .testTag("open_add_dialog_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = "Tạo lịch trình",
                                    tint = Color(0xFF0284C7),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            Column {
                // Taskbar Link Icons at the bottom level
                BottomAppLinksBar()
                
                // Primary tab navigation
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = currentTab == 0,
                        onClick = { viewModel.setTab(0) },
                        icon = { Icon(Icons.Filled.EditCalendar, contentDescription = "Bảng Nhập Liệu") },
                        label = { Text("Nhập Liệu") },
                        modifier = Modifier.testTag("nav_input_tab")
                    )
                    NavigationBarItem(
                        selected = currentTab == 1,
                        onClick = { viewModel.setTab(1) },
                        icon = { Icon(Icons.Filled.CalendarMonth, contentDescription = "Lịch Trình Chi Tiết") },
                        label = { Text("Lịch Trình") },
                        modifier = Modifier.testTag("nav_calendar_tab")
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                0 -> TaskEntryListView(
                    allTasks = allTasks,
                    onDelete = { viewModel.deleteTask(it) },
                    onToggleCheckItem = { task, itemId -> viewModel.toggleChecklistItem(task, itemId) },
                    isPartnerConnected = isPartnerConnected,
                    partnerName = partnerName,
                    partnerTasks = partnerTasks,
                    myConnectionCode = myConnectionCode,
                    sentProposalCode = sentProposalCode,
                    isProposalSent = isProposalSent,
                    incomingProposalSender = incomingProposalSender,
                    incomingProposalCode = incomingProposalCode,
                    onSendProposal = { code, name -> viewModel.sendConnectionProposal(code, name) },
                    onAcceptIncomingProposal = { viewModel.acceptIncomingProposal() },
                    onDeclineIncomingProposal = { viewModel.declineIncomingProposal() },
                    onSimulatePartnerAccept = { viewModel.simulatePartnerAcceptance() },
                    onDisconnectPartner = { viewModel.disconnectPartner() },
                    onDeletePartnerTask = { viewModel.deletePartnerTask(it) },
                    onTogglePartnerCheckItem = { task, itemId -> viewModel.togglePartnerChecklistItem(task, itemId) },
                    onTriggerAddPartnerTask = { showAddPartnerDialog = true }
                )
                1 -> CalendarViewScreen(
                    allTasks = allTasks,
                    selectedDateStr = selectedDate,
                    viewMode = calendarViewMode,
                    onChangeViewMode = { viewModel.setCalendarViewMode(it) },
                    onSelectDate = { viewModel.selectDate(it) },
                    onToggleCheckItem = { task, itemId -> viewModel.toggleChecklistItem(task, itemId) }
                )
            }

            // Dialog for creating tasks/schedules
            if (showAddDialog) {
                AddTaskDialog(
                    onDismiss = { showAddDialog = false },
                    onConfirm = { date, depTime, arrTime, content, checklist, repeatMode, colorHex, driveFileUri, driveFileName ->
                        viewModel.addTask(date, depTime, arrTime, content, checklist, repeatMode, colorHex, driveFileUri, driveFileName)
                        showAddDialog = false
                        Toast.makeText(context, "Đã thêm lịch trình & kích hoạt nhắc nhở!", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // Dialog for creating tasks/schedules for partner
            if (showAddPartnerDialog) {
                AddTaskDialog(
                    onDismiss = { showAddPartnerDialog = false },
                    onConfirm = { date, depTime, arrTime, content, checklist, repeatMode, colorHex, driveFileUri, driveFileName ->
                        viewModel.addPartnerTask(date, depTime, arrTime, content, checklist, repeatMode, colorHex, driveFileUri, driveFileName)
                        showAddPartnerDialog = false
                        Toast.makeText(context, "Đã đặt lịch trình & chia sẻ với $partnerName thành công!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

@Composable
fun TaskEntryListView(
    allTasks: List<TaskEntity>,
    onDelete: (TaskEntity) -> Unit,
    onToggleCheckItem: (TaskEntity, String) -> Unit,
    isPartnerConnected: Boolean,
    partnerName: String,
    partnerTasks: List<TaskEntity>,
    myConnectionCode: String,
    sentProposalCode: String,
    isProposalSent: Boolean,
    incomingProposalSender: String?,
    incomingProposalCode: String?,
    onSendProposal: (code: String, name: String) -> Unit,
    onAcceptIncomingProposal: () -> Unit,
    onDeclineIncomingProposal: () -> Unit,
    onSimulatePartnerAccept: () -> Unit,
    onDisconnectPartner: () -> Unit,
    onDeletePartnerTask: (TaskEntity) -> Unit,
    onTogglePartnerCheckItem: (TaskEntity, String) -> Unit,
    onTriggerAddPartnerTask: () -> Unit
) {
    var expandedTaskId by remember { mutableStateOf<Int?>(null) }
    var expandedPartnerTaskId by remember { mutableStateOf<Int?>(null) }
    var partnerInputCode by remember { mutableStateOf("") }
    var partnerInputName by remember { mutableStateOf("") }
    var isConnectionSectionExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // --- 1. My Schedule Section ---
        item {
            Text(
                "Danh Sách Lịch Trình Của Bạn",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        if (allTasks.isEmpty()) {
            item {
                ThreeDBlock(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    backgroundColor = Color.White,
                    borderColor = Color(0xFF0284C7),
                    shadowColor = Color(0xFF0284C7).copy(alpha = 0.15f),
                    cornerRadius = 14.dp,
                    borderWidth = 2.dp,
                    shadowOffset = 4.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("✨", fontSize = 32.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Chưa có lịch trình cá nhân nào được tạo.\nHãy nhấn nút \"Thêm Lịch Trình\" (➕) ở trên để bắt đầu!",
                                textAlign = TextAlign.Center,
                                color = Color(0xFF0C4A6E),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        } else {
            item {
                ThreeDBlock(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    backgroundColor = Color(0xFF0284C7), // Vibrant primary sky blue
                    borderColor = Color(0xFF0C4A6E), // Deep ocean slate
                    shadowColor = Color(0xFF0C4A6E).copy(alpha = 0.2f),
                    cornerRadius = 12.dp,
                    borderWidth = 1.8.dp,
                    shadowOffset = 3.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("STT", modifier = Modifier.weight(0.7f), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                        Text("Ngày", modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                        Text("Đi / Đến", modifier = Modifier.weight(2.3f), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                        Text("Nội dung", modifier = Modifier.weight(3.5f), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                        Text("Check", modifier = Modifier.weight(1.8f), fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.End, color = Color.White)
                    }
                }
            }

            itemsIndexed(allTasks) { index, task ->
                val checklistItems = ChecklistItem.deserialize(task.checklistRaw)
                val totalCheck = checklistItems.size
                val checkedCount = checklistItems.count { it.isChecked }
                val isExpanded = expandedTaskId == task.id

                val taskColor = remember(task.colorHex) {
                    runCatching { Color(android.graphics.Color.parseColor(task.colorHex)) }
                        .getOrDefault(Color(0xFF0284C7))
                }

                ThreeDBlock(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedTaskId = if (isExpanded) null else task.id }
                        .padding(vertical = 4.dp),
                    backgroundColor = Color.White,
                    borderColor = taskColor,
                    shadowColor = taskColor.copy(alpha = 0.15f),
                    cornerRadius = 14.dp,
                    borderWidth = if (isExpanded) 3.dp else 2.dp,
                    shadowOffset = 4.dp
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Circular colored marker index
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(taskColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("${index + 1}", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                            }
                            Spacer(modifier = Modifier.width(6.dp))

                            Text(formatTaskDate(task.date), modifier = Modifier.weight(2f), fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
                            
                            Column(modifier = Modifier.weight(2.3f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🛫 ", fontSize = 11.sp)
                                    Text(task.departureTime, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0C4A6E))
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🛬 ", fontSize = 11.sp)
                                    Text(task.arrivalTime, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF4F1A))
                                }
                            }

                            Text(task.content, modifier = Modifier.weight(3.5f), maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0C4A6E))
                            
                            Row(modifier = Modifier.weight(1.8f), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                                Text("$checkedCount/$totalCheck", fontSize = 11.sp, fontWeight = FontWeight.Black, color = if (checkedCount == totalCheck && totalCheck > 0) Color(0xFF10B981) else Color.DarkGray, modifier = Modifier.padding(end = 4.dp))
                                Icon(imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, contentDescription = null, tint = taskColor, modifier = Modifier.size(16.dp))
                            }
                        }

                        if (isExpanded) {
                            Divider(color = taskColor.copy(alpha = 0.3f), thickness = 1.dp)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White.copy(alpha = 0.5f))
                                    .padding(12.dp)
                            ) {
                                if (task.recurringType != "NONE") {
                                    AssistChip(
                                        onClick = {},
                                        label = { Text("Lặp lại: ${getRecuringLabel(task.recurringType)}", fontSize = 11.sp) },
                                        leadingIcon = { Icon(Icons.Filled.Repeat, null, modifier = Modifier.size(12.dp)) }
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                }

                                if (!task.driveFileName.isNullOrBlank()) {
                                    Text("Tài liệu hành trình:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                            .clickable {
                                                runCatching {
                                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(task.driveFileUri ?: "https://drive.google.com"))
                                                    intent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                    context.startActivity(intent)
                                                }.onFailure {
                                                    Toast.makeText(context, "Mở tài liệu Drive: ${task.driveFileName}", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Filled.Cloud, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = task.driveFileName ?: "",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.weight(1f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Icon(Icons.Filled.OpenInNew, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                }

                                Text("Checklist chuẩn bị:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 6.dp))

                                if (checklistItems.isEmpty()) {
                                    Text("Không có checklist chuẩn bị.", fontSize = 12.sp, color = Color.Gray)
                                } else {
                                    checklistItems.forEachIndexed { itemIndex, item ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { onToggleCheckItem(task, item.id) }
                                                .padding(vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val bulletColor = when (itemIndex % 3) {
                                                0 -> Color(0xFF3B82F6)
                                                1 -> Color(0xFF10B981)
                                                else -> Color(0xFFF97316)
                                            }
                                            if (item.isChecked) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(22.dp)
                                                        .clip(CircleShape)
                                                        .background(bulletColor),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Check,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                }
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .size(22.dp)
                                                        .border(1.5.dp, Color.LightGray, CircleShape)
                                                        .background(Color.White, CircleShape)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                item.text,
                                                fontSize = 13.sp,
                                                style = if (item.isChecked) {
                                                    MaterialTheme.typography.bodyMedium.copy(color = Color.Gray, textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)
                                                } else {
                                                    MaterialTheme.typography.bodyMedium
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    TextButton(onClick = { onDelete(task) }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Xóa")
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Xóa Lịch Trình")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Display next task detail if list is not empty
        val selectedNextTask = allTasks.find { it.id == expandedTaskId } ?: allTasks.firstOrNull()
        selectedNextTask?.let { task ->
            item {
                Spacer(modifier = Modifier.height(10.dp))
                val checklistItems = ChecklistItem.deserialize(task.checklistRaw)

                ThreeDBlock(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color(0xFFF0F9FF), // Sky blue tint background
                    borderColor = Color(0xFF0284C7),
                    shadowColor = Color(0xFF0284C7).copy(alpha = 0.15f),
                    cornerRadius = 16.dp,
                    borderWidth = 2.dp,
                    shadowOffset = 4.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Chi tiết công việc tiếp theo của bạn",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black, color = Color(0xFF0C4A6E))
                            )
                            Box(modifier = Modifier.background(Color(0xFFE0F2FE), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                                val originalIndex = allTasks.indexOf(task)
                                Text(text = "STT: %02d".format(if (originalIndex != -1) originalIndex + 1 else 1), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1.1f)) {
                                Text(text = "Nội dung:", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Text(text = task.content, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0C4A6E), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Column(modifier = Modifier.weight(0.9f)) {
                                Text(text = "Khoảng thời gian:", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Text(text = "Đi: ${task.departureTime} - Đến: ${task.arrivalTime}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0C4A6E))
                            }
                        }

                        if (checklistItems.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(text = "Checklist chuẩn bị:", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                checklistItems.take(4).forEach { item ->
                                    val prefix = if (item.isChecked) "✓" else "○"
                                    Box(
                                        modifier = Modifier
                                            .background(Color.White, RoundedCornerShape(8.dp))
                                            .border(1.dp, Color(0xFFBAE6FD), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(text = "$prefix ${item.text}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0C4A6E))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 2. LINK DEVICES & PARTNER CONTAINER (Collapsible) ---
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))
            
            ThreeDBlock(
                modifier = Modifier
                    .clickable { isConnectionSectionExpanded = !isConnectionSectionExpanded }
                    .testTag("toggle_partner_connection_section"),
                backgroundColor = if (isPartnerConnected) Color.White else Color(0xFFF5F5F5),
                borderColor = if (isPartnerConnected) Color(0xFF22C55E) else Color(0xFF0C4A6E),
                shadowColor = (if (isPartnerConnected) Color(0xFF22C55E) else Color(0xFF0C4A6E)).copy(alpha = 0.15f),
                cornerRadius = 14.dp,
                borderWidth = 2.dp,
                shadowOffset = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isPartnerConnected) Icons.Filled.CloudSync else Icons.Filled.Share,
                            contentDescription = null,
                            tint = if (isPartnerConnected) Color(0xFF10B981) else Color(0xFF2563EB),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "Kết nối với bạn bè",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isPartnerConnected) "Đã liên kết với $partnerName 💚" 
                                       else if (incomingProposalSender != null) "📬 Có thư ngỏ kết nối từ $incomingProposalSender !" 
                                       else if (isProposalSent) "Đang chờ đối phương xác nhận..." 
                                       else "Mật mã & Thư ngỏ",
                                fontSize = 11.sp,
                                color = if (incomingProposalSender != null) Color(0xFFF97316) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        imageVector = if (isConnectionSectionExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = "Thu gọn/Mở rộng",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        if (isConnectionSectionExpanded) {
            if (!isPartnerConnected) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Mỗi tài khoản có một mật mã riêng. Khi gửi mật mã đúng, đối phương sẽ nhận được một thư ngỏ giới thiệu thiết bị của nhau để chính thức đồng ý liên kết.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Mine passcode container
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    "MẬT MÃ LIÊN KẾT THIẾT BỊ CỦA BẠN:",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = myConnectionCode,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    TextButton(
                                        onClick = {
                                            runCatching {
                                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                                val clip = android.content.ClipData.newPlainText("PASSCODE", myConnectionCode)
                                                clipboard.setPrimaryClip(clip)
                                            }
                                            Toast.makeText(context, "Đã sao chép mật mã: $myConnectionCode", Toast.LENGTH_SHORT).show()
                                        },
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Icon(Icons.Filled.ContentCopy, null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Sao chép", fontSize = 11.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Text("GỬI THƯ NGỎ KẾT NỐI:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground)
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = partnerInputName,
                                onValueChange = { partnerInputName = it },
                                placeholder = { Text("Tên đối phương") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                leadingIcon = { Icon(Icons.Filled.Person, null, tint = Color.Gray) }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = partnerInputCode,
                                onValueChange = { partnerInputCode = it },
                                placeholder = { Text("Nhập mật mã đối phương") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                leadingIcon = { Icon(Icons.Filled.Key, null, tint = Color.Gray) }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    if (partnerInputCode.isNotBlank()) {
                                        onSendProposal(partnerInputCode.trim(), partnerInputName.trim())
                                        Toast.makeText(context, "Đã gửi thư ngỏ giới thiệu kết nối thành công!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Hãy nhập mật mã đối phương để gửi thư ngỏ!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Gửi thư ngỏ liên kết thiết bị", fontWeight = FontWeight.Bold)
                            }

                            if (isProposalSent) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                "Đang chờ đối phương chấp nhận thư ngỏ...",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Đã chuyển thư tới mã đối tác: $sentProposalCode", fontSize = 11.sp, color = Color.Gray)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Button(
                                            onClick = onSimulatePartnerAccept,
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                            modifier = Modifier.align(Alignment.End)
                                        ) {
                                            Text("Phê duyệt nhanh", fontSize = 10.sp, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Incoming connection invitation "Thư ngỏ"
                if (incomingProposalSender != null) {
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
                            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFF97316))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Mail, contentDescription = null, tint = Color(0xFFF97316), modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "📬 THƯ NGỎ KẾT NỐI ĐỀ XUẤT",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp,
                                        color = Color(0xFF7C2D12)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = "Xin chào bạn thân mến!\n\nMình là $incomingProposalSender, mật mã $incomingProposalCode. Mình gửi thư ngỏ nhận kết nối chung không gian thời gian phối hợp lịch đi, đến, checklist và tài liệu cùng bạn trên ứng dụng TimeMaster Pro.\n\nSau khi bạn nhấn xác nhận đồng ý, chúng ta có thể:\n- Xem được đầy đủ lịch thời gian biểu phân tách màu sắc riêng biệt của nhau.\n- Chủ động lên cuộc họp Google Meet trực tiếp.\n- Đặt thời gian và lịch trình trực tiếp cho đối phương.\n- Share tài liệu lưu trữ Google Drive với nhau.",
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp,
                                        color = Color(0xFF7C2D12)
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = onDeclineIncomingProposal,
                                        modifier = Modifier.weight(1.3f),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                                    ) {
                                        Text("Từ chối nhận", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = onAcceptIncomingProposal,
                                        modifier = Modifier.weight(1.7f),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                                    ) {
                                        Text("Chấp nhận liên kết", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // PARTNER CONNECTED SPACE (Không gian của đối phương)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF4CAF50))
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "KHÔNG GIAN CỦA $partnerName",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                IconButton(onClick = onDisconnectPartner, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Filled.LinkOff, contentDescription = "Ngắt Kết Nối", tint = Color.Red, modifier = Modifier.size(18.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Trạng thái: Đã kết nối thành công 💚",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            Divider(color = Color(0xFFCAC4D0).copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(10.dp))

                            // Interaction Tools Row
                            Text("TỰ ĐỘNG ĐỒNG BỘ & ĐIỀU PHỐI:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Invite to Meeting
                                Button(
                                    onClick = {
                                        AppLauncherUtils.launchGoogleMeet(context)
                                        Toast.makeText(context, "Đã gửi lời mời họp Meet tới $partnerName!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                                ) {
                                    Icon(Icons.Filled.VideoCall, null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Mở Meet", fontSize = 11.sp)
                                }

                                // Share Documents on Drive
                                Button(
                                    onClick = {
                                        AppLauncherUtils.launchGoogleDrive(context)
                                        Toast.makeText(context, "Đã chia sẻ tài liệu lưu trữ Drive cùng $partnerName!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                                ) {
                                    Icon(Icons.Filled.FolderShared, null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Share Drive", fontSize = 11.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Set/Assign schedules on behalf of partner (Đặt thời gian cho nhau)
                            Button(
                                onClick = onTriggerAddPartnerTask,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Filled.AddTask, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Đặt thời gian/Lịch hẹn cho $partnerName", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "XEM LỊCH TRÌNH CỦA ĐỐI PHƯƠNG: $partnerName",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )

                            if (partnerTasks.isEmpty()) {
                                Text(
                                    "Chưa có lịch trình nào cho đối phương. Hãy nhấn nút đặt thời gian ở trên để thêm!",
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                                )
                            } else {
                                // Partner Schedule List
                                partnerTasks.forEachIndexed { idx, pTask ->
                                    val isPartnerExpanded = expandedPartnerTaskId == pTask.id
                                    val pChecklist = ChecklistItem.deserialize(pTask.checklistRaw)
                                    val pTotal = pChecklist.size
                                    val pChecked = pChecklist.count { it.isChecked }

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clickable { expandedPartnerTaskId = if (isPartnerExpanded) null else pTask.id },
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2)), // Soft pastel rose/pink
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDA4AF))
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("${idx + 1}", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(end = 6.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(pTask.content, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                    Text("Đi: ${pTask.departureTime} -> Đến: ${pTask.arrivalTime} | ${formatTaskDate(pTask.date)}", fontSize = 10.sp, color = Color.DarkGray)
                                                }
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text("$pChecked/$pTotal", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF9F1239))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Icon(imageVector = if (isPartnerExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, contentDescription = null, modifier = Modifier.size(16.dp))
                                                }
                                            }

                                            if (isPartnerExpanded) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Divider(color = Color(0xFFFDA4AF).copy(alpha = 0.5f))
                                                Spacer(modifier = Modifier.height(6.dp))

                                                if (!pTask.driveFileName.isNullOrBlank()) {
                                                    Text("Tài liệu hành trình:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF9F1239))
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .background(Color(0xFFFFE4E6), RoundedCornerShape(8.dp))
                                                            .clickable {
                                                                runCatching {
                                                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(pTask.driveFileUri ?: "https://drive.google.com"))
                                                                    intent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                                    context.startActivity(intent)
                                                                }.onFailure {
                                                                    Toast.makeText(context, "Mở tài liệu Drive đối phương: ${pTask.driveFileName}", Toast.LENGTH_SHORT).show()
                                                                }
                                                            }
                                                            .padding(6.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(Icons.Filled.Cloud, null, tint = Color(0xFF9F1239), modifier = Modifier.size(16.dp))
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = pTask.driveFileName ?: "",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.weight(1f),
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                        Icon(Icons.Filled.OpenInNew, null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                                                    }
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                }

                                                Text("Checklist của đối phương:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF9F1239))
                                                if (pChecklist.isEmpty()) {
                                                    Text("Không có checklist nào.", fontSize = 11.sp, color = Color.Gray)
                                                } else {
                                                    pChecklist.forEach { item ->
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .clickable { onTogglePartnerCheckItem(pTask, item.id) }
                                                                .padding(vertical = 2.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Checkbox(
                                                                checked = item.isChecked,
                                                                onCheckedChange = { onTogglePartnerCheckItem(pTask, item.id) },
                                                                modifier = Modifier.size(20.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                            Text(item.text, fontSize = 11.sp, style = if (item.isChecked) MaterialTheme.typography.bodyMedium.copy(color = Color.Gray) else MaterialTheme.typography.bodyMedium)
                                                        }
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(6.dp))
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                                    TextButton(
                                                        onClick = { onDeletePartnerTask(pTask) },
                                                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                                                    ) {
                                                        Icon(Icons.Filled.Delete, contentDescription = "Hủy lịch", modifier = Modifier.size(12.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Hủy lịch này", fontSize = 11.sp)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun borderStrokeForTask(task: TaskEntity, isExpanded: Boolean): androidx.compose.foundation.BorderStroke? {
    val baseColor = runCatching { Color(android.graphics.Color.parseColor(task.colorHex)) }
        .getOrDefault(MaterialTheme.colorScheme.primary)
    return if (isExpanded) {
        androidx.compose.foundation.BorderStroke(2.dp, baseColor)
    } else {
        androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    }
}

// Calendar View Screen incorporating Switchers for Daily, Weekly, and Monthly views
@Composable
fun CalendarViewScreen(
    allTasks: List<TaskEntity>,
    selectedDateStr: String,
    viewMode: String,
    onChangeViewMode: (String) -> Unit,
    onSelectDate: (String) -> Unit,
    onToggleCheckItem: (TaskEntity, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Switch view buttons styled as beautiful 3D block tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val modes = listOf("DAILY" to "Ngày", "WEEKLY" to "Tuần", "MONTHLY" to "Tháng")
            modes.forEach { (mode, label) ->
                val isSelected = viewMode == mode
                ThreeDBlock(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onChangeViewMode(mode) },
                    backgroundColor = if (isSelected) Color(0xFF0284C7) else Color.White,
                    borderColor = if (isSelected) Color(0xFF0C4A6E) else Color(0xFFBAE6FD),
                    shadowColor = (if (isSelected) Color(0xFF0C4A6E) else Color(0xFFBAE6FD)).copy(alpha = 0.15f),
                    cornerRadius = 14.dp,
                    borderWidth = 2.dp,
                    shadowOffset = 3.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else Color(0xFF0C4A6E),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Header Date Info Navigator
        CalendarHeaderNavigator(selectedDateStr, viewMode, onSelectDate)

        Spacer(modifier = Modifier.height(12.dp))

        // Display current overview active mode
        when (viewMode) {
            "DAILY" -> DailyCalendarGrid(allTasks, selectedDateStr, onToggleCheckItem)
            "WEEKLY" -> WeeklyCalendarGrid(allTasks, selectedDateStr, onSelectDate, onToggleCheckItem)
            "MONTHLY" -> MonthlyCalendarGrid(allTasks, selectedDateStr, onSelectDate)
        }
    }
}

// Header Navigation: Day/Week/Month changer
@Composable
fun CalendarHeaderNavigator(
    selectedDateStr: String,
    viewMode: String,
    onSelectDate: (String) -> Unit
) {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val cal = Calendar.getInstance()
    cal.time = sdf.parse(selectedDateStr) ?: Date()

    val currentLabel = when (viewMode) {
        "DAILY" -> {
            val outSdf = SimpleDateFormat("EEEE, 'Ngày' dd/MM/yyyy", Locale("vi", "VN"))
            outSdf.format(cal.time)
        }
        "WEEKLY" -> {
            // Find start and end of week (Monday to Sunday)
            val currentDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            val diffToMonday = if (currentDayOfWeek == Calendar.SUNDAY) -6 else Calendar.MONDAY - currentDayOfWeek
            cal.add(Calendar.DAY_OF_YEAR, diffToMonday)
            val startStr = SimpleDateFormat("dd/MM", Locale.getDefault()).format(cal.time)
            cal.add(Calendar.DAY_OF_YEAR, 6)
            val endStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(cal.time)
            "Tuần: $startStr - $endStr"
        }
        "MONTHLY" -> {
            val outSdf = SimpleDateFormat("'Tháng' MM/yyyy", Locale("vi", "VN"))
            outSdf.format(cal.time)
        }
        else -> ""
    }

    ThreeDBlock(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color(0xFFE0F2FE), // Sea breeze color
        borderColor = Color(0xFF0284C7),
        shadowColor = Color(0xFF0284C7).copy(alpha = 0.15f),
        cornerRadius = 14.dp,
        borderWidth = 1.8.dp,
        shadowOffset = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                // Subtract offset
                val c = Calendar.getInstance()
                c.time = sdf.parse(selectedDateStr) ?: Date()
                when (viewMode) {
                    "DAILY" -> c.add(Calendar.DAY_OF_YEAR, -1)
                    "WEEKLY" -> c.add(Calendar.WEEK_OF_YEAR, -1)
                    "MONTHLY" -> c.add(Calendar.MONTH, -1)
                }
                onSelectDate(sdf.format(c.time))
            }) {
                Icon(Icons.Filled.ChevronLeft, "Lùi lại", tint = Color(0xFF0C4A6E))
            }

            Text(
                currentLabel,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                color = Color(0xFF0C4A6E),
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = {
                // Add offset
                val c = Calendar.getInstance()
                c.time = sdf.parse(selectedDateStr) ?: Date()
                when (viewMode) {
                    "DAILY" -> c.add(Calendar.DAY_OF_YEAR, 1)
                    "WEEKLY" -> c.add(Calendar.WEEK_OF_YEAR, 1)
                    "MONTHLY" -> c.add(Calendar.MONTH, 1)
                }
                onSelectDate(sdf.format(c.time))
            }) {
                Icon(Icons.Filled.ChevronRight, "Tiến tới", tint = Color(0xFF0C4A6E))
            }
        }
    }
}

// 1. Daily Calendar Core view: left hours, right items
@Composable
fun DailyCalendarGrid(
    allTasks: List<TaskEntity>,
    selectedDateStr: String,
    onToggleCheckItem: (TaskEntity, String) -> Unit
) {
    val dailyTasks = allTasks.filter { it.date == selectedDateStr }
        .sortedBy { it.departureTime }

    if (dailyTasks.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Không có lịch trình nào trong ngày này.", color = Color.Gray, fontSize = 14.sp)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(dailyTasks) { task ->
                val taskColor = remember(task.colorHex) {
                    runCatching { Color(android.graphics.Color.parseColor(task.colorHex)) }
                        .getOrDefault(Color(0xFF0284C7))
                }
                
                ThreeDBlock(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    backgroundColor = Color.White,
                    borderColor = taskColor,
                    shadowColor = taskColor.copy(alpha = 0.15f),
                    cornerRadius = 14.dp,
                    borderWidth = 2.dp,
                    shadowOffset = 4.dp
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(taskColor)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "${task.departureTime} - ${task.arrivalTime}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                                Text("STT: ${allTasks.indexOf(task) + 1}", fontSize = 11.sp, modifier = Modifier.padding(horizontal = 4.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            task.content,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        val checklistItems = ChecklistItem.deserialize(task.checklistRaw)
                        if (checklistItems.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Checklist chuẩn bị:",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            checklistItems.forEachIndexed { itemIndex, item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onToggleCheckItem(task, item.id) }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val bulletColor = when (itemIndex % 3) {
                                        0 -> Color(0xFF3B82F6)
                                        1 -> Color(0xFF10B981)
                                        else -> Color(0xFFF97316)
                                    }
                                    if (item.isChecked) {
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(bulletColor),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .border(1.5.dp, Color.LightGray, CircleShape)
                                                .background(Color.White, CircleShape)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        item.text,
                                        fontSize = 12.sp,
                                        style = if (item.isChecked) {
                                            MaterialTheme.typography.bodyMedium.copy(color = Color.Gray, textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)
                                        } else {
                                            MaterialTheme.typography.bodyMedium
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 2. Weekly Calendar Grid View: Left labels, Top columns (Mon-Sun tabs), contents inside
@Composable
fun WeeklyCalendarGrid(
    allTasks: List<TaskEntity>,
    selectedDateStr: String,
    onSelectDate: (String) -> Unit,
    onToggleCheckItem: (TaskEntity, String) -> Unit
) {
    // Math to get Monday-Sunday dates corresponding to selectedDateStr
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val datesOfWeek = remember(selectedDateStr) {
        val cal = Calendar.getInstance()
        cal.time = sdf.parse(selectedDateStr) ?: Date()
        val currentDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val diffToMonday = if (currentDayOfWeek == Calendar.SUNDAY) -6 else Calendar.MONDAY - currentDayOfWeek
        cal.add(Calendar.DAY_OF_YEAR, diffToMonday)
        
        val list = mutableListOf<Triple<String, String, String>>() // <DateStr, DOW_Name, SimpleDateStr>
        val daysInVi = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
        for (i in 0..6) {
            val dStr = sdf.format(cal.time)
            val dSimple = SimpleDateFormat("dd/MM", Locale.getDefault()).format(cal.time)
            list.add(Triple(dStr, daysInVi[i], dSimple))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    var selectedDayTab by remember { mutableStateOf(selectedDateStr) }
    // Ensure sync
    LaunchedEffect(selectedDateStr) {
        selectedDayTab = selectedDateStr
    }

    // Weekly grid of days on top styled as 3D selectable elements
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        datesOfWeek.forEach { (dateStr, dowLabel, daySimple) ->
            val isCurrent = dateStr == selectedDayTab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 2.dp)
            ) {
                if (isCurrent) {
                    ThreeDBlock(
                        modifier = Modifier.clickable {
                            selectedDayTab = dateStr
                            onSelectDate(dateStr)
                        },
                        backgroundColor = Color(0xFF0284C7),
                        borderColor = Color(0xFF0C4A6E),
                        shadowColor = Color(0xFF0C4A6E).copy(alpha = 0.2f),
                        cornerRadius = 10.dp,
                        borderWidth = 1.5.dp,
                        shadowOffset = 2.5.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(dowLabel, fontWeight = FontWeight.Black, fontSize = 11.sp, color = Color.White)
                            Text(daySimple, fontSize = 10.sp, color = Color.White)
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFBAE6FD), RoundedCornerShape(10.dp))
                            .clickable {
                                selectedDayTab = dateStr
                                onSelectDate(dateStr)
                            }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(dowLabel, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF0C4A6E))
                            Text(daySimple, fontSize = 10.sp, color = Color.DarkGray)
                        }
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Display Hourly block or list of tasks of selected day tab
    Text(
        "Bên trái là thời gian, tương ứng bên hàng dọc là ô công việc:",
        style = MaterialTheme.typography.titleSmall,
        color = Color.DarkGray,
        modifier = Modifier.padding(bottom = 6.dp)
    )

    val tasksOnScheduledTab = allTasks.filter { it.date == selectedDayTab }
        .sortedBy { it.departureTime }

    if (tasksOnScheduledTab.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Ngày này trống lịch trình.", color = Color.Gray, fontSize = 14.sp)
        }
    } else {
        // Vertical grid mapping left hand time - right hand dynamic cards with different colors
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tasksOnScheduledTab) { task ->
                val taskColor = runCatching { Color(android.graphics.Color.parseColor(task.colorHex)) }
                    .getOrDefault(MaterialTheme.colorScheme.primaryContainer)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // LEFT HAND TIME
                    Column(
                        modifier = Modifier
                            .width(60.dp)
                            .padding(end = 8.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            task.departureTime,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Đến: " + task.arrivalTime,
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }

                    // RIGHT HAND COLORED WORK BLOCK CARD
                    ThreeDBlock(
                        modifier = Modifier.weight(1f),
                        backgroundColor = Color.White,
                        borderColor = taskColor,
                        shadowColor = taskColor.copy(alpha = 0.15f),
                        cornerRadius = 12.dp,
                        borderWidth = 1.8.dp,
                        shadowOffset = 3.5.dp
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                "Nội dung: ${task.content}",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0C4A6E),
                                fontSize = 13.sp
                            )
                            val checklistItems = ChecklistItem.deserialize(task.checklistRaw)
                            if (checklistItems.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.padding(top = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Filled.Checklist,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                        tint = taskColor
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    val completed = checklistItems.count { it.isChecked }
                                    Text(
                                        "Checklist: $completed/${checklistItems.size}",
                                        fontSize = 11.sp,
                                        color = Color.DarkGray,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 3. Monthly Calendar Grid view: classic calendar grid, click date to jump
@Composable
fun MonthlyCalendarGrid(
    allTasks: List<TaskEntity>,
    selectedDateStr: String,
    onSelectDate: (String) -> Unit
) {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val selectedDate = sdf.parse(selectedDateStr) ?: Date()
    
    val cal = Calendar.getInstance()
    cal.time = selectedDate
    val currentMonth = cal.get(Calendar.MONTH)
    val currentYear = cal.get(Calendar.YEAR)

    // Calculate dates to render
    cal.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // Sunday=1, Monday=2..
    // Calculate prefix items
    val prefixDays = if (firstDayOfWeek == Calendar.SUNDAY) 6 else firstDayOfWeek - Calendar.MONDAY
    
    cal.add(Calendar.DAY_OF_MONTH, -prefixDays)
    
    val calendarDays = remember(selectedDateStr) {
        val list = mutableListOf<String>()
        // Show 42 days (6 weeks)
        for (i in 0 until 42) {
            list.add(sdf.format(cal.time))
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        list
    }

    Column {
        // Mon-Sun Titles
        Row(modifier = Modifier.fillMaxWidth()) {
            val dows = listOf("Hai", "Ba", "Tư", "Năm", "Sáu", "Bảy", "CN")
            dows.forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Color.DarkGray
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Month Days Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth(),
            userScrollEnabled = false,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(calendarDays.size) { index ->
                val dateStr = calendarDays[index]
                val d = sdf.parse(dateStr) ?: Date()
                val c = Calendar.getInstance().apply { time = d }
                val isCurrentMonth = c.get(Calendar.MONTH) == currentMonth
                val isSelected = dateStr == selectedDateStr
                
                val dayNum = c.get(Calendar.DAY_OF_MONTH).toString()
                
                // Fetch tasks on this day for bullet decoration
                val tasksOnDay = allTasks.filter { it.date == dateStr }

                if (isSelected) {
                    ThreeDBlock(
                        modifier = Modifier
                            .aspectRatio(1.0f)
                            .clickable { onSelectDate(dateStr) },
                        backgroundColor = Color(0xFFE0F2FE),
                        borderColor = Color(0xFF0284C7),
                        shadowColor = Color(0xFF0284C7).copy(alpha = 0.15f),
                        cornerRadius = 8.dp,
                        borderWidth = 1.8.dp,
                        shadowOffset = 2.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                dayNum,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0C4A6E)
                            )

                            // Decoration bullets representing scheduled tasks/events of different colors
                            if (tasksOnDay.isNotEmpty()) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    modifier = Modifier.padding(bottom = 2.dp)
                                ) {
                                    tasksOnDay.take(3).forEach { task ->
                                        val colorVal = runCatching { Color(android.graphics.Color.parseColor(task.colorHex)) }
                                            .getOrDefault(Color(0xFF0284C7))
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(colorVal)
                                        )
                                    }
                                    if (tasksOnDay.size > 3) {
                                        Text("+", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .aspectRatio(1.0f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isCurrentMonth) Color.White
                                else Color.Transparent
                            )
                            .border(
                                1.dp,
                                if (isCurrentMonth) Color(0xFFBAE6FD).copy(alpha = 0.5f)
                                else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onSelectDate(dateStr) }
                            .padding(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            dayNum,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCurrentMonth) Color(0xFF0C4A6E) else Color.LightGray
                        )

                        // Decoration bullets representing scheduled tasks/events of different colors
                        if (tasksOnDay.isNotEmpty()) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                modifier = Modifier.padding(bottom = 2.dp)
                            ) {
                                tasksOnDay.take(3).forEach { task ->
                                    val colorVal = runCatching { Color(android.graphics.Color.parseColor(task.colorHex)) }
                                        .getOrDefault(Color(0xFF0284C7))
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(colorVal)
                                    )
                                }
                                if (tasksOnDay.size > 3) {
                                    Text("+", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }
        }
    }
}

// Dialog for adding a task/schedule (Nhập Liệu form)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        date: String,
        depTime: String,
        arrTime: String,
        content: String,
        checklist: List<ChecklistItem>,
        repeatMode: String,
        colorHex: String,
        driveFileUri: String?,
        driveFileName: String?
    ) -> Unit
) {
    val context = LocalContext.current
    val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())

    var dateStr by remember { mutableStateOf(sdfDate.format(Date())) }
    var depTimeStr by remember { mutableStateOf("08:00") }
    var arrTimeStr by remember { mutableStateOf("08:30") }
    var contentStr by remember { mutableStateOf("") }
    var recurringType by remember { mutableStateOf("NONE") }

    // Google Drive Attachment States
    var driveFileUri by remember { mutableStateOf<String?>(null) }
    var driveFileName by remember { mutableStateOf<String?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            driveFileUri = uri.toString()
            val contentResolver = context.contentResolver
            var name: String? = null
            runCatching {
                contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIdx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIdx != -1 && cursor.moveToFirst()) {
                        name = cursor.getString(nameIdx)
                    }
                }
            }
            if (name == null) {
                name = uri.lastPathSegment ?: "document_${System.currentTimeMillis()}"
            }
            driveFileName = name
            Toast.makeText(context, "Đã chọn file: $name. Tài liệu sẽ được đồng bộ lên Google Drive!", Toast.LENGTH_LONG).show()
        }
    }

    // Color list pickers
    val colors = listOf(
        "#FFCDD2", // Red
        "#C8E6C9", // Green
        "#BBDEFB", // Blue
        "#FFF9C4", // Yellow
        "#E1BEE7", // Purple
        "#FFE0B2"  // Orange
    )
    var selectedColorHex by remember { mutableStateOf(colors[2]) }

    // Checklist creation state
    var checklistInput by remember { mutableStateOf("") }
    val currentChecklist = remember { mutableStateListOf<ChecklistItem>() }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 12.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    ThreeDBlock(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        backgroundColor = Color(0xFF0284C7),
                        borderColor = Color(0xFF0C4A6E),
                        shadowColor = Color(0xFF0C4A6E).copy(alpha = 0.2f),
                        cornerRadius = 12.dp,
                        borderWidth = 1.8.dp,
                        shadowOffset = 3.dp
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "THÊM LỊCH TRÌNH MỚI",
                                fontWeight = FontWeight.Black,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                        }
                    }
                }

                item {
                    // Date input form
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = dateStr,
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Ngày tháng năm *") },
                                modifier = Modifier.fillMaxWidth().testTag("add_input_date"),
                                leadingIcon = { Icon(Icons.Filled.DateRange, null, tint = MaterialTheme.colorScheme.primary) }
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable {
                                        val cal = Calendar.getInstance()
                                        runCatching {
                                            val parsed = sdfDate.parse(dateStr)
                                            if (parsed != null) cal.time = parsed
                                        }
                                        android.app.DatePickerDialog(
                                            context,
                                            { _, y, m, d ->
                                                val c = Calendar.getInstance()
                                                c.set(y, m, d)
                                                dateStr = sdfDate.format(c.time)
                                            },
                                            cal.get(Calendar.YEAR),
                                            cal.get(Calendar.MONTH),
                                            cal.get(Calendar.DAY_OF_MONTH)
                                        ).show()
                                    }
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { AppLauncherUtils.launchGoogleCalendar(context) },
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                        ) {
                            Text("📅", fontSize = 20.sp)
                        }
                    }
                }

                item {
                    // Departure Time Input Form
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = depTimeStr,
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Thời gian bắt đầu đi *") },
                                modifier = Modifier.fillMaxWidth().testTag("add_input_dep_time"),
                                leadingIcon = { Icon(Icons.Filled.DepartureBoard, null, tint = MaterialTheme.colorScheme.primary) }
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable {
                                        val parts = depTimeStr.split(":")
                                        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 8
                                        val min = parts.getOrNull(1)?.toIntOrNull() ?: 0
                                        android.app.TimePickerDialog(
                                            context,
                                            { _, h, m ->
                                                depTimeStr = String.format(Locale.getDefault(), "%02d:%02d", h, m)
                                            },
                                            hour,
                                            min,
                                            true
                                        ).show()
                                    }
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { AppLauncherUtils.launchClockAlarm(context) },
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                        ) {
                            Text("⏰", fontSize = 20.sp)
                        }
                    }
                }

                item {
                    // Arrival Time Input Form
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = arrTimeStr,
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Thời gian phải có mặt *") },
                                modifier = Modifier.fillMaxWidth().testTag("add_input_arr_time"),
                                leadingIcon = { Icon(Icons.Filled.Schedule, null, tint = MaterialTheme.colorScheme.primary) }
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable {
                                        val parts = arrTimeStr.split(":")
                                        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 8
                                        val min = parts.getOrNull(1)?.toIntOrNull() ?: 30
                                        android.app.TimePickerDialog(
                                            context,
                                            { _, h, m ->
                                                arrTimeStr = String.format(Locale.getDefault(), "%02d:%02d", h, m)
                                            },
                                            hour,
                                            min,
                                            true
                                        ).show()
                                    }
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { AppLauncherUtils.launchGoogleMeet(context) },
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                        ) {
                            Text("🎥", fontSize = 20.sp)
                        }
                    }
                }

                item {
                    // Nội dung
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = contentStr,
                            onValueChange = { contentStr = it },
                            label = { Text("Nội dung *") },
                            modifier = Modifier.weight(1f).testTag("add_input_content"),
                            leadingIcon = { Icon(Icons.Filled.Assignment, null) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { AppLauncherUtils.launchZalo(context) },
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                        ) {
                            Text("💬", fontSize = 20.sp)
                        }
                    }
                }

                item {
                    // Recurrence modes UI dropdown mock (using segment button list)
                    Text("Tự động lặp lại:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    val rTypes = listOf("NONE", "DAILY", "WEEKLY", "MONTHLY", "YEARLY")
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(rTypes) { type ->
                            val isSel = recurringType == type
                            FilterChip(
                                selected = isSel,
                                onClick = { recurringType = type },
                                label = { Text(type, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                item {
                    // Accent color select indicator
                    Text("Chọn màu hiển thị trên lịch:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        colors.forEach { hex ->
                            val isSelected = selectedColorHex == hex
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(hex)))
                                    .border(
                                        width = if (isSelected) 3.dp else 0.dp,
                                        color = if (isSelected) Color.DarkGray else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColorHex = hex }
                            )
                        }
                    }
                }

                item {
                    Divider()
                    // Preparation Checklist builder Section inside Add form
                    Text("Checklist chuẩn bị:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = checklistInput,
                            onValueChange = { checklistInput = it },
                            placeholder = { Text("Tên đồ dùng/sách vở...") },
                            modifier = Modifier.weight(1f).heightIn(max = 50.dp),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Filled.PlaylistAddCheck, null) }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = {
                                if (checklistInput.isNotBlank()) {
                                    currentChecklist.add(
                                        ChecklistItem(
                                            id = UUID.randomUUID().toString(),
                                            text = checklistInput.trim(),
                                            isChecked = false
                                        )
                                    )
                                    checklistInput = ""
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                        ) {
                            Text("➕", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = { AppLauncherUtils.launchZalo(context) },
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                        ) {
                            Text("💬", fontSize = 20.sp)
                        }
                    }

                    // Display preview list
                    if (currentChecklist.isNotEmpty()) {
                        Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                            currentChecklist.forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("✅", fontSize = 12.sp, modifier = Modifier.padding(end = 4.dp))
                                        Text(item.text, fontSize = 12.sp)
                                    }
                                    IconButton(
                                        onClick = { currentChecklist.remove(item) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Filled.Close, contentDescription = "Xóa", tint = Color.Red, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Divider()
                    Text("Tải tài liệu lên Drive đám mây:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1f)) {
                            if (driveFileName != null) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.Cloud, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(driveFileName ?: "", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text("Lưu trữ đồng bộ trên Google Drive ☁️", fontSize = 10.sp, color = Color.Gray)
                                    }
                                    IconButton(onClick = {
                                        driveFileUri = null
                                        driveFileName = null
                                    }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Filled.Close, contentDescription = "gỡ", tint = Color.Red, modifier = Modifier.size(16.dp))
                                    }
                                 }
                            } else {
                                Button(
                                    onClick = {
                                        runCatching {
                                            filePickerLauncher.launch("*/*")
                                        }.onFailure {
                                            Toast.makeText(context, "Mô phỏng đính kèm tệp...", Toast.LENGTH_SHORT).show()
                                            driveFileName = "baocao_hanhtrinh_${System.currentTimeMillis() % 1000}.pdf"
                                            driveFileUri = "https://drive.google.com/open?id=mock-file-id"
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Filled.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Chọn tài liệu thiết bị", fontSize = 13.sp)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { AppLauncherUtils.launchGoogleDrive(context) },
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                        ) {
                            Text("☁️", fontSize = 20.sp)
                        }
                    }
                }

                item {
                    // Action Buttons Styled as gorgeous 3D Blocks side-by-side
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Cancel 3D Button style
                        Box(
                            modifier = Modifier.weight(1f)
                        ) {
                            ThreeDBlock(
                                modifier = Modifier.clickable { onDismiss() },
                                backgroundColor = Color(0xFFF1F5F9), // Slate gray tint
                                borderColor = Color(0xFF64748B),
                                shadowColor = Color(0xFF64748B).copy(alpha = 0.15f),
                                cornerRadius = 12.dp,
                                borderWidth = 1.8.dp,
                                shadowOffset = 3.dp
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("HỦY BỎ", fontWeight = FontWeight.Bold, color = Color(0xFF475569), fontSize = 12.sp)
                                }
                            }
                        }

                        // Confirm & Save 3D Button style
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("add_dialog_confirm")
                        ) {
                            ThreeDBlock(
                                modifier = Modifier.clickable {
                                    if (contentStr.isNotBlank()) {
                                        onConfirm(
                                            dateStr,
                                            depTimeStr,
                                            arrTimeStr,
                                            contentStr,
                                            currentChecklist.toList(),
                                            recurringType,
                                            selectedColorHex,
                                            driveFileUri,
                                            driveFileName
                                        )
                                    } else {
                                        Toast.makeText(context, "Vui lòng nhập nội dung!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                backgroundColor = Color(0xFF22C55E), // Lively bright green for save action
                                borderColor = Color(0xFF15803D),
                                shadowColor = Color(0xFF15803D).copy(alpha = 0.2f),
                                cornerRadius = 12.dp,
                                borderWidth = 1.8.dp,
                                shadowOffset = 3.dp
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("LƯU LẠI", fontWeight = FontWeight.Black, color = Color.White, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Icon application launchbar links matching Sleek Interface Design Footer
@Composable
fun BottomAppLinksBar() {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE0F2FE)) // Light sea breeze light blue container!
            .border(width = 1.5.dp, color = Color(0xFFBAE6FD))
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "TRUY CẬP NHANH THIẾT BỊ",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp,
                color = Color(0xFF0C4A6E)
            ),
            modifier = Modifier.padding(bottom = 10.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Calendar with Light Blue 3D Block (xanh biển sáng)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { AppLauncherUtils.launchGoogleCalendar(context) }
                    .padding(2.dp)
            ) {
                ThreeDBlock(
                    backgroundColor = Color.White,
                    borderColor = Color(0xFF0EA5E9),
                    shadowColor = Color(0xFF0EA5E9).copy(alpha = 0.2f),
                    cornerRadius = 12.dp,
                    borderWidth = 1.8.dp,
                    shadowOffset = 3.dp,
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📅", fontSize = 23.sp)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Calendar",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0C4A6E)
                )
            }

            // Meet with Light Green 3D Block (xanh lá sáng)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { AppLauncherUtils.launchGoogleMeet(context) }
                    .padding(2.dp)
            ) {
                ThreeDBlock(
                    backgroundColor = Color.White,
                    borderColor = Color(0xFF22C55E),
                    shadowColor = Color(0xFF22C55E).copy(alpha = 0.2f),
                    cornerRadius = 12.dp,
                    borderWidth = 1.8.dp,
                    shadowOffset = 3.dp,
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🎥", fontSize = 23.sp)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Meet",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0C4A6E)
                )
            }

            // Drive with Yellow Orange 3D Block (vàng cam sáng)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { AppLauncherUtils.launchGoogleDrive(context) }
                    .padding(2.dp)
            ) {
                ThreeDBlock(
                    backgroundColor = Color.White,
                    borderColor = Color(0xFFFFA000),
                    shadowColor = Color(0xFFFFA000).copy(alpha = 0.2f),
                    cornerRadius = 12.dp,
                    borderWidth = 1.8.dp,
                    shadowOffset = 3.dp,
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("☁️", fontSize = 23.sp)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Drive",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0C4A6E)
                )
            }

            // Clock with Orange Red 3D Block (cam đỏ)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { AppLauncherUtils.launchClockAlarm(context) }
                    .padding(2.dp)
            ) {
                ThreeDBlock(
                    backgroundColor = Color.White,
                    borderColor = Color(0xFFFF4F1A),
                    shadowColor = Color(0xFFFF4F1A).copy(alpha = 0.2f),
                    cornerRadius = 12.dp,
                    borderWidth = 1.8.dp,
                    shadowOffset = 3.dp,
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("⏰", fontSize = 23.sp)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Clock",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0C4A6E)
                )
            }
        }
    }
}

// Helpers
fun formatTaskDate(dateStr: String): String {
    return try {
        val sdfIn = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sdfOut = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dateVal = sdfIn.parse(dateStr)
        if (dateVal != null) sdfOut.format(dateVal) else dateStr
    } catch (e: Exception) {
        dateStr
    }
}

fun getRecuringLabel(type: String): String {
    return when (type) {
        "DAILY" -> "Hàng ngày"
        "WEEKLY" -> "Hàng tuần"
        "MONTHLY" -> "Hàng tháng"
        "YEARLY" -> "Hàng năm"
        else -> "Một lần"
    }
}

fun getOnColor(hex: String): Color {
    // Return high contrast text color for pastel backgrounds
    return Color(0xFF212121)
}

@Composable
fun ThreeDBlock(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    borderColor: Color = Color(0xFF0C4A6E),
    shadowColor: Color = Color(0xFF0C4A6E).copy(alpha = 0.15f),
    cornerRadius: androidx.compose.ui.unit.Dp = 14.dp,
    borderWidth: androidx.compose.ui.unit.Dp = 2.dp,
    shadowOffset: androidx.compose.ui.unit.Dp = 4.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.padding(bottom = shadowOffset, end = shadowOffset)
    ) {
        // Shadow base
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = shadowOffset, y = shadowOffset)
                .background(shadowColor, RoundedCornerShape(cornerRadius))
        )
        // Surface card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor, RoundedCornerShape(cornerRadius))
                .border(borderWidth, borderColor, RoundedCornerShape(cornerRadius))
        ) {
            content()
        }
    }
}
