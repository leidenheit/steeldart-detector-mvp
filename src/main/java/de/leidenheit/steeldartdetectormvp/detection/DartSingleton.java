package de.leidenheit.steeldartdetectormvp.detection;

import de.leidenheit.steeldartdetectormvp.FxUtil;
import lombok.Getter;
import lombok.Setter;
import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class DartSingleton implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static DartSingleton instance;

    // ignored by serialization
    public transient Mat errorImage = null;

    // TODO candidate of removal
    // ignored by serialization
    public transient Point arrowTip = null;
    // ignored by serialization
    public transient Point arrowTipBoundingBoxTl = null;
    // ignored by serialization
    public transient Point arrowTipBoundingBoxBr = null;

    // ignored by serialization
    private final transient List<String> debugList = new ArrayList<>();
    public final transient List<Double> euclideanDistances = new ArrayList<>();
    public final transient List<Double> arrowAngles = new ArrayList<>();

    // scale factor configuration
    public double scaleFactor = 1d;

    public int vidGaussian = 5;
    public int vidCloseIterations = 1;
    public int vidErodeIterations = 1;
    public int vidDilateIterations = 1;
    public int vidSubtractorThreshold = 75;
    public int vidMinContourArea = 100;
    public int vidMaxMergedContourArea = 15_000;
    public double vidAspectRatioLow = 0;
    public double vidAspectRatioHigh = 3;
    public double vidUnpluggingThreshold = 15_000;
    // TODO add max merged contour area

    private DartSingleton() throws URISyntaxException {
        // hide constructor
        errorImage = FxUtil.retrieveResourceAsMat("images/placeholder", "error.jpg");
    }

    public static DartSingleton getInstance() {
        if (instance == null) {
            try {
                instance = new DartSingleton();
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        return instance;
    }

    public boolean serialize(final String fileName) throws LeidenheitException {
        try (FileOutputStream fos = new FileOutputStream(fileName)) { {
                try (ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                    // write scale factor configuration
                    // TODO still required?
                    oos.writeDouble(scaleFactor);

                    // arrow tip configuration
                    oos.writeInt(vidGaussian);
                    oos.writeInt(vidCloseIterations);
                    oos.writeInt(vidErodeIterations);
                    oos.writeInt(vidDilateIterations);
                    oos.writeInt(vidSubtractorThreshold);
                    oos.writeInt(vidMinContourArea);
                    oos.writeInt(vidMaxMergedContourArea);
                    oos.writeDouble(vidAspectRatioLow);
                    oos.writeDouble(vidAspectRatioHigh);
                    oos.writeDouble(vidUnpluggingThreshold);

                    return true;
                }
            }
        } catch (Exception e) {
            throw new LeidenheitException("Error while serialization", e);
        }
    }

    public boolean deserialize(final String fileName) throws LeidenheitException {
        try (FileInputStream fis = new FileInputStream(fileName)) {
            try (ObjectInputStream ois = new ObjectInputStream(fis)) {
                // scale factor configuration
                // TODO required?
                scaleFactor = ois.readDouble();

                // arrow tip configuration
                vidGaussian = ois.readInt();
                vidCloseIterations = ois.readInt();
                vidErodeIterations = ois.readInt();
                vidDilateIterations = ois.readInt();
                vidSubtractorThreshold = ois.readInt();
                vidMinContourArea = ois.readInt();
                vidMaxMergedContourArea = ois.readInt();
                vidAspectRatioLow = ois.readDouble();
                vidAspectRatioHigh = ois.readDouble();
                vidUnpluggingThreshold = ois.readDouble();

                return true;
            }
        } catch (IOException e) {
            throw new LeidenheitException("Error while deserialization", e);
        }
    }
}
