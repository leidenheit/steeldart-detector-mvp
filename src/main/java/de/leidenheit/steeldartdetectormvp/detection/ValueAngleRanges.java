package de.leidenheit.steeldartdetectormvp.detection;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ValueAngleRanges implements Serializable {

    // FIXME must hold center which the values belongs to

    private static ValueAngleRanges instance;

    public final List<Integer> segmentValueIndexes = List.of(
            6, // twice since dartboard does not start vertically; 0° to x° and x° to 360°
            10,
            15,
            2,
            17,
            3,
            19,
            7,
            16,
            8,
            11,
            14,
            9,
            12,
            5,
            20,
            1,
            18,
            4,
            13,
            6 // twice since dartboard does not start vertically; 0° to x° and x° to 360°
    );

    private final HashMap<ValueRange, Integer> valueAngleRangeMap = new HashMap<>();

    public static ValueAngleRanges getInstance() {
        if (instance == null) {
            instance = new ValueAngleRanges();
        }
        return instance;
    }

    public Integer putValueForAngleRange(final int valueSegmentIndex, final double angleStart, final double angleEnd) {
        // FIXME: implementation is wrong since items are added multiple times
        if (this.valueAngleRangeMap.size() == segmentValueIndexes.size()) {
            this.valueAngleRangeMap.clear();
        }
        return this.valueAngleRangeMap.putIfAbsent(new ValueRange(angleStart, angleEnd), this.segmentValueIndexes.get(valueSegmentIndex));
    }

    public Integer findValueByAngle(final double angle) throws RuntimeException {
        for (Map.Entry<ValueRange, Integer> entry : valueAngleRangeMap.entrySet()) {
            final var inRange = entry.getKey().minValue <= angle
                    && entry.getKey().maxValue >= angle;
            if (inRange) {
                return entry.getValue();
            }
        }
        // TODO throw new RuntimeException(String.format("Cannot find angle %s in range set", angle));
        return null;
    }

    public record ValueRange(double minValue, double maxValue) implements Serializable, Comparable<ValueRange> {

        @Override
        public int compareTo(ValueRange other) {
            if (this.minValue < other.minValue) {
                return -1;
            } else if (this.minValue > other.minValue) {
                return 1;
            } else {
                return Double.compare(this.maxValue, other.maxValue);
            }
        }

        @Override
        public String toString() {
            return "[" + minValue + ", " + maxValue + "]";
        }
    }
}
