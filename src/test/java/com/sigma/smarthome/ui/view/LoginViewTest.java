package com.sigma.smarthome.ui.view;

import com.sigma.smarthome.ui.view.LoginView;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Parent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class LoginViewTest {

    @BeforeAll
    static void initJavaFx() {
        new JFXPanel();
    }

    @Test
    void loginView_isCreatedSuccessfully() {
        LoginView view = new LoginView();
        Parent root = view.getView();

        assertNotNull(root);
    }

    @Test
    void getters_returnDefaultValuesInitially() {
        LoginView view = new LoginView();

        assertEquals("", view.getEmail());
        assertEquals("", view.getPassword());
    }

    @Test
    void clearFields_and_clearPassword_doNotFail() {
        LoginView view = new LoginView();

        view.clearPassword();
        view.clearFields();

        assertEquals("", view.getEmail());
        assertEquals("", view.getPassword());
    }

    @Test
    void setMessage_acceptsErrorAndSuccess() {
        LoginView view = new LoginView();

        view.setMessage("Login failed", true);
        view.setMessage("Login ok", false);

        assertNotNull(view.getView());
    }

    @Test
    void hideMessage_doesNotFail() {
        LoginView view = new LoginView();

        view.setMessage("Something", true);
        view.hideMessage();

        assertNotNull(view.getView());
    }

    @Test
    void setLoading_togglesState() {
        LoginView view = new LoginView();

        view.setLoading(true);
        view.setLoading(false);

        assertNotNull(view.getView());
    }

    @Test
    void callbacks_canBeAssigned() {
        LoginView view = new LoginView();
        AtomicBoolean loginCalled = new AtomicBoolean(false);
        AtomicBoolean createCalled = new AtomicBoolean(false);

        view.setOnLogin(() -> loginCalled.set(true));
        view.setOnCreateAccount(() -> createCalled.set(true));

        assertNotNull(view.getView());
    }

    @Test
    void focusEmailField_doesNotFail() {
        LoginView view = new LoginView();
        view.focusEmailField();

        assertNotNull(view.getView());
    }
}