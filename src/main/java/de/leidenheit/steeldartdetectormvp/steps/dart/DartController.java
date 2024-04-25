package de.leidenheit.steeldartdetectormvp.steps.dart;

import de.leidenheit.steeldartdetectormvp.FxUtil;

import de.leidenheit.steeldartdetectormvp.detection.CamSingleton;
import de.leidenheit.steeldartdetectormvp.detection.DartSingleton;
import de.leidenheit.steeldartdetectormvp.steps.ContentWithCameraController;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;

public class DartController extends ContentWithCameraController {

    @Override
    protected void takeSnapshot() {
        File dartSetupTempDir = Path.of("dart_setup_temp").toFile();
        boolean dirExists = dartSetupTempDir.exists();
        if (!dirExists) {
            dirExists = dartSetupTempDir.mkdir();
        }
        if (dirExists) {
            int fileCount = Objects.requireNonNull(dartSetupTempDir.listFiles()).length;
            String filepath = "%s/tmp_img_%d.jpg".formatted(dartSetupTempDir.getPath(), fileCount);
            Imgcodecs.imwrite(filepath, frame);
            File targetImage = new File(filepath);
            if (!targetImage.exists()) {
                throw new RuntimeException("Snapshot was not successfully saved to file system: %s".formatted(targetImage.getPath()));
            }

            log("Snapshot of {0}", filepath);
        } else {
            throw new RuntimeException("Output directory was not successfully created: %s".formatted(dartSetupTempDir.getPath()));
        }
    }

    @Override
    protected void customInit() {
        log("Setup Step: Darts");
    }

    @Override
    protected Mat customHandleFrame(final Mat frame) {
        return frame.clone();
    }

    @Override
    protected VideoCapture cameraInit(VideoCapture videoCapture) {
        videoCapture.open(CamSingleton.getInstance().getSelectedCameraIndex(), Videoio.CAP_DSHOW);
        if (!videoCapture.isOpened()) {
            videoCapture.open(
                    FxUtil.retrieveResourceAsTempFile("static/video", "setup.mp4").getAbsolutePath());
            if (!videoCapture.isOpened()) {
                log("Warning: video capture seems not to be ready...");
            }
        }
        return videoCapture;
    }

    @Override
    protected void onDetectionTaskFailed() {
        log("Setup Step: Darts -> task failed");
    }

    @Override
    protected void onDetectionTaskCancelled() {

        log("Setup Step: Darts -> task cancelled");
    }

    @Override
    protected void onDetectionTaskSucceeded() {

        log("Setup Step: Darts -> task succeeded");
    }

    @Override
    protected void onDetectionTaskRunning() {
        log("Setup Step: Darts -> task running");
    }
}

