package FXMini;

import java.util.Optional;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.util.Pair;

public class index extends Application {

    private Stage primaryStage;
    private BorderPane root;
    private ViolationDataService dataService = new ViolationDataService();

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.root = new BorderPane();
        
        showPublicSearchView();

        Scene scene = new Scene(root, 1280, 800);
        
        // GLOBAL DEBUG FILTER: Identify what is being clicked
        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            System.out.println("DEBUG: Mouse Pressed at X=" + e.getSceneX() + ", Y=" + e.getSceneY() + 
                               " | Target: " + e.getTarget() + " | Type: " + e.getEventType());
        });

        String cssPath = "/styles.css";
        try {
             scene.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
        } catch (Exception e) {
            System.err.println("Could not find CSS file: " + cssPath);
            showAlert("Styling Error", "The application's CSS file could not be loaded. The UI will not be styled correctly.");
        }

        primaryStage.setTitle("TrafficEnforce");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1024);
        primaryStage.setMinHeight(768);
        primaryStage.show();
    }

    private boolean isAdminLoggedIn = false;

    public void showLoginDialog() {
        System.out.println("DEBUG: Entering showLoginDialog()");
        if (isAdminLoggedIn) {
            System.out.println("DEBUG: Admin already logged in, showing dashboard.");
            showAdminDashboard();
            return;
        }

        try {
            LoginDialog loginDialog = new LoginDialog(primaryStage);
            System.out.println("DEBUG: LoginDialog instance created.");
            Optional<Pair<String, String>> result = loginDialog.showDialogAndWait();

            result.ifPresent(credentials -> {
                System.out.println("DEBUG: Credentials received.");
                if ("adminJava".equals(credentials.getKey()) && "123".equals(credentials.getValue())) {
                    isAdminLoggedIn = true;
                    System.out.println("DEBUG: Login successful. Showing Admin Dashboard.");
                    showAdminDashboard();
                } else {
                    System.out.println("DEBUG: Login failed. Invalid credentials.");
                    showAlert("Login Failed", "Invalid username or password.");
                }
            });
            if (result.isEmpty()) {
                System.out.println("DEBUG: Login dialog dismissed without result.");
            }
        } catch (Exception e) {
            System.err.println("ERROR: Exception while showing LoginDialog: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void logout() {
        isAdminLoggedIn = false;
        showPublicSearchView();
    }

    private void showAdminDashboard() {
        Sidebar sidebar = new Sidebar(this);
        root.setLeft(sidebar.getView());
        root.setTop(null); // Sidebar has its own logo/header
        showAllViolations();
    }

    public void showAllViolations() {
        AllViolationsView allViolationsView = new AllViolationsView(dataService);
        root.setCenter(allViolationsView.getView());
    }

    public void showDetectViolationView() {
        DetectViolationView detectViolationView = new DetectViolationView(primaryStage, this, dataService);
        root.setCenter(detectViolationView.getView());
    }

    public void showPublicSearchView() {
        PublicSearchView publicSearchView = new PublicSearchView(this::showLoginDialog, dataService);
        root.setLeft(null);
        root.setTop(createFixedHeader()); // ADD FIXED HEADER TO TOP
        root.setCenter(publicSearchView.getView());
    }

    private HBox createFixedHeader() {
        HBox header = new HBox();
        header.setPadding(new Insets(20, 60, 20, 60));
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("top-nav");

        Label logo = new Label("TrafficEnforce");
        logo.getStyleClass().add("public-view-logo");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button adminPortalButton = new Button("Admin Portal");
        adminPortalButton.getStyleClass().add("primary-button");
        adminPortalButton.setOnAction(e -> {
            System.out.println("DEBUG: Admin Portal button CLICKED from FIXED HEADER!");
            showLoginDialog();
        });

        header.getChildren().addAll(logo, spacer, adminPortalButton);
        return header;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
