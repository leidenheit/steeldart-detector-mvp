package de.leidenheit.steeldartdetectormvp.steps.dartboard;

import de.leidenheit.steeldartdetectormvp.FxUtil;
import de.leidenheit.steeldartdetectormvp.detection.*;
import de.leidenheit.steeldartdetectormvp.steps.ContentController;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import org.opencv.core.Mat;

import java.util.Arrays;

public class ContentMaskSegmentFineAdjustmentController extends ContentController {

    private static final double ANGLE_RANGE_START = 0d;
    private static final double ANGLE_RANGE_END = 360d;
    private static final double ANGLE_INCREMENT = 0.1d;

    @FXML
    public Spinner<Double> spinnerAngleFrom;
    @FXML
    public Spinner<Double> spinnerAngleTo;
    @FXML
    public ChoiceBox<Integer> choiceBoxSegment;


    @Override
    protected void customInit() {
        choiceBoxSegment.setItems(FXCollections.observableList(Arrays.stream(Segment.values()).map(Segment::getValue).toList()));
        choiceBoxSegment.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            // retrieve segment from dropdown selection value
            SegmentData selectedSegment = retrieveSegmentByValue(newValue);
            // min=0° max=360° value=boundaryAngle, increment 0,5°
            spinnerAngleFrom.setValueFactory(
                    new SpinnerValueFactory.DoubleSpinnerValueFactory(ANGLE_RANGE_START, ANGLE_RANGE_END, selectedSegment.getLowerBoundaryAngle(), ANGLE_INCREMENT));
            spinnerAngleTo.setValueFactory(
                    new SpinnerValueFactory.DoubleSpinnerValueFactory(ANGLE_RANGE_START, ANGLE_RANGE_END, selectedSegment.getUpperBoundaryAngle(), ANGLE_INCREMENT));
        });

        // change listener for start angle control
        spinnerAngleFrom.valueProperty().addListener((observable, oldValue, newValue) -> {
            // retrieve segment from dropdown selection value
            SegmentData segment = retrieveSegmentByValue(choiceBoxSegment.getValue());
            // apply modified angle
            segment.setLowerBoundaryAngle(newValue);
            // draw the segment
            final var pointCenter = Detection.findCircleCenter(MaskSingleton.getInstance().baseImageBGR, MaskSingleton.getInstance().innerBullMask);
            Mat img = MaskSingleton.getInstance().baseImageBGR.clone();
            Detection.drawSegment(img, pointCenter, segment);
            imageViewSource.setImage(FxUtil.matToImage(MaskSingleton.getInstance().baseImageBGR));
            imageViewComputed.setImage(FxUtil.matToImage(img));
        });

        // change listener for end angle control
        spinnerAngleTo.valueProperty().addListener((observable, oldValue, newValue) -> {
            // retrieve segment from dropdown selection value
            SegmentData segment = retrieveSegmentByValue(choiceBoxSegment.getValue());
            // apply modified angle
            segment.setUpperBoundaryAngle(newValue);
            // draw the segment
            final var pointCenter = Detection.findCircleCenter(MaskSingleton.getInstance().baseImageBGR, MaskSingleton.getInstance().innerBullMask);
            Mat img = MaskSingleton.getInstance().baseImageBGR.clone();
            Detection.drawSegment(img, pointCenter, segment);
            imageViewSource.setImage(FxUtil.matToImage(MaskSingleton.getInstance().baseImageBGR));
            imageViewComputed.setImage(FxUtil.matToImage(img));
        });

        resetParams();
    }

    @Override
    protected void resetParams() {
        log("params reset");
        doDetect();
    }

    @Override
    protected void doDetect() {
        log("Find adjustment of segments");

        imageViewSource.setImage(FxUtil.matToImage(MaskSingleton.getInstance().baseImageBGR));
        imageViewComputed.setImage(FxUtil.matToImage(MaskSingleton.getInstance().segments));
    }

    private SegmentData retrieveSegmentByValue(final int value) {
        return MaskSingleton.getInstance().segmentDataList.stream()
                .filter(s -> s.getSegment().getValue() == value)
                .findFirst()
                .orElseThrow(() -> new LeidenheitException(String.format("Not able to find segment for value %d", value)));
    }
}
