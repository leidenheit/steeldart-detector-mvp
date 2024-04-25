package de.leidenheit.steeldartdetectormvp.steps;

import de.leidenheit.steeldartdetectormvp.FxUtil;
import de.leidenheit.steeldartdetectormvp.detection.CamSingleton;
import de.leidenheit.steeldartdetectormvp.detection.MaskSingleton;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ReferenceController extends ContentWithCameraController {

    @FXML
    public Button buttonReplay;
    @FXML
    public Button buttonResetParams;
    @FXML
    public Slider sliderMinApproxCurveRows;
    @FXML
    public Slider sliderMinContourArea;
    @FXML
    public Slider sliderCannyThres2;
    @FXML
    public Slider sliderCannyThres1;
    @FXML
    public Slider sliderBilateralSigSpace;
    @FXML
    public Slider sliderBilateralSigColor;
    @FXML
    public Slider sliderBilateralDiameter;
    @FXML
    public CheckBox checkboxCircleExtraction;

    @Override
    protected void customInit() {
        log("Setup Step: Reference image");

        buttonReplay.setOnAction(event -> initialize());
        checkboxCircleExtraction.setOnAction(actionEvent -> {
            sliderBilateralDiameter.setDisable(!((CheckBox) actionEvent.getSource()).isSelected());
            sliderBilateralSigColor.setDisable(!((CheckBox) actionEvent.getSource()).isSelected());
            sliderBilateralSigSpace.setDisable(!((CheckBox) actionEvent.getSource()).isSelected());
            sliderCannyThres1.setDisable(!((CheckBox) actionEvent.getSource()).isSelected());
            sliderCannyThres2.setDisable(!((CheckBox) actionEvent.getSource()).isSelected());
            sliderMinContourArea.setDisable(!((CheckBox) actionEvent.getSource()).isSelected());
            sliderMinApproxCurveRows.setDisable(!((CheckBox) actionEvent.getSource()).isSelected());
        });
        sliderBilateralDiameter.setDisable(!checkboxCircleExtraction.isSelected());
        sliderBilateralSigColor.setDisable(!checkboxCircleExtraction.isSelected());
        sliderBilateralSigSpace.setDisable(!checkboxCircleExtraction.isSelected());
        sliderCannyThres1.setDisable(!checkboxCircleExtraction.isSelected());
        sliderCannyThres2.setDisable(!checkboxCircleExtraction.isSelected());
        sliderMinContourArea.setDisable(!checkboxCircleExtraction.isSelected());
        sliderMinApproxCurveRows.setDisable(!checkboxCircleExtraction.isSelected());
    }

    /**
     * @param bgrImage
     * @param bilateralDiameter   5
     * @param bilateralSigmaColor 175
     * @param bilateralSigmaSpace 175
     * @param cannyThres1         300
     * @param cannyThres2         400
     * @param minContourArea      50
     * @param minApproxCurveRows  8
     * @return
     */
    private Mat extractCircle(
            final Mat bgrImage,
            final int bilateralDiameter,
            final double bilateralSigmaColor,
            final double bilateralSigmaSpace,
            final double cannyThres1,
            final double cannyThres2,
            final double minContourArea,
            final double minApproxCurveRows) {
        Mat raw = bgrImage.clone();
        Mat bilateral = new Mat();
        Mat edges = new Mat();
        Mat hierarchy = new Mat();
        MatOfPoint mergedContour = new MatOfPoint();
        MatOfInt hull = new MatOfInt();
        MatOfPoint hullPoints = new MatOfPoint();
        try {
            // bilateral
            Imgproc.bilateralFilter(raw, bilateral, bilateralDiameter, bilateralSigmaColor, bilateralSigmaSpace);
            // canny
            Imgproc.Canny(bilateral, edges, cannyThres1, cannyThres2);
            // contour
            List<MatOfPoint> contours = new ArrayList<>();
            Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));
            // approxpoly
            List<MatOfPoint> candidateContours = new ArrayList<>();
            for (MatOfPoint contour : contours) {
                MatOfPoint2f approxCurve = new MatOfPoint2f();
                MatOfPoint2f contour2f = new MatOfPoint2f(contour.toArray());
                double approxDistance = Imgproc.arcLength(contour2f, true) * 0.01;
                Imgproc.approxPolyDP(contour2f, approxCurve, approxDistance, true);
                double contourArea = Imgproc.contourArea(contour);
                if (contourArea > minContourArea && approxCurve.rows() > minApproxCurveRows) {
                    candidateContours.add(contour);
                }
                contour2f.release();
                approxCurve.release();
            }
            // Merge contours
            for (MatOfPoint c : candidateContours) {
                mergedContour.push_back(c);
            }
            // Convex Hull
            Imgproc.convexHull(mergedContour, hull);
            List<Point> points = new ArrayList<>();
            for (int i = 0; i < hull.rows(); i++) {
                int index = (int) hull.get(i, 0)[0];
                points.add(mergedContour.toList().get(index));
            }
            hullPoints.fromList(points);
            List<MatOfPoint> hullContours = new ArrayList<>();
            hullContours.add(hullPoints);
            // extract a mask from dart board circle
            Mat mask = new Mat(raw.rows(), raw.cols(), CvType.CV_8UC1, new Scalar(0));
            Imgproc.drawContours(mask, hullContours, -1, new Scalar(255), -1);
            // colour everything black outside the contour
            Mat result = new Mat();
            Core.bitwise_and(raw, raw, result, mask);

            return result;
        } finally {
            mergedContour.release();
            hull.release();
            hullPoints.release();
            hierarchy.release();
            edges.release();
            bilateral.release();
            raw.release();
        }
    }

    @Override
    protected Mat customHandleFrame(final Mat frame) {
        if (checkboxCircleExtraction.isSelected()) {
            var bilateralDiameter = (int) sliderBilateralDiameter.getValue();
            var bilateralColor = sliderBilateralSigColor.getValue();
            var bilateralSpace = sliderBilateralSigSpace.getValue();
            var cannyThres1 = sliderCannyThres1.getValue();
            var cannyThres2 = sliderCannyThres2.getValue();
            var minContourArea = sliderMinContourArea.getValue();
            var minApproxCurveRows = sliderMinApproxCurveRows.getValue();

            var matExtractedBoardCircle = extractCircle(
                    frame,
                    bilateralDiameter,
                    bilateralColor,
                    bilateralSpace,
                    cannyThres1,
                    cannyThres2,
                    minContourArea,
                    minApproxCurveRows);

            // set ref image to singletons
            MaskSingleton.getInstance().baseImageBGR.release();
            MaskSingleton.getInstance().baseImageGray.release();

            MaskSingleton.getInstance().baseImageBGR = matExtractedBoardCircle.clone();
            Imgproc.cvtColor(MaskSingleton.getInstance().baseImageBGR, MaskSingleton.getInstance().baseImageGray, Imgproc.COLOR_BGR2GRAY);

            return MaskSingleton.getInstance().baseImageBGR.clone();
        }

        return frame;
    }

    @Override
    protected VideoCapture cameraInit(VideoCapture videoCapture) {
        videoCapture.open(CamSingleton.getInstance().getSelectedCameraIndex(), Videoio.CAP_DSHOW);
//         videoCapture.open(FxUtil.retrieveResourceURL("static/video", "gameplay.mp4").getPath());
//         videoCapture.open(FxUtil.retrieveResourceURL("static/video", "debug.mp4").getPath());
//        videoCapture.open(FxUtil.retrieveResourceURL("static/video", "debug2.mp4").getPath());
//        videoCapture.open(FxUtil.retrieveResourceURL("static/video", "debug3.mp4").getPath()); // broken
//        videoCapture.open(FxUtil.retrieveResourceURL("static/video", "debug4.mp4").getPath());
        if (!videoCapture.isOpened()) {
            videoCapture.open(
                    FxUtil.retrieveResourceAsTempFile("static/video", "setup.mp4").getPath());
            if (!videoCapture.isOpened()) {
                log("Warning: video capture seems not to be ready...");
            }
        }
        return videoCapture;
    }

