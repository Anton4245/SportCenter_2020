//package ru.sport_center.ui
//
//import androidx.arch.core.executor.testing.InstantTaskExecutorRule
//import androidx.lifecycle.MutableLiveData
//import io.mockk.clearMocks
//import io.mockk.every
//import io.mockk.mockk
//import io.mockk.verify
//import org.junit.Assert.*
//import org.junit.Before
//import org.junit.Rule
//import org.junit.Test
//import ru.sport_center.data.OrdersRepository
//import ru.sport_center.data.entity.Order
//import ru.sport_center.data.model.OrderResult
//import ru.sport_center.ui.main.MainViewModel
//
//class MainViewModelTest {
//
//    @get:Rule
//    val taskExecutorRule = InstantTaskExecutorRule()
//
//    private val mockRepository = mockk<OrdersRepository>()
//    private val ordersLiveData = MutableLiveData<OrderResult>()
//
//    private lateinit var viewModel: MainViewModel
//
//
//    @Before
//    fun setup() {
//        clearMocks(mockRepository)
//        every { mockRepository.getOrders() } returns ordersLiveData
//        viewModel = MainViewModel(mockRepository)
//    }
//
//
//    @Test
//    fun `should call getOrders`() {
//        verify(exactly = 1) { mockRepository.getOrders() }
//    }
//
//    @Test
//    fun `should return orders`() {
//        var result: List<Order>? = null
//        val testData = listOf(Order("1"), Order("2"))
//        viewModel.getViewState().observeForever {
//            result = it.data
//        }
//        ordersLiveData.value = OrderResult.Success(testData)
//        assertEquals(testData, result)
//    }
//
//    @Test
//    fun `should return error`() {
//        var result: Throwable? = null
//        val testData = Throwable("error")
//        viewModel.getViewState().observeForever {
//            result = it?.error
//        }
//        ordersLiveData.value = OrderResult.Error(error = testData)
//        assertEquals(testData, result)
//    }
//
//    @Test
//    fun `should remove observer`() {
//        viewModel.onCleared()
//        assertFalse(ordersLiveData.hasObservers())
//    }
//}