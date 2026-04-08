package FXMini;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
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

public class AllViolationsView {

    private VBox view;
    private FlowPane violationsGrid;
    private ViolationDataService dataService;

    public AllViolationsView(ViolationDataService dataService) {
        this.dataService = dataService;

        view = new VBox(20);
        view.setPadding(new Insets(30));
        view.getStyleClass().add("admin-view");

        Label title = new Label("Recent Violations Dashboard");
        title.getStyleClass().add("admin-view-title");

        TextField searchField = new TextField();
        searchField.setPromptText("Search by license plate...");
        searchField.getStyleClass().add("search-field");

        Button deleteAllButton = new Button("Delete All Challans");
        deleteAllButton.getStyleClass().add("danger-button");
        deleteAllButton.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Bulk Deletion");
            confirm.setHeaderText("Delete ALL violation records?");
            confirm.setContentText("This will permanently remove all challans from the system. This action cannot be undone.");
            
            ButtonType deleteButton = new ButtonType("Delete All", ButtonType.OK.getButtonData());
            confirm.getButtonTypes().setAll(deleteButton, ButtonType.CANCEL);
            
            confirm.showAndWait().ifPresent(response -> {
                if (response == deleteButton) {
                    dataService.deleteAllViolations();
                    refresh();
                }
            });
        });

        HBox header = new HBox(15, title, searchField, deleteAllButton);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        header.setAlignment(Pos.CENTER_LEFT);

        violationsGrid = new FlowPane(20, 20);
        violationsGrid.setAlignment(Pos.TOP_LEFT);

        ScrollPane scrollPane = new ScrollPane(violationsGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("scroll-pane");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        refresh();

        view.getChildren().addAll(header, scrollPane);
    }

    public void refresh() {
        violationsGrid.getChildren().clear();
        for (Violation violation : dataService.getViolations()) {
            violationsGrid.getChildren().add(createViolationCard(violation));
        }
    }

    private Node createViolationCard(Violation violation) {
        VBox card = new VBox();
        card.getStyleClass().add("violation-card");

        // Image Section
        VBox imageContainer = new VBox();
        imageContainer.getStyleClass().add("violation-image-container");
        
        String imageUrl = "https://placehold.co/400x150/6366f1/ffffff?text=" + violation.getPlate().replace(" ", "+");
        ImageView imageView = new ImageView();
        try {
            imageView.setImage(new Image(imageUrl, true));
        } catch (Exception e) {
            System.err.println("Failed to load placeholder image: " + e.getMessage());
        }
        imageView.setFitHeight(160);
        imageView.setFitWidth(320);
        imageView.setPreserveRatio(false);
        imageView.getStyleClass().add("violation-image");
        imageContainer.getChildren().add(imageView);

        // Content Section
        VBox content = new VBox();
        content.getStyleClass().add("violation-card-content");
        
        Label plateLabel = new Label(violation.getPlate());
        plateLabel.getStyleClass().add("violation-card-plate");

        VBox details = new VBox(8);
        details.getChildren().addAll(
            createDetailIconRow("M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67z", violation.getTimestamp()),
            createDetailIconRow("M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z", violation.getLocation())
        );

        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setSpacing(10);
        
        Label statusBadge = new Label(violation.getFineStatus());
        statusBadge.getStyleClass().add("status-badge");
        updateStatusBadge(statusBadge, violation.getFineStatus());
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        ComboBox<String> statusToggle = new ComboBox<>();
        statusToggle.getItems().addAll("Paid", "Unpaid");
        statusToggle.setValue(violation.getFineStatus());
        statusToggle.getStyleClass().add("status-toggle");

        statusToggle.setOnAction(e -> {
            String newStatus = statusToggle.getValue();
            dataService.updateViolationStatus(violation.getPlate(), newStatus);
            updateStatusBadge(statusBadge, newStatus);
        });

        // Sync badge if data changes elsewhere
        violation.fineStatusProperty().addListener((obs, oldV, newV) -> {
            statusToggle.setValue(newV);
            updateStatusBadge(statusBadge, newV);
        });

        Button deleteButton = new Button();
        deleteButton.getStyleClass().add("danger-button");
        SVGPath deleteIcon = new SVGPath();
        deleteIcon.setContent("M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z");
        deleteIcon.getStyleClass().add("delete-icon");
        deleteButton.setGraphic(deleteIcon);
        
        deleteButton.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Deletion");
            confirm.setHeaderText("Delete violation record?");
            confirm.setContentText("This action cannot be undone.");
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    dataService.deleteViolation(violation);
                    refresh();
                }
            });
        });

        footer.getChildren().addAll(statusBadge, spacer, statusToggle, deleteButton);
        content.getChildren().addAll(plateLabel, details, footer);

        card.getChildren().addAll(imageContainer, content);
        return card;
    }

    private void updateStatusBadge(Label badge, String status) {
        badge.getStyleClass().removeAll("status-paid", "status-unpaid");
        badge.setText(status);
        if (status.equalsIgnoreCase("Paid")) {
            badge.getStyleClass().add("status-paid");
        } else {
            badge.getStyleClass().add("status-unpaid");
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
