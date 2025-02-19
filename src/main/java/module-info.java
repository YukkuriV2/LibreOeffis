module fhtw.libreoeffis {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.json;
    requires com.fasterxml.jackson.databind;


    opens fhtw.libreoeffis to javafx.fxml;
    exports fhtw.libreoeffis;
}