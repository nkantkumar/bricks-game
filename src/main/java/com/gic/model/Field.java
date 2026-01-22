package com.gic.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Field {
    private final int width;
    private final int height;
    private final Map<Coordinate, Block> grid;

    public Field(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new HashMap<>();
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public boolean isEmpty(Coordinate coord) {
        return !grid.containsKey(coord);
    }

    public Optional<Block> getBlock(Coordinate coord) {
        return Optional.ofNullable(grid.get(coord));
    }

    public void setBlock(Coordinate coord, Block block) {
        if (isValidCoordinate(coord)) {
            grid.put(coord, block);
        }
    }
    
    public void removeBlock(Coordinate coord) {
        grid.remove(coord);
    }

    public boolean isValidCoordinate(Coordinate coord) {
        return coord.x() >= 0 && coord.x() < width && coord.y() >= 0 && coord.y() < height;
    }
}