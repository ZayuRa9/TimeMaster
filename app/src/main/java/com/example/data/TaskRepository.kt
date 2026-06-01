package com.example.data

import android.content.Context
import com.example.util.SchedulerUtils
import kotlinx.coroutines.flow.Flow

class TaskRepository(
    private val context: Context,
    private val taskDao: TaskDao
) {
    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()
    val partnerTasks: Flow<List<TaskEntity>> = taskDao.getPartnerTasks()

    fun getTasksByDate(date: String): Flow<List<TaskEntity>> = taskDao.getTasksByDate(date)

    suspend fun insertTask(task: TaskEntity) {
        // Insert the master task
        val masterId = taskDao.insertTask(task).toInt()
        val updatedMaster = taskDao.getTaskById(masterId)
        
        if (updatedMaster != null) {
            // Schedule reminder alarm for the master task
            SchedulerUtils.scheduleTaskReminder(context, updatedMaster)
        }

        // If it is a recurring task, automatically generate future instances in the database
        if (task.recurringType != "NONE") {
            val futureDates = SchedulerUtils.getFutureDates(task.date, task.recurringType)
            for (futureDate in futureDates) {
                val futureTask = task.copy(
                    id = 0, // autoGenerate will create a new unique ID
                    date = futureDate,
                    recurringType = "NONE" // Child instances don't recursively trigger more instances
                )
                val childId = taskDao.insertTask(futureTask).toInt()
                val updatedChild = taskDao.getTaskById(childId)
                if (updatedChild != null) {
                    // Schedule reminder alarm for each future occurrence
                    SchedulerUtils.scheduleTaskReminder(context, updatedChild)
                }
            }
        }
    }

    suspend fun updateTask(task: TaskEntity) {
        taskDao.updateTask(task)
        // Reschedule alarm
        SchedulerUtils.scheduleTaskReminder(context, task)
    }

    suspend fun deleteTask(task: TaskEntity) {
        // Cancel alarm first
        SchedulerUtils.cancelTaskReminder(context, task.id)
        taskDao.deleteTask(task)
    }

    suspend fun deleteById(id: Int) {
        SchedulerUtils.cancelTaskReminder(context, id)
        taskDao.deleteById(id)
    }
}