//    private void start() {
//        try {
//            double resolutionWidth = videoCapture.get(Videoio.CAP_PROP_FRAME_WIDTH);
//            double resolutionHeight = videoCapture.get(Videoio.CAP_PROP_FRAME_HEIGHT);
//            double SCALE_FACTOR = Detection.calculateScaleFactor1024(resolutionWidth);
//            for (; ; ) {
//                if (videoCapture.read(frame)) {
//                    // resize
//                    Imgproc.resize(frame, frame, new Size(
//                            resolutionWidth * SCALE_FACTOR,
//                            resolutionHeight * SCALE_FACTOR));
//                    imageView.setImage(FxUtil.matToImage(frame));
//
//                    // TODO Test
//                    // set ref image to singletons
//                    DartSingleton.getInstance().referenceImage = frame.clone();
//                    MaskSingleton.getInstance().baseImageBGR = frame.clone();
//                    Imgproc.cvtColor(MaskSingleton.getInstance().baseImageBGR, MaskSingleton.getInstance().baseImageGray, Imgproc.COLOR_BGR2GRAY);
//
//                    // TODO remove
//                    Detection.debugShowImage(MaskSingleton.getInstance().baseImageBGR, "");
//                    Detection.debugShowImage(MaskSingleton.getInstance().baseImageGray, "");
//                    Detection.debugShowImage(DartSingleton.getInstance().referenceImage, "");
//
//                } else {
//                    log("EOV reached");
//                    break;
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            log("\nError while reading video frames; err=%s", e.getMessage());
//            imageView.setImage(FxUtil.matToImage(DartSingleton.getInstance().errorImage));
//        }
//    }

    @Override
    protected void onDetectionTaskFailed() {
        log("Setup Step: Reference Image -> Task failed");
    }

    @Override
    protected void onDetectionTaskCancelled() {
        log("Setup Step: Reference Image -> Task cancelled");
    }

    @Override
    protected void onDetectionTaskSucceeded() {
        log("Setup Step: Reference Image -> Task succeeded");
    }

    @Override
    protected void onDetectionTaskRunning() {
        log("Setup Step: Reference Image -> Task running");
    }

    @Override
    protected void takeSnapshot() {
        if (!MaskSingleton.getInstance().baseImageBGR.empty()) {
            File setupTempDir = Path.of("setup_temp").toFile();
            boolean dirExists = setupTempDir.exists();
            if (!dirExists) {
                dirExists = setupTempDir.mkdir();
            }
            if (dirExists) {
                int fileCount = Objects.requireNonNull(setupTempDir.listFiles()).length;
                String filepath = "%s/tmp_img_%d.jpg".formatted(setupTempDir.getPath(), fileCount);
                Imgcodecs.imwrite(filepath, MaskSingleton.getInstance().baseImageBGR);
                File targetImage = new File(filepath);
                if (!targetImage.exists()) {
                    throw new RuntimeException("Snapshot was not successfully saved to file system: %s".formatted(targetImage.getPath()));
                }
                log("Snapshot of {0}", filepath);
            } else {
                throw new RuntimeException("Output directory was not successfully created: %s".formatted(setupTempDir.getPath()));
            }
        } else {
            log("Ignored snapshot since empty");
        }
    }
}
