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
        root.setPadding(new Insets(26));
        root.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 18;" +
                "-fx-border-color: #e2e8f0;" +
                "-fx-border-radius: 18;" +
                "-fx-border-width: 1;"
        );

        Label sectionLabel = new Label("Module");
        sectionLabel.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #94a3b8;"
        );

        Label title = new Label(titleText);
        title.setStyle(
                "-fx-font-size: 30px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #0f172a;"
        );

        Label description = new Label(descriptionText);
        description.setWrapText(true);
        description.setStyle(
                "-fx-font-size: 15px;" +
                "-fx-text-fill: #64748b;"
        );

        root.getChildren().addAll(sectionLabel, title, description);
    }

    public Parent getView() {
        return root;
    }
}