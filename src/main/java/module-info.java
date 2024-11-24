module libre.libroeffis {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;

    opens libre.libroeffis to javafx.fxml;
    exports libre.libroeffis;
}