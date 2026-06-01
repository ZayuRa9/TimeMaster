package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

data class ChecklistItem(
    val id: String,
    val text: String,
    val isChecked: Boolean
) {
    companion object {
        // Simple and robust text-based serializer to avoid external library version issues
        fun serialize(items: List<ChecklistItem>): String {
            return items.joinToString("\n") { item ->
                "${item.id.replace("|", "##").replace("\n", " ")}_FD_${item.text.replace("|", "##").replace("\n", " ")}|${if (item.isChecked) "1" else "0"}"
            }
        }

        fun deserialize(serialized: String): List<ChecklistItem> {
            if (serialized.isBlank()) return emptyList()
            return serialized.split("\n").mapNotNull { line ->
                val parts = line.split("|")
                if (parts.size >= 2) {
                    val idAndText = parts[0].split("_FD_")
                    val (id, text) = if (idAndText.size >= 2) {
                        Pair(idAndText[0].replace("##", "|"), idAndText[1].replace("##", "|"))
                    } else {
                        Pair(parts[0].replace("##", "|"), parts[0].replace("##", "|"))
                    }
                    val isChecked = parts[1] == "1"
                    ChecklistItem(id, text, isChecked)
                } else null
            }
        }
    }
}

private const val FlagDivider = "_FD_"

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,             // Format: YYYY-MM-DD
    val departureTime: String,    // Format: HH:mm
    val arrivalTime: String,      // Format: HH:mm
    val content: String,
    val checklistRaw: String,      // Serialized ChecklistItem list
    val recurringType: String,    // "NONE", "DAILY", "WEEKLY", "MONTHLY", "YEARLY"
    val colorHex: String,         // Custom task background color hex code
    val isReminderScheduled: Boolean = false,
    val driveFileUri: String? = null,
    val driveFileName: String? = null,
    val isPartnerTask: Boolean = false
)
