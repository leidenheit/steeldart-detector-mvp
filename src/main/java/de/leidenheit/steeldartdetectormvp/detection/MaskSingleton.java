package de.leidenheit.steeldartdetectormvp.detection;

import de.leidenheit.steeldartdetectormvp.FxUtil;
import lombok.Data;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@Data
public class MaskSingleton implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static MaskSingleton instance;

    // ignored by serialization
    public transient Mat baseImageBGR = new Mat();
    // ignored by serialization
    public transient Mat baseImageGray = new Mat();
    // ignored by serialization
    public transient Mat errorImage = null;
    // ignored by serialization
    public transient Mat segments;

    public double scaleFactor = 1d;

    public int greenMaskThresholdValue = 16;
    public int greenMaskGaussianValue = 1;
    public int greenMaskMorphDilateValue = 1;
    public int greenMaskMorphErodeValue = 2;
    public int greenMaskMorphCloseValue = 1;
    public transient Mat greenMask = null;

    public int redMaskThresholdValue = 50;
    public int redMaskGaussianValue = 5;
    public int redMaskMorphDilateValue = 1;
    public int redMaskMorphErodeValue = 2;
    public int redMaskMorphCloseValue = 1;
    public transient Mat redMask = null;

    public int multipliersMaskMorphDilateValue = 1;
    public transient Mat multipliersMask = null;

    public int multiRingsMaskMorphCloseValue = 3;
    public transient Mat multiRingsMask = null;

    public int dartboardMaskMorphDilateIterationsValue = 5;
    public int dartboardMaskMorphErodeIterationsValue = 11;
    public transient Mat dartboardMask = null;

    public transient Mat singleMask = null;
    public transient Mat doubleMask = null;
    public transient Mat tripleMask = null;
    public transient Mat outerBullMask = null;
    public transient Mat innerBullMask = null;
    public transient Mat missMask = null;

    public int segmentsMaskLineCandidateCannyGaussian = 1;
    public int segmentsMaskLineCandidateDilateKernelSize = 4;
    public int segmentsMaskLineGroupTolerance = 5;
    public int segmentsMaskCannyThreshold1 = 400;
    public int segmentsMaskCannyThreshold2 = 420;
    public int segmentsMaskHoughThreshold = 210;
    public int segmentsMaskHoughMinLineLength = 160;
    public int segmentsMaskHoughMaxLineGap = 80;

    // TODO consider serializing center point too...

    public List<SegmentData> segmentDataList = new ArrayList<>();

    // ignored by serialization
    private transient final List<String> debugList = new ArrayList<>();

    // ignored by serialization
    public transient Point coordinateOfSegment6 = new Point(0, 0);

    private MaskSingleton() throws URISyntaxException {
        // hide constructor
        errorImage = FxUtil.retrieveResourceAsMat("images/placeholder", "error.jpg");
    }

    public static MaskSingleton getInstance() {
        if (instance == null) {
            try {
                instance = new MaskSingleton();
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        return instance;
    }

    public boolean serialize(final String fileName) throws LeidenheitException {
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            try (ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                // var bos = new ByteArrayOutputStream();
                //            var oos = new ObjectOutputStream(fos);

                // scale factor configuration
                oos.writeDouble(scaleFactor);

                // green mask configuration
                oos.writeInt(greenMaskThresholdValue);
                oos.writeInt(greenMaskGaussianValue);
                oos.writeInt(greenMaskMorphDilateValue);
                oos.writeInt(greenMaskMorphErodeValue);
                oos.writeInt(greenMaskMorphCloseValue);
                // red mask configuration
                oos.writeInt(redMaskThresholdValue);
                oos.writeInt(redMaskGaussianValue);
                oos.writeInt(redMaskMorphDilateValue);
                oos.writeInt(redMaskMorphErodeValue);
                oos.writeInt(redMaskMorphCloseValue);
                // dartboard mask configuration
                oos.writeInt(dartboardMaskMorphDilateIterationsValue);
                oos.writeInt(dartboardMaskMorphErodeIterationsValue);
                // multipliers mask configuration
                oos.writeInt(multipliersMaskMorphDilateValue);
                // multi rings mask configuration
                oos.writeInt(multiRingsMaskMorphCloseValue);
                // segments mask configuration
                oos.writeInt(segmentsMaskLineCandidateCannyGaussian);
                oos.writeInt(segmentsMaskLineCandidateDilateKernelSize);
                oos.writeInt(segmentsMaskLineGroupTolerance);
                oos.writeInt(segmentsMaskCannyThreshold1);
                oos.writeInt(segmentsMaskCannyThreshold2);
                oos.writeInt(segmentsMaskHoughThreshold);
                oos.writeInt(segmentsMaskHoughMinLineLength);
                oos.writeInt(segmentsMaskHoughMaxLineGap);

                // mat
                writeMat(oos, greenMask);
                writeMat(oos, redMask);
                writeMat(oos, multipliersMask);
                writeMat(oos, multiRingsMask);
                writeMat(oos, dartboardMask);
                writeMat(oos, singleMask);
                writeMat(oos, doubleMask);
                writeMat(oos, tripleMask);
                writeMat(oos, outerBullMask);
                writeMat(oos, innerBullMask);
                writeMat(oos, missMask);

                // determined segment angles
                oos.writeObject(segmentDataList);

                return true;
            }
        } catch (IOException e) {
            throw new LeidenheitException("Error while serialization", e);
        }
    }

    public boolean deserialize(final String fileName) throws LeidenheitException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName))) {
            // scale factor configuration
            scaleFactor = ois.readDouble();

            // green mask configuration
            greenMaskThresholdValue = ois.readInt();
            greenMaskGaussianValue = ois.readInt();
            greenMaskMorphDilateValue = ois.readInt();
            greenMaskMorphErodeValue = ois.readInt();
            greenMaskMorphCloseValue = ois.readInt();
            // red mask configuration
            redMaskThresholdValue = ois.readInt();
            redMaskGaussianValue = ois.readInt();
            redMaskMorphDilateValue = ois.readInt();
            redMaskMorphErodeValue = ois.readInt();
            redMaskMorphCloseValue = ois.readInt();
            // dartboard mask configuration
            dartboardMaskMorphDilateIterationsValue = ois.readInt();
            dartboardMaskMorphErodeIterationsValue = ois.readInt();
            // multipliers mask configuration
            multipliersMaskMorphDilateValue = ois.readInt();
            // multi rings mask configuration
            multiRingsMaskMorphCloseValue = ois.readInt();
            // segments mask configuration
            segmentsMaskLineCandidateCannyGaussian = ois.readInt();
            segmentsMaskLineCandidateDilateKernelSize = ois.readInt();
            segmentsMaskLineGroupTolerance = ois.readInt();
            segmentsMaskCannyThreshold1 = ois.readInt();
            segmentsMaskCannyThreshold2 = ois.readInt();
            segmentsMaskHoughThreshold = ois.readInt();
            segmentsMaskHoughMinLineLength = ois.readInt();
            segmentsMaskHoughMaxLineGap = ois.readInt();

            // this way the deserialization works
            greenMask = readMat(ois);
            redMask = readMat(ois);
            multipliersMask = readMat(ois);
            multiRingsMask = readMat(ois);
            dartboardMask = readMat(ois);
            singleMask = readMat(ois);
            doubleMask = readMat(ois);
            tripleMask = readMat(ois);
            outerBullMask = readMat(ois);
            innerBullMask = readMat(ois);
            missMask = readMat(ois);

            // determined segment angles
            segmentDataList = (List<SegmentData>) ois.readObject();

            return true;
        } catch (IOException | ClassNotFoundException e) {
            throw new LeidenheitException("Error while deserialization", e);
        }
    }

    private byte[] serializeMat(final Mat matToSerialize) {
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".jpg", matToSerialize, matOfByte);
        return matOfByte.toArray();
    }

    private Mat deserializeMat(final byte[] serializedMat) {
        return Imgcodecs.imdecode(new MatOfByte(serializedMat), Imgcodecs.IMREAD_UNCHANGED);
    }

    private void writeMat(final ObjectOutputStream oos, final Mat mat) throws IOException {
        // that way the serialization works
        byte[] maskSerialized = serializeMat(mat);
        int maskLen = maskSerialized.length;
        oos.writeInt(maskLen);
        oos.write(maskSerialized);
    }

    private Mat readMat(final ObjectInputStream ois) throws IOException {
        // this way the deserialization works
        int maskLen = ois.readInt();
        byte[] maskBytes = ois.readNBytes(maskLen);
        return deserializeMat(maskBytes);
    }
}
