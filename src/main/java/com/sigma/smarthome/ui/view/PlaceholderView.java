package com.sigma.smarthome.ui.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class PlaceholderView {

    private final VBox root = new VBox(14);

    public PlaceholderView(String titleText, String descriptionText) {
        root.setAlignment(Pos.TOP_LEFT);
        root.setPadding(new Insets(20));

        Label title = new Label(titleText);
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");

        Label description = new Label(descriptionText);
        description.setStyle("-fx-font-size: 15px; -fx-text-fill: #6b7280;");

        root.getChildren().addAll(title, description);
    }

    public Parent getView() {
        return root;
    }
}