package de.leidenheit.steeldartdetectormvp.steps.dartboard;

import de.leidenheit.steeldartdetectormvp.FxUtil;
import de.leidenheit.steeldartdetectormvp.detection.Detection;
import de.leidenheit.steeldartdetectormvp.detection.LeidenheitException;
import de.leidenheit.steeldartdetectormvp.detection.MaskSingleton;
import de.leidenheit.steeldartdetectormvp.steps.ContentController;

public class ContentMaskTripleController extends ContentController {

    @Override
    protected void customInit() {
        this.log("extracting triple mask");

        // start detection after initialization
        resetParams();
    }

    @Override
    protected void doDetect() {
        // call detection
        try {
            MaskSingleton.getInstance().tripleMask = Detection.extractMaskTriple(
                    MaskSingleton.getInstance().dartboardMask,
                    MaskSingleton.getInstance().doubleMask,
                    MaskSingleton.getInstance().singleMask);
            imageViewSource.setImage(FxUtil.matToImage(MaskSingleton.getInstance().baseImageBGR));
            imageViewComputed.setImage(FxUtil.matToImage(MaskSingleton.getInstance().tripleMask));
        } catch (LeidenheitException e) {
            imageViewComputed.setImage(FxUtil.matToImage(MaskSingleton.getInstance().errorImage));
        }
    }

    @Override
    protected void resetParams() {
        doDetect();
    }
}
