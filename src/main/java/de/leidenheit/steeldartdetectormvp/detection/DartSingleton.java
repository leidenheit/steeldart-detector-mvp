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

    public static int estimateSegmentByPoint(final Point dartboardCenter, final Point point) {
        double angle = Detection.calculateAngle(dartboardCenter, point);
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

    public static double calculateMean(final List<Double> data) {
        double mean = 0d;
        for (double num : data) {
            mean += num;
        }
        return mean / data.size();
    }
    public static double calculateVariance(final List<Double> data, double mean) {
        double variance = 0d;
        for (double num : data) {
            variance += Math.pow(num - mean, 2);
        }
        return variance / data.size();
    }

    // Method to perform Bayesian estimation
    public static double bayesianEstimate(double priorMean, double priorVariance, double measurement, double measurementVariance) {
        double posteriorVariance = 1 / ((1 / priorVariance) + (1 / measurementVariance));
        double posteriorMean = posteriorVariance * ((priorMean / priorVariance) + (measurement / measurementVariance));
        return posteriorMean;
    }
    public static Point estimateCoveredArrowTipForSegment(final int segmentValue, final Point arrowMassCenter, final double arrowAngle, final double arrowDistance) {
        Segment segment = Arrays.stream(Segment.values())
                .filter(s -> s.getValue() == segmentValue)
                .findFirst()
                .orElseThrow(() -> new LeidenheitException(format("segment not found for value %s", segmentValue)));

        // estimation
        List<Double> meanAngles = new ArrayList<>();
        var anglesCurrent = DartSingleton.getInstance().arrowAnglesBySegment.get(segment.getValue());
        var anglesPrevious = DartSingleton.getInstance().arrowAnglesBySegment.get(segment.getPrevious().getValue());
        var anglesNext = DartSingleton.getInstance().arrowAnglesBySegment.get(segment.getNext().getValue());
        if (anglesCurrent != null) {
            meanAngles.add(calculateMean(anglesCurrent));
        }
        if (anglesPrevious != null) {
            meanAngles.add(calculateMean(anglesPrevious));
        }
        if (anglesNext != null) {
            meanAngles.add(calculateMean(anglesNext));
        }

        List<Double> meanEuclideanDistances = new ArrayList<>();
        var euclideanDistancesCurrent = DartSingleton.getInstance().euclideanDistancesBySegment.get(segment.getValue());
        var euclideanDistancesPrevious = DartSingleton.getInstance().euclideanDistancesBySegment.get(segment.getPrevious().getValue());
        var euclideanDistancesNext = DartSingleton.getInstance().euclideanDistancesBySegment.get(segment.getNext().getValue());
        if (euclideanDistancesCurrent != null) {
            meanEuclideanDistances.add(calculateMean(euclideanDistancesCurrent));
        }
        if (euclideanDistancesPrevious != null) {
            meanEuclideanDistances.add(calculateMean(euclideanDistancesPrevious));
        }
        if (euclideanDistancesNext != null) {
            meanEuclideanDistances.add(calculateMean(euclideanDistancesNext));
        }

        double meanArrowAngle = 0;
        double meanEuclideanDistance = 0;
        if (meanAngles.isEmpty() || meanEuclideanDistances.isEmpty()) {
            var distances = new ArrayList<Double>();
            DartSingleton.getInstance().euclideanDistancesBySegment.forEach((key, value) -> distances.addAll(value));
            var angles = new ArrayList<Double>();
            DartSingleton.getInstance().arrowAnglesBySegment.forEach((key, value) -> angles.addAll(value));
            meanEuclideanDistance = calculateMean(distances);
            meanArrowAngle = calculateMean(angles);
        } else {
            for (double mean : meanAngles) {
                meanArrowAngle += mean;
            }
            meanArrowAngle = meanArrowAngle / meanAngles.size();
            for (double distance : meanEuclideanDistances) {
                meanEuclideanDistance += distance;
            }
            meanEuclideanDistance = meanEuclideanDistance / meanEuclideanDistances.size();
        }


        double estimatedX = arrowMassCenter.x + meanEuclideanDistance * Math.cos(Math.toRadians(meanArrowAngle));
        double estimatedY = arrowMassCenter.y + meanEuclideanDistance * Math.sin(Math.toRadians(meanArrowAngle));




        /*
            priorMean = meanBySegment
                if priorMean == null
                    priorMean = meanOfAllSegments

           priorVariance = varianceBySegment
                if priorVariance == null
                    priorVariance = varianceOfAllSegments
         */
        var angles = DartSingleton.getInstance().arrowAnglesBySegment.get(segment.getValue());
        if (angles == null) {
            angles = new ArrayList<>();
            ArrayList<Double> finalAngles = angles;
            DartSingleton.getInstance().arrowAnglesBySegment.forEach((key, value) -> finalAngles.addAll(value));
        }
        // we have now the prior data for angles; let's calculcate mean and variance
        var angleMean = calculateMean(meanAngles);
//        var angleVariance = calculateVariance(medianAngles, angleMean);
        // read covered data
//        var coveredArrowAngle = arrowAngle;
//        var coveredArrowAngleVariance = 1d; // TODO fixed value io?
        // estimation
//        var estimatedAngle = bayesianEstimate(angleMean, angleVariance, coveredArrowAngle, coveredArrowAngleVariance);
//        var distances = DartSingleton.getInstance().euclideanDistancesBySegment.get(segment.getValue());
//        if (distances == null) {
//            distances = new ArrayList<>();
//            ArrayList<Double> finalDistances = distances;
//            DartSingleton.getInstance().euclideanDistancesBySegment.forEach((key, value) -> finalDistances.addAll(value));
//        }
        // we have now the prior data for distances; let's calculcate mean and variance
        var distanceMean = calculateMean(meanEuclideanDistances);
        var distanceVariance = calculateVariance(meanEuclideanDistances, distanceMean);
        // read covered data
        var coveredArrowDistance = Detection.calculateEuclideanDistance(arrowMassCenter, new Point(estimatedX, estimatedY));
        var coveredArrowDistanceVariance = 1d; // TODO fixed value io?
        // estimation
        var estimatedDistance = bayesianEstimate(distanceMean, distanceVariance, coveredArrowDistance, coveredArrowDistanceVariance);

        if (!Double.isNaN(estimatedDistance)) {
            estimatedX = arrowMassCenter.x + (estimatedDistance /*+ estimatedDistance * 0.1*/) * Math.cos(Math.toRadians(angleMean));
            estimatedY = arrowMassCenter.y + (estimatedDistance /*+ estimatedDistance * 0.1*/) * Math.sin(Math.toRadians(angleMean));
        }
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
