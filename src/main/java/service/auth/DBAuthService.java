package service.auth;

import model.dto.UserSessionInfo;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repo.repository.AuthRepository;

import java.util.Optional;

/**
 * Service layer for authentication. Orchestrates the authentication process
 * by using a repository to fetch user data and then applying business logic.
 */
public class DBAuthService implements AuthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBAuthService.class);
    private final AuthRepository authRepository;

    /**
     * Constructor for dependency injection.
     * @param authRepository The repository for accessing authentication data.
     */
    public DBAuthService(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    @Override
    public boolean authenticate(String username, String password) throws Exception {
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            return false;
        }

        try {
            Optional<UserSessionInfo> userSessionInfoOpt = authRepository.findUserByUsername(username.toLowerCase());

            if (userSessionInfoOpt.isPresent()) {
                UserSessionInfo userInfo = userSessionInfoOpt.get();
                // Business logic: password verification happens here, in the service layer.
                if (BCrypt.checkpw(password, userInfo.getHashedPassword())) {
                    // Session management logic also belongs in the service layer.
                    SessionManager.getInstance().setLoggedInEmployeeId(userInfo.getId());
                    SessionManager.getInstance().setLoggedInEmployeeName(userInfo.getName());
                    SessionManager.getInstance().setManager(userInfo.isManager());
                    return true;
                }
            }
            return false; // User not found or password incorrect

        } catch (Exception e) {
            LOGGER.error("Error during authentication process.", e);
            throw new Exception("Authentication failed due to a system error.", e);
        }
    }
}