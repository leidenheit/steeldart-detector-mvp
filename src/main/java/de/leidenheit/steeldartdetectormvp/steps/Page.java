package de.leidenheit.steeldartdetectormvp.steps;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Page {
    MASK_GREEN(0, "01/14 Green Mask", "Detect the green parts of a dartboard.", "mask-green.fxml"),
    MASK_RED(1, "02/14 Red Mask", "Detect the red parts of a dartboard.", "mask-red.fxml"),
    MASK_MULTIPLIERS(2, "03/14 Multipliers Mask", "Combine green and red to retrieve multipliers.", "mask-multipliers.fxml"),
    MASK_MULTIRINGS(3, "04/14 Multi Rings Mask", "Morph the multipliers to retrieve multi rings.", "mask-multirings.fxml"),
    MASK_DARTBOARD(4, "05/14 Dartboard Mask", "Detect the dartboard itself.", "mask-dartboard.fxml"),
    MASK_SINGLE(5, "06/14 Single Mask", "Determined the single area of a dartboard.", "mask-single.fxml"),
    MASK_DOUBLE(6, "07/14 Double Mask", "Determined the double area of a dartboard.", "mask-double.fxml"),
    MASK_TRIPLE(7, "08/14 Triple Mask", "Determined triple area of a dartboard.", "mask-triple.fxml"),
    MASK_OUTERBULL(8, "09/14 Outer Bull Mask", "Determined outer bull area of a dartboard.", "mask-outerbull.fxml"),
    MASK_INNERBULL(9, "10/14 Inner Bull Mask", "Determined inner bull area of a dartboard.", "mask-innerbull.fxml"),
    MASK_MISS(10, "11/14 Miss Mask", "Determined miss area of a dartboard.", "mask-miss.fxml"),
    MASK_SEGMENT_ADJUSTMENT(11, "12/14 Segment Orientation", "Set position of segment 6 by clicking in the dartboard.", "mask-segment-adjustment.fxml"),
    MASK_SEGMENTS(12, "13/14 Segments Mask", "Detect segments of a dartboard.", "mask-segments.fxml"),
    MASK_SEGMENT_FINE_ADJUSTMENT(14, "14/14 Segment Fine Adjustment", "Adjust the lower and upper boundary for a segment", "mask-segment-fine-adjustment.fxml"),
    DARTBOARD_SUMMARY(14, "Summary", "Save and export the configuration if satisfying.", "summary.fxml"),
    DART_DARTS(15, "01/02 Darts Detection Preparation", "Put once at a time a single arrow in the board and make a snapshot.", "darts.fxml"),
    DART_TIP(16, "02/02 Dart Tips", "Determine arrow tips.", "dart-tips.fxml"),
    DART_SUMMARY(17, "Summary", "Save and export the configuration if satisfying.", "summary.fxml"),

    // shared ref
    REFERENCE_IMAGE(18, "Reference Image", "Take reference image showing only the dartboard without any arrows.", "reference.fxml"),

    // eval
    EVALUATION(19, "Evaluation", "Good Darts!", "eval.fxml"),

    // home
    HOME(20, "Home", "Select camera and let's go", "camera-selection.fxml");


    private final int index;
    private final String title;
    private final String description;
    private final String resource;

    public boolean hasNext() {
        return Pagination.hasNext(this);
    }

    public boolean hasPrevious() {
        return Pagination.hasPrevious(this);
    }

    public Page getNext() {
        return Pagination.nextPage(this);
    }

    public Page getPrevious() {
        return Pagination.previousPage(this);
    }
}
