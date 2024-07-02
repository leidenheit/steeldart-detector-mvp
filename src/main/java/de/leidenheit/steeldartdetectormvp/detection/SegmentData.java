package de.leidenheit.steeldartdetectormvp.detection;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class SegmentData implements Serializable, Comparable<SegmentData> {

    private Segment segment;
    private double lowerBoundaryAngle;
    private double upperBoundaryAngle;

    @Override
    public int compareTo(final SegmentData other) {
        if (this.lowerBoundaryAngle < other.lowerBoundaryAngle) {
            return -1;
        } else if (this.lowerBoundaryAngle > other.lowerBoundaryAngle) {
            return 1;
        } else {
            return Double.compare(this.upperBoundaryAngle, other.upperBoundaryAngle);
        }
    }
}
