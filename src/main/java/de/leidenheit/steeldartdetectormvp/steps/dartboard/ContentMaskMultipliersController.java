package de.leidenheit.steeldartdetectormvp.steps.dartboard;

import de.leidenheit.steeldartdetectormvp.FxUtil;
import de.leidenheit.steeldartdetectormvp.detection.Detection;
import de.leidenheit.steeldartdetectormvp.detection.LeidenheitException;
import de.leidenheit.steeldartdetectormvp.detection.MaskSingleton;
import de.leidenheit.steeldartdetectormvp.steps.ContentController;
import javafx.fxml.FXML;
import javafx.scene.control.Slider;

public class ContentMaskMultipliersController extends ContentController {

    @FXML
    private Slider sliderDilateIterations;

    @Override
    protected void customInit() {
        this.log("extracting multiplier mask");

        // add handlers
        sliderDilateIterations.setOnMouseReleased(mouseEvent -> doDetect());
        sliderDilateIterations.setOnKeyReleased(keyEvent -> doDetect());

        // start detection after initialization
        resetParams();
    }

    @Override
    protected void doDetect() {
        // read values from controls
        final var dilateIterations = (int) sliderDilateIterations.getValue();
        log("dilateIterations={0}", dilateIterations);

        MaskSingleton.getInstance().multipliersMaskMorphDilateValue = dilateIterations;

        // call detection
        try {
            MaskSingleton.getInstance().multipliersMask = Detection.extractMaskMultipliers(
                    MaskSingleton.getInstance().redMask,
                    MaskSingleton.getInstance().greenMask,
                    dilateIterations);
            imageViewSource.setImage(FxUtil.matToImage(MaskSingleton.getInstance().baseImageBGR));
            imageViewComputed.setImage(FxUtil.matToImage(MaskSingleton.getInstance().multipliersMask));
        } catch (LeidenheitException e) {
            imageViewComputed.setImage(FxUtil.matToImage(MaskSingleton.getInstance().errorImage));
        }
    }

    @Override
    protected void resetParams() {
        sliderDilateIterations.setValue(MaskSingleton.getInstance().multipliersMaskMorphDilateValue);
        doDetect();
    }
}
