package de.leidenheit.steeldartdetectormvp;

import de.leidenheit.steeldartdetectormvp.steps.ContentWithCameraController;
import de.leidenheit.steeldartdetectormvp.steps.Page;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneSetupController {

    @FXML
    Button buttonDiscard;
    @FXML
    Button buttonStartMaskDetection;
    @FXML
    Button buttonStartDartDetection;
    @FXML
    Label labelStepTitle;
    @FXML
    Label labelStepDescription;
    @FXML
    StackPane stackPane;

    private static final String CONTENT_RESOURCE_PATH = "forms/setup";
    private final Page currentPage = Page.REFERENCE_IMAGE;

    @FXML
    public void initialize() {
        applyTitleAndDescription();
        initContent();
    }

    public void startMaskDetection() throws IOException {
        // TODO cleanup
        var context = new FXMLLoader(FxUtil.retrieveResourceURL(CONTENT_RESOURCE_PATH, currentPage.getResource()));
        try {
            context.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (context.getController() instanceof ContentWithCameraController) {
            ((ContentWithCameraController) context.getController()).cleanup();
        }

        final var loader = new FXMLLoader(FxUtil.retrieveResourceURL("forms/dartboard", "main.fxml"));
        final var root = (Parent) loader.load();

        // hack
        final var stage = (Stage) buttonStartMaskDetection.getScene().getWindow();
        final var scene = new Scene(root, 1280, 960);

        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();
        stage.centerOnScreen();
    }

    public void startDartDetection() throws IOException {
        // TODO cleanup
        var context = new FXMLLoader(FxUtil.retrieveResourceURL(CONTENT_RESOURCE_PATH, currentPage.getResource()));
        try {
            context.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (context.getController() instanceof ContentWithCameraController) {
            ((ContentWithCameraController) context.getController()).cleanup();
        }

        final var loader = new FXMLLoader(FxUtil.retrieveResourceURL("forms/dart", "main.fxml"));
        final var root = (Parent) loader.load();

        // hack
        final var stage = (Stage) buttonDiscard.getScene().getWindow();
        final var scene = new Scene(root, 1280, 960);

        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();
        stage.centerOnScreen();
    }

    public void discardAndQuit() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(FxUtil.retrieveResourceURL("forms/home", "main.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            final var stage = (Stage) buttonStartDartDetection.getScene().getWindow();
            stage.setTitle("leidenheit MVP - Setup");
            stage.setScene(scene);
            stage.setWidth(1280);
            stage.setHeight(960);
            stage.show();


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void applyTitleAndDescription() {
        labelStepTitle.setText(currentPage.getTitle());
        labelStepDescription.setText(currentPage.getDescription());
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
