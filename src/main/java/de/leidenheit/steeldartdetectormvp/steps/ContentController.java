package de.leidenheit.steeldartdetectormvp.steps;

import de.leidenheit.steeldartdetectormvp.FxUtil;
import de.leidenheit.steeldartdetectormvp.detection.MaskSingleton;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import org.opencv.core.Mat;

import java.text.MessageFormat;

public abstract class ContentController {

    @FXML
    public ImageView imageViewComputed;
    @FXML
    public ImageView imageViewSource;
    @FXML
    public Button buttonResetParams;

    @FXML
    public void initialize() {
        System.out.println("Hello World from " + getClass().getSimpleName());
        initPlaceholders();

        buttonResetParams.setOnAction(event -> resetParams());

        customInit();
    }

    protected void initPlaceholders() {
        Mat image = FxUtil.retrieveResourceAsMat("images/placeholder", "placeholder.jpg");
        imageViewSource.setImage(FxUtil.matToImage(image));
        imageViewComputed.setImage(FxUtil.matToImage(image));
    }

    protected void log(String pattern, Object... objects) {
        var msg = MessageFormat.format(pattern, objects);
        MaskSingleton.getInstance().getDebugList().add(msg);
        System.out.println(msg);
    }

    protected abstract void customInit();
    protected abstract void resetParams();
    protected abstract void doDetect();
}
