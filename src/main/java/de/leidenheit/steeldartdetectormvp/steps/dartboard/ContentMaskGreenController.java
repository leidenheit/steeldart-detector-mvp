package de.leidenheit.steeldartdetectormvp.steps.dartboard;

import de.leidenheit.steeldartdetectormvp.FxUtil;
import de.leidenheit.steeldartdetectormvp.detection.Detection;
import de.leidenheit.steeldartdetectormvp.detection.LeidenheitException;
import de.leidenheit.steeldartdetectormvp.detection.MaskSingleton;
import de.leidenheit.steeldartdetectormvp.steps.ContentController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class ContentMaskGreenController extends ContentController {

    @FXML
    public Slider sliderThreshold;
    @FXML
    public Slider sliderGaussian;
    @FXML
    public Slider sliderErodeIterations;
    @FXML
    public Slider sliderDilateIterations;
    @FXML
    public Slider sliderCloseIterations;
    @FXML
    public Button buttonResetParams;

    @Override
    protected void customInit() {
        log("extracting green mask");

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
        buttonResetParams.setOnAction(event -> resetParams());

        // start detection after initialization
        resetParams();
    }

    @Override
    protected void resetParams() {
        sliderThreshold.setValue(MaskSingleton.getInstance().greenMaskThresholdValue);
        sliderGaussian.setValue(MaskSingleton.getInstance().greenMaskGaussianValue);
        sliderErodeIterations.setValue(MaskSingleton.getInstance().greenMaskMorphErodeValue);
        sliderDilateIterations.setValue(MaskSingleton.getInstance().greenMaskMorphDilateValue);
        sliderCloseIterations.setValue(MaskSingleton.getInstance().greenMaskMorphCloseValue);

        doDetect();
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

        MaskSingleton.getInstance().greenMaskThresholdValue = (int)threshold;
        MaskSingleton.getInstance().greenMaskGaussianValue = gaussian;
        MaskSingleton.getInstance().greenMaskMorphErodeValue = erodeIterations;
        MaskSingleton.getInstance().greenMaskMorphDilateValue = dilateIterations;
        MaskSingleton.getInstance().greenMaskMorphCloseValue = closeIterations;

        // call detection
        try {
            MaskSingleton.getInstance().greenMask = Detection.extractMaskGreen(
                    MaskSingleton.getInstance().baseImageBGR,
                    MaskSingleton.getInstance().baseImageGray,
                    threshold,
                    gaussian,
                    erodeIterations,
                    dilateIterations,
                    closeIterations);

            Mat greenFields = MaskSingleton.getInstance().baseImageBGR.clone();
            Mat hierarchy = new Mat();
            List<MatOfPoint> contours = new ArrayList<>();
            Imgproc.findContours(
                    MaskSingleton.getInstance().greenMask,
                    contours,
                    hierarchy,
                    Imgproc.RETR_EXTERNAL,
                    Imgproc.CHAIN_APPROX_SIMPLE
                    );
            Imgproc.drawContours(greenFields, contours, -1, new Scalar(255, 0, 0), 1);

            imageViewSource.setImage(FxUtil.matToImage(greenFields));
            imageViewComputed.setImage(FxUtil.matToImage(MaskSingleton.getInstance().greenMask));
        } catch (LeidenheitException e) {
            imageViewComputed.setImage(FxUtil.matToImage(MaskSingleton.getInstance().errorImage));
        }
    }
}
