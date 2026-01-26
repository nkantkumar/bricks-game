package com.gic.investment;

import java.util.*;

class Field {
    private final int width;
    private final int height;
    private final Map<Position, Character> cells;

    public Field(int width, int height) {
        this.width = width;
        this.height = height;
        this.cells = new HashMap<>();
    }

    private Field(int width, int height, Map<Position, Character> cells) {
        this.width = width;
        this.height = height;
        this.cells = new HashMap<>(cells);
    }

    public boolean isValidPosition(Position pos) {
        return pos.row() >= 0 && pos.row() < height &&
                pos.col() >= 0 && pos.col() < width;
    }

    public boolean isOccupied(Position pos) {
        return cells.containsKey(pos);
    }

    public boolean canPlaceBrick(Brick brick) {
        return brick.getBlocks().stream()
                .allMatch(block -> isValidPosition(block.pos()) && !isOccupied(block.pos()));
    }

    public Field placeBrick(Brick brick) {
        Map<Position, Character> newCells = new HashMap<>(cells);
        brick.getBlocks().forEach(block -> newCells.put(block.pos(), block.symbol()));
        return new Field(width, height, newCells);
    }

    public Field removeMatches() {
        Set<Position> toRemove = new HashSet<>();

        // Check horizontal matches
        for (int row = 0; row < height; row++) {
            for (int col = 0; col <= width - 3; col++) {
                toRemove.addAll(checkMatch(row, col, 0, 1));
            }
        }

        // Check vertical matches
        for (int col = 0; col < width; col++) {
            for (int row = 0; row <= height - 3; row++) {
                toRemove.addAll(checkMatch(row, col, 1, 0));
            }
        }

        if (toRemove.isEmpty()) {
            return this;
        }

        Map<Position, Character> newCells = new HashMap<>(cells);
        toRemove.forEach(newCells::remove);
        return new Field(width, height, newCells);
    }

    private Set<Position> checkMatch(int startRow, int startCol, int rowDelta, int colDelta) {
        Set<Position> matches = new HashSet<>();
        Position p1 = new Position(startRow, startCol);
        Position p2 = new Position(startRow + rowDelta, startCol + colDelta);
        Position p3 = new Position(startRow + 2 * rowDelta, startCol + 2 * colDelta);

        if (cells.containsKey(p1) && cells.containsKey(p2) && cells.containsKey(p3)) {
            char c1 = cells.get(p1);
            char c2 = cells.get(p2);
            char c3 = cells.get(p3);

            if (c1 == c2 && c2 == c3) {
                matches.add(p1);
                matches.add(p2);
                matches.add(p3);
            }
        }

        return matches;
    }

    public String render(Optional<Brick> activeBrick) {
        Map<Position, Character> display = new HashMap<>(cells);
        activeBrick.ifPresent(brick ->
                brick.getBlocks().forEach(block -> display.put(block.pos(), block.symbol()))
        );

        StringBuilder sb = new StringBuilder();

        for (int row = 0; row < height; row++) {
            sb.append("|");
            for (int col = 0; col < width; col++) {
                Position pos = new Position(row, col);
                sb.append(display.getOrDefault(pos, '.'));
            }
            sb.append("|\n");
        }
        return sb.toString();
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
}