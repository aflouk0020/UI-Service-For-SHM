package com.sigma.smarthome.ui.view;

import com.sigma.smarthome.ui.view.PlaceholderView;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Parent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlaceholderViewTest {

    @BeforeAll
    static void initJavaFx() {
        new JFXPanel();
    }

    @Test
    void placeholderView_isCreatedSuccessfully() {
        PlaceholderView view = new PlaceholderView("Dashboard", "Welcome message");
        Parent root = view.getView();

        assertNotNull(root);
    }
}