package de.leidenheit.steeldartdetectormvp.steps.dart;

import de.leidenheit.steeldartdetectormvp.FxUtil;
import de.leidenheit.steeldartdetectormvp.detection.*;
import de.leidenheit.steeldartdetectormvp.steps.ContentWithCameraController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.util.Pair;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.BackgroundSubtractorMOG2;
import org.opencv.video.Video;
import org.opencv.videoio.VideoCapture;

import java.nio.file.Path;
import java.util.List;

public class DartTipController extends ContentWithCameraController {

    @FXML
    public ImageView imageView;
    @FXML
    public Slider sliderCloseIterations;
    @FXML
    public Slider sliderMinContourArea;
    @FXML
    public Button buttonResetParams;
    @FXML
    public Slider sliderDilateIterations;
    @FXML
    public Slider sliderErodeIterations;
    @FXML
    public Slider sliderGaussian;
    @FXML
    public Slider sliderSubtractorThreshold;
    @FXML
    public Slider sliderMaxMergedContourArea;
    @FXML
    public Button buttonReplay;

    private BackgroundSubtractorMOG2 subtractor;
    private Mat mask = new Mat();

    @Override
    protected void onDetectionTaskFailed() {
        log("Setup Step: Extract arrow tips -> task failed");
        enableControls(true);
    }

    @Override
    protected void onDetectionTaskCancelled() {
        log("Setup Step: Extract arrow tips -> task cancelled");
        enableControls(true);
    }

    @Override
    protected void onDetectionTaskSucceeded() {
        log("Setup Step: Extract arrow tips -> task succeeded");
        enableControls(true);
    }

    @Override
    protected void onDetectionTaskRunning() {
        log("Setup Step: Extract arrow tips -> task running");
        enableControls(false);
    }

    @Override
    protected void takeSnapshot() {
        // ignored for now
    }

    @Override
    protected void customInit() {
        log("Setup Step: Extract arrow tips");
//        initPlaceholders();

        // handlers
        buttonResetParams.setOnAction(event -> resetParams());
        buttonReplay.setOnAction(event -> doDetect());

        resetParams();
    }

    @Override
    protected Mat customHandleFrame(Mat frame) {
        subtractor.apply(frame, mask);

        // morph DO NOT TOUCH
        Imgproc.morphologyEx(mask, mask, Imgproc.MORPH_CLOSE,
                Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3)),
                new Point(-1, -1),
                DartSingleton.getInstance().vidCloseIterations);
        Imgproc.morphologyEx(mask, mask, Imgproc.MORPH_DILATE,
                Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5)),
                new Point(-1, -1),
                DartSingleton.getInstance().vidDilateIterations);
        Imgproc.morphologyEx(mask, mask, Imgproc.MORPH_ERODE,
                Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5)),
                new Point(-1, -1),
                DartSingleton.getInstance().vidErodeIterations);

        MatOfPoint mergedContour = Detection.extractMergedContour(mask, 50); // image
        if (Imgproc.contourArea(mergedContour) > DartSingleton.getInstance().vidMaxMergedContourArea) { // video
            return frame.clone();
        }

        MatOfPoint convexHull = Detection.findConvexHull(mergedContour);
        Imgproc.drawContours(frame, List.of(mergedContour), -1, new Scalar(240, 16, 255), 1);

        Pair<Double, Point[]> res = Detection.findArrowTip(frame, convexHull);
        Point[] tipAndBB = res.getValue();
        Imgproc.rectangle(frame, tipAndBB[1], tipAndBB[2], new Scalar(0, 255, 0), 1);
        Imgproc.drawMarker(frame, tipAndBB[0], new Scalar(100, 100, 254), Imgproc.MARKER_STAR, 50, 1);

        // bounding box
        int offset = 0;
        Point biggerTl = new Point(tipAndBB[1].x - offset, tipAndBB[1].y - offset);
        Point biggerBr = new Point(tipAndBB[2].x + offset * 2, tipAndBB[2].y + offset * 2);
        mask = new Mat(mask, new Rect(biggerTl, biggerBr));
        frame = new Mat(frame, new Rect(biggerTl, biggerBr));

        imageView.setImage(FxUtil.matToImage(frame));
        try {
            Thread.sleep(2_500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return frame.clone();
    }

    @Override
    protected VideoCapture cameraInit(VideoCapture videoCapture) {
        videoCapture.open(Path.of("dart_setup_temp/tmp_img_%d.jpg").toAbsolutePath().toString());
        if (!videoCapture.isOpened()) {
            log("Warning: video capture seems not to be ready...");
        }

        subtractor = Video.createBackgroundSubtractorMOG2();
        subtractor.setVarThreshold(DartSingleton.getInstance().vidSubtractorThreshold);
        subtractor.setDetectShadows(false);
        subtractor.setHistory(2);
        return videoCapture;
    }

    protected void resetParams() {
        sliderGaussian.setValue(DartSingleton.getInstance().vidGaussian);
        sliderCloseIterations.setValue(DartSingleton.getInstance().vidCloseIterations);
        sliderErodeIterations.setValue(DartSingleton.getInstance().vidErodeIterations);
        sliderDilateIterations.setValue(DartSingleton.getInstance().vidDilateIterations);
        sliderSubtractorThreshold.setValue(DartSingleton.getInstance().vidSubtractorThreshold);
        sliderMinContourArea.setValue(DartSingleton.getInstance().vidMinContourArea);
        sliderMaxMergedContourArea.setValue(DartSingleton.getInstance().vidMaxMergedContourArea);
    }

    protected void doDetect() {
        // read values from controls
        final var gaussian = (int) sliderGaussian.getValue();
        final var closeIterations = (int) sliderCloseIterations.getValue();
        final var erodeIterations = (int) sliderErodeIterations.getValue();
        final var dilateIterations = (int) sliderDilateIterations.getValue();
        final var subtractorThreshold = (int) sliderSubtractorThreshold.getValue();
        final var minContourArea = (int) sliderMinContourArea.getValue();
        final var maxMergedContourArea = (int) sliderMaxMergedContourArea.getValue();

        log("gaussian={0}", gaussian);
        log("closeIterations={0}", closeIterations);
        log("erodeIterations={0}", erodeIterations);
        log("dilateIterations={0}", dilateIterations);
        log("subtractorThreshold={0}", subtractorThreshold);
        log("minContourArea={0}", minContourArea);
        log("maxMergedContourArea={0}", maxMergedContourArea);

        DartSingleton.getInstance().vidGaussian = gaussian;
        DartSingleton.getInstance().vidErodeIterations = erodeIterations;
        DartSingleton.getInstance().vidCloseIterations = closeIterations;
        DartSingleton.getInstance().vidDilateIterations = dilateIterations;
        DartSingleton.getInstance().vidSubtractorThreshold = subtractorThreshold;
        DartSingleton.getInstance().vidMinContourArea = minContourArea;
        DartSingleton.getInstance().vidMaxMergedContourArea = maxMergedContourArea;

        // call detection
        initialize();
    }

    private void enableControls(boolean enabled) {
        sliderSubtractorThreshold.setDisable(!enabled);
        sliderGaussian.setDisable(!enabled);
        sliderDilateIterations.setDisable(!enabled);
        sliderErodeIterations.setDisable(!enabled);
        sliderCloseIterations.setDisable(!enabled);
        sliderMinContourArea.setDisable(!enabled);
        sliderMaxMergedContourArea.setDisable(!enabled);
        buttonReplay.setDisable(!enabled);
        buttonResetParams.setDisable(!enabled);
    }
}
