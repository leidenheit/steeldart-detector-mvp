package de.leidenheit.steeldartdetectormvp;

import javafx.scene.image.Image;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.*;
import java.net.URL;
import java.util.Objects;

public class FxUtil {

    public static Image matToImage(Mat mat) {
        try {
            final var matResized = new Mat();
            Imgproc.resize(mat, matResized, new Size(mat.width(), mat.height()));
            MatOfByte matOfByte = new MatOfByte();
            Imgcodecs.imencode(".jpg", matResized, matOfByte);
            final var inputStream = new ByteArrayInputStream(matOfByte.toArray());
            return new Image(inputStream);
        } catch (Exception e) {
            System.out.printf("Error while encoding OpenCV to FXImage: %s", e.getMessage());
        }
        return null;
    }

    public static URL retrieveResourceURL(String subResourcePath, String fileName) {
        URL res = Launcher.class.getResource("%s/%s".formatted(subResourcePath, fileName));
        System.out.printf("Reading resource %s/%s: %s\n", subResourcePath, fileName, res);
        return res;
    }

    public static Mat retrieveResourceAsMat(String subResourcePath, String fileName) {
        System.out.printf("Reading resource as Mat %s/%s\n", subResourcePath, fileName);
        byte[] bytes;
        try {
            try (InputStream res = Launcher.class.getResourceAsStream("%s/%s".formatted(subResourcePath, fileName))) {
                bytes = Objects.requireNonNull(res).readAllBytes();
                Mat resMat = new MatOfByte(bytes);
                return Imgcodecs.imdecode(resMat, Imgcodecs.IMREAD_UNCHANGED);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static File retrieveResourceAsTempFile(String subResourcePath, String fileName) {
        System.out.printf("Reading resource as temp file %s/%s\n", subResourcePath, fileName);
        byte[] bytes;
        try {
            try (InputStream res = Launcher.class.getResourceAsStream("%s/%s".formatted(subResourcePath, fileName))) {
                bytes = Objects.requireNonNull(res).readAllBytes();

                File tempFile = File.createTempFile("tmp/temp_%s".formatted(fileName), ".tmp");
                tempFile.deleteOnExit();
                try (OutputStream outputStream = new FileOutputStream(tempFile)) {
                    outputStream.write(bytes);
                    return tempFile;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
