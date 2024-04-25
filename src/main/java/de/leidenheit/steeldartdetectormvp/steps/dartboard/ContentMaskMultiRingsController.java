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

public class ContentMaskMultiRingsController extends ContentController {

    @FXML
    private Slider sliderCloseIterations;

    protected void customInit() {
        this.log("extracting multirings mask");

        // add handlers
        sliderCloseIterations.setOnMouseReleased(mouseEvent -> doDetect());
        sliderCloseIterations.setOnKeyReleased(keyEvent -> doDetect());

        // start detection after initialization
        resetParams();
    }

    @Override
    protected void doDetect() {
        // read values from controls
        final var closeIterations = (int) sliderCloseIterations.getValue();
        log("closeIterations={0}", closeIterations);

        MaskSingleton.getInstance().multiRingsMaskMorphCloseValue = closeIterations;

        // call detection
        try {
            MaskSingleton.getInstance().multiRingsMask = Detection.extractMaskMultiRings(
                    MaskSingleton.getInstance().multipliersMask,
                    closeIterations);

            Mat multiRings = MaskSingleton.getInstance().baseImageBGR.clone();
            Mat hierarchy = new Mat();
            List<MatOfPoint> contours = new ArrayList<>();
            Imgproc.findContours(
                    MaskSingleton.getInstance().multiRingsMask,
                    contours,
                    hierarchy,
                    Imgproc.RETR_EXTERNAL,
                    Imgproc.CHAIN_APPROX_SIMPLE
            );
            Imgproc.drawContours(multiRings, contours, -1, new Scalar(255, 0, 0), 1);

            imageViewSource.setImage(FxUtil.matToImage(multiRings));
            imageViewComputed.setImage(FxUtil.matToImage(MaskSingleton.getInstance().multiRingsMask));
        } catch (LeidenheitException e) {
            imageViewComputed.setImage(FxUtil.matToImage(MaskSingleton.getInstance().errorImage));
        }
    }

    @Override
    protected void resetParams() {
        sliderCloseIterations.setValue(MaskSingleton.getInstance().multiRingsMaskMorphCloseValue);
        doDetect();
    }
}
