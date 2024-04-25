package de.leidenheit.steeldartdetectormvp.detection;

import lombok.Data;
import org.opencv.videoio.VideoCapture;

@Data
public class CamSingleton {

    private static CamSingleton instance;

    private int selectedCameraIndex = -1; // -1 is the error state
    private String cameraName = "n/a";

    private VideoCapture videoCapture = new VideoCapture();

    public static CamSingleton getInstance() {
        if (instance == null) {
            instance = new CamSingleton();
        }
        return instance;
    }
}
