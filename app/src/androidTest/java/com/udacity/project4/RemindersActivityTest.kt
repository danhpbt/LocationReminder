package com.udacity.project4

import android.app.Activity
import android.app.Application
import android.view.View
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application


    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    @Before
    fun registerIdlingResource(): Unit = IdlingRegistry.getInstance().run {
        register(EspressoIdlingResource.countingIdlingResource)
        register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource(): Unit = IdlingRegistry.getInstance().run {
        unregister(EspressoIdlingResource.countingIdlingResource)
        unregister(dataBindingIdlingResource)
    }

    private fun getActivity(activityScenario: ActivityScenario<RemindersActivity>): Activity? {
        var activity: Activity? = null
        activityScenario.onActivity {
            activity = it
        }
        return activity
    }


    @Test
    fun addReminder_checkIfIsAdd() {
        val scenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(scenario)

        val activity = getActivity(scenario)

        onView(withId(R.id.noDataTextView))
            .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
        onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())
        onView(withId(R.id.selectLocation)).perform(ViewActions.click())

        onView(withId(R.id.mapFragment)).perform(ViewActions.longClick())
        onView(withId(R.id.btn_save)).perform(ViewActions.click())
        onView(withId(R.id.reminderTitle))
            .perform(ViewActions.typeText("Title 1"))
        onView(withId(R.id.reminderDescription))
            .perform(ViewActions.typeText("Description 1"))

        Espresso.closeSoftKeyboard()
        onView(withId(R.id.saveReminder)).perform(ViewActions.click())

        onView(withText(R.string.reminder_saved)).inRoot(withDecorView(not(`is`(activity?.window?.decorView))))
            .check(
                matches(
                    isDisplayed()
                )
            )

        onView(withText("Title 1")).check(matches(isDisplayed()))

    }

    @Test
    fun addReminder_errorOnEnterTitle() {
        val scenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(scenario)

        onView(withId(R.id.noDataTextView))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())
        onView(withId(R.id.selectLocation)).perform(ViewActions.click())

        onView(withId(R.id.mapFragment)).perform(ViewActions.longClick())
        onView(withId(R.id.btn_save)).perform(ViewActions.click())

        Espresso.closeSoftKeyboard()
        onView(withId(R.id.saveReminder)).perform(ViewActions.click())

        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.err_enter_title)))

        runBlocking {
            delay(3000)
        }
    }

}
