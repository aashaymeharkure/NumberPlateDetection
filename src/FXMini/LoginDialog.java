package FXMini;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Pair;
import java.util.Optional;

public class LoginDialog extends Stage {

    private Optional<Pair<String, String>> result = Optional.empty();

    public LoginDialog(Stage owner) {
        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.TRANSPARENT);

        VBox dialogRoot = new VBox(25);
        dialogRoot.getStyleClass().add("login-dialog");
        dialogRoot.setPadding(new Insets(40));
        dialogRoot.setAlignment(Pos.CENTER);
        dialogRoot.setMinWidth(400);

        // Stylized Lock Icon
        SVGPath lockIcon = new SVGPath();
        lockIcon.setContent("M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1s3.1 1.39 3.1 3.1v2z");
        lockIcon.getStyleClass().add("hero-icon");
        lockIcon.setScaleX(2.5);
        lockIcon.setScaleY(2.5);
        VBox.setMargin(lockIcon, new Insets(0, 0, 10, 0));

        Label title = new Label("Admin Login");
        title.getStyleClass().add("admin-view-title");
        title.setStyle("-fx-font-size: 24px;"); // slightly smaller for dialog
        
        Label subtitle = new Label("Please enter your credentials to access the portal.");
        subtitle.getStyleClass().add("view-subtitle");
        subtitle.setAlignment(Pos.CENTER);

        VBox form = new VBox(15);
        form.setPadding(new Insets(10, 0, 20, 0));
        
        VBox userBox = new VBox(5);
        Label usernameLabel = new Label("Username");
        usernameLabel.getStyleClass().add("feature-title");
        usernameLabel.setStyle("-fx-font-size: 14px; -fx-padding: 0;");
        TextField usernameField = new TextField();
        usernameField.setPromptText("adminJava");
        userBox.getChildren().addAll(usernameLabel, usernameField);

        VBox passBox = new VBox(5);
        Label passwordLabel = new Label("Password");
        passwordLabel.getStyleClass().add("feature-title");
        passwordLabel.setStyle("-fx-font-size: 14px; -fx-padding: 0;");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("•••••");
        passBox.getChildren().addAll(passwordLabel, passwordField);

        form.getChildren().addAll(userBox, passBox);

        Button loginButton = new Button("Sign In");
        loginButton.getStyleClass().add("primary-button");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setDefaultButton(true);

        Button cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().add("outline-button");
        cancelButton.setMaxWidth(Double.MAX_VALUE);
        
        loginButton.setOnAction(e -> {
            result = Optional.of(new Pair<>(usernameField.getText(), passwordField.getText()));
            close();
        });
        
        cancelButton.setOnAction(e -> close());

        dialogRoot.getChildren().addAll(lockIcon, title, subtitle, form, loginButton, cancelButton);

        Scene scene = new Scene(dialogRoot);
        scene.setFill(null);
        
        try {
            System.out.println("DEBUG: Loading CSS in LoginDialog...");
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
            System.out.println("DEBUG: CSS loaded successfully in LoginDialog.");
        } catch (Exception e) {
            System.err.println("ERROR: Could not load CSS in LoginDialog: " + e.getMessage());
            e.printStackTrace();
        }

        setScene(scene);
        System.out.println("DEBUG: LoginDialog initialized.");
    }

    public Optional<Pair<String, String>> showDialogAndWait() {
        super.showAndWait();
        return result;
    }
}
