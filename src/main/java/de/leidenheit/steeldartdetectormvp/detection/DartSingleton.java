package de.leidenheit.steeldartdetectormvp.detection;

import de.leidenheit.steeldartdetectormvp.FxUtil;
import lombok.Getter;
import lombok.Setter;
import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.io.*;
import java.net.URISyntaxException;
import java.util.*;

import static java.lang.String.format;

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
    public final transient Map<Integer, ArrayList<Double>> euclideanDistancesBySegment = new HashMap<>();
    public final transient Map<Integer, ArrayList<Double>> arrowAnglesBySegment = new HashMap<>();
    // TODO persist segment median euclidean distance and median angle

    public static int estimateSegmentByArrowMassCenter(final Point dartboardCenter, final Point arrowMassCenter) {
        double angle = Detection.calculateAngle(dartboardCenter, arrowMassCenter);
        var segment = MaskSingleton.getInstance().valueAngleRanges.getValueAngleRangeMap().entrySet().stream()
                .filter(entry -> {
                    var angleToUse = (angle + 360) % 360;
                    var min = (entry.getKey().minValue() + 360) % 360;
                    var max = (entry.getKey().maxValue() + 360) % 360;

                    return min <= max ?
                            angleToUse >= min && angleToUse <= max
                            : angleToUse >= min || angleToUse <= max;
                })
                .findFirst()
                .orElseThrow(() -> new LeidenheitException("Not able to determine segment for arrow tip"));
        return segment.getValue();
    }
    public static Point estimateCoveredArrowTipForSegment(final int segmentValue, final Point arrowMassCenter) {
        Segment segment = Arrays.stream(Segment.values())
                .filter(s -> s.getValue() == segmentValue)
                .findFirst()
                .orElseThrow(() -> new LeidenheitException(format("segment not found for value %s", segmentValue)));

        // estimation
        List<Double> medianAngles = new ArrayList<>();
        var anglesCurrent = DartSingleton.getInstance().arrowAnglesBySegment.get(segment.getValue());
        var anglesPrevious = DartSingleton.getInstance().arrowAnglesBySegment.get(segment.getPrevious().getValue());
        var anglesNext = DartSingleton.getInstance().arrowAnglesBySegment.get(segment.getNext().getValue());
        if (anglesCurrent != null) {
            medianAngles.add(Detection.calcMedian(anglesCurrent));
        }
        if (anglesPrevious != null) {
            medianAngles.add(Detection.calcMedian(anglesPrevious));
        }
        if (anglesNext != null) {
            medianAngles.add(Detection.calcMedian(anglesNext));
        }
        double meanArrowAngle = 0;
        for (double medianAngle : medianAngles) {
            meanArrowAngle += medianAngle;
        }
        meanArrowAngle = meanArrowAngle / medianAngles.size();


        List<Double> medianEuclideanDistances = new ArrayList<>();
        var euclideanDistancesCurrent = DartSingleton.getInstance().euclideanDistancesBySegment.get(segment.getValue());
        var euclideanDistancesPrevious = DartSingleton.getInstance().euclideanDistancesBySegment.get(segment.getPrevious().getValue());
        var euclideanDistancesNext = DartSingleton.getInstance().euclideanDistancesBySegment.get(segment.getNext().getValue());
        if (euclideanDistancesCurrent != null) {
            medianEuclideanDistances.add(Detection.calcMedian(euclideanDistancesCurrent));
        }
        if (euclideanDistancesPrevious != null) {
            medianEuclideanDistances.add(Detection.calcMedian(euclideanDistancesPrevious));
        }
        if (euclideanDistancesNext != null) {
            medianEuclideanDistances.add(Detection.calcMedian(euclideanDistancesNext));
        }
        double meanEuclideanDistance = 0;
        for (double medianEuclideanDistance : medianEuclideanDistances) {
            meanEuclideanDistance += medianEuclideanDistance;
        }
        meanEuclideanDistance = meanEuclideanDistance / medianEuclideanDistances.size();


        double estimatedX = arrowMassCenter.x + meanEuclideanDistance * Math.cos(Math.toRadians(meanArrowAngle));
        double estimatedY = arrowMassCenter.y + meanEuclideanDistance * Math.sin(Math.toRadians(meanArrowAngle));
        // override
        return new Point(estimatedX, estimatedY);
    }



    // scale factor configuration
    public double scaleFactor = 1d;

    public int vidGaussian = 5;
    public int vidCloseIterations = 1;
    public int vidErodeIterations = 1;
    public int vidDilateIterations = 1;
    public int vidSubtractorThreshold = 75;
    public int vidMinContourArea = 100;
    public int vidMaxMergedContourArea = 15_000*1000; // TODO AVa fix this
    public double vidAspectRatioLow = 0;
    public double vidAspectRatioHigh = 3;
    public double vidUnpluggingThreshold = 15_000;

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
