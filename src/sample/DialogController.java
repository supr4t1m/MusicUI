package sample;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class DialogController {
    @FXML
    private TextField nameField;

    public void fillName(String name) {
        nameField.setText(name);
    }

    public String retreiveName() {
        return nameField.getText();
    }
}
