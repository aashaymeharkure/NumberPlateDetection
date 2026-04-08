package FXMini;

import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;

public class PublicSearchView {

    private VBox view;
    private ViolationDataService dataService;
    private VBox resultsContainer;
    private TextField searchField;

    public PublicSearchView(Runnable onAdminPortalClick, ViolationDataService dataService) {
        this.dataService = dataService;

        view = new VBox(0);
        view.setAlignment(Pos.TOP_CENTER);
        view.getStyleClass().add("public-view");
        
        // 1. Hero Section
        VBox hero = new VBox(25);
        hero.setPadding(new Insets(60, 20, 40, 20));
        hero.setAlignment(Pos.CENTER);

        // SVG Car Icon / Illustration placeholder
        SVGPath carIcon = new SVGPath();
        carIcon.setContent("M18.92 6.01C18.72 5.42 18.16 5 17.5 5h-11c-.66 0-1.21.42-1.42 1.01L3 12v8c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-1h12v1c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-8l-2.08-5.99zM6.5 16c-.83 0-1.5-.67-1.5-1.5S5.67 13 6.5 13s1.5.67 1.5 1.5S7.33 16 6.5 16zm11 0c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5zM5 11l1.5-4.5h11L19 11H5z");
        carIcon.setScaleX(5);
        carIcon.setScaleY(5);
        carIcon.getStyleClass().add("hero-icon");
        VBox.setMargin(carIcon, new Insets(0, 0, 40, 0));

        Label title = new Label("Traffic Violation Check");
        title.getStyleClass().add("public-view-title");

        Label subtitle = new Label("Enter your license plate to check for outstanding fines and compliance issues.");
        subtitle.getStyleClass().add("public-view-subtitle");
        
        hero.getChildren().addAll(carIcon, title, subtitle);

        // 3. Search Card
        VBox searchCard = new VBox(25);
        searchCard.getStyleClass().add("search-card");
        searchCard.setPadding(new Insets(40));
        searchCard.setMaxWidth(700);
        searchCard.setAlignment(Pos.CENTER);

        searchField = new TextField();
        searchField.setPromptText("Enter License Plate (e.g., MH12AB1234)");
        searchField.setPrefHeight(55);
        searchField.getStyleClass().add("hero-search-field");
        
        Button searchButton = new Button("Search Violations");
        searchButton.getStyleClass().add("primary-button");
        searchButton.setPrefHeight(55);
        searchButton.setMinWidth(200);
        
        HBox searchInputBox = new HBox(15, searchField, searchButton);
        searchInputBox.setAlignment(Pos.CENTER);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        
        searchCard.getChildren().add(searchInputBox);
        
        // 4. Features Section
        HBox features = new HBox(40);
        features.setPadding(new Insets(60, 20, 60, 20));
        features.setAlignment(Pos.CENTER);
        features.getChildren().addAll(
            createFeatureNode("M13 3h-2v10h2V3zm4.83 2.17l-1.42 1.42C17.99 7.86 19 9.81 19 12c0 3.87-3.13 7-7 7s-7-3.13-7-7c0-2.19 1.01-4.14 2.58-5.42L6.17 5.17C4.23 6.82 3 9.26 3 12c0 4.97 4.03 9 9 9s9-4.03 9-9c0-2.74-1.23-5.18-3.17-6.83z", "Instant Results", "Get real-time data directly from the national database."),
            createFeatureNode("M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm0 10.99h7c-.53 4.12-3.28 7.79-7 8.94V12H5V6.3l7-3.11v8.8z", "Secure & Private", "Your data is protected with end-to-end encryption."),
            createFeatureNode("M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67z", "24/7 Support", "Always available to help with your compliance queries.")
        );

        resultsContainer = new VBox(30);
        resultsContainer.setAlignment(Pos.CENTER);
        resultsContainer.setPadding(new Insets(0, 0, 80, 0));

        searchButton.setOnAction(e -> performSearch(searchField.getText()));
        searchField.setOnAction(e -> performSearch(searchField.getText()));

        VBox mainContainer = new VBox(hero, searchCard, features, resultsContainer);
        mainContainer.setAlignment(Pos.TOP_CENTER);
        
        ScrollPane scrollPane = new ScrollPane(mainContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.getStyleClass().add("transparent-scroll");
        
        view.getChildren().add(scrollPane);
    }

    private Node createFeatureNode(String svg, String title, String desc) {
        VBox feature = new VBox(10);
        feature.setAlignment(Pos.CENTER);
        feature.setMaxWidth(250);
        
        SVGPath icon = new SVGPath();
        icon.setContent(svg);
        icon.getStyleClass().add("feature-icon");
        
        Label featureTitle = new Label(title);
        featureTitle.getStyleClass().add("feature-title");
        
        Label featureDesc = new Label(desc);
        featureDesc.getStyleClass().add("feature-desc");
        featureDesc.setWrapText(true);
        featureDesc.setAlignment(Pos.CENTER);
        
        feature.getChildren().addAll(icon, featureTitle, featureDesc);
        return feature;
    }


    private void performSearch(String plateNumber) {
        resultsContainer.getChildren().clear();
        if (plateNumber == null || plateNumber.trim().isEmpty()) {
            return;
        }

        Label resultTitle = new Label("Search Results for \"" + plateNumber.toUpperCase() + "\"");
        resultTitle.getStyleClass().add("card-subtitle");
        resultsContainer.getChildren().add(resultTitle);
        
        ObservableList<Violation> searchResults = dataService.searchByPlate(plateNumber);

        ObservableList<Violation> unpaidViolations = searchResults.stream()
                .filter(v -> "Unpaid".equalsIgnoreCase(v.getFineStatus()))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        if (unpaidViolations.isEmpty()) {
            resultsContainer.getChildren().add(createNoViolationsNode());
        } else {
            FlowPane violationCardsPane = new FlowPane(20, 20);
            violationCardsPane.setAlignment(Pos.CENTER);
            for (Violation violation : unpaidViolations) {
                violationCardsPane.getChildren().add(createViolationCard(violation));
            }
            resultsContainer.getChildren().add(violationCardsPane);
        }
    }

    private Node createNoViolationsNode() {
        VBox container = new VBox(20);
        container.getStyleClass().add("violation-card");
        container.setPadding(new Insets(40));
        container.setAlignment(Pos.CENTER);
        container.setMinWidth(600);

        SVGPath icon = new SVGPath();
        icon.setContent("M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1.07 13.29l-2.12-2.12 1.41-1.41 2.12 2.12-4.24-4.24 1.41-1.41 4.24 4.24 2.12-2.12 1.41 1.41-2.12 2.12-1.41-1.41-1.41 1.41z");
        icon.getStyleClass().add("detail-icon-small");
        icon.setScaleX(3);
        icon.setScaleY(3);

        Label title = new Label("No Violations Found");
        title.getStyleClass().add("violation-card-plate");

        Label subtitle = new Label("This license plate has a clean record in our system.");
        subtitle.getStyleClass().add("detail-text-small");
        
        container.getChildren().addAll(icon, title, subtitle);
        return container;
    }

    private Node createViolationCard(Violation violation) {
        VBox card = new VBox();
        card.getStyleClass().add("violation-card");
        card.setCursor(javafx.scene.Cursor.HAND);
        card.setOnMouseClicked(e -> showPaymentDetailsPopup(violation));

        VBox imageContainer = new VBox();
        imageContainer.getStyleClass().add("violation-image-container");
        
        String imageUrl = "https://placehold.co/400x150/6366f1/ffffff?text=" + violation.getPlate().replace(" ", "+");
        ImageView imageView = new ImageView();
        try {
            imageView.setImage(new Image(imageUrl, true));
        } catch(Exception e) {
            System.err.println("Failed to load image for result card: " + e.getMessage());
        }
        imageView.setFitHeight(160);
        imageView.setFitWidth(320);
        imageView.setPreserveRatio(false);
        imageView.getStyleClass().add("violation-image");
        imageContainer.getChildren().add(imageView);

        VBox content = new VBox(12);
        content.getStyleClass().add("violation-card-content");

        HBox typeStatusBox = new HBox(10);
        typeStatusBox.setAlignment(Pos.CENTER_LEFT);
        Label plateLabel = new Label(violation.getPlate());
        plateLabel.getStyleClass().add("violation-card-plate");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button status = new Button(violation.getFineStatus());
        status.getStyleClass().addAll("status-badge", "status-unpaid");
        status.setCursor(javafx.scene.Cursor.HAND);
        status.setOnAction(e -> {
            e.consume(); // Prevent card click from firing redundantly
            showPaymentDetailsPopup(violation);
        });

        typeStatusBox.getChildren().addAll(plateLabel, spacer, status);

        VBox details = new VBox(8);
        details.getChildren().addAll(
            createDetailIconRow("M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67z", violation.getTimestamp()),
            createDetailIconRow("M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z", violation.getLocation())
        );

        content.getChildren().addAll(typeStatusBox, details, fineLabel);
        card.getChildren().addAll(imageContainer, content);
        return card;
    }

    private void showPaymentDetailsPopup(Violation violation) {
        javafx.stage.Stage dialog = new javafx.stage.Stage();
        dialog.initOwner(view.getScene().getWindow());
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialog.setTitle("Violation Details & Payment");

        VBox container = new VBox(25);
        container.setPadding(new Insets(40));
        container.setAlignment(Pos.CENTER);
        container.setStyle("-fx-background-color: white; -fx-background-radius: 20;");

        Label title = new Label("Secure Fine Payment");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        VBox infoBox = new VBox(15);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        infoBox.setPadding(new Insets(20));
        infoBox.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 12; -fx-border-color: #e2e8f0; -fx-border-radius: 12;");

        String[][] details = {
            {"Registration Number", violation.getPlate()},
            {"Violation Type", violation.getViolationType()},
            {"Location", violation.getLocation()},
            {"Detected At", violation.getTimestamp()},
            {"Fine Amount", "₹" + violation.getFineAmount()}
        };

        for (String[] detail : details) {
            HBox row = new HBox(10);
            Label key = new Label(detail[0] + ":");
            key.setStyle("-fx-font-weight: bold; -fx-text-fill: #64748b; -fx-min-width: 150;");
            Label val = new Label(detail[1]);
            val.setStyle("-fx-text-fill: #0f172a; -fx-font-weight: 600;");
            row.getChildren().addAll(key, val);
            infoBox.getChildren().add(row);
        }

        Label totalLabel = new Label("Total Fine Payable: ₹" + violation.getFineAmount());
        totalLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: 800; -fx-text-fill: #ef4444;");

        Button payBtn = new Button("Secure Pay with Razorpay");
        payBtn.getStyleClass().add("primary-button");
        payBtn.setPrefHeight(50);
        payBtn.setMinWidth(300);
        payBtn.setStyle("-fx-background-color: #3395ff; -fx-font-size: 16px;");
        payBtn.setOnAction(e -> {
            dialog.close();
            handlePayment(violation);
        });

        Button closeBtn = new Button("Close");
        closeBtn.getStyleClass().add("secondary-button");
        closeBtn.setOnAction(e -> dialog.close());

        container.getChildren().addAll(title, infoBox, totalLabel, payBtn, closeBtn);

        Scene scene = new Scene(container);
        try {
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        } catch(Exception ex) {}
        
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void handlePayment(Violation violation) {
        String keyId = "rzp_test_SNzoYnfncgbjxu"; // As provided by user
        int amount = violation.getFineAmount() * 100; // In paise
        String plate = violation.getPlate();

        String html = "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "    <title>Razorpay Payment</title>\n" +
            "    <script src=\"https://checkout.razorpay.com/v1/checkout.js\"></script>\n" +
            "</head>\n" +
            "<body style=\"font-family: sans-serif; display: flex; align-items: center; justify-content: center; height: 100vh; background: #f4f7fe;\">\n" +
            "    <div style=\"background: white; padding: 40px; border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.1);\">\n" +
            "        <h2>Processing Payment for " + plate + "</h2>\n" +
            "        <p>Amount: ₹" + (amount/100.0) + "</p>\n" +
            "        <p id=\"status\">Redirecting to Razorpay...</p>\n" +
            "    </div>\n" +
            "    <script>\n" +
            "        var options = {\n" +
            "            \"key\": \"" + keyId + "\",\n" +
            "            \"amount\": \"" + amount + "\",\n" +
            "            \"name\": \"Traffic Enforcement System\",\n" +
            "            \"description\": \"Fine Payment for " + plate + "\",\n" +
            "            \"handler\": function (response){\n" +
            "                document.getElementById('status').innerText = \"Payment Successful! ID: \" + response.razorpay_payment_id;\n" +
            "                alert(\"Payment success. You may close this window and refresh the application.\");\n" +
            "                window.close();\n" +
            "            },\n" +
            "            \"prefill\": {\n" +
            "                \"name\": \"Citizen\",\n" +
            "                \"email\": \"citizen@example.com\"\n" +
            "            },\n" +
            "            \"theme\": {\n" +
            "                \"color\": \"#6366f1\"\n" +
            "            }\n" +
            "        };\n" +
            "        var rzp1 = new Razorpay(options);\n" +
            "        rzp1.open();\n" +
            "    </script>\n" +
            "</body>\n" +
            "</html>";

        try {
            java.io.File tempFile = java.io.File.createTempFile("razorpay_", ".html");
            java.nio.file.Files.write(tempFile.toPath(), html.getBytes());
            java.awt.Desktop.getDesktop().browse(tempFile.toURI());
            
            // For the sake of this application, we simulate the 'after payment' logic
            // Since we don't have a real server/callback, we'll mark as paid and remove after a dialog
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
            alert.setTitle("Process Payment Reward");
            alert.setHeaderText("Has the payment been completed?");
            alert.setContentText("Click confirm once payment is done to remove violation records.");
            
            alert.showAndWait().ifPresent(response -> {
                if (response == javafx.scene.control.ButtonType.OK) {
                    dataService.processPayment(violation);
                    performSearch(searchField.getText());
                    
                    javafx.scene.control.Alert success = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                    success.setTitle("Success");
                    success.setContentText("Payment processed. Violation record has been removed.");
                    success.show();
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private HBox createDetailIconRow(String svg, String text) {
        HBox row = new HBox(8);
        row.getStyleClass().add("detail-row");
        
        SVGPath icon = new SVGPath();
        icon.setContent(svg);
        icon.getStyleClass().add("detail-icon-small");
        
        Label label = new Label(text);
        label.getStyleClass().add("detail-text-small");
        
        row.getChildren().addAll(icon, label);
        return row;
    }


    public Node getView() {
        return view;
    }
}
