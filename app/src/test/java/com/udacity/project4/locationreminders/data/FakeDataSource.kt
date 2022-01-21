package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {

    private val reminderDTOs = mutableListOf<ReminderDTO>()
    private var foreceError = false

    fun setForeceError(value: Boolean) {
        foreceError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (foreceError)
            return Result.Error("Test Error")

        return Result.Success(reminderDTOs)
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminderDTOs.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (foreceError)
            return Result.Error("Test Error")

        for (reminder in reminderDTOs) {
            if (reminder.id == id) {
                return Result.Success<ReminderDTO>(reminder)
            }
        }
        return Result.Error("Can not find the reminder with id $id.", -1)

    }

    override suspend fun deleteAllReminders() {
        reminderDTOs.clear()
    }


}