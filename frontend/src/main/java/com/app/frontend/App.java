package com.app.frontend;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;

public class App extends Application {

    private final ApiClient apiClient = new ApiClient("http://localhost:8081");
    private final ObservableList<ItemRow> itemList = FXCollections.observableArrayList();
    private Label statusLabel;

    @Override
    public void start(Stage stage) {
        // ---- Header ----
        Label title = new Label("📦 Item Manager");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#2c3e50"));

        statusLabel = new Label("Connecting...");
        statusLabel.setFont(Font.font("Segoe UI", 12));
        statusLabel.setTextFill(Color.web("#7f8c8d"));

        VBox header = new VBox(4, title, statusLabel);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(16));

        // ---- Input Form ----
        TextField nameField = new TextField();
        nameField.setPromptText("Item name");
        nameField.setPrefWidth(200);

        TextField descField = new TextField();
        descField.setPromptText("Description");
        descField.setPrefWidth(300);

        Button addBtn = new Button("➕ Add Item");
        addBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16;");

        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16;");

        HBox form = new HBox(10, nameField, descField, addBtn, refreshBtn);
        form.setAlignment(Pos.CENTER_LEFT);
        form.setPadding(new Insets(0, 16, 12, 16));

        // ---- Table ----
        TableView<ItemRow> table = new TableView<>(itemList);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ItemRow, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);

        TableColumn<ItemRow, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<ItemRow, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<ItemRow, String> dateCol = new TableColumn<>("Created At");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        TableColumn<ItemRow, Void> actionCol = new TableColumn<>("Action");
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button deleteBtn = new Button("🗑 Delete");
            {
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 11;");
                deleteBtn.setOnAction(e -> {
                    ItemRow item = getTableView().getItems().get(getIndex());
                    deleteItem(item.getId());
                });
            }
            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : deleteBtn);
            }
        });

        table.getColumns().addAll(idCol, nameCol, descCol, dateCol, actionCol);

        // ---- Layout ----
        VBox root = new VBox(header, new Separator(), form, table);
        root.setStyle("-fx-background-color: #ecf0f1;");
        VBox.setVgrow(table, Priority.ALWAYS);

        Scene scene = new Scene(root, 800, 520);
        stage.setTitle("JavaFX + Spring Boot + MySQL");
        stage.setScene(scene);
        stage.show();

        // ---- Event Handlers ----
        addBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String desc = descField.getText().trim();
            if (name.isEmpty()) {
                setStatus("⚠️ Name is required", true);
                return;
            }
            createItem(name, desc);
            nameField.clear();
            descField.clear();
        });

        refreshBtn.setOnAction(e -> loadItems());

        // Initial load
        checkHealthAndLoad();
    }

    private void checkHealthAndLoad() {
        new Thread(() -> {
            try {
                String health = apiClient.checkHealth();
                Platform.runLater(() -> setStatus("✅ Backend: " + health, false));
                loadItems();
            } catch (Exception e) {
                Platform.runLater(() -> setStatus("❌ Cannot connect to backend: " + e.getMessage(), true));
            }
        }).start();
    }

    private void loadItems() {
        new Thread(() -> {
            try {
                List<Map<String, Object>> items = apiClient.getAllItems();
                Platform.runLater(() -> {
                    itemList.clear();
                    for (Map<String, Object> m : items) {
                        long id = ((Number) m.get("id")).longValue();
                        String name = (String) m.getOrDefault("name", "");
                        String desc = (String) m.getOrDefault("description", "");
                        String created = m.getOrDefault("createdAt", "").toString();
                        itemList.add(new ItemRow(id, name, desc, created));
                    }
                    setStatus("Loaded " + items.size() + " items", false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> setStatus("❌ Load failed: " + e.getMessage(), true));
            }
        }).start();
    }

    private void createItem(String name, String description) {
        new Thread(() -> {
            try {
                apiClient.createItem(name, description);
                Platform.runLater(() -> setStatus("✅ Item created", false));
                loadItems();
            } catch (Exception e) {
                Platform.runLater(() -> setStatus("❌ Create failed: " + e.getMessage(), true));
            }
        }).start();
    }

    private void deleteItem(long id) {
        new Thread(() -> {
            try {
                apiClient.deleteItem(id);
                Platform.runLater(() -> setStatus("🗑 Item deleted", false));
                loadItems();
            } catch (Exception e) {
                Platform.runLater(() -> setStatus("❌ Delete failed: " + e.getMessage(), true));
            }
        }).start();
    }

    private void setStatus(String text, boolean isError) {
        statusLabel.setText(text);
        statusLabel.setTextFill(isError ? Color.web("#e74c3c") : Color.web("#27ae60"));
    }

    // ---- Table Row Model ----
    public static class ItemRow {
        private final long id;
        private final String name;
        private final String description;
        private final String createdAt;

        public ItemRow(long id, String name, String description, String createdAt) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.createdAt = createdAt;
        }

        public long getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getCreatedAt() { return createdAt; }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
