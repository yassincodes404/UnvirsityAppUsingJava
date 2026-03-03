module com.app.frontend {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.google.gson;

    opens com.app.frontend to javafx.fxml, com.google.gson;
    exports com.app.frontend;
}
