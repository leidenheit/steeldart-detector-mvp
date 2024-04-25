package de.leidenheit.steeldartdetectormvp.steps.dartboard;

import de.leidenheit.steeldartdetectormvp.FxUtil;
import de.leidenheit.steeldartdetectormvp.detection.Detection;
import de.leidenheit.steeldartdetectormvp.detection.LeidenheitException;
import de.leidenheit.steeldartdetectormvp.detection.MaskSingleton;
import de.leidenheit.steeldartdetectormvp.steps.ContentController;
import javafx.fxml.FXML;
import javafx.scene.control.Slider;

public class ContentMaskDartboardController extends ContentController {

    @FXML
    private Slider sliderDilateIterations;
    @FXML
    private Slider sliderErodeIterations;

    protected void customInit() {
        this.log("extracting dartboard mask");

        // add handlers
        sliderDilateIterations.setOnMouseReleased(mouseEvent -> doDetect());
        sliderErodeIterations.setOnMouseReleased(mouseEvent -> doDetect());
        sliderDilateIterations.setOnKeyReleased(keyEvent -> doDetect());
        sliderErodeIterations.setOnKeyReleased(keyEvent -> doDetect());

        // start detection after initialization
        resetParams();
    }

    @Override
    protected void doDetect() {
        // read values from controls
        final var dilateIterations = (int) sliderDilateIterations.getValue();
        final var erodeIterations = (int) sliderErodeIterations.getValue();
        log("dilateIterations={0}", dilateIterations);
        log("erodeIterations={0}", erodeIterations);

        MaskSingleton.getInstance().dartboardMaskMorphDilateIterationsValue = dilateIterations;
        MaskSingleton.getInstance().dartboardMaskMorphErodeIterationsValue = erodeIterations;

        // call detection
        try {
            MaskSingleton.getInstance().dartboardMask = Detection.extractMaskDartboard(
                    MaskSingleton.getInstance().multiRingsMask,
                    dilateIterations,
                    erodeIterations);
            imageViewSource.setImage(FxUtil.matToImage(MaskSingleton.getInstance().baseImageBGR));
            imageViewComputed.setImage(FxUtil.matToImage(MaskSingleton.getInstance().dartboardMask));
        } catch (LeidenheitException e) {
            imageViewComputed.setImage(FxUtil.matToImage(MaskSingleton.getInstance().errorImage));
        }
    }

    @Override
    protected void resetParams() {
        sliderDilateIterations.setValue(MaskSingleton.getInstance().dartboardMaskMorphDilateIterationsValue);
        sliderErodeIterations.setValue(MaskSingleton.getInstance().dartboardMaskMorphErodeIterationsValue);

        doDetect();
    }
}
