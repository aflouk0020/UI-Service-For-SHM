package sigma.smarthome.ui.util;

import org.junit.jupiter.api.Test;

import com.sigma.smarthome.ui.util.SessionManager;

import static org.junit.jupiter.api.Assertions.*;

class SessionManagerTest {

    @Test
    void startSession_setsUserData() {

        SessionManager.startSession("token123", "test@example.com", "PROPERTY_MANAGER");

        assertEquals("test@example.com", SessionManager.getEmail());
        assertEquals("token123", SessionManager.getAccessToken());
        assertEquals("PROPERTY_MANAGER", SessionManager.getRole());
        assertTrue(SessionManager.isLoggedIn());
    }

    @Test
    void clearSession_resetsState() {

        SessionManager.startSession("token456", "user@test.com", "MAINTENANCE_STAFF");

        SessionManager.clearSession();

        assertNull(SessionManager.getEmail());
        assertNull(SessionManager.getAccessToken());
        assertNull(SessionManager.getRole());
        assertFalse(SessionManager.isLoggedIn());
    }
    @Test
    void isLoggedIn_false_whenNoSession() {

        SessionManager.clearSession();

        assertFalse(SessionManager.isLoggedIn());
    }
}
