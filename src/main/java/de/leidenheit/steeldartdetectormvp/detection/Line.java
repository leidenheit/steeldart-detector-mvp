package de.leidenheit.steeldartdetectormvp.detection;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.opencv.core.Point;

// line is relative from a given center
@Getter
@Setter
@Builder(setterPrefix = "set")
public class Line {
    private Point center;
    private Point linePoint;
    private double angle;
}
