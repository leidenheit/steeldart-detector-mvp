package de.leidenheit.steeldartdetectormvp.steps;

import de.leidenheit.steeldartdetectormvp.FxUtil;
import de.leidenheit.steeldartdetectormvp.detection.CamSingleton;
import de.leidenheit.steeldartdetectormvp.detection.DartSingleton;
import de.leidenheit.steeldartdetectormvp.detection.Detection;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.text.MessageFormat;

public abstract class ContentWithCameraController {

    @FXML
    public Button buttonTakeImage;
    @FXML
    public ImageView imageView;

    protected double fps = -1d;
    protected double frameCount = -1d;
    protected double framePos = -1d;
    protected double resolutionWidth = -1d;
    protected double resolutionHeight = -1d;
    protected double calculatedScaleFactor = 1d;

    protected Mat frame;

    private Thread thread;

    @FXML
    public void initialize() {
        // release VC
        CamSingleton.getInstance().getVideoCapture().release();

        // reset frame position
        framePos = -1d;
        frameCount = -1d;

        log("Hello World from " + getClass().getSimpleName());

        buttonTakeImage.setOnAction(event -> takeSnapshot());

        initPlaceholders();
        cameraInit(CamSingleton.getInstance().getVideoCapture());

        boolean configChanged;
        configChanged = CamSingleton.getInstance().getVideoCapture().set(Videoio.CAP_PROP_FRAME_WIDTH, 10_000);
        configChanged = configChanged && CamSingleton.getInstance().getVideoCapture().set(Videoio.CAP_PROP_FRAME_HEIGHT, 10_000);
        if (!configChanged) {
            log("WARNING: desired camera resolution was not applied");
        }
        customInit();


        // call detection
        try {
            Task<Void> detectionTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    start();
                    return null;
                }
            };
            detectionTask.setOnRunning(event -> {
                onDetectionTaskRunning();
            });
            detectionTask.setOnSucceeded(event -> {
                onDetectionTaskSucceeded();
                event.getSource().cancel();
            });
            detectionTask.setOnCancelled(event -> {
                onDetectionTaskCancelled();
                event.getSource().cancel();
            });
            detectionTask.setOnFailed(event -> {
                onDetectionTaskFailed();
                event.getSource().cancel();
            });

            thread = new Thread(detectionTask);
            thread.setDaemon(true);
            thread.start();



        } catch (Exception e) {
            log("Error: {0}", e.getMessage());
            imageView.setImage(FxUtil.matToImage(DartSingleton.getInstance().errorImage));
        }

    }

    public void cleanup() {
        System.out.printf("Nanny is cleaning up @ %s\n", getClass().getSimpleName());
        thread.interrupt();
    }

    protected abstract void onDetectionTaskFailed();

    protected abstract void onDetectionTaskCancelled();

    protected abstract void onDetectionTaskSucceeded();

    protected abstract void onDetectionTaskRunning();

    protected void initPlaceholders() {
        Mat image = FxUtil.retrieveResourceAsMat("images/placeholder", "placeholder.jpg");
        imageView.setImage(FxUtil.matToImage(image));
    }

    protected void log(String pattern, Object... objects) {
        var msg = MessageFormat.format(pattern, objects);
        DartSingleton.getInstance().getDebugList().add(msg);
        System.out.println(msg);
    }

    private void start() {
        try {
            frameCount = CamSingleton.getInstance().getVideoCapture().get(Videoio.CAP_PROP_FRAME_COUNT);
            fps = (CamSingleton.getInstance().getVideoCapture().get(Videoio.CAP_PROP_FPS));
            fps = fps == 0 ? 30d : fps;
            resolutionWidth = CamSingleton.getInstance().getVideoCapture().get(Videoio.CAP_PROP_FRAME_WIDTH);
            resolutionHeight = CamSingleton.getInstance().getVideoCapture().get(Videoio.CAP_PROP_FRAME_HEIGHT);
            calculatedScaleFactor = Detection.calculateScaleFactor1024(resolutionWidth);

            Size targetResolutionSize = new Size(resolutionWidth * calculatedScaleFactor, resolutionHeight * calculatedScaleFactor);
            Size gaussSize = new Size(DartSingleton.getInstance().vidGaussian, DartSingleton.getInstance().vidGaussian);

            frame = new Mat();
            try {
                while (!thread.isInterrupted() && CamSingleton.getInstance().getVideoCapture().read(frame)) {
                    // FIXME Camera does not support this feature :/
                    //  framePos = CamSingleton.getInstance().getVideoCapture().get(Videoio.CAP_PROP_POS_FRAMES);
                    framePos++;

                    if (!frame.empty()) {
                        // resize
                        Mat resizedFrame = new Mat();
                        Imgproc.resize(frame, resizedFrame, targetResolutionSize);

                        // reduce noise
                        Mat blurredFrame = new Mat();
                        Imgproc.GaussianBlur(resizedFrame, blurredFrame, gaussSize, 0);

                        Mat customHandledFrame = customHandleFrame(blurredFrame);

                        javafx.scene.image.Image image = FxUtil.matToImage(customHandledFrame);
                        Platform.runLater(() -> {
                            imageView.setImage(image);
                        });

                        resizedFrame.release(); // Release resized frame
                        blurredFrame.release(); // Release blurred frame
                        customHandledFrame.release();

                        // Sleep for a short while to prevent high CPU usage
                        Thread.sleep(25);
                    }
                }
                log("EOV reached");
            } finally {
                frame.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
            log("Error while reading video frames; err={0}", e.getMessage());
            Platform.runLater(() -> imageView.setImage(FxUtil.matToImage(DartSingleton.getInstance().errorImage)));
        }
    }

    protected abstract void takeSnapshot();

    protected abstract void customInit();

    protected abstract Mat customHandleFrame(final Mat frame);

    protected abstract VideoCapture cameraInit(final VideoCapture videoCapture);
}
