package sigma.smarthome.ui.view;

import com.sigma.smarthome.ui.view.RegisterView;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Parent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class RegisterViewTest {

    @BeforeAll
    static void initJavaFx() {
        new JFXPanel();
    }

    @Test
    void registerView_isCreatedSuccessfully() {
        RegisterView view = new RegisterView();
        Parent root = view.getView();

        assertNotNull(root);
    }

    @Test
    void getters_returnDefaultValuesInitially() {
        RegisterView view = new RegisterView();

        assertEquals("", view.getEmail());
        assertEquals("", view.getPassword());
        assertNull(view.getRole());
    }

    @Test
    void setOnRegister_executesCallback() {
        RegisterView view = new RegisterView();
        AtomicBoolean called = new AtomicBoolean(false);

        view.setOnRegister(() -> called.set(true));

        assertNotNull(view.getView());
    }

    @Test
    void setOnBackToLogin_executesCallback() {
        RegisterView view = new RegisterView();
        AtomicBoolean called = new AtomicBoolean(false);

        view.setOnBackToLogin(() -> called.set(true));

        assertNotNull(view.getView());
    }

    @Test
    void setMessage_acceptsErrorAndSuccess() {
        RegisterView view = new RegisterView();

        view.setMessage("Error happened", true);
        view.setMessage("Success happened", false);

        assertNotNull(view.getView());
    }

    @Test
    void setLoading_togglesState() {
        RegisterView view = new RegisterView();

        view.setLoading(true);
        view.setLoading(false);

        assertNotNull(view.getView());
    }
}