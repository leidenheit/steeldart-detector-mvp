package de.leidenheit.steeldartdetectormvp.steps.dart;

import de.leidenheit.steeldartdetectormvp.detection.DartSingleton;
import de.leidenheit.steeldartdetectormvp.detection.LeidenheitException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;

public class ContentSummaryController {

    @FXML
    public Button buttonExport;
    @FXML
    public TextArea textAreaSummary;

    private final DartSingleton dartSingleton = DartSingleton.getInstance();

    @FXML
    public void initialize() {
        buttonExport.setOnAction(event -> doExport());
        textAreaSummary.setText(String.join("\n", dartSingleton.getDebugList()));
        // FIXME scroll to bottom
    }

    private void doExport() {
        try {
            if (dartSingleton.serialize("dart_conf.ldh")) {
                var alert = new Alert(Alert.AlertType.INFORMATION, "Successfully Exported", ButtonType.OK);
                alert.show();
            }
        } catch (LeidenheitException e) {
            throw new RuntimeException(e);
        }
    }
}
