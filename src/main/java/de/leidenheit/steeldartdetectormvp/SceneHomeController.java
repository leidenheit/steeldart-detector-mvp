package de.leidenheit.steeldartdetectormvp;

import de.leidenheit.steeldartdetectormvp.detection.DartSingleton;
import de.leidenheit.steeldartdetectormvp.detection.LeidenheitException;
import de.leidenheit.steeldartdetectormvp.detection.MaskSingleton;
import de.leidenheit.steeldartdetectormvp.steps.Page;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class SceneHomeController {

    @FXML
    Button buttonQuit;
    @FXML
    Button buttonStartSetup;
    @FXML
    Button buttonStartEvaluation;
    @FXML
    Label labelStepTitle;
    @FXML
    Label labelStepDescription;
    @FXML
    StackPane stackPane;

    private static final String CONTENT_RESOURCE_PATH = "forms/home";
    private final Page currentPage = Page.HOME;

    private boolean configurationLoaded = false;

    @FXML
    public void initialize() {
        applyTitleAndDescription();
        applyButtonStates();
        initContent();

        File file;
        try {
            file = new File("dartboard_conf.ldh");
            if (file.exists()) {
                configurationLoaded = MaskSingleton.getInstance().deserialize("dartboard_conf.ldh");
            }

            file = new File("dart_conf.ldh");
            if (file.exists()) {
                configurationLoaded = configurationLoaded && DartSingleton.getInstance().deserialize("dart_conf.ldh");
            }
        } catch (LeidenheitException e) {
            throw new RuntimeException(e);
        }
    }

    public void quit() {
        Platform.exit();
    }

    public void startEvaluation() throws IOException {
        final var loader = new FXMLLoader(FxUtil.retrieveResourceURL("forms/eval", "main.fxml"));
        final var root = (Parent) loader.load();

        // hack
        final var stage = (Stage) labelStepTitle.getScene().getWindow();
        final var scene = new Scene(root, 1280, 960);

        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();
        stage.centerOnScreen();
    }

    public void startSetup() throws IOException {
        final var loader = new FXMLLoader(FxUtil.retrieveResourceURL("forms/setup", "main.fxml"));
        final var root = (Parent) loader.load();

        // hack
        final var stage = (Stage) labelStepTitle.getScene().getWindow();
        final var scene = new Scene(root, 1280, 960);

        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();
        stage.centerOnScreen();
    }

    private void applyTitleAndDescription() {
        labelStepTitle.setText(currentPage.getTitle());
        labelStepDescription.setText(currentPage.getDescription());
    }

    private void applyButtonStates() {
//TODO
//        boolean camSelected = CamSingleton.getInstance().getSelectedCameraIndex() == -1;
//        buttonStartSetup.setDisable(!camSelected);
//        buttonStartEvaluation.setDisable(!camSelected || !configurationLoaded);
    }

    private void initContent() {
        try {
            var resource = FxUtil.retrieveResourceURL(CONTENT_RESOURCE_PATH, currentPage.getResource());
            if (resource == null) throw new RuntimeException("Resource not found");
            Parent context = FXMLLoader.load(resource);
            stackPane.getChildren().removeAll();
            stackPane.getChildren().setAll(context);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
