module org.example.edom {

    // JavaFX
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.web;

    // Java
    requires java.sql;
    requires java.desktop;

    // Third-party
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;

    // JavaFX mora moći instancirati Application
    exports org.example.edom;
    opens org.example.edom to javafx.graphics;

    // FXML controllers
    opens controllers to javafx.fxml;

    // modeli (TableView, PropertyValueFactory)
    opens model to javafx.base;

    // ako se ikad koriste u FXML
    opens dao to javafx.fxml;
    opens service to javafx.fxml;
}
