package de.leidenheit.steeldartdetectormvp.steps.dartboard;

import de.leidenheit.steeldartdetectormvp.FxUtil;
import de.leidenheit.steeldartdetectormvp.detection.Detection;
import de.leidenheit.steeldartdetectormvp.detection.LeidenheitException;
import de.leidenheit.steeldartdetectormvp.detection.MaskSingleton;
import de.leidenheit.steeldartdetectormvp.steps.ContentController;

public class ContentMaskSingleController extends ContentController {

    protected void customInit() {
        this.log("extracting single mask");

        // start detection after initialization
        resetParams();
    }

    @Override
    protected void doDetect() {
        // call detection
        try {
            MaskSingleton.getInstance().singleMask = Detection.extractMaskSingle(
                    MaskSingleton.getInstance().dartboardMask,
                    MaskSingleton.getInstance().multiRingsMask);
            imageViewSource.setImage(FxUtil.matToImage(MaskSingleton.getInstance().baseImageBGR));
            imageViewComputed.setImage(FxUtil.matToImage(MaskSingleton.getInstance().singleMask));
        } catch (LeidenheitException e) {
            imageViewComputed.setImage(FxUtil.matToImage(MaskSingleton.getInstance().errorImage));
        }
    }

    @Override
    protected void resetParams() {
        doDetect();
    }
}
