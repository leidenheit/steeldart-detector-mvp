package de.leidenheit.steeldartdetectormvp.steps.dartboard;

import de.leidenheit.steeldartdetectormvp.FxUtil;
import de.leidenheit.steeldartdetectormvp.detection.Detection;
import de.leidenheit.steeldartdetectormvp.detection.LeidenheitException;
import de.leidenheit.steeldartdetectormvp.detection.MaskSingleton;
import de.leidenheit.steeldartdetectormvp.steps.ContentController;

public class ContentMaskDoubleController extends ContentController {

    protected void customInit() {
        this.log("extracting double mask");

        // start detection after initialization
        resetParams();
    }

    @Override
    protected void doDetect() {
        // call detection
        try {
            MaskSingleton.getInstance().doubleMask = Detection.extractMaskDouble(
                    MaskSingleton.getInstance().dartboardMask,
                    MaskSingleton.getInstance().singleMask);
            imageViewSource.setImage(FxUtil.matToImage(MaskSingleton.getInstance().baseImageBGR));
            imageViewComputed.setImage(FxUtil.matToImage(MaskSingleton.getInstance().doubleMask));
        } catch (LeidenheitException e) {
            imageViewComputed.setImage(FxUtil.matToImage(MaskSingleton.getInstance().errorImage));
        }
    }

    @Override
    protected void resetParams() {
        doDetect();
    }
}
