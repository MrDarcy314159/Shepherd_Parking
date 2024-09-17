package za.varsitycollege.shepherd_parking

import androidx.compose.runtime.mutableStateOf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.CollectionReference
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.kotlin.any
import android.content.Context
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue

class LatePageUnitTest {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var userManager: UserManager
    private lateinit var mockCollection: CollectionReference
    private lateinit var mockUser: FirebaseUser

    @Before
    fun setup() {
        // Mock Firestore and Auth instances
        val mockContext: Context = mock()
        firestore = mock()
        auth = mock()
        mockCollection = mock()
        mockUser = mock()

        // Mock FirebaseAuth behavior
        whenever(auth.currentUser).thenReturn(mockUser)
        whenever(mockUser.email).thenReturn("student@example.com")

        // Mock Firestore CollectionReference and DocumentReference
        val mockDocumentReference: DocumentReference = mock()
        val mockGetTask: Task<DocumentSnapshot> = mock()
        val mockAddTask: Task<DocumentReference> = mock()

        // Mock Firestore collection behavior for users
        whenever(firestore.collection("users")).thenReturn(mockCollection)
        whenever(mockCollection.document(any())).thenReturn(mockDocumentReference)
        whenever(mockDocumentReference.get()).thenReturn(mockGetTask)

        // Mock Firestore collection behavior for late submissions
        whenever(firestore.collection("late_submissions")).thenReturn(mockCollection)
        whenever(mockCollection.add(any())).thenReturn(mockAddTask)

        // Mock success scenario for fetching student number
        val mockDocumentSnapshot: DocumentSnapshot = mock()
        whenever(mockGetTask.addOnSuccessListener(any())).thenAnswer { invocation ->
            (invocation.arguments[0] as OnSuccessListener<DocumentSnapshot>).onSuccess(mockDocumentSnapshot)
            mockGetTask
        }
        whenever(mockDocumentSnapshot.getString("studentNumber")).thenReturn("ST10000001")

        // Mock failure scenario for fetching student number
        whenever(mockGetTask.addOnFailureListener(any())).thenAnswer { invocation ->
            (invocation.arguments[0] as OnFailureListener).onFailure(Exception("Mocked Firestore fetch failure"))
            mockGetTask
        }

        // Mock success and failure scenarios for adding a form
        whenever(mockAddTask.addOnSuccessListener(any())).thenAnswer { invocation ->
            (invocation.arguments[0] as OnSuccessListener<DocumentReference>).onSuccess(mockDocumentReference)
            mockAddTask
        }
        whenever(mockAddTask.addOnFailureListener(any())).thenAnswer { invocation ->
            (invocation.arguments[0] as OnFailureListener).onFailure(Exception("Firestore add failed"))
            mockAddTask
        }

        // Initialize UserManager with mocked FirebaseAuth and Firestore
        userManager = UserManager(mockContext, auth, firestore)
    }


    @Test
    fun testFetchStudentNumber() {
        val studentNumber = mutableStateOf("")

        // Simulate fetching student number (success case)
        userManager.getStudentNumber(
            onSuccess = { fetchedNumber: String -> studentNumber.value = fetchedNumber },
            onFailure = { /* Handle failure */ }
        )

        // Assert the result
        assertEquals("ST10000001", studentNumber.value)
    }

    @Test
    fun testFetchStudentNumberFailure() {
        val didFail = mutableStateOf(false)

        // Simulate fetching student number (failure case)
        userManager.getStudentNumber(
            onSuccess = { /* no-op */ },
            onFailure = { didFail.value = true }  // This should be triggered
        )

        // Assert that failure handling was triggered
        assertTrue(didFail.value)
    }

    @Test
    fun testSubmitFormToFirestore() {
        // Set up state variables to simulate the form data
        val studentNumber = "ST10000001"
        val selectedLecturer = "John Doe"
        val selectedReason = "Traffic"
        val extraInformation = "Late due to traffic"

        // Simulate Firestore form submission
        val lateData = hashMapOf(
            "studentNumber" to studentNumber,
            "lecturer" to selectedLecturer,
            "reason" to selectedReason,
            "extraInformation" to extraInformation,
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection("late_submissions").add(lateData)

        // Verify Firestore submission
        verify(mockCollection).add(any())
    }

    @Test
    fun testSubmitFormToFirestoreFailure() {
        val didFail = mutableStateOf(false)

        // Simulate Firestore form submission (failure case)
        val lateData = hashMapOf(
            "studentNumber" to "ST10000001",
            "lecturer" to "John Doe",
            "reason" to "Traffic",
            "extraInformation" to "Late due to traffic",
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection("late_submissions").add(lateData)
            .addOnFailureListener { didFail.value = true }

        // Assert that failure handling was triggered
        assertTrue(didFail.value)
    }
}
