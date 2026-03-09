package sigma.smarthome.ui.view;

import com.sigma.smarthome.ui.view.DashboardView;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Parent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DashboardViewTest {

    @BeforeAll
    static void initJavaFx() {
        new JFXPanel();
    }

    @Test
    void dashboardView_loadsSuccessfully() {

        DashboardView view = new DashboardView(
                "test@example.com",
                "PROPERTY_MANAGER",
                () -> {}
        );

        Parent root = view.getView();

        assertNotNull(root);
    }
}