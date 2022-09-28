//package ru.sport_center.data.provider
//
//import androidx.arch.core.executor.testing.InstantTaskExecutorRule
//import com.google.android.gms.tasks.OnSuccessListener
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.auth.FirebaseUser
//import com.google.firebase.firestore.CollectionReference
//import com.google.firebase.firestore.DocumentReference
//import com.google.firebase.firestore.DocumentSnapshot
//import com.google.firebase.firestore.EventListener
//import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.firestore.QuerySnapshot
//import io.mockk.clearAllMocks
//import io.mockk.every
//import io.mockk.mockk
//import io.mockk.slot
//import io.mockk.verify
//import org.junit.After
//import org.junit.AfterClass
//import org.junit.Assert.*
//import org.junit.Before
//import org.junit.BeforeClass
//import org.junit.Rule
//import org.junit.Test
//import ru.sport_center.data.entity.Order
//import ru.sport_center.data.errors.NoAuthException
//import ru.sport_center.data.model.OrderResult
//
//
//class FireStoreProviderTest {
//
//    @get:Rule
//    val taskExecutorRule = InstantTaskExecutorRule()
//
//    private val mockDb = mockk<FirebaseFirestore>()
//    private val mockAuth = mockk<FirebaseAuth>()
//    private val mockResultCollection = mockk<CollectionReference>()
//    private val mockUser = mockk<FirebaseUser>()
//
//    private val mockDocument1 = mockk<DocumentSnapshot>()
//    private val mockDocument2 = mockk<DocumentSnapshot>()
//    private val mockDocument3 = mockk<DocumentSnapshot>()
//
//    private val testOrders = listOf(Order("1"), Order("2"), Order("3"))
//
//    private val provider = FireStoreProvider(mockAuth, mockDb)
//
//    companion object {
//        @BeforeClass
//        fun setupClass() {
//
//        }
//
//
//        @AfterClass
//        fun tearDownClass() {
//
//        }
//    }
//
//
//    @Before
//    fun setup() {
//        clearAllMocks()
//        every { mockAuth.currentUser } returns mockUser
//        every { mockUser.uid } returns ""
//        every { mockDb.collection(any()).document(any()).collection(any()) } returns mockResultCollection
//
//        every { mockDocument1.toObject(Order::class.java) } returns testOrders[0]
//        every { mockDocument2.toObject(Order::class.java) } returns testOrders[1]
//        every { mockDocument3.toObject(Order::class.java) } returns testOrders[2]
//    }
//
//    @After
//    fun tearDown() {
//
//    }
//
//    @Test
//    fun `should throw NoAuthException if no auth`() {
//        var result: Any? = null
//        every { mockAuth.currentUser } returns null
//        provider.subscribeToAllOrders().observeForever {
//            result = (it as? OrderResult.Error)?.error
//        }
//        assertTrue(result is NoAuthException)
//    }
//
//    @Test
//    fun `saveOrder calls set`() {
//        val mockDocumentReference = mockk<DocumentReference>()
//        every { mockResultCollection.document(testOrders[0].id) } returns mockDocumentReference
//        provider.saveOrder(testOrders[0])
//        verify(exactly = 1) { mockDocumentReference.set(testOrders[0]) }
//    }
//
//    @Test
//    fun `subscribe to all orders returns orders`() {
//        var result: List<Order>? = null
//        val mockSnapshot = mockk<QuerySnapshot>()
//        val slot = slot<EventListener<QuerySnapshot>>()
//
//        every { mockSnapshot.documents } returns listOf(mockDocument1, mockDocument2, mockDocument3)
//        every { mockResultCollection.addSnapshotListener(capture(slot)) } returns mockk()
//        provider.subscribeToAllOrders().observeForever{
//            result = (it as? OrderResult.Success<List<Order>>)?.data
//        }
//        slot.captured.onEvent(mockSnapshot, null)
//        assertEquals(testOrders, result)
//    }
//
//    @Test
//    fun `deleteOrder calls document delete`() {
//        val mockDocumentReference = mockk<DocumentReference>()
//        every { mockResultCollection.document(testOrders[0].id) } returns mockDocumentReference
//        provider.deleteOrder(testOrders[0].id)
//        verify(exactly = 1) { mockDocumentReference.delete() }
//    }
//
//    @Test
//    fun `getOrderByIs should return Order`() {
//        var result: Order? = null
//        val slot = slot<OnSuccessListener<DocumentSnapshot>>()
//        every { mockResultCollection.document(testOrders[0].id).get().addOnSuccessListener (capture(slot))} returns mockk()
//
//        provider.getOrderById(testOrders[0].id).observeForever{
//            result = (it as? OrderResult.Success<Order>)?.data
//        }
//
//        slot.captured.onSuccess(mockDocument1)
//        assertEquals(testOrders[0], result)
//    }
//
//}
