package de.leidenheit.steeldartdetectormvp.steps.home;

import de.leidenheit.steeldartdetectormvp.detection.CamSingleton;
import de.leidenheit.steeldartdetectormvp.steps.ContentWithCameraController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

public class CameraSelectionController extends ContentWithCameraController {

    @FXML
    public ComboBox<String> comboBoxCam;
    @FXML
    public Label labelFeed;

    @Override
    protected void onDetectionTaskFailed() {
        log("Home:  Camera Selection -> Task failed");
    }

    @Override
    protected void onDetectionTaskCancelled() {
        log("Home:  Camera Selection -> Task cancelled");
    }

    @Override
    protected void onDetectionTaskSucceeded() {
        log("Home:  Camera Selection -> Task succeeded");
    }

    @Override
    protected void onDetectionTaskRunning() {
        log("Home:  Camera Selection -> Task running");
    }

    @Override
    protected void takeSnapshot() {
        // ignored
    }

    @Override
    protected void customInit() {
        log("Home:  Camera Selection");
    }

    @Override
    protected Mat customHandleFrame(final Mat frame) {
        Platform.runLater(() ->
                labelFeed.setText("Res: %.0fx%.0f\nFrame: %.0f/%.0f @ %.0f FPS"
                        .formatted(resolutionWidth, resolutionHeight, framePos, frameCount, fps)));
        return frame.clone();
    }

    @Override
    protected VideoCapture cameraInit(VideoCapture videoCapture) {
        // cameras
        int camIndex = 0;
        while (true) {
            videoCapture.open(camIndex, Videoio.CAP_DSHOW);
            if (videoCapture.isOpened()) {
                camIndex++;
                String cameraName = Videoio.getBackendName(camIndex);
                comboBoxCam.getItems().add("Camera %d: %s".formatted(camIndex, cameraName));
            } else {
                break;
            }
        }
        comboBoxCam.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            log("Camera selection changed to {0}", newValue);
            int selIndex = comboBoxCam.getSelectionModel().getSelectedIndex() - 1;
            CamSingleton.getInstance().setSelectedCameraIndex(selIndex);
            CamSingleton.getInstance().setCameraName(Videoio.getBackendName(selIndex));

            initialize();
        });

        videoCapture.open(CamSingleton.getInstance().getSelectedCameraIndex(), Videoio.CAP_DSHOW);
        if (!videoCapture.isOpened()) {
            log("Warning: video capture seems not to be ready...");
        }
        return videoCapture;
    }
}
