package de.leidenheit.steeldartdetectormvp;

import de.leidenheit.steeldartdetectormvp.steps.ContentController;
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

public class SceneDartboardSetupController {

    @FXML
    Button buttonDiscard;
    @FXML
    Button buttonPrevious;
    @FXML
    Button buttonNext;
    @FXML
    Label labelStepTitle;
    @FXML
    Label labelStepDescription;
    @FXML
    StackPane stackPane;

    private static final String CONTENT_RESOURCE_PATH = "forms/dartboard";
    private Page currentPage = Page.MASK_GREEN;

    @FXML
    public void initialize() {
        applyTitleAndDescription();
        applyButtonStates();
        initContent();
    }

    public void next() {
        if (currentPage.hasNext()) {
            currentPage = currentPage.getNext();
            applyTitleAndDescription();
            applyButtonStates();
            initContent();
        }
    }

    public void previous() {
        if (currentPage.hasPrevious()) {
            currentPage = currentPage.getPrevious();
            applyTitleAndDescription();
            applyButtonStates();
            initContent();
        }
    }

    public void discardAndQuit() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(FxUtil.retrieveResourceURL("forms/setup", "main.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            final var stage = (Stage) this.buttonDiscard.getScene().getWindow();
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

    private void applyButtonStates() {
        buttonNext.setDisable(!currentPage.hasNext());
        buttonPrevious.setDisable(!currentPage.hasPrevious());
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
