package com.sigma.smarthome.ui.app;

import com.sigma.smarthome.ui.service.UserApiService;
import com.sigma.smarthome.ui.view.LoginView;
import com.sigma.smarthome.ui.view.RegisterView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

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
                String token = userApiService.login(email, password);

                System.out.println("LOGIN SUCCESS");
                System.out.println("JWT TOKEN: " + token);

                loginView.setMessage("Login successful.", false);

            } catch (Exception ex) {
                ex.printStackTrace();
                loginView.setMessage("Login failed. Check credentials or service connection.", true);
            } finally {
                loginView.setLoading(false);
            }
        });

        loginView.setOnCreateAccount(() -> showRegister(stage));

        Scene scene = new Scene(loginView.getView(), 1100, 700);
        stage.setTitle("Smart Home Maintenance Platform");
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

            } catch (Exception ex) {
                ex.printStackTrace();
                registerView.setMessage("Registration failed. Check details or service connection.", true);
            } finally {
                registerView.setLoading(false);
            }
        });

        registerView.setOnBackToLogin(() -> showLogin(stage));

        Scene scene = new Scene(registerView.getView(), 1100, 700);
        stage.setTitle("Smart Home Maintenance Platform");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}