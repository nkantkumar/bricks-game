package com.gic.model;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public record Brick(List<Block> blocks, Orientation orientation) {

    public List<Coordinate> getOccupiedCoordinates(Coordinate center) {
        return switch (orientation) {
            case HORIZONTAL -> IntStream.range(0, 3)
                    .mapToObj(i -> new Coordinate(center.x() - 1 + i, center.y()))
                    .collect(Collectors.toList());
            case VERTICAL -> IntStream.range(0, 3)
                    .mapToObj(i -> new Coordinate(center.x(), center.y() - 1 + i))
                    .collect(Collectors.toList());
        };
    }
}