package de.leidenheit.steeldartdetectormvp.steps.eval;

import de.leidenheit.steeldartdetectormvp.FxUtil;
import de.leidenheit.steeldartdetectormvp.detection.*;
import de.leidenheit.steeldartdetectormvp.steps.ContentWithCameraController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Pair;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.BackgroundSubtractorMOG2;
import org.opencv.video.Video;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EvaluationController extends ContentWithCameraController {

    @FXML
    public TextArea textAreaLog;
    @FXML
    public Button buttonReplay;
    @FXML
    public Button buttonResetScore;
    @FXML
    public Label labelFeedInfo;
    @FXML
    public Label labelCameraInfo;
    @FXML
    public Label labelSuccessRatio;
    @FXML
    public Label labelRemainingScore;
    @FXML
    public TextField textfieldFirst;
    @FXML
    public TextField textfieldSecond;
    @FXML
    public TextField textfieldThird;
    @FXML
    public ImageView imageViewEvalMask;
    @FXML
    public ImageView imageViewArrowTipCenter;
    @FXML
    public Slider sliderGaussian;
    @FXML
    public Slider sliderSubtractorThreshold;
    @FXML
    public Slider sliderCloseIterations;
    @FXML
    public Slider sliderErodeIterations;
    @FXML
    public Slider sliderDilateIterations;
    @FXML
    public Slider sliderAspectRatioLow;
    @FXML
    public Slider sliderAspectRatioHigh;
    @FXML
    public Slider sliderUnpluggingThreshold;
    @FXML
    public Slider sliderEstimationThresholdPercentage;
    @FXML
    public Button buttonExport;

    private BackgroundSubtractorMOG2 subtractor;
    private Mat ref = new Mat();
    // TODO introduce type
    private final int[] scoreArray = new int[]{-1, -1, -1};
    // used to determine unplugging of darts
    private boolean skipUntilDiffZero = false;
    private List<Point> arrowTips = new ArrayList<>();
    private int dartThrows = 0;
    private int falsePositives = 0;

    private Image placeHolder = FxUtil.matToImage(
            FxUtil.retrieveResourceAsMat("images/placeholder", "placeholder.jpg"));

    @Override
    protected void onDetectionTaskFailed() {
        log("Eval Step: Detection task failed");
        ref.release();
        arrowTips.clear();
        dartThrows = 0;
        falsePositives = 0;
        enableControls(true);
    }

    @Override
    protected void onDetectionTaskCancelled() {
        log("Eval Step: Detection task cancelled");
        ref.release();
        arrowTips.clear();
        dartThrows = 0;
        falsePositives = 0;
        enableControls(true);
    }

    @Override
    protected void onDetectionTaskSucceeded() {
        log("Eval Step: Detection task succeeded");
        ref.release();
        arrowTips.clear();
        dartThrows = 0;
        falsePositives = 0;
        enableControls(true);
    }

    @Override
    protected void onDetectionTaskRunning() {
        log("Eval Step: Detection task running");
        enableControls(false);
    }

    @Override
    protected void takeSnapshot() {
        // ignored for now
    }

    @Override
    protected void customInit() {
        log("Eval Step: Initialize evaluation");

        // handlers
        buttonResetScore.setOnAction(event -> {
            dartThrows += 3;
            falsePositives += 3;
            resetScore();
        });
        buttonReplay.setOnAction(event -> {
            arrowTips.clear();
            DartSingleton.getInstance().euclideanDistancesBySegment.clear();
            DartSingleton.getInstance().arrowAnglesBySegment.clear();

            dartThrows = 0;
            falsePositives = 0;

            initialize();
        });
        buttonExport.setOnAction(event -> {
            try {
                if (DartSingleton.getInstance().serialize("dart_conf.ldh")) {
                    var alert = new Alert(Alert.AlertType.INFORMATION, "Successfully Exported", ButtonType.OK);
                    alert.show();
                }
            } catch (LeidenheitException e) {
                throw new RuntimeException(e);
            }
        });

        resetScore();

        sliderGaussian.setValue(DartSingleton.getInstance().vidGaussian);
        sliderCloseIterations.setValue(DartSingleton.getInstance().vidCloseIterations);
        sliderErodeIterations.setValue(DartSingleton.getInstance().vidErodeIterations);
        sliderDilateIterations.setValue(DartSingleton.getInstance().vidDilateIterations);
        sliderSubtractorThreshold.setValue(DartSingleton.getInstance().vidSubtractorThreshold);
        sliderAspectRatioLow.setValue(DartSingleton.getInstance().vidAspectRatioLow);
        sliderAspectRatioHigh.setValue(DartSingleton.getInstance().vidAspectRatioHigh);
        sliderUnpluggingThreshold.setValue(DartSingleton.getInstance().vidUnpluggingThreshold);
        sliderEstimationThresholdPercentage.setValue(DartSingleton.getInstance().estimationThresholdPercentage);

        // reset skip flag
        skipUntilDiffZero = false;
    }

    private boolean shouldSkipFrame() {
        boolean isFirstFrame = framePos <= 1;
        return isFirstFrame || ref.empty();
    }

    private void tryApplyReferenceImage(final Mat frame) {
        if ((int) framePos == (int) fps) {
            ref = frame.clone();
            Imgproc.cvtColor(ref, ref, Imgproc.COLOR_BGR2GRAY);
            Imgproc.GaussianBlur(ref, ref, new Size(11, 11), 0);
        }
    }

    private void resizeAndGaussFrame(final Mat frame, final double resizeScaleFactor, final int gauss) {
        Imgproc.resize(frame, frame, new Size(resolutionWidth * resizeScaleFactor, resolutionHeight * resizeScaleFactor));
        Imgproc.GaussianBlur(frame, frame, new Size(gauss, gauss), 0);
    }

    private boolean checkDiffZero(final Mat referenceFrame, final Mat frame) {
        Mat compareFrame = frame.clone();
        Imgproc.cvtColor(compareFrame, compareFrame, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(compareFrame, compareFrame, new Size(11, 11), 0);
        Mat differenceFrame = new Mat();
        Core.absdiff(referenceFrame, compareFrame, differenceFrame);
        Mat thresholdFrame = new Mat();
        Imgproc.threshold(differenceFrame, thresholdFrame, 50, 255, Imgproc.THRESH_BINARY);
        return Core.countNonZero(thresholdFrame) == 0;
    }

    private void handleDeepFrameEvaluation(final int framePosFrom, final int framePosTo) {
        Mat nextFrame = new Mat();
        Mat nextMask = new Mat();
        Mat maskToEval = new Mat();

        int unpluggingThreshold = (int) DartSingleton.getInstance().vidUnpluggingThreshold * 1000;

        // evaluate the next frames in order to find the one that fits the evaluation requirements
        for (int i = framePosFrom; i < framePosTo; i++) {
            if (CamSingleton.getInstance().getVideoCapture().read(nextFrame)) {
                resizeAndGaussFrame(nextFrame, calculatedScaleFactor, DartSingleton.getInstance().vidGaussian);

                Mat filtered = new Mat();
                Imgproc.bilateralFilter(nextFrame, filtered, 5, 50, 50);
                nextFrame = filtered;

                subtractor.apply(nextFrame, nextMask);
                int countNonZeroPixels = Core.countNonZero(nextMask);

                // most likely in motion
                if (Detection.hasSignificantChanges(nextMask, 10_000, unpluggingThreshold)) {
                    log("Eval Step: [frame={0}] Seems to be in motion (diff={1}); ignored", i, countNonZeroPixels);
                    continue;
                }
                // most likely unplugging
                if (Detection.hasSignificantChanges(nextMask, unpluggingThreshold, Integer.MAX_VALUE)) {
                    log("Eval Step: [frame={0}] Seems to be unplugging darts (diff={1})", i, countNonZeroPixels);
                    log("Skipping due to zero difference threshold");
                    // waits in the main loop for the frame to be reached before doing any other evaluation
                    skipUntilDiffZero = true;
                    maskToEval.release();
                    return;
                }

                // evaluation of candidate
                boolean significantChanges = Detection.hasSignificantChanges(nextMask, 0, 10);
                if (!maskToEval.empty() && significantChanges) {
                    // merge found dart contours into a single one
                    MatOfPoint mergedContour = morphMergeContours(maskToEval);
                    double contourArea = Imgproc.contourArea(mergedContour);
                    if (contourArea > DartSingleton.getInstance().vidMaxMergedContourArea) {
                        log("Skipping since merged contour is larger than threshold");
                        maskToEval.release();
                        return;
                    }
                    // calculate bounding rectangle
                    Rect boundingRect = Imgproc.boundingRect(mergedContour);
                    // calculate aspect ratio
                    double aspectRatio = (double) boundingRect.width / boundingRect.height;
                    // check if aspect ratio falls within desired range
                    if (aspectRatio >= sliderAspectRatioLow.getValue() && aspectRatio <= sliderAspectRatioHigh.getValue()) {
                        // evaluation of dart arrow
                        log("Dart Arrow Detected: start evaluation at frame {0} with aspect ratio {1}", framePos, aspectRatio);
                        Pair<Pair<Integer, Mat>, Point[]> scoreAndDartTipBoundingBoxPair = evaluateDartContour(nextFrame, mergedContour);

                        // draw hit segment mask
                        Point center = Detection.findCircleCenter(nextFrame, MaskSingleton.getInstance().innerBullMask);
                        Imgproc.circle(nextFrame, center, 3, new Scalar(100, 255, 255), -1);
                        Point firstPoint = new Point(resolutionWidth, center.y);
                        var segment = Detection.estimateSegmentByPoint(center, scoreAndDartTipBoundingBoxPair.getValue()[0]);
                        double radianMin = Math.toRadians(segment.getLowerBoundaryAngle());
                        double radianMax = Math.toRadians(segment.getUpperBoundaryAngle());
                        Point newPointMin = Detection.rotatePoint(center, firstPoint, radianMin);
                        Point newPointMax = Detection.rotatePoint(center, firstPoint, radianMax);
                        Imgproc.line(nextFrame, center, newPointMin, new Scalar(0, 255, 0), 2);
                        Imgproc.line(nextFrame, center, newPointMax, new Scalar(200, 0, 0), 2);

                        applyDebugDetails(nextFrame, maskToEval, scoreAndDartTipBoundingBoxPair);
                        arrowTips.add(scoreAndDartTipBoundingBoxPair.getValue()[0]);
                        dartThrows++;
                    } else {
                        log("Skipping due to aspect ratio threshold");
                        maskToEval.release();
                        return;
                    }
                    maskToEval.release();
                    return;
                }
            }

            // apply candidate for next iteration
            if (Detection.hasSignificantChanges(nextMask, 1_000, 30_000)) {
                maskToEval.release();
                maskToEval = nextMask.clone();
            }
        }
    }

    private Mat extractHitMask(final Mat frame, final Pair<Pair<Integer, Mat>, Point[]> scoreTipPair) {
        Mat res = new Mat();
        Core.bitwise_and(frame, frame, res, scoreTipPair.getKey().getValue());
        return res;
    }

    private void applyDebugDetails(final Mat frame, final Mat maskToEval, final Pair<Pair<Integer, Mat>, Point[]> scoreAndDartTipBoundingBoxPair) {
        // draw bounding rectangle
        Mat image = frame.clone();
        try {
            image = extractHitMask(image, scoreAndDartTipBoundingBoxPair);
            if (!image.empty()) {

                Imgproc.rectangle(
                        image,
                        scoreAndDartTipBoundingBoxPair.getValue()[1], // tl
                        scoreAndDartTipBoundingBoxPair.getValue()[2], // br
                        new Scalar(125, 125, 20), 2);
                Imgproc.circle(image, scoreAndDartTipBoundingBoxPair.getValue()[3], 4, new Scalar(0, 0, 255), -1);
                Imgproc.circle(image, scoreAndDartTipBoundingBoxPair.getValue()[0], 4, new Scalar(255, 0, 0), -1);

                Mat finalImage = image.clone();
                image.release();
                Platform.runLater(() -> {
                    imageView.setImage(FxUtil.matToImage(finalImage));
                    imageViewEvalMask.setImage(FxUtil.matToImage(maskToEval));
                    imageViewArrowTipCenter.setImage(FxUtil.matToImage(finalImage));
                    finalImage.release();
                });
                Thread.sleep(50);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private MatOfPoint morphMergeContours(final Mat maskToEval) {
        Imgproc.morphologyEx(maskToEval, maskToEval, Imgproc.MORPH_CLOSE,
                Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3)),
                new Point(-1, -1),
                DartSingleton.getInstance().vidCloseIterations);
        Imgproc.morphologyEx(maskToEval, maskToEval, Imgproc.MORPH_DILATE,
                Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2)),
                new Point(-1, -1),
                DartSingleton.getInstance().vidDilateIterations);
        Imgproc.morphologyEx(maskToEval, maskToEval, Imgproc.MORPH_ERODE,
                Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2)),
                new Point(-1, -1),
                DartSingleton.getInstance().vidErodeIterations);

        // TODO consider configurable contour area threshold
        return Detection.extractMergedContour(maskToEval, 50);
    }

    private Pair<Pair<Integer, Mat>, Point[]> evaluateDartContour(final Mat frame, final MatOfPoint dartContour) {
        MatOfPoint convexHull = Detection.findConvexHull(dartContour);
        Pair<Double, Point[]> res = Detection.findArrowTip(frame, convexHull);
        Point[] tipAndBB = res.getValue();

        // TODO introduce type
        Pair<Integer, Mat> scoreMaskPair = new Pair<>(0, new Mat());
        try {
            if (!Detection.pointIntersectsMask(tipAndBB[0], MaskSingleton.getInstance().dartboardMask)) {
                log("Eval Step: Ignored since not in dartboard mask");
            } else {
                Point center = Detection.findCircleCenter(frame, MaskSingleton.getInstance().innerBullMask);
                double angle = Detection.calculateAngle(center, tipAndBB[0]);
                log("Eval Step: Detected angle of tip of dart: {0} @ frame {1}", angle, framePos);
                scoreMaskPair = Detection.evaluatePoint(
                        center,
                        tipAndBB[0],
                        MaskSingleton.getInstance().dartboardMask,
                        MaskSingleton.getInstance().innerBullMask,
                        MaskSingleton.getInstance().outerBullMask,
                        MaskSingleton.getInstance().tripleMask,
                        MaskSingleton.getInstance().doubleMask,
                        MaskSingleton.getInstance().singleMask);
            }
            if (scoreArray[0] == -1) {
                scoreArray[0] = scoreMaskPair.getKey();
            } else if (scoreArray[1] == -1) {
                scoreArray[1] = scoreMaskPair.getKey();
            } else {
                scoreArray[2] = scoreMaskPair.getKey();
            }
            return new Pair<>(scoreMaskPair, tipAndBB);
        } catch (LeidenheitException e) {
            throw new RuntimeException(e);
        } finally {
            convexHull.release();
        }
    }

    @Override
    protected Mat customHandleFrame(final Mat frame) {
        // read values from controls
        final var gaussian = (int) sliderGaussian.getValue();
        final var closeIterations = (int) sliderCloseIterations.getValue();
        final var erodeIterations = (int) sliderErodeIterations.getValue();
        final var dilateIterations = (int) sliderDilateIterations.getValue();
        final var subtractorThreshold = (int) sliderSubtractorThreshold.getValue();
        final var aspectRatioLow = (int) sliderAspectRatioLow.getValue();
        final var aspectRatioHigh = (int) sliderAspectRatioHigh.getValue();
        final var unpluggingThreshold = (int) sliderUnpluggingThreshold.getValue();
        final var estimationThresholdPercentage = (int) sliderEstimationThresholdPercentage.getValue();
        DartSingleton.getInstance().vidGaussian = gaussian;
        DartSingleton.getInstance().vidErodeIterations = erodeIterations;
        DartSingleton.getInstance().vidCloseIterations = closeIterations;
        DartSingleton.getInstance().vidDilateIterations = dilateIterations;
        DartSingleton.getInstance().vidSubtractorThreshold = subtractorThreshold;
        DartSingleton.getInstance().vidAspectRatioLow = aspectRatioLow;
        DartSingleton.getInstance().vidAspectRatioHigh = aspectRatioHigh;
        DartSingleton.getInstance().vidUnpluggingThreshold = unpluggingThreshold;
        DartSingleton.getInstance().estimationThresholdPercentage = estimationThresholdPercentage;

        tryApplyReferenceImage(frame);
        if (shouldSkipFrame()) return frame;

        // detection
        Mat mask = new Mat();
        try {
            subtractor.apply(frame, mask);
            if (skipUntilDiffZero && Core.countNonZero(mask) > 0) return frame;
            if (skipUntilDiffZero && checkDiffZero(ref, frame)) {
                skipUntilDiffZero = false;
                log("Skipping due to zero difference threshold done");
                resetScore();
            }
            boolean significantChanges = Detection.hasSignificantChanges(mask, 1_000, 30_000);
            if (significantChanges) {
                int framePosToStart = (int) framePos;
                int framePosToEnd = frameCount == -1 ? framePosToStart + (int) (fps) : Math.min(framePosToStart + (int) (fps), (int) frameCount);
                // nested loop deep evaluation
                handleDeepFrameEvaluation(framePosToStart, framePosToEnd);
            }
            drawArrowTips(frame);

            // all darts thrown
            if (Arrays.stream(scoreArray).filter(v -> v == -1).findAny().isEmpty()) {
                Mat shape = new Mat(frame.size(), frame.type());
                Imgproc.rectangle(shape, new Rect(0, 0, frame.width(), frame.height()), new Scalar(200, 200, 200), -1,Imgproc.LINE_AA);
                Core.addWeighted(frame, 0.5d, shape, 1d - 0.5d, 0, frame);
                Size textSize = Imgproc.getTextSize("Please Remove Darts",  Imgproc.FONT_HERSHEY_SIMPLEX, 1.5d, 5, null);
                Imgproc.putText(frame, "Please Remove Darts", new Point((frame.width() / 2) - (textSize.width / 2), (frame.height() / 2) + (textSize.height / 2)), Imgproc.FONT_HERSHEY_SIMPLEX, 1.5d, new Scalar(255, 0, 0), 5);
            }
        } finally {
            mask.release();
            updateUI();
        }
        return frame.clone();
    }

    private void drawArrowTips(final Mat frame) {
        for (final Point hit : this.arrowTips) {
            Imgproc.circle(frame, hit, 3, new Scalar(100, 255, 255), -1, Imgproc.LINE_AA);
        }
    }

    private void updateUI() {
        Platform.runLater(() -> {
            updateInfoInUI();
            updateLogInUI();
            updateScoreInUI();
        });
    }

    private void updateInfoInUI() {
        labelFeedInfo.setText(MessageFormat.format("Frame: {0}/{1} @ {2} FPS", framePos, frameCount, fps));
        labelCameraInfo.setText(MessageFormat.format("Camera: {0} @ Index [{1}] with {2}x{3}",
                CamSingleton.getInstance().getCameraName(),
                CamSingleton.getInstance().getSelectedCameraIndex(),
                resolutionWidth * calculatedScaleFactor,
                resolutionHeight * calculatedScaleFactor));

        double successRatio = 100d - calculateFailRatio(dartThrows, falsePositives);
        labelSuccessRatio.setText(MessageFormat.format("Success Ratio: {0}% ({1}:{2})", successRatio, falsePositives, dartThrows));
    }

    private double calculateFailRatio(final int dartThrows, final int falsePositives) {
        if (dartThrows <= 0 || falsePositives <= 0) return 0;
        return (double) (falsePositives * 100) / dartThrows;
    }

    @Override
    protected VideoCapture cameraInit(VideoCapture videoCapture) {
        videoCapture.open(CamSingleton.getInstance().getSelectedCameraIndex(), Videoio.CAP_DSHOW);
        if (!videoCapture.isOpened()) {
            videoCapture.open(FxUtil.retrieveResourceAsTempFile("static/video", "gameplay.mp4").getAbsolutePath());
            if (!videoCapture.isOpened()) {
                log("Warning: video capture seems not to be ready...");
            }
        }
        subtractor = Video.createBackgroundSubtractorMOG2();
        subtractor.setVarThreshold(DartSingleton.getInstance().vidSubtractorThreshold);
        subtractor.setDetectShadows(false);
        subtractor.setHistory(frameCount == -1d ? (int) fps : (frameCount == 6 ? 1 : 2));
        return videoCapture;
    }

    private void enableControls(boolean enabled) {
        // ignored
    }

    private void updateLogInUI() {
        textAreaLog.setText(String.join("\n", DartSingleton.getInstance().getDebugList()));
        textAreaLog.setScrollTop(Double.MAX_VALUE);
    }

    private void updateScoreInUI() {
        textfieldFirst.setText(scoreArray[0] >= 0 ? String.valueOf(scoreArray[0]) : "-");
        textfieldSecond.setText(scoreArray[1] >= 0 ? String.valueOf(scoreArray[1]) : "-");
        textfieldThird.setText(scoreArray[2] >= 0 ? String.valueOf(scoreArray[2]) : "-");

        int score = 0;
        for (int i = 0; i < scoreArray.length; i++) {
            score += Math.max(scoreArray[i], 0);
        }
        labelRemainingScore.setText(String.valueOf(score));
    }

    private void resetScore() {
        log("Resetting score");
        Arrays.fill(scoreArray, -1);
        this.arrowTips.clear();

        Platform.runLater(() -> {
            imageViewArrowTipCenter.setImage(placeHolder);
            imageViewEvalMask.setImage(placeHolder);

            updateUI();
        });
    }

    public void markAsFalsePositive() {
        falsePositives++;
        log("Marked as false positive");
    }
}
