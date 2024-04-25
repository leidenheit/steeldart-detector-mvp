package de.leidenheit.steeldartdetectormvp.steps.dartboard;

import de.leidenheit.steeldartdetectormvp.FxUtil;
import de.leidenheit.steeldartdetectormvp.detection.Detection;
import de.leidenheit.steeldartdetectormvp.detection.LeidenheitException;
import de.leidenheit.steeldartdetectormvp.detection.MaskSingleton;
import de.leidenheit.steeldartdetectormvp.steps.ContentController;
import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class ContentMaskRedController extends ContentController {

    @FXML
    private Slider sliderThreshold;
    @FXML
    private Slider sliderGaussian;
    @FXML
    private Slider sliderErodeIterations;
    @FXML
    private Slider sliderDilateIterations;
    @FXML
    private Slider sliderCloseIterations;

    @Override
    protected void customInit() {
        this.log("extracting red mask");

        // add handlers
        sliderThreshold.setOnMouseReleased(mouseEvent -> doDetect());
        sliderGaussian.setOnMouseReleased(mouseEvent -> doDetect());
        sliderErodeIterations.setOnMouseReleased(mouseEvent -> doDetect());
        sliderDilateIterations.setOnMouseReleased(mouseEvent -> doDetect());
        sliderCloseIterations.setOnMouseReleased(mouseEvent -> doDetect());
        sliderThreshold.setOnKeyReleased(keyEvent -> doDetect());
        sliderGaussian.setOnKeyReleased(keyEvent -> doDetect());
        sliderErodeIterations.setOnKeyReleased(keyEvent -> doDetect());
        sliderDilateIterations.setOnKeyReleased(keyEvent -> doDetect());
        sliderCloseIterations.setOnKeyReleased(keyEvent -> doDetect());

        // start detection after initialization
        resetParams();
    }

    @Override
    protected void doDetect() {
        // read values from controls
        final var threshold = sliderThreshold.getValue();
        final var gaussian = (int) sliderGaussian.getValue();
        final var erodeIterations = (int) sliderErodeIterations.getValue();
        final var dilateIterations = (int) sliderDilateIterations.getValue();
        final var closeIterations = (int) sliderCloseIterations.getValue();

        log("threshold={0}", threshold);
        log("gaussianFilterSize={0}", gaussian);
        log("erodeIterations={0}", erodeIterations);
        log("dilateIterations={0}", dilateIterations);
        log("closeIterations={0}", closeIterations);


        MaskSingleton.getInstance().redMaskThresholdValue = (int)threshold;
        MaskSingleton.getInstance().redMaskGaussianValue = gaussian;
        MaskSingleton.getInstance().redMaskMorphErodeValue = erodeIterations;
        MaskSingleton.getInstance().redMaskMorphDilateValue = dilateIterations;
        MaskSingleton.getInstance().redMaskMorphCloseValue = closeIterations;

        // call detection
        try {
            MaskSingleton.getInstance().redMask = Detection.extractMaskRed(
                    MaskSingleton.getInstance().baseImageBGR,
                    MaskSingleton.getInstance().baseImageGray,
                    threshold,
                    gaussian,
                    erodeIterations,
                    dilateIterations,
                    closeIterations);

            Mat redFields = MaskSingleton.getInstance().baseImageBGR.clone();
            Mat hierarchy = new Mat();
            List<MatOfPoint> contours = new ArrayList<>();
            Imgproc.findContours(
                    MaskSingleton.getInstance().redMask,
                    contours,
                    hierarchy,
                    Imgproc.RETR_EXTERNAL,
                    Imgproc.CHAIN_APPROX_SIMPLE
            );
            Imgproc.drawContours(redFields, contours, -1, new Scalar(255, 0, 0), 1);

            imageViewSource.setImage(FxUtil.matToImage(redFields));
            imageViewComputed.setImage(FxUtil.matToImage(MaskSingleton.getInstance().redMask));
        } catch (LeidenheitException e) {
            imageViewComputed.setImage(FxUtil.matToImage(MaskSingleton.getInstance().errorImage));
        }
    }

    @Override
    protected void resetParams() {
        sliderThreshold.setValue(MaskSingleton.getInstance().redMaskThresholdValue);
        sliderGaussian.setValue(MaskSingleton.getInstance().redMaskGaussianValue);
        sliderErodeIterations.setValue(MaskSingleton.getInstance().redMaskMorphErodeValue);
        sliderDilateIterations.setValue(MaskSingleton.getInstance().redMaskMorphDilateValue);
        sliderCloseIterations.setValue(MaskSingleton.getInstance().redMaskMorphCloseValue);

        doDetect();
    }
}
