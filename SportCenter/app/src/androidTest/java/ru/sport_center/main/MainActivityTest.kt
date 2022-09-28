package ru.sport_center.main

import androidx.lifecycle.MutableLiveData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.ReceiveChannel
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.android.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module
import org.koin.standalone.StandAloneContext.loadKoinModules
import org.koin.standalone.StandAloneContext.stopKoin
import ru.sport_center.R
import ru.sport_center.data.entity.Order
import ru.sport_center.ui.main.MainActivity
import ru.sport_center.ui.main.MainViewModel
import ru.sport_center.ui.main.MainViewState
import ru.sport_center.ui.main.OrdersRVAdapter

class MainActivityTest {

//    @get:Rule
//    val activityTestRule = IntentsTestRule(MainActivity::class.java, true, false)
//
//    private val model: MainViewModel = mockk(relaxed = true)
//    private val viewStateLiveData = ReceiveChannel<MainViewState>
//
//    private val testOrders = listOf(
//        Order("1", "title1", "text1"),
//        Order("2", "title2", "text2"),
//        Order("3", "title3", "text3")
//    )
//
//    @Before
//    fun setUp() {
//        loadKoinModules(
//            listOf(
//                module {
//                    viewModel(override = true) { model }
//                }
//            )
//        )
//
//        every { model.getViewState() } returns viewStateLiveData
//        activityTestRule.launchActivity(null)
//        viewStateLiveData.postValue(MainViewState(orders = testOrders))
//    }
//
//    @After
//    fun tearDown(){
//        stopKoin()
//    }
//
//    @Test
//    fun check_data_is_displayed(){
//        onView(withId(R.id.rv_orders)).perform(scrollToPosition<OrdersRVAdapter.ViewHolder>(1))
//        onView(withText(testOrders[1].text)).check(matches(isDisplayed()))
//    }
}