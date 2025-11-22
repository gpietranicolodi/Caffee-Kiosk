package service.auth;

import model.dto.UserSessionInfo;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import repo.repository.AuthRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("unit")
class DBAuthServiceTest {

    private DBAuthService authService;

    @Mock
    private AuthRepository mockAuthRepository;

    private final String testUser = "testuser";
    private final String testPassword = "password123";
    private final String hashedPassword = BCrypt.hashpw(testPassword, BCrypt.gensalt());

    @BeforeAll
    void printHeader() {
        System.out.println("====================================================================");
        System.out.println("FILE TESTING: DBAuthServiceTest.java");
        System.out.println("PURPOSE: Tests the database authentication service logic.");
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
        // Inject the mocked repository into the service
        authService = new DBAuthService(mockAuthRepository);
    }

    @Test
    @DisplayName("1. testAuthenticateSuccess()")
    void testAuthenticateSuccess() throws Exception {
        UserSessionInfo mockUserInfo = new UserSessionInfo(1, "Test User", hashedPassword, false);
        // O repositório agora é insensível a maiúsculas e minúsculas, então o mock retorna o usuário
        // independentemente do caso. O serviço passa o nome de usuário como está.
        when(mockAuthRepository.findUserByUsername(testUser.toLowerCase()))
                .thenReturn(Optional.of(mockUserInfo));

        boolean result = authService.authenticate(testUser, testPassword);

        assertTrue(result, "Authentication should succeed with correct credentials.");
    }

    @Test
    @DisplayName("1.1. testAuthenticateSuccessWithDifferentCase()")
    void testAuthenticateSuccessWithDifferentCase() throws Exception {
        UserSessionInfo mockUserInfo = new UserSessionInfo(1, "Test User", hashedPassword, false);
        // O serviço passa "TestUser" e o repositório mockado é configurado para responder a "testuser".
        when(mockAuthRepository.findUserByUsername("testuser")).thenReturn(Optional.of(mockUserInfo));

        assertTrue(authService.authenticate("TestUser", testPassword), "Authentication should succeed with a mixed-case username.");
    }

    @Test
    @DisplayName("2. testAuthenticateFailsWithWrongPassword()")
    void testAuthenticateFailsWithWrongPassword() throws Exception {
        UserSessionInfo mockUserInfo = new UserSessionInfo(1, "Test User", hashedPassword, false);
        when(mockAuthRepository.findUserByUsername(testUser.toLowerCase())).thenReturn(Optional.of(mockUserInfo));

        boolean result = authService.authenticate(testUser, "wrongpassword");

        assertFalse(result, "Authentication should fail with the wrong password.");
    }

    @Test
    @DisplayName("3. testAuthenticateFailsForEmptyUsername()")
    void testAuthenticateFailsForEmptyUsername() throws Exception {
        boolean result = authService.authenticate("", "somepassword");

        assertFalse(result, "Authentication should fail with an empty username.");
        // Verify that the repository was never called for an empty username
        verify(mockAuthRepository, never()).findUserByUsername(anyString());
    }
}