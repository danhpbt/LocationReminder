package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.*
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var reminderDataBase : RemindersDatabase
    private lateinit var remindersLocalRepository: RemindersLocalRepository

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDb(){
        reminderDataBase = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().context,
            RemindersDatabase::class.java).allowMainThreadQueries().build()

        remindersLocalRepository = RemindersLocalRepository(reminderDataBase.reminderDao(),
            Dispatchers.Main)
    }

    @After
    fun closeDB() {
        reminderDataBase.close()
    }

    @Test
    fun insertReminder_success() = runBlocking {
        val reminder = ReminderDTO("Title","Description",
            "location",0.0,0.0)

        remindersLocalRepository.saveReminder(reminder)

        val result = remindersLocalRepository.getReminders() as Result.Success

        assertThat(result.data.size, `is`(1))
        assertThat(result.data.contains(reminder), `is`(true))
    }

    @Test
    fun getReminder_success() = runBlocking {
        val reminder = ReminderDTO("Title","Description",
            "location",0.0,0.0)

        remindersLocalRepository.saveReminder(reminder)

        val result = remindersLocalRepository.getReminder(reminder.id) as Result.Success

        assertThat(result.data, `is`(notNullValue()))
    }

    @Test
    fun getReminder_error() = runBlocking {
        val reminder = ReminderDTO("Title","Description",
            "location",0.0,0.0)

        val result = remindersLocalRepository.getReminder(reminder.id) as Result.Error

        assertThat(result.message, `is`("Reminder not found!"))
        assertThat(result.statusCode, `is`(nullValue()))
    }

    @Test
    fun deleteAll_success() = runBlocking {
        val reminder = ReminderDTO("Title","Description",
            "location",0.0,0.0)

        remindersLocalRepository.saveReminder(reminder)

        remindersLocalRepository.deleteAllReminders()

        val result = remindersLocalRepository.getReminders() as Result.Success

        Assert.assertEquals(0, result.data.size)
    }



}