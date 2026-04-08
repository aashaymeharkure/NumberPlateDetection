package FXMini;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;

public class Sidebar {

    private VBox view;
    private index mainApp;
    private Button allViolationsButton;
    private Button detectViolationButton;

    public Sidebar(index mainApp) {
        this.mainApp = mainApp;

        view = new VBox(10);
        view.setPadding(new Insets(20));
        view.getStyleClass().add("sidebar");
        view.setAlignment(Pos.TOP_LEFT);

        Label logo = new Label("TrafficEnforce");
        logo.getStyleClass().add("sidebar-logo");
        VBox.setMargin(logo, new Insets(0, 0, 30, 0));

        allViolationsButton = createMenuButton(
            "M16 8.5l-6 4.5-6-4.5v-2.5l6 4.5 6-4.5v2.5z M16 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2z",
            "All Violations"
        );
        allViolationsButton.setOnAction(e -> {
            setActive(allViolationsButton);
            mainApp.showAllViolations();
        });

        detectViolationButton = createMenuButton(
            "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm-1-13h2v6h-2zm0 8h2v2h-2z",
            "Detect Violation"
        );
        detectViolationButton.setOnAction(e -> {
            setActive(detectViolationButton);
            mainApp.showDetectViolationView();
        });

        Button publicSearchButton = createMenuButton(
            "M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z",
            "Public Search"
        );
        publicSearchButton.setOnAction(e -> mainApp.showPublicSearchView());

        Button logoutButton = createMenuButton(
            "M13 3h-2v10h2V3zm4.83 2.17l-1.42 1.42C17.99 7.86 19 9.81 19 12c0 3.87-3.13 7-7 7s-7-3.13-7-7c0-2.19 1.01-4.14 2.58-5.42L6.17 5.17C4.23 6.82 3 9.26 3 12c0 4.97 4.03 9 9 9s9-4.03 9-9c0-2.74-1.23-5.18-3.17-6.83z",
            "Logout"
        );
        logoutButton.setOnAction(e -> mainApp.logout());

        view.getChildren().addAll(
            logo,
            new Label("ADMIN PORTAL"),
            detectViolationButton,
            allViolationsButton,
            new Label("SESSION"),
            logoutButton,
            new Label("PUBLIC"),
            publicSearchButton
        );

        setActive(allViolationsButton);
    }

    private Button createMenuButton(String svgPath, String text) {
        SVGPath icon = new SVGPath();
        icon.setContent(svgPath);
        icon.getStyleClass().add("sidebar-icon");

        Button button = new Button(text);
        button.setGraphic(icon);
        button.setAlignment(Pos.CENTER_LEFT);
        button.getStyleClass().add("sidebar-button");
        return button;
    }

    private void setActive(Button activeButton) {
        allViolationsButton.getStyleClass().remove("active");
        detectViolationButton.getStyleClass().remove("active");
        activeButton.getStyleClass().add("active");
    }

    public Node getView() {
        return view;
    }
}
