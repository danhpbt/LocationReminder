package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.ExpectFailure.assertThat
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.util.MainCoroutineRule
import com.udacity.project4.locationreminders.util.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.not
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    //TODO: provide testing to the RemindersListViewModel and its live data objects
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var viewModel: RemindersListViewModel
    private lateinit var dataSource: FakeDataSource

    @Before
    fun init() {
        dataSource = FakeDataSource()
        viewModel = RemindersListViewModel(Application(),dataSource)
    }

    @After
    fun stop() {
        stopKoin()
    }

    @Test
    fun getReminderList_listOneElements() = mainCoroutineRule.runBlockingTest {
        val reminder = ReminderDTO("Title 1", "Description 1", "Location 1", 0.0, 0.0)
        dataSource.saveReminder(reminder)

        viewModel.loadReminders()
        val list = viewModel.remindersList.getOrAwaitValue()

        assertThat(list,(not(nullValue())))
        assertEquals(1,list.size)
    }

    @Test
    fun loadReminders_Error() = mainCoroutineRule.runBlockingTest {
        dataSource.setForeceError(true)
        viewModel.loadReminders()
        val snackBarMessage = viewModel.showSnackBar.getOrAwaitValue()
        assertEquals("Test Error",snackBarMessage)
    }

}