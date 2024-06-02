package de.leidenheit.steeldartdetectormvp.steps.dartboard;

import de.leidenheit.steeldartdetectormvp.FxUtil;
import de.leidenheit.steeldartdetectormvp.detection.MaskSingleton;
import de.leidenheit.steeldartdetectormvp.steps.ContentController;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class ContentMaskSegmentAdjustmentController extends ContentController {

    @Override
    protected void customInit() {
        imageViewSource.setOnMouseClicked(mouseEvent -> {
            MaskSingleton.getInstance().coordinateOfSegment6 =
                    new Point(mouseEvent.getX(), mouseEvent.getY());
            doDetect();
        });

        doDetect();
    }

    @Override
    protected void resetParams() {
        // ignored
    }

    @Override
    protected void doDetect() {
        Mat matWithClickHighlight = MaskSingleton.getInstance().baseImageBGR.clone();
        Imgproc.circle(matWithClickHighlight, MaskSingleton.getInstance().coordinateOfSegment6, 4, new Scalar(0, 255, 0), 3, Imgproc.LINE_AA);
        imageViewSource.setImage(FxUtil.matToImage(matWithClickHighlight));
        matWithClickHighlight.release();
    }
}
