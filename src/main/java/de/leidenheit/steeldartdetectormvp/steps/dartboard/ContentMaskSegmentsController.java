package de.leidenheit.steeldartdetectormvp.steps.dartboard;

import de.leidenheit.steeldartdetectormvp.FxUtil;
import de.leidenheit.steeldartdetectormvp.detection.Detection;
import de.leidenheit.steeldartdetectormvp.detection.LeidenheitException;
import de.leidenheit.steeldartdetectormvp.detection.MaskSingleton;
import de.leidenheit.steeldartdetectormvp.steps.ContentController;
import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class ContentMaskSegmentsController extends ContentController {

    @FXML
    private Slider sliderLineGroupTolerance;
    @FXML
    private Slider sliderLineCandidateCannyGaussian;
    @FXML
    private Slider sliderLineCandidateDilateKernelSize;
    @FXML
    private Slider sliderCannyThres1;
    @FXML
    private Slider sliderCannyThres2;
    @FXML
    private Slider sliderHoughThres;
    @FXML
    private Slider sliderHoughMinLineLength;
    @FXML
    private Slider sliderHoughMaxLineGap;

    @Override
    protected void customInit() {
        sliderLineGroupTolerance.setOnMouseReleased(mouseEvent -> doDetect());
        sliderLineCandidateCannyGaussian.setOnMouseReleased(mouseEvent -> doDetect());
        sliderLineCandidateDilateKernelSize.setOnMouseReleased(mouseEvent -> doDetect());
        sliderCannyThres1.setOnMouseReleased(mouseEvent -> doDetect());
        sliderCannyThres2.setOnMouseReleased(mouseEvent -> doDetect());
        sliderHoughThres.setOnMouseReleased(mouseEvent -> doDetect());
        sliderHoughMinLineLength.setOnMouseReleased(mouseEvent -> doDetect());
        sliderHoughMaxLineGap.setOnMouseReleased(mouseEvent -> doDetect());

        sliderLineGroupTolerance.setOnKeyReleased(keyEvent -> doDetect());
        sliderLineCandidateCannyGaussian.setOnKeyReleased(keyEvent -> doDetect());
        sliderLineCandidateDilateKernelSize.setOnKeyReleased(keyEvent -> doDetect());
        sliderCannyThres1.setOnKeyReleased(keyEvent -> doDetect());
        sliderCannyThres2.setOnKeyReleased(keyEvent -> doDetect());
        sliderHoughThres.setOnKeyReleased(keyEvent -> doDetect());
        sliderHoughMinLineLength.setOnKeyReleased(keyEvent -> doDetect());
        sliderHoughMaxLineGap.setOnKeyReleased(keyEvent -> doDetect());

        resetParams();
    }

    @Override
    protected void resetParams() {
        log("params reset");
        sliderLineGroupTolerance.setValue(MaskSingleton.getInstance().segmentsMaskLineGroupTolerance);
        sliderLineCandidateCannyGaussian.setValue(MaskSingleton.getInstance().segmentsMaskLineCandidateCannyGaussian);
        sliderLineCandidateDilateKernelSize.setValue(MaskSingleton.getInstance().segmentsMaskLineCandidateDilateKernelSize);
        sliderCannyThres1.setValue(MaskSingleton.getInstance().getSegmentsMaskCannyThreshold1());
        sliderCannyThres2.setValue(MaskSingleton.getInstance().getSegmentsMaskCannyThreshold2());
        sliderHoughThres.setValue(MaskSingleton.getInstance().segmentsMaskHoughThreshold);
        sliderHoughMinLineLength.setValue(MaskSingleton.getInstance().segmentsMaskHoughMinLineLength);
        sliderHoughMaxLineGap.setValue(MaskSingleton.getInstance().segmentsMaskHoughMaxLineGap);

        doDetect();
    }

    @Override
    protected void doDetect() {
        log("determining Segments from detected masks");
        // grab values from controls
        final var lineGroupTolerance = sliderLineGroupTolerance.getValue();
        final var lineCandidateCannyGaussian = sliderLineCandidateCannyGaussian.getValue();
        final var lineCandidateDilateKernelSize = sliderLineCandidateDilateKernelSize.getValue();
        final var cannyThres1 = sliderCannyThres1.getValue();
        final var cannyThres2 = sliderCannyThres2.getValue();
        final var houghThres = sliderHoughThres.getValue();
        final var houghMaxLineGap = sliderHoughMaxLineGap.getValue();
        final var houghMinLineLength = sliderHoughMinLineLength.getValue();

        log("lineGroupTolerance={0}", lineGroupTolerance);
        log("lineCandidateCannyGaussian={0}", lineCandidateCannyGaussian);
        log("lineCandidateDilateKernelSize={0}", lineCandidateDilateKernelSize);
        log("cannyThres1={0}", cannyThres1);
        log("cannyThres2={0}", cannyThres2);
        log("houghThres={0}", houghThres);
        log("houghMaxLineGap={0}", houghMaxLineGap);
        log("houghMinLineLength={0}", houghMinLineLength);

        MaskSingleton.getInstance().segmentsMaskLineGroupTolerance = (int) lineGroupTolerance;
        MaskSingleton.getInstance().segmentsMaskLineCandidateCannyGaussian = (int) lineCandidateCannyGaussian;
        MaskSingleton.getInstance().segmentsMaskLineCandidateDilateKernelSize = (int) lineCandidateDilateKernelSize;
        MaskSingleton.getInstance().segmentsMaskCannyThreshold1 = (int) cannyThres1;
        MaskSingleton.getInstance().segmentsMaskCannyThreshold2 = (int) cannyThres2;
        MaskSingleton.getInstance().segmentsMaskHoughThreshold = (int) houghThres;
        MaskSingleton.getInstance().segmentsMaskHoughMaxLineGap = (int) houghMaxLineGap;
        MaskSingleton.getInstance().segmentsMaskHoughMinLineLength = (int) houghMinLineLength;

        try {
            final var pointCenter = Detection.findCircleCenter(MaskSingleton.getInstance().baseImageBGR, MaskSingleton.getInstance().innerBullMask);
            MaskSingleton.getInstance().segments = MaskSingleton.getInstance().baseImageBGR.clone();

            Mat bilateral = new Mat();
            Imgproc.bilateralFilter(MaskSingleton.getInstance().baseImageBGR, bilateral, 5, 50, 50);

            MaskSingleton.getInstance().valueAngleRanges = Detection.determineDartboardSegments(
                    pointCenter,
                    bilateral,
                    MaskSingleton.getInstance().segments,
                    MaskSingleton.getInstance().innerBullMask,
                    MaskSingleton.getInstance().doubleMask,
                    MaskSingleton.getInstance().tripleMask,
                    MaskSingleton.getInstance().multiRingsMask,
                    (int) lineCandidateCannyGaussian,
                    (int) lineCandidateDilateKernelSize,
                    lineGroupTolerance,
                    (int) cannyThres1,
                    (int) cannyThres2,
                    (int) houghThres,
                    (int) houghMinLineLength,
                    (int) houghMaxLineGap,
                    MaskSingleton.getInstance().coordinateOfSegment6
            );

            imageViewSource.setImage(FxUtil.matToImage(MaskSingleton.getInstance().baseImageBGR));
            imageViewComputed.setImage(FxUtil.matToImage(MaskSingleton.getInstance().segments));
        } catch (LeidenheitException e) {
            log("Error: {0}", e.getMessage());
            imageViewComputed.setImage(FxUtil.matToImage(MaskSingleton.getInstance().errorImage));
        }
    }
}
