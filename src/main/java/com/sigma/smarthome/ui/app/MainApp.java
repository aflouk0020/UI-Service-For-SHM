package com.sigma.smarthome.ui.app;

import com.sigma.smarthome.ui.service.UserApiService;
import com.sigma.smarthome.ui.service.UserApiService.LoginResult;
import com.sigma.smarthome.ui.util.SessionManager;
import com.sigma.smarthome.ui.view.DashboardView;
import com.sigma.smarthome.ui.view.LoginView;
import com.sigma.smarthome.ui.view.RegisterView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainApp extends Application {

    private static final Logger LOGGER = Logger.getLogger(MainApp.class.getName());
    private static final String APP_TITLE = "Smart Home Maintenance Platform";

    private final UserApiService userApiService = new UserApiService();

    @Override
    public void start(Stage primaryStage) {
        showLogin(primaryStage);
    }

    private void showLogin(Stage stage) {
        LoginView loginView = new LoginView();

        loginView.setOnLogin(() -> {
            String email = loginView.getEmail();
            String password = loginView.getPassword();

            if (email.isBlank() || password.isBlank()) {
                loginView.setMessage("Email and password are required.", true);
                return;
            }

            loginView.setLoading(true);

            try {
                LoginResult result = userApiService.login(email, password);

                SessionManager.startSession(
                        result.accessToken(),
                        result.email(),
                        result.role()
                );

                LOGGER.info("Login successful for " + result.email());
                showDashboard(stage);

            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                LOGGER.log(Level.SEVERE, "Login interrupted.", ex);
                loginView.setMessage("Login was interrupted. Please try again.", true);
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Login failed due to service communication error.", ex);
                loginView.setMessage("Login failed. Check credentials or service connection.", true);
            } catch (RuntimeException ex) {
                LOGGER.log(Level.SEVERE, "Login failed.", ex);
                loginView.setMessage("Login failed. Check credentials or service connection.", true);
            } finally {
                loginView.setLoading(false);
            }
        });

        loginView.setOnCreateAccount(() -> showRegister(stage));

        Scene scene = new Scene(loginView.getView(), 1100, 700);
        stage.setTitle(APP_TITLE);
        stage.setScene(scene);
        stage.setMinWidth(950);
        stage.setMinHeight(650);
        stage.show();

        loginView.focusEmailField();
    }

    private void showRegister(Stage stage) {
        RegisterView registerView = new RegisterView();

        registerView.setOnRegister(() -> {
            String email = registerView.getEmail();
            String password = registerView.getPassword();
            String role = registerView.getRole();

            if (email.isBlank() || password.isBlank() || role == null || role.isBlank()) {
                registerView.setMessage("Email, password, and role are required.", true);
                return;
            }

            registerView.setLoading(true);

            try {
                String registeredEmail = userApiService.register(email, password, role);
                registerView.setMessage("Account created successfully for " + registeredEmail + ".", false);

            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                LOGGER.log(Level.SEVERE, "Registration interrupted.", ex);
                registerView.setMessage("Registration was interrupted. Please try again.", true);
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Registration failed due to service communication error.", ex);
                registerView.setMessage("Registration failed. Check details or service connection.", true);
            } catch (RuntimeException ex) {
                LOGGER.log(Level.SEVERE, "Registration failed.", ex);
                registerView.setMessage("Registration failed. Check details or service connection.", true);
            } finally {
                registerView.setLoading(false);
            }
        });

        registerView.setOnBackToLogin(() -> showLogin(stage));

        Scene scene = new Scene(registerView.getView(), 1100, 700);
        stage.setTitle(APP_TITLE);
        stage.setScene(scene);
        stage.show();
    }

    private void showDashboard(Stage stage) {
        DashboardView dashboardView = new DashboardView(
                SessionManager.getEmail(),
                SessionManager.getRole(),
                () -> {
                    SessionManager.clearSession();
                    showLogin(stage);
                }
        );

        Scene scene = new Scene(dashboardView.getView(), 1280, 800);
        stage.setTitle(APP_TITLE);
        stage.setScene(scene);
        stage.setMinWidth(1100);
        stage.setMinHeight(700);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}