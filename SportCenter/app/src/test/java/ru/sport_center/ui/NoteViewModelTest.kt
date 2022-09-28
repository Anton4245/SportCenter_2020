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
//import ru.sport_center.ui.order.OrderViewModel
//import ru.sport_center.ui.order.OrderData
//
//class OrderViewModelTest {
//
//    @get:Rule
//    val taskExecutorRule = InstantTaskExecutorRule()
//
//    private val mockRepository = mockk<OrdersRepository>()
//    private val orderLiveData = MutableLiveData<OrderResult>()
//
//    private val testOrder = Order("1", "title", "text")
//
//    private lateinit var viewModel: OrderViewModel
//
//
//    @Before
//    fun setup() {
//        clearMocks(mockRepository)
//        every { mockRepository.getOrderById(testOrder.id) } returns orderLiveData
//        every { mockRepository.deleteOrder(testOrder.id) } returns orderLiveData
//        every { mockRepository.saveOrder(testOrder) } returns  orderLiveData
//        viewModel = OrderViewModel(mockRepository)
//    }
//
//
//    @Test
//    fun `loadOrder should return OrderViewState Data`() {
//        var result: OrderData.Data? = null
//        val testData = OrderData.Data(false, testOrder)
//        viewModel.getViewState().observeForever {
//            result = it.data
//        }
//        viewModel.loadOrder(testOrder.id)
//        orderLiveData.value = OrderResult.Success(testOrder)
//        assertEquals(testData, result)
//    }
//
//    @Test
//    fun `loadOrder should return error`() {
//        var result: Throwable? = null
//        val testData = Throwable("error")
//        viewModel.getViewState().observeForever {
//            result = it.error
//        }
//        viewModel.loadOrder(testOrder.id)
//        orderLiveData.value = OrderResult.Error(error = testData)
//        assertEquals(testData, result)
//    }
//
//    @Test
//    fun `deleteOrder should return OrderViewState Data wish isDeleted`() {
//        var result: OrderData.Data? = null
//        val testData = OrderData.Data(true, null)
//        viewModel.getViewState().observeForever {
//            result = it.data
//        }
//        viewModel.save(testOrder)
//        viewModel.deleteOrder()
//        orderLiveData.value = OrderResult.Success(null)
//        assertEquals(testData, result)
//    }
//
//    @Test
//    fun `deleteOrder should return error`() {
//        var result: Throwable? = null
//        val testData = Throwable("error")
//        viewModel.getViewState().observeForever {
//            result = it.error
//        }
//        viewModel.save(testOrder)
//        viewModel.deleteOrder()
//        orderLiveData.value = OrderResult.Error(error = testData)
//        assertEquals(testData, result)
//    }
//
//    @Test
//    fun `should save changes`() {
//        viewModel.save(testOrder)
//        viewModel.onCleared()
//        verify { mockRepository.saveOrder(testOrder) }
//    }
//}