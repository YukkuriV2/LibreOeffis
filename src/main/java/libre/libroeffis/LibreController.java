package libre.libroeffis;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class LibreController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("");
    }
}