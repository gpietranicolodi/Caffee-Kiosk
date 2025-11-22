package service.receipt;

import model.entity.ReceiptInfo;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import repo.repository.ReceiptRepository;
import service.auth.SessionManager;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("unit")
class DBReceiptServiceTest {

    private DBReceiptService dbReceiptService;
    private MockedStatic<SessionManager> mockedSessionManager;

    @Mock
    private ReceiptRepository mockReceiptRepository;
    @Mock
    private SessionManager mockSessionManager;

    @BeforeAll
    void printHeader() {
        System.out.println("====================================================================");
        System.out.println("FILE TESTING: DBReceiptServiceTest.java");
        System.out.println("PURPOSE: Tests the database service for saving and retrieving receipts.");
        System.out.println("--------------------------------------------------------------------");
    }

    @AfterAll
    void printFooter() {
        System.out.println("====================================================================\n");
    }

    @AfterEach
    void reportTestResult(TestInfo testInfo) {
        System.out.println("  - " + testInfo.getDisplayName() + " - PASSED");
    }

    @BeforeEach
    void setUp() {
        dbReceiptService = new DBReceiptService(mockReceiptRepository);
        mockedSessionManager = mockStatic(SessionManager.class);
        when(SessionManager.getInstance()).thenReturn(mockSessionManager);
    }

    @AfterEach
    void tearDown() {
        mockedSessionManager.close();
    }

    @Test
    @DisplayName("1. testSaveReceipt()")
    void testSaveReceipt() throws Exception {
        byte[] fileData = {1, 2, 3};
        dbReceiptService.saveReceipt("Test Customer", 1, fileData);
        verify(mockReceiptRepository).save("Test Customer", 1, fileData);
    }

    @Test
    @DisplayName("2. testGetReceiptHistoryAsManager()")
    void testGetReceiptHistoryAsManager() throws Exception {
        when(mockSessionManager.isManager()).thenReturn(true);
        when(mockReceiptRepository.findRecentReceiptsForManager()).thenReturn(Collections.singletonList(mock(ReceiptInfo.class)));

        List<ReceiptInfo> history = dbReceiptService.getReceiptHistory();

        assertNotNull(history);
        assertEquals(1, history.size());
        verify(mockReceiptRepository).findRecentReceiptsForManager();
        verify(mockReceiptRepository, never()).findRecentReceiptsForEmployee(anyInt());
    }

    @Test
    @DisplayName("3. testGetReceiptHistoryAsEmployee()")
    void testGetReceiptHistoryAsEmployee() throws Exception {
        when(mockSessionManager.isManager()).thenReturn(false);
        when(mockSessionManager.getLoggedInEmployeeId()).thenReturn(101);
        when(mockReceiptRepository.findRecentReceiptsForEmployee(101)).thenReturn(Collections.singletonList(mock(ReceiptInfo.class)));

        List<ReceiptInfo> history = dbReceiptService.getReceiptHistory();

        assertNotNull(history);
        assertEquals(1, history.size());
        verify(mockReceiptRepository).findRecentReceiptsForEmployee(101);
        verify(mockReceiptRepository, never()).findRecentReceiptsForManager();
    }
}