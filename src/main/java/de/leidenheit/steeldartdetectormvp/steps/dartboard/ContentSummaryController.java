package de.leidenheit.steeldartdetectormvp.steps.dartboard;

import de.leidenheit.steeldartdetectormvp.detection.LeidenheitException;
import de.leidenheit.steeldartdetectormvp.detection.MaskSingleton;
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

    private final MaskSingleton maskSingleton = MaskSingleton.getInstance();

    @FXML
    public void initialize() {
        buttonExport.setOnAction(event -> doExport());
        textAreaSummary.setText(String.join("\n", maskSingleton.getDebugList()));
        // FIXME scroll to bottom
    }

    private void doExport() {
        try {
            if (maskSingleton.serialize("dartboard_conf.ldh")) {
                var alert = new Alert(Alert.AlertType.INFORMATION, "Successfully Exported", ButtonType.OK);
                alert.show();
            }
        } catch (LeidenheitException e) {
            throw new RuntimeException(e);
        }
    }
}
