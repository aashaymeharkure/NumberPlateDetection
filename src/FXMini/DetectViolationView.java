package FXMini;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.SVGPath;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class DetectViolationView {

    private VBox view;
    private Stage ownerStage;
    private index mainApp;
    private ViolationDataService dataService;

    private File selectedImageFile;
    private ImageView previewImage = new ImageView();
    private Label selectedFileName = new Label("No file selected.");
    private Button detectButton;
    private ProgressIndicator progressIndicator = new ProgressIndicator();
    private Label detectionStatus = new Label();

    private TextField licensePlateField = new TextField();
    private TextField locationField = new TextField();
    private ComboBox<String> violationTypeCombo = new ComboBox<>();
    private ComboBox<String> vehicleTypeCombo = new ComboBox<>();
    private Button confirmButton;

    public DetectViolationView(Stage ownerStage, index mainApp, ViolationDataService dataService) {
        this.ownerStage = ownerStage;
        this.mainApp = mainApp;
        this.dataService = dataService;
        view = new VBox(30);
        view.setAlignment(Pos.TOP_CENTER);
        view.setPadding(new Insets(40));
        view.getStyleClass().add("detect-violation-view");
        Label title = new Label("Detect New Violation");
        title.getStyleClass().add("view-title");
        Label subtitle = new Label("Upload an image to automatically detect the license plate.");
        subtitle.getStyleClass().add("view-subtitle");
        view.getChildren().addAll(title, subtitle, createStep1Box());
    }

    private Node createStep1Box() {
        VBox step1Container = new VBox(30);
        step1Container.setAlignment(Pos.CENTER);
        step1Container.getStyleClass().add("step-box");
        step1Container.setMaxWidth(800);
        
        Label step1Title = new Label("1. Select Source Material");
        step1Title.getStyleClass().add("step-title");
        step1Title.setStyle("-fx-font-size: 20px;");
        
        VBox dropZone = new VBox(20);
        dropZone.setAlignment(Pos.CENTER);
        dropZone.getStyleClass().add("drop-zone");
        
        SVGPath uploadIcon = new SVGPath();
        uploadIcon.setContent("M19.35 10.04C18.67 6.59 15.64 4 12 4 9.11 4 6.6 5.64 5.35 8.04 2.34 8.36 0 10.91 0 14c0 3.31 2.69 6 6 6h13c2.76 0 5-2.24 5-5 0-2.64-2.05-4.78-4.65-4.96zM14 13v4h-4v-4H7l5-5 5 5h-3z");
        uploadIcon.getStyleClass().add("drop-zone-icon");
        uploadIcon.setScaleX(1.5);
        uploadIcon.setScaleY(1.5);
        
        Label dropLabel = new Label("Drag & drop evidence image here");
        dropLabel.getStyleClass().add("view-subtitle");
        
        HBox actionButtons = new HBox(20);
        actionButtons.setAlignment(Pos.CENTER);
        
        Button browseButton = new Button("Browse Files");
        browseButton.getStyleClass().add("secondary-button");
        browseButton.setOnAction(e -> onBrowseFile());

        Button webcamButton = new Button("Open Live Webcam");
        webcamButton.getStyleClass().add("primary-button");
        webcamButton.setStyle("-fx-background-color: -fx-secondary-color;");
        webcamButton.setOnAction(e -> onScanWebcam());

        actionButtons.getChildren().addAll(browseButton, webcamButton);

        dropZone.getChildren().addAll(uploadIcon, dropLabel, new Label("OR"), actionButtons);
        
        detectButton = new Button("Start Auto-Detection");
        detectButton.getStyleClass().add("primary-button");
        detectButton.setDisable(true);
        detectButton.setPrefHeight(50);
        detectButton.setMinWidth(250);
        detectButton.setOnAction(e -> onDetectPlate());
        
        selectedFileName.getStyleClass().add("view-subtitle");
        detectionStatus.getStyleClass().add("view-subtitle");
        detectionStatus.setVisible(false);
        progressIndicator.setVisible(false);
        progressIndicator.setMaxSize(40, 40);
        
        step1Container.getChildren().addAll(step1Title, dropZone, selectedFileName, detectButton, progressIndicator, detectionStatus);
        return step1Container;
    }

    private void onScanWebcam() {
        detectionStatus.setText("Opening webcam... Press 'Q' in webcam window to close.");
        detectionStatus.setVisible(true);
        progressIndicator.setVisible(true);

        Task<String> webcamTask = new Task<>() {
            @Override
            protected String call() throws Exception {
                String pythonExecutablePath = resolvePythonExecutable();
                String scriptPath = Paths.get("Extract_AI", "extractor.py").toString();

                ProcessBuilder pb = new ProcessBuilder(
                        pythonExecutablePath,
                        scriptPath,
                        "--webcam"
                );
                pb.redirectErrorStream(true);

                Process process = pb.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                String lastAddedPlate = "";
                long lastAddedTime = 0;

                while ((line = reader.readLine()) != null) {
                    System.out.println("Webcam output: " + line);
                    if (line.contains("DETECTED:")) {
                        String plate = line.split("DETECTED:")[1].trim().split(" ")[0];
                        long currentTime = System.currentTimeMillis();
                        
                        // Prevent duplicate adding of same plate within 5 seconds
                        if (!plate.equals(lastAddedPlate) || (currentTime - lastAddedTime > 5000)) {
                            final String finalPlate = plate;
                            lastAddedPlate = plate;
                            lastAddedTime = currentTime;

                            Platform.runLater(() -> {
                                String timestamp = new SimpleDateFormat("MMM dd, yyyy h:mm a").format(new Date());
                                Violation autoViolation = new Violation(
                                    finalPlate,
                                    timestamp,
                                    "Webcam Automatic Detection",
                                    "Speeding", 
                                    "Unpaid",
                                    "Car"
                                );
                                dataService.addViolation(autoViolation);
                                licensePlateField.setText(finalPlate);
                                detectionStatus.setText("AUTO-CHALLAN CREATED for: " + finalPlate);
                            });
                        }
                    }
                }
                process.waitFor();
                return lastAddedPlate;
            }
        };

        webcamTask.setOnSucceeded(e -> {
            progressIndicator.setVisible(false);
            String lastPlate = webcamTask.getValue();
            if (lastPlate != null && !lastPlate.isEmpty()) {
                mainApp.showAllViolations();
            } else {
                detectionStatus.setText("Webcam closed.");
            }
        });

        webcamTask.setOnFailed(e -> {
            progressIndicator.setVisible(false);
            detectionStatus.setText("Failed to start webcam.");
            showAlert(Alert.AlertType.ERROR, "Webcam Error", "Could not start the webcam process.");
        });

        new Thread(webcamTask).start();
    }

    private void onBrowseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select License Plate Image");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(ownerStage);
        if (file != null) {
            selectedImageFile = file;
            selectedFileName.setText("Selected file: " + selectedImageFile.getName());
            detectButton.setDisable(true);
            detectionStatus.setText("Detecting plate automatically...");
            detectionStatus.setVisible(true);
            onDetectPlate();
        }
    }
    
    private void onDetectPlate() {
        if (selectedImageFile == null) return;

        progressIndicator.setVisible(true);
        detectButton.setDisable(true);
        detectionStatus.setVisible(true);

        Task<String> detectionTask = new Task<>() {
            @Override
            protected String call() throws Exception {
                String pythonExecutablePath = resolvePythonExecutable();
                String scriptPath = Paths.get("Extract_AI", "extractor.py").toString();

                ProcessBuilder pb = new ProcessBuilder(
                        pythonExecutablePath,
                        scriptPath,
                        selectedImageFile.getAbsolutePath()
                );
                pb.redirectErrorStream(true);

                Process process = pb.start();
                
                String fullOutput = new BufferedReader(new InputStreamReader(process.getInputStream()))
                        .lines().collect(Collectors.joining("\n"));

                process.waitFor();
                return fullOutput;
            }
        };

        detectionTask.setOnSucceeded(e -> {
            progressIndicator.setVisible(false);
            String result = detectionTask.getValue().trim();
            
            if (result.startsWith("ERROR:") || result.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Detection Failed", "Could not extract text. Python script returned: " + result);
                detectButton.setDisable(false);
                detectionStatus.setText("Auto-detection failed. You can retry detection.");
            } else {
                detectionStatus.setText("Plate detected successfully.");
                licensePlateField.setText(result);
                showStep2();
            }
        });

        detectionTask.setOnFailed(e -> {
            progressIndicator.setVisible(false);
            detectButton.setDisable(false);
            detectionStatus.setText("Auto-detection failed. You can retry detection.");
            showAlert(Alert.AlertType.ERROR, "Execution Error", "Failed to run the Python script. Is the venv path correct?");
            detectionTask.getException().printStackTrace();
        });

        new Thread(detectionTask).start();
    }

    private String resolvePythonExecutable() {
        String osName = System.getProperty("os.name", "").toLowerCase();

        if (osName.contains("win")) {
            Path workspaceVenvPython = Paths.get(".venv", "Scripts", "python.exe");
            if (Files.exists(workspaceVenvPython)) {
                return workspaceVenvPython.toString();
            }

            Path bundledPython = Paths.get("Extract_AI", "venv", "Scripts", "python.exe");
            if (Files.exists(bundledPython)) {
                return bundledPython.toString();
            }
        } else {
            Path workspaceVenvPython = Paths.get(".venv", "bin", "python3");
            if (Files.exists(workspaceVenvPython)) {
                return workspaceVenvPython.toString();
            }

            Path bundledPython = Paths.get("Extract_AI", "venv", "bin", "python3");
            if (Files.exists(bundledPython)) {
                return bundledPython.toString();
            }
        }

        return "python";
    }

    private void showStep2() {
        view.getChildren().clear();
        Label title = new Label("Confirm Violation");
        title.getStyleClass().add("view-title");
        HBox container = new HBox(40);
        container.setAlignment(Pos.CENTER);
        VBox leftPane = createLeftPaneStep2();
        VBox rightPane = createRightPaneStep2();
        container.getChildren().addAll(leftPane, rightPane);
        view.getChildren().addAll(title, container);
    }

    private VBox createLeftPaneStep2() {
        VBox leftPane = new VBox(20);
        leftPane.setAlignment(Pos.CENTER);
        leftPane.getStyleClass().add("step-box");
        Label stepTitle = new Label("1. Uploaded Image");
        stepTitle.getStyleClass().add("step-title");
        try {
            Image image = new Image(selectedImageFile.toURI().toString());
            previewImage.setImage(image);
            previewImage.setFitWidth(300);
            previewImage.setPreserveRatio(true);
            previewImage.getStyleClass().add("preview-image");
        } catch(Exception e) {
            System.err.println("Could not load preview image.");
        }
        leftPane.getChildren().addAll(stepTitle, previewImage);
        return leftPane;
    }

    private VBox createRightPaneStep2() {
        VBox rightPane = new VBox(15);
        rightPane.getStyleClass().add("step-box");
        Label stepTitle = new Label("2. Confirm Violation");
        stepTitle.getStyleClass().add("step-title");
        Label plateLabel = new Label("License Plate");
        licensePlateField.setPromptText("e.g., MH12AB1234");
        Label locationLabel = new Label("Location");
        locationField.setPromptText("e.g., Main St & 1st Ave");
        Label violationTypeLabel = new Label("Violation Type");
        violationTypeCombo.getItems().addAll("Speeding", "Red Light", "Illegal Parking");
        violationTypeCombo.setValue("Speeding");
        Label vehicleTypeLabel = new Label("Vehicle Type");
        vehicleTypeCombo.getItems().addAll("Car", "Bike", "Truck", "Bus");
        vehicleTypeCombo.setValue("Car");
        confirmButton = new Button("✔ Confirm Violation");
        confirmButton.getStyleClass().add("confirm-button");
        confirmButton.setOnAction(e -> onConfirmViolation());
        rightPane.getChildren().addAll(
            stepTitle, plateLabel, licensePlateField,
            locationLabel, locationField, violationTypeLabel, violationTypeCombo,
            vehicleTypeLabel, vehicleTypeCombo, confirmButton
        );
        return rightPane;
    }

    private void onConfirmViolation() {
        String timestamp = new SimpleDateFormat("MMM dd, yyyy h:mm a").format(new Date());
        Violation newViolation = new Violation(
            licensePlateField.getText(),
            timestamp,
            locationField.getText(),
            violationTypeCombo.getValue(),
            "Unpaid",
            vehicleTypeCombo.getValue()
        );
        dataService.addViolation(newViolation);
        showAlert(Alert.AlertType.INFORMATION, "Success", "Violation has been successfully recorded.");
        mainApp.showAllViolations();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public Node getView() {
        return view;
    }
}
