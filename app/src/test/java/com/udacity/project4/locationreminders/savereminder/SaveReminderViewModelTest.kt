package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.util.MainCoroutineRule
import com.udacity.project4.locationreminders.util.getOrAwaitValue

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var viewModel: SaveReminderViewModel

    @get:Rule
    val instantTaskExecRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun init() {
        fakeDataSource = FakeDataSource()
        viewModel = SaveReminderViewModel(
            ApplicationProvider.getApplicationContext(),
            fakeDataSource)
    }

    @After
    fun stop() {
        stopKoin()
    }

    @Test
    fun viewModel_save_success() {
        val reminder = ReminderDataItem("Title","Description",
            "location",0.0,0.0)

        viewModel.validateAndSaveReminder(reminder)

        val toastMessage = viewModel.showToast.getOrAwaitValue()

        assertThat(
            toastMessage,
            Matchers.`is`(ApplicationProvider.getApplicationContext<Application>()
                    .getString(R.string.reminder_saved)
            )
        )

    }

    @Test
    fun viewModel_save_failed() {
        val reminder = ReminderDataItem("","Description",
            "location",0.0,0.0)

        viewModel.validateAndSaveReminder(reminder)

        assertThat(
            viewModel.showSnackBarInt.getOrAwaitValue(),
            Matchers.`is`(R.string.err_enter_title)
        )
    }

    @Test
    fun viewModel_showLoading() {
        val reminder = ReminderDataItem("Title","Description",
            "location",0.0,0.0)

        mainCoroutineRule.pauseDispatcher()

        viewModel.validateAndSaveReminder(reminder)
        assertThat(
            viewModel.showLoading.getOrAwaitValue(),
            Matchers.`is`(true)
        )

        mainCoroutineRule.resumeDispatcher()
        assertThat(
            viewModel.showLoading.getOrAwaitValue(),
            Matchers.`is`(false)
        )
    }


}