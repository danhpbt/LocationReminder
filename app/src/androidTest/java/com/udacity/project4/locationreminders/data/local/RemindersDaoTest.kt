package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.*

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    private lateinit var remindersDatabase: RemindersDatabase
    private lateinit var remindersDao: RemindersDao

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDb() {
        remindersDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()

        remindersDao = remindersDatabase.reminderDao()
    }

    @After
    fun closeDb() {
        remindersDatabase.close()
    }

    @Test
    fun insertReminder_success() = runBlockingTest {
        val reminder = ReminderDTO("Title","Description",
            "location",0.0,0.0)

        remindersDao.saveReminder(reminder)

        assertThat(remindersDao.getReminders().size, `is`(1))
        assertThat(remindersDao.getReminders().contains(reminder), `is`(true))
    }

    @Test
    fun getReminder_success() = runBlockingTest {
        val reminder = ReminderDTO("Title","Description",
            "location",0.0,0.0)

        remindersDao.saveReminder(reminder)

        val getReminder = remindersDao.getReminderById(reminder.id)

        assertThat(getReminder, `is`(notNullValue()))
    }

    @Test
    fun deleteAll_success() = runBlockingTest {
        val reminder = ReminderDTO("Title","Description",
            "location",0.0,0.0)

        remindersDao.saveReminder(reminder)

        remindersDao.deleteAllReminders()
        Assert.assertEquals(0, remindersDao.getReminders().size)
    }

}