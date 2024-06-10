package de.leidenheit.steeldartdetectormvp.detection;

import javafx.util.Pair;
import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.text.MessageFormat;
import java.util.*;

import static java.lang.String.format;

public class Detection {

    /**
     * Presents an image for debugging purposes.
     *
     * @param matImage
     * @param windowName
     */
    public static void debugShowImage(final Mat matImage, final String windowName) {
        HighGui.imshow(windowName, matImage);
        HighGui.waitKey(0);
        HighGui.destroyAllWindows();
    }

    /**
     * Extracts the red mask of a dartboard.
     *
     * @param bgrImage              - dartboard image as bgr
     * @param grayImage             - dartboard image as gray
     * @param threshold             recommended: 50
     * @param gaussian              recommended: 5
     * @param morphErodeIterations  recommended: 1
     * @param morphDilateIterations recommended: 2
     * @param morphCloseIterations  recommended: 1
     * @return {@link Mat}
     */
    public static Mat extractMaskRed(
            final Mat bgrImage,
            final Mat grayImage,
            final double threshold,
            final int gaussian,
            final int morphErodeIterations,
            final int morphDilateIterations,
            final int morphCloseIterations) throws LeidenheitException {
        Mat resultMask = new Mat();
        Mat subtractedRegionRed = new Mat();
        Mat regionRedThreshold = new Mat();
        List<Mat> regionsBGR = new ArrayList<>();
        try {
            Core.split(bgrImage, regionsBGR); // returns colors in BGR
            Mat regionRed = regionsBGR.get(2); // red
            Core.subtract(regionRed, grayImage, subtractedRegionRed);
            // reduce noises
            Imgproc.GaussianBlur(subtractedRegionRed, subtractedRegionRed, new Size(gaussian, gaussian), 0);
            // grab red contents
            Imgproc.threshold(subtractedRegionRed, regionRedThreshold, threshold, 255, Imgproc.THRESH_BINARY);
            // morph mask for optimization
            Imgproc.morphologyEx(regionRedThreshold, regionRedThreshold, Imgproc.MORPH_ERODE,
                    Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2)), new Point(-1, -1), morphErodeIterations);
            Imgproc.morphologyEx(regionRedThreshold, regionRedThreshold, Imgproc.MORPH_DILATE,
                    Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2)), new Point(-1, -1), morphDilateIterations);
            Imgproc.morphologyEx(regionRedThreshold, resultMask, Imgproc.MORPH_CLOSE,
                    Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5)), new Point(-1, -1), morphCloseIterations);
            System.out.println("Extraction of red mask completed.");

            subtractedRegionRed.release();
            regionRedThreshold.release();
            regionRed.release();
            return resultMask;
        } catch (Exception e) {
            throw new LeidenheitException("Error while extracting red mask", e);
        }
    }

    /**
     * Extracts the green mask of a dartboard.
     *
     * @param bgrImage              - dartboard image as bgr
     * @param grayImage             - dartboard image as gray
     * @param threshold             recommended: 16
     * @param gaussian              recommended: 1
     * @param morphErodeIterations  recommended: 1
     * @param morphDilateIterations recommended: 2
     * @param morphCloseIterations  recommended: 1
     * @return {@link Mat}
     */
    public static Mat extractMaskGreen(
            final Mat bgrImage,
            final Mat grayImage,
            final double threshold,
            final int gaussian,
            final int morphErodeIterations,
            final int morphDilateIterations,
            final int morphCloseIterations) throws LeidenheitException {
        try {
            List<Mat> regionsBGR = new ArrayList<>();
            Core.split(bgrImage, regionsBGR); // returns colors in BGR
            Mat regionGreen = regionsBGR.get(1); // green
            Mat subtractedRegionGreen = new Mat();
            Core.subtract(regionGreen, grayImage, subtractedRegionGreen);
            Mat regionGreenThreshold = new Mat();
            // reduce noises
            Imgproc.GaussianBlur(subtractedRegionGreen, subtractedRegionGreen, new Size(gaussian, gaussian), 0);
            // grab green contents
            Imgproc.threshold(subtractedRegionGreen, regionGreenThreshold, threshold, 255, Imgproc.THRESH_BINARY);
            // morph mask for optimization
            Mat resultMask = new Mat();
            Imgproc.morphologyEx(regionGreenThreshold, regionGreenThreshold, Imgproc.MORPH_ERODE,
                    Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2)), new Point(-1, -1), morphErodeIterations);
            Imgproc.morphologyEx(regionGreenThreshold, regionGreenThreshold, Imgproc.MORPH_DILATE,
                    Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2)), new Point(-1, -1), morphDilateIterations);
            Imgproc.morphologyEx(regionGreenThreshold, resultMask, Imgproc.MORPH_CLOSE,
                    Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5)), new Point(-1, -1), morphCloseIterations);
            System.out.println("Extraction of green mask completed.");

            subtractedRegionGreen.release();
            regionGreenThreshold.release();

            return resultMask;
        } catch (Exception e) {
            throw new LeidenheitException("Error while extracting green mask", e);
        }
    }

    /**
     * Extracts the multiplier mask of a dartboard.
     *
     * @param redMask
     * @param greenMask
     * @param morphDilateIterations recommended: 1
     * @return {@link Mat}
     */
    public static Mat extractMaskMultipliers(
            final Mat redMask,
            final Mat greenMask,
            final int morphDilateIterations) throws LeidenheitException {
        try {
            Mat resultMask = new Mat();
            Mat multipliers = new Mat();
            // combine green and red masks
            Core.add(redMask, greenMask, multipliers);
            // morph mask for optimization
            Imgproc.morphologyEx(multipliers, resultMask, Imgproc.MORPH_DILATE,
                    Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3)), new Point(-1, -1), morphDilateIterations);
            System.out.println("Extraction of multiplier mask completed.");

            multipliers.release();

            return resultMask;
        } catch (Exception e) {
            throw new LeidenheitException("error while extracting multiplier mask", e);
        }
    }

    /**
     * Extracts the multirings mask of a dartboard.
     *
     * @param multipliersMask
     * @param morphCloseIterations recommended: 3
     * @return {@link Mat}
     */
    public static Mat extractMaskMultiRings(final Mat multipliersMask, final int morphCloseIterations)
            throws LeidenheitException {
        try {
            final var resultMask = new Mat();
            Imgproc.morphologyEx(multipliersMask, resultMask, Imgproc.MORPH_CLOSE,
                    Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(3, 3)), new Point(-1, -1), morphCloseIterations);
            System.out.println("Extraction of multi ring mask completed.");
            return resultMask;
        } catch (Exception e) {
            throw new LeidenheitException("Error while extracting multirings mask", e);
        }
    }

    /**
     * Extracts the dartboard mask of a dartboard.
     *
     * @param multiRingsMask
     * @param morphDilateIterations recommended: 5
     * @param morphErodeIterations  recommended: 11
     * @return {@link Mat}
     */
    public static Mat extractMaskDartboard(final Mat multiRingsMask,
                                           final int morphDilateIterations,
                                           final int morphErodeIterations)
            throws LeidenheitException {
        try {
            Mat resultMask = new Mat();
            Mat temporaryDartboardMask = imfillHoles(multiRingsMask);

            Imgproc.morphologyEx(temporaryDartboardMask, resultMask, Imgproc.MORPH_DILATE,
                    Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(5, 5)), new Point(-1, -1), morphDilateIterations);
            Imgproc.morphologyEx(resultMask, resultMask, Imgproc.MORPH_ERODE,
                    Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(3, 3)), new Point(-1, -1), morphErodeIterations);
            System.out.println("Extraction of dartboard mask completed.");

            temporaryDartboardMask.release();

            return resultMask;
        } catch (Exception e) {
            throw new LeidenheitException("Error while extracting dartboard mask", e);
        }
    }

    /**
     * Extracts the miss mask of a dartboard.
     *
     * @param dartboardMask
     * @return {@link Mat}
     */
    public static Mat extractMaskMiss(final Mat dartboardMask) throws LeidenheitException {
        try {
            Mat resultMask = new Mat();
            Core.bitwise_not(dartboardMask, resultMask);
            System.out.println("Extraction of miss mask completed.");
            return resultMask;
        } catch (Exception e) {
            throw new LeidenheitException("Error while extracting dartboard mask", e);
        }
    }

    /**
     * Extracts the single mask of a dartboard.
     *
     * @param dartboardMask
     * @param multiRingsMask
     * @return {@link Mat}
     */
    public static Mat extractMaskSingle(final Mat dartboardMask, final Mat multiRingsMask)
            throws LeidenheitException {
        try {
            Mat tempSingleMask = new Mat();
            Core.subtract(dartboardMask, multiRingsMask, tempSingleMask);
            System.out.println("Extraction of single mask completed.");
            return tempSingleMask;
        } catch (Exception e) {
            throw new LeidenheitException("Error while extracting single mask", e);
        }
    }

    /**
     * Extracts the double mask of a dartboard.
     *
     * @param dartboardMask
     * @param singleMask
     * @return {@link Mat}
     */
    public static Mat extractMaskDouble(final Mat dartboardMask, final Mat singleMask)
            throws LeidenheitException {
        try {
            Mat resultMask = new Mat();
            Mat singleFilledMask = imfillHoles(singleMask);
            Core.subtract(dartboardMask, singleFilledMask, resultMask);
            System.out.println("Extraction of double mask completed.");

            singleFilledMask.release();
            return resultMask;
        } catch (Exception e) {
            throw new LeidenheitException("Error while extracting double mask", e);
        }
    }

    /**
     * Extracts the triple mask of a dartboard.
     *
     * @param dartboardMask
     * @param doubleMask
     * @param singleMask
     * @return {@link Mat}
     */
    public static Mat extractMaskTriple(final Mat dartboardMask, final Mat doubleMask, final Mat singleMask)
            throws LeidenheitException {
        try {
            Mat resultMask = new Mat();
            // combine double and triple masks
            Mat doubleAndSingleMask = new Mat();
            Core.add(singleMask, doubleMask, doubleAndSingleMask);
            // subtract them from dartboard mask
            Mat innerRingMask = new Mat();
            Core.subtract(dartboardMask, doubleAndSingleMask, innerRingMask);

            Mat floodFillImage = innerRingMask.clone();
            Mat mask = new Mat();
            Imgproc.floodFill(
                    floodFillImage,
                    mask,
                    new Point(0, 0),
                    Scalar.all(255));
            Mat floodFillImageInverse = new Mat();
            Core.bitwise_not(floodFillImage, floodFillImageInverse);
            Mat maskInnerRing = new Mat();
            Imgproc.floodFill(
                    floodFillImageInverse,
                    maskInnerRing,
                    new Point(1, 1),
                    Scalar.all(255));
            Core.bitwise_not(floodFillImageInverse, innerRingMask);
            Mat doubleAndSingleMask2 = new Mat();
            Core.add(singleMask, doubleMask, doubleAndSingleMask2);
            Mat triple = new Mat();
            Core.subtract(dartboardMask, doubleAndSingleMask2, triple);
            Core.subtract(triple, innerRingMask, resultMask);

            System.out.println("Extraction of triple mask completed.");

            doubleAndSingleMask.release();
            doubleAndSingleMask2.release();
            triple.release();
            maskInnerRing.release();
            floodFillImage.release();
            floodFillImageInverse.release();
            return resultMask;
        } catch (Exception e) {
            throw new LeidenheitException("Error while extracting triple mask", e);
        }
    }

    /**
     * Extracts the outer bull mask of a dartboard.
     *
     * @param multiRingsMask
     * @param doubleMask
     * @param tripleMask
     * @param greenMask
     * @return {@link Mat}
     */
    public static Mat extractMaskOuterBull(final Mat multiRingsMask, final Mat doubleMask, final Mat tripleMask, final Mat greenMask)
            throws LeidenheitException {
        try {
            Mat resultMask = new Mat();
            Mat doubleAndTripleMask = new Mat();
            Core.add(doubleMask, tripleMask, doubleAndTripleMask);
            Mat tempMask = new Mat();
            Core.subtract(multiRingsMask, doubleAndTripleMask, tempMask);
            Core.bitwise_and(tempMask, greenMask, resultMask);
            System.out.println("Extraction of outer bull mask completed.");

            doubleAndTripleMask.release();
            tempMask.release();
            return resultMask;
        } catch (Exception e) {
            throw new LeidenheitException("Error while extracting outer bull mask", e);
        }
    }

    /**
     * Extracts the inner bull mask of a dartboard.
     *
     * @param multiRingsMask
     * @param doubleMask
     * @param tripleMask
     * @param redMask
     * @return {@link Mat}
     */
    public static Mat extractMaskInnerBull(final Mat multiRingsMask, final Mat doubleMask, final Mat tripleMask, final Mat redMask)
            throws LeidenheitException {
        try {
            Mat resultMask = new Mat();
            Mat doubleAndTripleMask = new Mat();
            Core.add(doubleMask, tripleMask, doubleAndTripleMask);
            Mat tempMask = new Mat();
            Core.subtract(multiRingsMask, doubleAndTripleMask, tempMask);
            Core.bitwise_and(tempMask, redMask, resultMask);
            System.out.println("Extraction of inner bull mask completed.");

            doubleAndTripleMask.release();
            tempMask.release();
            return resultMask;
        } catch (Exception e) {
            throw new LeidenheitException("Error while extracting inner bull mask", e);
        }
    }

    /**
     * Determines if a given line intersects a given mask.
     *
     * @param lineBegin
     * @param lineEnd
     * @param mask
     * @return true, when line intersects the mask. Otherwise, false.
     */
    public static boolean lineIntersectsMask(final Point lineBegin, final Point lineEnd, final Mat mask) throws LeidenheitException {
        try {
            Mat lineMask = Mat.zeros(mask.size(), mask.type());
            Imgproc.line(lineMask, lineBegin, lineEnd, new Scalar(255));
            Mat matOverlapsMask = new Mat();
            Core.bitwise_and(lineMask, mask, matOverlapsMask);
            int nonZeroCount = Core.countNonZero(matOverlapsMask);

            lineMask.release();
            matOverlapsMask.release();
            return nonZeroCount > 0;
        } catch (Exception e) {
            throw new LeidenheitException("Error while determining line intersection with mask", e);
        }
    }

    /**
     * Determines Dartboard Segments by HoughLinesP and Canny Edge Detector
     *
     * @param center
     * @param bgr
     * @param segments                      - result
     * @param maskInnerBull
     * @param maskDouble
     * @param maskTriple
     * @param maskMultiRings
     * @param cannyGaussian                 - recommended: 1
     * @param lineCandidateDilateKernelSize - recommended: 4 -> use higher value when fewer candidates are detected
     * @param lineGroupTolerance            - recommended: 5.0
     * @param cannyThreshold1               - recommend: 400
     * @param cannyThreshold1               - recommended: 420
     * @param houghThreshold                - recommended: 210
     * @param houghMinLineLength            - recommended: 160
     * @param houghMaxGap                   - recommended: 80
     * @return Angles for dartboard segments
     * @throws LeidenheitException
     */
    public static ValueAngleRanges determineDartboardSegments(
            final Point center,
            final Mat bgr,
            final Mat segments,
            final Mat maskInnerBull,
            final Mat maskDouble,
            final Mat maskTriple,
            final Mat maskMultiRings,
            final int cannyGaussian,
            final int lineCandidateDilateKernelSize,
            final double lineGroupTolerance,
            final int cannyThreshold1,
            final int cannyThreshold2,
            final int houghThreshold,
            final int houghMinLineLength,
            final int houghMaxGap,
            final Point clickPointOfSegment6
    ) throws LeidenheitException {
        try {
            List<List<Point>> lineCandidates = Detection.findLineCandidatesByCannyAndHoughLinesP(
                    bgr,
                    lineCandidateDilateKernelSize,
                    cannyThreshold1,
                    cannyThreshold2,
                    houghThreshold,
                    houghMinLineLength,
                    houghMaxGap);
            List<Line> lines = new ArrayList<>();
            for (List<Point> candidate : lineCandidates) {
                for (Point point : candidate) {
                    double angle = Detection.calculateAngle(center, point);
                    Line line = Line.builder()
                            .setCenter(center)
                            .setLinePoint(point)
                            .setAngle(angle)
                            .build();
                    boolean considerableLine = validateLineIntersections(
                            line, maskInnerBull, maskDouble, maskTriple, maskMultiRings);
                    if (considerableLine) {
                        lines.add(line);
                    }
                }
            }
            // System.out.printf("Amount of lines found: %s%n", lines.size());
            List<List<Line>> groupedSegments = groupLinesByTolerance(lines, lineGroupTolerance);
            List<Double> segmentLineAngles = groupedSegments.stream()
                    .map(groupedLine -> {
                        var groupAngles = groupedLine.stream().map(Line::getAngle).toList();
                        return calcMedian(groupAngles);
                    })
                    .toList();
            // debug
            //        for (final var line : segmentLines) {
            //            final var considerableLine = rgbMat.clone();
            //            Imgproc.line(segments, line.getCenter(), line.getLinePoint(), new Scalar(0, 255, 200), 2);
            // debugShowImage(considerableLine, "considerable_line");
            //        }
            ValueAngleRanges valueAngleRanges = ValueAngleRanges.getInstance();
            if (segmentLineAngles.size() == 20) {
                valueAngleRanges = associateValueAngleRanges(center, segmentLineAngles, clickPointOfSegment6);
                List<Map.Entry<ValueAngleRanges.ValueRange, Integer>> list = new ArrayList<>(valueAngleRanges.getValueAngleRangeMap().entrySet().stream().toList());
                list.sort(Comparator.comparingDouble(valueRangeIntegerEntry ->
                        valueRangeIntegerEntry.getKey().minValue()));
                for (var o : list) {
                    System.out.printf("\tsegment=%s, angles=%s\n", o.getValue(), o.getKey().toString());
                }
            } else {
                System.out.printf("\nWARNING: requires 20 segment lines but found %d", segmentLineAngles.size());

            }
            // horizontal line
            Imgproc.line(segments, new Point(0, center.y), new Point(1000, center.y), new Scalar(200, 200, 200), 1);
            // segment lines
            Scalar lineColor = segmentLineAngles.size() == 20 ? new Scalar(0, 0, 255) : new Scalar(0, 255, 255);
            for (double segmentLineAngle : segmentLineAngles) {
                var segmentLinePointX = center.x + 300 * Math.cos((segmentLineAngle * (Math.PI / 180)));
                var segmentLinePointY = center.y + 300 * Math.sin((segmentLineAngle * (Math.PI / 180)));
                Imgproc.line(segments, center, new Point(segmentLinePointX, segmentLinePointY), lineColor, 1);
            }
            valueAngleRanges.getValueAngleRangeMap().forEach((valueRange, integer) -> {
                var segment = ValueAngleRanges.getInstance().findValueByAngle(valueRange.maxValue()-0.1d);
                double estimatedX = center.x + 250 * Math.cos(Math.toRadians(valueRange.maxValue()-(18*0.33)));
                double estimatedY = center.y + 250 * Math.sin(Math.toRadians(valueRange.maxValue()-(18*0.33)));
                Point point = new Point(estimatedX, estimatedY);
                Imgproc.putText(segments, String.valueOf(segment), point, Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, lineColor, 2);
            });

            //        System.out.printf("valueAngleRanges: %s\n", valueAngleRanges.getValueAngleRangeMap().keySet().size());
            //        debugShowImage(considerableLines, "considerable lines");
            return valueAngleRanges;
        } catch (Exception e) {
            throw new LeidenheitException(
                    MessageFormat.format("Error while determining segments ({0})", e.getMessage()), e);
        }
    }

    /**
     * Determines if a given point intersects a given mask.
     *
     * @param point
     * @param mask
     * @return true, when point intersects the mask. Otherwise, false.
     */
    public static boolean pointIntersectsMask(final Point point, final Mat mask) throws LeidenheitException {
        try {
            if (point.inside(new Rect(0, 0, mask.cols(), mask.rows()))) {
                double[] pixel = mask.get((int) point.y, (int) point.x);
                return pixel[0] >= 252; // almost white
            } else {
                return false;
            }
        } catch (Exception e) {
            throw new LeidenheitException("Error while determining point intersects mask", e);
        }
    }

    /**
     * Determines the segment value of a given point.
     *
     * @param valueAngleRanges
     * @param angle
     * @param point
     * @param maskDartboard
     * @param maskInnerBull
     * @param maskOuterBull
     * @param maskTriple
     * @param maskDouble
     * @param maskSingle
     * @return Segment value
     */
    public static Pair<Integer, Mat> evaluatePoint(
            final ValueAngleRanges valueAngleRanges,
            final double angle,
            final Point point,
            final Mat maskDartboard,
            final Mat maskInnerBull,
            final Mat maskOuterBull,
            final Mat maskTriple,
            final Mat maskDouble,
            final Mat maskSingle
    ) throws LeidenheitException {
        boolean touchesDartboard = pointIntersectsMask(point, maskDartboard);
        boolean touchesInnerBull = pointIntersectsMask(point, maskInnerBull);
        boolean touchesOuterBull = pointIntersectsMask(point, maskOuterBull);
        boolean touchesTriple = pointIntersectsMask(point, maskTriple);
        boolean touchesDouble = pointIntersectsMask(point, maskDouble);
        boolean touchesSingle = pointIntersectsMask(point, maskSingle);
        if (!touchesDartboard) {
            System.out.printf("Hitpoint%s -> segment Out\n", point);
            return new Pair<>(0, maskDartboard);
        } else if (touchesInnerBull) {
            System.out.printf("Hitpoint%s -> segment Bullseye\n", point);
            return new Pair<>(50, maskInnerBull);
        } else if (touchesOuterBull) {
            System.out.printf("Hitpoint %s -> segment Bull\n", point);
            return new Pair<>(25, maskOuterBull);
        } else {
            try {
                final var segmentHit = valueAngleRanges.findValueByAngle(angle);
                if (touchesDouble) {
                    System.out.printf("Hitpoint %s -> segment D%d\n", point, segmentHit);
                    return new Pair<>(segmentHit * 2, maskDouble);
                } else if (touchesTriple) {
                    System.out.printf("Hitpoint %s -> segment T%d\n", point, segmentHit);
                    return new Pair<>(segmentHit * 3, maskTriple);
                } else if (touchesSingle) {
                    System.out.printf("Hitpoint %s -> segment %d\n", point, segmentHit);
                    return new Pair<>(segmentHit, maskSingle);
                } else {
                    System.out.printf("Hitpoint %s -> segment not determinable -> fallback: Single\n", point);
                    return new Pair<>(segmentHit, maskSingle);
                }
            } catch (RuntimeException e) {
                throw new LeidenheitException("Unexpected error", e);
            }
        }
    }

    /**
     * returns the center mass of the inner bull mask.
     *
     * @param innerBullMask
     * @return x & y coordinates of center mass
     */
    // TODO refactor semantics
    public static Point findCircleCenter(final Mat frame, final Mat innerBullMask) throws LeidenheitException {
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        try {
            Imgproc.findContours(innerBullMask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
            if (!contours.isEmpty()) {
                for (MatOfPoint contour : contours) {
                    if (Imgproc.contourArea(contour) >= 5.0) {
                        // calculate the center mass of this contour
                        Moments moments = Imgproc.moments(contour);
                        double centerX = moments.get_m10() / moments.get_m00();
                        double centerY = moments.get_m01() / moments.get_m00();
                        if (!Double.isNaN(centerX) && !Double.isNaN(centerY)) {
                            return new Point(centerX, centerY);
                        }
                    }
                }
            }
            throw new LeidenheitException("Expected center mass (x, y) to not be NaN", null);
        } finally {
            hierarchy.release();
        }
    }

    /**
     * Returns the angle of a point relative to a center point.
     *
     * @param center
     * @param point
     * @return Angle
     */
    public static double calculateAngle(Point center, Point point) {
        double angleCenterToPoint = Math.toDegrees(Math.atan2(point.y - center.y, point.x - center.x));
        if (angleCenterToPoint < 0) {
            angleCenterToPoint += 360;
        }
        return angleCenterToPoint;
    }

    private static ValueAngleRanges associateValueAngleRanges(final Point center, final List<Double> segmentLineAngles, final Point clickPointOfSegmentToStartWith) throws LeidenheitException {
        ValueAngleRanges result = ValueAngleRanges.getInstance();
        result.getValueAngleRangeMap().clear();
        List<Integer> segmentIndices = ValueAngleRanges.getInstance().segmentValueIndexes;

        // find the position of segment 6 (i)
        var angleOfSegment6 = calculateAngle(center, clickPointOfSegmentToStartWith);

        System.out.printf("associateValueAngleRanges: center=%s, segmentLinesAngles=%s, angleOfSegment=%.2f%n", center, segmentLineAngles.size(), angleOfSegment6);

        var sorted = segmentLineAngles.stream()
                .sorted(Comparator.comparingDouble(Double::doubleValue)).toList();

        var lineUpperBoundary = sorted.stream()
                .filter(lineAngle ->  lineAngle >= angleOfSegment6 || (360d-angleOfSegment6) <= 18)
                .findFirst()
                .orElseThrow();
        var lastLineIndex = segmentLineAngles.indexOf(lineUpperBoundary);
        var lineLowerBoundary = sorted.get((lastLineIndex - 1 + sorted.size()) % sorted.size()); // cyclic iteration
        System.out.printf("determine clicked segment 6: lastLineIndex=%s low=%.2f, high=%.2f%n", lastLineIndex, lineLowerBoundary, lineUpperBoundary);
        // add segment 6
        result.putValueForAngleRange(
                0,
                lineLowerBoundary,
                lineUpperBoundary);

        // iterate other indices
        for (int i = 1; i < segmentIndices.size(); i++) {
            lineUpperBoundary = sorted.get((lastLineIndex + 1) % sorted.size());
            lastLineIndex = sorted.indexOf(lineUpperBoundary);
            lineLowerBoundary = sorted.get((lastLineIndex - 1 + sorted.size()) % sorted.size()); // cyclic iteration
            System.out.printf("auto determine segment: segmentIndex=%s, segment=%s, lastLineIndex=%s, low=%.2f, high=%.2f%n", i, segmentIndices.get(i), lastLineIndex, lineLowerBoundary, lineUpperBoundary);
            // add segment
            result.putValueForAngleRange(
                    i,
                    lineLowerBoundary,
                    lineUpperBoundary);
        }
        return result;
    }

    /**
     * finds lines candidates by HoughLinesP
     *
     * @param gray
     * @param gaussian           - recommended: 1
     * @param dilateKernelSize   - recommend: 4 -> use higher value when too less segment lines are detected
     * @param cannyThreshold1    - recommend: 400
     * @param cannyThreshold1    - recommended: 420
     * @param houghThreshold     - recommended: 210
     * @param houghMinLineLength - recommended: 160
     * @param houghMaxGap        - recommended: 80
     * @return Line Points of candidate lines
     */
    private static List<List<Point>> findLineCandidatesByCannyAndHoughLinesP(
            final Mat bgr,
            final int dilateKernelSize,
            final int cannyThreshold1,
            final int cannyThreshold2,
            final int houghThreshold,
            final int houghMinLineLength,
            final int houghMaxGap
    ) throws LeidenheitException {
        try {
            Mat gray = new Mat();
            Imgproc.cvtColor(bgr, gray, Imgproc.COLOR_BGR2GRAY);
            Mat blurredFrame = new Mat();
            Imgproc.bilateralFilter(gray, blurredFrame, 5, 50, 50);

            Mat thres = new Mat();
            Imgproc.threshold(blurredFrame, thres, 0, 255, Imgproc.THRESH_BINARY_INV | Imgproc.THRESH_OTSU);

            Mat kernelDilate = Imgproc.getStructuringElement(Imgproc.MORPH_DILATE, new Size(dilateKernelSize, dilateKernelSize));
            Mat thresholdDilate = new Mat();
            Imgproc.dilate(
                    thres,
                    thresholdDilate,
                    kernelDilate);

            // canny edge detection
            Mat edges = new Mat();
            Imgproc.Canny(thresholdDilate, edges, cannyThreshold1, cannyThreshold2);

            // line detection
            Mat linesP = new Mat();
            Imgproc.HoughLinesP(edges, linesP, 1, Math.PI / 180, houghThreshold, houghMinLineLength, houghMaxGap);
            List<List<Point>> candidates = new ArrayList<>();
            for (int row = 0; row < linesP.rows(); row++) {
                double[] l = linesP.get(row, 0);
                Point lineBegin = new Point(l[0], l[1]);
                Point lineEnd = new Point(l[2], l[3]);
                candidates.add(List.of(lineBegin, lineEnd));
            }

            // TODO Debug
//            Mat x = bgr.clone();
//            for (int i = 0; i < linesP.rows(); i++) {
//                double[] l = linesP.get(i, 0);
//                Point pt1 = new Point(l[0], l[1]);
//                Point pt2 = new Point(l[2], l[3]);
//                Imgproc.line(x, pt1, pt2, new Scalar(0, 0, 255), 1);
//            }
//            debugShowImage(x, "");


            gray.release();
            thres.release();
            edges.release();
            kernelDilate.release();
            thresholdDilate.release();
            linesP.release();

            return candidates;
        } catch (Exception e) {
            throw new LeidenheitException("Error while line detection", e);
        }
    }

    private static boolean validateLineIntersections(final Line line, final Mat maskInnerBull, final Mat maskDouble, final Mat maskTriple, final Mat maskMultiRings) throws LeidenheitException {
        boolean intersectsBullseye = lineIntersectsMask(line.getCenter(), line.getLinePoint(), maskInnerBull);
        boolean intersectsDoubleRing = lineIntersectsMask(line.getCenter(), line.getLinePoint(), maskDouble);
        boolean intersectsTripleRing = lineIntersectsMask(line.getCenter(), line.getLinePoint(), maskTriple);
        boolean intersectsMultiRings = lineIntersectsMask(line.getCenter(), line.getLinePoint(), maskMultiRings);
        return intersectsBullseye && intersectsTripleRing && intersectsDoubleRing && intersectsMultiRings;
    }

    private static List<List<Line>> groupLinesByTolerance(final List<Line> lines, final double tolerance) {
        List<List<Line>> groupedSegments = new ArrayList<>();
        lines.sort(Comparator.comparingDouble(Line::getAngle));
        // group segments by mean value of angle
        double currentAngleSum = lines.get(0).getAngle();
        int groupSize = 1;

        for (int i = 1; i < lines.size(); i++) {
            double angle = lines.get(i).getAngle();
            if (Math.abs(angle - currentAngleSum / groupSize) <= tolerance) {
                // add to current group
                currentAngleSum += angle;
                groupSize++;
            } else {
                // new group
                List<Line> newGroup = new ArrayList<>(lines.subList(i - groupSize, i));
                groupedSegments.add(newGroup);
                currentAngleSum = angle;
                groupSize = 1;
            }
        }
        // add last group
        groupedSegments.add(new ArrayList<>(lines.subList(lines.size() - groupSize, lines.size())));

        // output
//        int gIndex = 1;
//        for (List<Line> group : groupedSegments) {
//            System.out.printf("Group[%s]:", gIndex);
//            for (Line segment : group) {
//                System.out.println("  Angle: " + segment.getAngle() + ", CenterX: " + segment.getLinePoint().x + ", CenterY: " + segment.getLinePoint().y);
//            }
//            gIndex++;
//        }

        return groupedSegments;
    }

    private static List<Line> calculateMeanLines(final List<List<Line>> lineGroups, final Point center) {
        List<Line> result = new ArrayList<>();
        for (List<Line> lineGroup : lineGroups) {
            double groupMeanAngle = 0;
            double groupMeanX = 0;
            double groupMeanY = 0;
            for (Line line : lineGroup) {
                groupMeanAngle += line.getAngle();
                groupMeanX += line.getLinePoint().x;
                groupMeanY += line.getLinePoint().y;
            }
            groupMeanAngle = groupMeanAngle / lineGroup.size();
            groupMeanX = groupMeanX / lineGroup.size();
            groupMeanY = groupMeanY / lineGroup.size();
            // System.out.printf("Group[%d] meanAngle=%.2f meanX=%.2f meanY=%.2f\n", gIndex2, groupMeanAngle, groupMeanX, groupMeanY);

            Line groupMeanLine = Line.builder()
                    .setCenter(center)
                    .setLinePoint(new Point(groupMeanX, groupMeanY))
                    .setAngle(groupMeanAngle)
                    .build();
            result.add(groupMeanLine);
        }
        return result;
    }

    private static Mat imfillHoles(final Mat imageToUse) {
        Mat dummy = imageToUse.clone();
        Mat mask = new Mat();
        Imgproc.floodFill(
                dummy,
                mask,
                new Point(0, 0),
                Scalar.all(255)
        );
        Mat floodFillInverse = new Mat();
        Core.bitwise_not(dummy, floodFillInverse);
        Mat filledImage = new Mat();
        Core.bitwise_or(imageToUse, floodFillInverse, filledImage);

        dummy.release();
        mask.release();
        floodFillInverse.release();

        return filledImage;
    }


    // TODO move to own file
    ///////////////////////////////////////////////////////////////
    // Dart Detection
    /**
     *
     * @param convexHull
     * @return
     */
    public static Pair<Double, Point[]> findArrowTip(final Mat frame, final MatOfPoint convexHull) {
        // contour mass center
        Moments moments = Imgproc.moments(convexHull);
        Point center = new Point(moments.get_m10() / moments.get_m00(), moments.get_m01() / moments.get_m00());

        // determine furthest point (tip)
        List<Point> contourPoints = convexHull.toList();
        Point arrowTip = findFurthestPoint(contourPoints, center);

        Point dartboardCenter = Detection.findCircleCenter(null, MaskSingleton.getInstance().innerBullMask);
        int segmentValueOfArrowMassCenter = DartSingleton.estimateSegmentByPoint(dartboardCenter, center);
        double euclideanDistance = calculateEuclideanDistance(center, arrowTip);
        double arrowAngle = calculateAngle(center, arrowTip);

        // determine if the arrow is covered and an arrow tip estimation should take place here
        if (shouldExtendArrowTip(segmentValueOfArrowMassCenter, euclideanDistance, arrowAngle)) {
            Point estimatedTip = DartSingleton.estimateCoveredArrowTipForSegment(segmentValueOfArrowMassCenter, center, arrowAngle, euclideanDistance);

//             TODO debugging
            Mat line = frame.clone();
            Imgproc.circle(line, estimatedTip, 3, new Scalar(0, 255, 0), -1);
            Imgproc.putText(line, "estimated", estimatedTip, Imgproc.FONT_HERSHEY_SIMPLEX, .75d, new Scalar(0), 2, Imgproc.LINE_AA);
            Imgproc.circle(line, center, 3, new Scalar(255, 0, 0), -1);
            Imgproc.putText(line, "center", center, Imgproc.FONT_HERSHEY_SIMPLEX, .75d, new Scalar(0), 2, Imgproc.LINE_AA);
            Imgproc.circle(line, arrowTip, 3, new Scalar(0, 0, 255), -1);
            Imgproc.putText(line, "tip", arrowTip, Imgproc.FONT_HERSHEY_SIMPLEX, .75d, new Scalar(0), 2, Imgproc.LINE_AA);
            Imgproc.line(line, center, estimatedTip, new Scalar(0, 255, 255), 2, Imgproc.LINE_AA);
             debugShowImage(line, "");
            line.release();

            // override
            arrowTip = estimatedTip;
        } else {
            int segmentValueOfArrowTip = DartSingleton.estimateSegmentByPoint(dartboardCenter, arrowTip);
            // handling for Euclidean distance
            DartSingleton.getInstance().euclideanDistancesBySegment.computeIfPresent(segmentValueOfArrowTip, (segmentValue, distances) -> {
                distances.add(euclideanDistance);
                return distances;
            });
            DartSingleton.getInstance().euclideanDistancesBySegment.computeIfAbsent(segmentValueOfArrowTip, segmentValue -> {
                return new ArrayList<>(List.of(euclideanDistance));
            });

            // handling for angle
            DartSingleton.getInstance().arrowAnglesBySegment.computeIfPresent(segmentValueOfArrowTip, (segmentValue, angles) -> {
                angles.add(arrowAngle);
                return angles;
            });
            DartSingleton.getInstance().arrowAnglesBySegment.computeIfAbsent(segmentValueOfArrowTip, segmentValue -> {
                return new ArrayList<>(List.of(arrowAngle));
            });
        }

        Rect boundingBox = Imgproc.boundingRect(convexHull);
        return new Pair<>(euclideanDistance, new Point[]{arrowTip, boundingBox.tl(), boundingBox.br(), center});
    }

    /**
     *
     * @param points
     * @param center
     * @return
     */
    private static Point findFurthestPoint(List<Point> points, Point center) {
        return points.stream()
                .max(Comparator.comparingDouble(p -> calculateEuclideanDistance(p, center)))
                .orElseThrow(() -> new IllegalArgumentException("List of points is empty"));
    }

    /**
     *
     * @param p1
     * @param p2
     * @return
     */
    public static double calculateEuclideanDistance(Point p1, Point p2) {
        return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }

    /**
     *
     * @param euclideanDistance
     * @return
     */
    private static boolean shouldExtendArrowTip(final int segmentValue, final double euclideanDistance, final double angle) {
        Segment segment = Arrays.stream(Segment.values())
                .filter(s -> s.getValue() == segmentValue)
                .findFirst()
                .orElseThrow(() -> new LeidenheitException(format("segment not found for value %s", segmentValue)));

//        List<Double> euclideanDistances = new ArrayList<>();
//        var euclideanDistancesCurrent = DartSingleton.getInstance().euclideanDistancesBySegment.get(segment.getValue());
//        var euclideanDistancesPrevious = DartSingleton.getInstance().euclideanDistancesBySegment.get(segment.getPrevious().getValue());
//        var euclideanDistancesNext = DartSingleton.getInstance().euclideanDistancesBySegment.get(segment.getNext().getValue());
//        if (euclideanDistancesCurrent != null) {
//            euclideanDistances.addAll(euclideanDistancesCurrent);
//        }
//        if (euclideanDistancesPrevious != null) {
//            euclideanDistances.addAll(euclideanDistancesPrevious);
//        }
//        if (euclideanDistancesNext != null) {
//            euclideanDistances.addAll(euclideanDistancesNext);
//        }
//
//        List<Double> angles = new ArrayList<>();
//        var anglesCurrent = DartSingleton.getInstance().arrowAnglesBySegment.get(segment.getValue());
//        var anglesPrevious = DartSingleton.getInstance().arrowAnglesBySegment.get(segment.getPrevious().getValue());
//        var anglesNext = DartSingleton.getInstance().arrowAnglesBySegment.get(segment.getNext().getValue());
//        if (anglesCurrent != null) {
//            angles.addAll(anglesCurrent);
//        }
//        if (anglesPrevious != null) {
//            angles.addAll(anglesPrevious);
//        }
//        if (anglesNext != null) {
//            angles.addAll(anglesNext);
//        }
//
//        if (euclideanDistances.isEmpty() || angles.isEmpty()) {
//            return false;
//        }
//        double medianEuclid = calcMedian(euclideanDistances);
//        double mediaAngle = calcMedian(angles);
//        boolean euclidBelowThreshold = (euclideanDistance < medianEuclid)
//                && ((((Math.abs(euclideanDistance - medianEuclid)) / medianEuclid) * 100) >= 20.0d);
//// TODO: most likely not relevant in this context
////        boolean angleBelowThreshold = (angle < mediaAngle)
////                && ((((Math.abs(angle - mediaAngle)) / mediaAngle) * 100) >= 66.6d);
////        boolean result = euclidBelowThreshold || angleBelowThreshold;


        var distances = new ArrayList<Double>();
        DartSingleton.getInstance().euclideanDistancesBySegment.forEach((key, value) -> distances.addAll(value));
        var meanEuclid = DartSingleton.calculateMean(distances);

        boolean euclidBelowThreshold = (euclideanDistance < meanEuclid)
                && ((((Math.abs(euclideanDistance - meanEuclid)) / meanEuclid) * 100) >= 20d); // FIXME: must be configurable



        boolean result = euclidBelowThreshold;
        System.out.printf("Arrow Tip must be estimated (%s): segment=%s, angle=%.2f, euclideanDist=%.2f (mean=%.2f)%n",
                result, segmentValue, angle, euclideanDistance, meanEuclid);
        return result;
    }

    /**
     * Determines if there are significant changes in a mask by checking non-zero-pixels against thresholds.
     *
     * @param mask
     * @param thresholdLow  - recommendation >1920x1080: vid=(1500, 6000), image=(1000, 50000)
     * @param thresholdHigh - recommendation >1920x1080: vid=(1500, 6000), image=(1000, 50000)
     * @return true if non-zero pixels are within the threshold range
     */
    public static boolean hasSignificantChanges(final Mat mask, final int thresholdLow, final int thresholdHigh) {
        int countNonZeroPixels = Core.countNonZero(mask);
        return countNonZeroPixels >= thresholdLow && countNonZeroPixels <= thresholdHigh;
    }

    /**
     * Extracts a merge contour from contours satisfying minimal contour area in a given mask
     *
     * @param minContourArea - recommendation @ >1920: >100
     * @return {@link MatOfPoint}
     */
    public static MatOfPoint extractMergedContour(final Mat mask, final int minContourArea) {
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        List<MatOfPoint> filteredContours = new ArrayList<>();
        for (MatOfPoint contour : contours) {
            double contourArea = Imgproc.contourArea(contour);
            if (contourArea >= minContourArea) {
//                System.out.printf("Found considerable contour area %.2f that fits the threshold of %d\n", contourArea, minContourArea);
                filteredContours.add(contour);
            }
        }
        // merge contours in case the dart contour is split
        final List<Point> mergedPoints = new ArrayList<>();
        if (!filteredContours.isEmpty()) {
//            System.out.printf("Merging %d contours\n", filteredContours.size());
            for (final var contour : filteredContours) {
                mergedPoints.addAll(contour.toList());
            }
        }
        final var mergedContour = new MatOfPoint();
        mergedContour.fromList(mergedPoints);

//        double mergedContourArea = Imgproc.contourArea(mergedContour);
//        System.out.printf("Merged contour area:  %.2f\n", mergedContourArea);

        hierarchy.release();

        return mergedContour;
    }

    /**
     *
     * @param contourPoints
     * @return
     */
    public static MatOfPoint findConvexHull(final MatOfPoint contourPoints) {
        MatOfInt hull = new MatOfInt();
        Imgproc.convexHull(contourPoints, hull);
        final Point[] contourArray = contourPoints.toArray();
        final Point[] hullPoints = new Point[hull.rows()];
        final List<Integer> hullContourIndexList = hull.toList();
        for (int i = 0; i < hullContourIndexList.size(); i++) {
            hullPoints[i] = contourArray[hullContourIndexList.get(i)];
        }

        hull.release();

        return new MatOfPoint(hullPoints);
    }

    /**
     *
     * @param frameWidth
     * @return
     */
    public static double calculateScaleFactor1024(final double frameWidth) {
        double res = 1.0;
        if (frameWidth > 1024d) {
            res = ((1024d * 100d) / frameWidth) / 100d;
            System.out.printf("Adjusting scale factor to %.2f\n", res);
        }
        return res;
    }

    /**
     * Rotates a point about a center point.
     *
     * @param center
     * @param point
     * @param angle
     * @return
     */
    public static Point rotatePoint(final Point center, final Point point, double angle) {
        double cosTheta = Math.cos(angle);
        double sinTheta = Math.sin(angle);

        double x = (point.x - center.x) * cosTheta - (point.y - center.y) * sinTheta + center.x;
        double y = (point.x - center.x) * sinTheta + (point.y - center.y) * cosTheta + center.y;

        return new Point(x, y);
    }

    /**
     *
     * @param elements
     * @return
     */
    public static double calcMedian(final List<Double> elements) {
        List<Double> workingCopy = new ArrayList<>(elements);
        workingCopy.sort(Double::compareTo);

        int halfLen = workingCopy.size() / 2;
        boolean isEven = (workingCopy.size() % 2) == 0;
        if (isEven) {
            double x = workingCopy.get(halfLen);
            double y = workingCopy.get((halfLen) - 1);
            return (x + y) / 2;
        }
        return workingCopy.get(halfLen);
    }
}
