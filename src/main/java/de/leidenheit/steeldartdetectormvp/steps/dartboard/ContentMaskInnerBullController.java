package de.leidenheit.steeldartdetectormvp.steps.dartboard;

import de.leidenheit.steeldartdetectormvp.FxUtil;
import de.leidenheit.steeldartdetectormvp.detection.Detection;
import de.leidenheit.steeldartdetectormvp.detection.LeidenheitException;
import de.leidenheit.steeldartdetectormvp.detection.MaskSingleton;
import de.leidenheit.steeldartdetectormvp.steps.ContentController;

public class ContentMaskInnerBullController extends ContentController {

    @Override
    protected void customInit() {
        this.log("extracting inner mask");

        // start detection after initialization
        resetParams();
    }

    @Override
    protected void doDetect() {
        // call detection
        try {
            MaskSingleton.getInstance().innerBullMask = Detection.extractMaskInnerBull(
                    MaskSingleton.getInstance().multiRingsMask,
                    MaskSingleton.getInstance().doubleMask,
                    MaskSingleton.getInstance().tripleMask,
                    MaskSingleton.getInstance().redMask);
            imageViewSource.setImage(FxUtil.matToImage(MaskSingleton.getInstance().baseImageBGR));
            imageViewComputed.setImage(FxUtil.matToImage(MaskSingleton.getInstance().innerBullMask));
        } catch (LeidenheitException e) {
            imageViewComputed.setImage(FxUtil.matToImage(MaskSingleton.getInstance().errorImage));
        }
    }

    @Override
    protected void resetParams() {
        doDetect();
    }
}
