package de.leidenheit.steeldartdetectormvp.detection;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum Segment {
    S6(6, 0),
    S10(10, 1),
    S15(15, 2),
    S2(2, 3),
    S17(17, 4),
    S3(3, 5),
    S19(19, 6),
    S7(7, 7),
    S16(16, 8),
    S8(8, 9),
    S11(11, 10),
    S14(14, 11),
    S9(9, 12),
    S12(12, 13),
    S5(5, 14),
    S20(20, 15),
    S1(1, 16),
    S18(18, 17),
    S4(4, 18),
    S13(13, 19);

    private final int value;
    private final int sortOrder;

    Segment(final int value, final int sortOrder) {
        this.value = value;
        this.sortOrder = sortOrder;
    }

    public Segment getNext() {
        int indexOfNext = ((this.sortOrder + 1) + Segment.values().length) % Segment.values().length;
        return Arrays.stream(Segment.values()).toList().get(indexOfNext);
    }

    public Segment getPrevious() {
        int indexOfNext = ((this.sortOrder - 1) + Segment.values().length) % Segment.values().length;
        return Arrays.stream(Segment.values()).toList().get(indexOfNext);
    }
}
