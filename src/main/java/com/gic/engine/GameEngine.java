package com.gic.engine;

import com.gic.model.*;

import java.util.*;
import java.util.stream.Collectors;

public class GameEngine {
    private final Field field;
    private final List<Brick> availableBricks;
    private int currentBrickIndex = 0;
    private Brick activeBrick;
    private Coordinate activeBrickPosition;
    private boolean gameOver = false;

    public GameEngine(Field field, List<Brick> availableBricks) {
        this.field = field;
        this.availableBricks = new ArrayList<>(availableBricks);
    }

    public void spawnNextBrick() {
        if (currentBrickIndex >= availableBricks.size()) {
            gameOver = true;
            return;
        }

        activeBrick = availableBricks.get(currentBrickIndex);
        currentBrickIndex++;

        int startX = field.width() / 2;
        int startY = activeBrick.orientation() == Orientation.HORIZONTAL ? 0 : 1;
        activeBrickPosition = new Coordinate(startX, startY);

        if (checkCollision(activeBrickPosition)) {
            gameOver = true;
            activeBrick = null;
        }
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public Optional<Brick> getActiveBrick() {
        return Optional.ofNullable(activeBrick);
    }

    public Coordinate getActiveBrickPosition() {
        return activeBrickPosition;
    }

    public void moveLeft() {
        if (activeBrick == null) return;
        Coordinate newPos = new Coordinate(activeBrickPosition.x() - 1, activeBrickPosition.y());
        if (!checkCollision(newPos)) {
            activeBrickPosition = newPos;
        }
    }

    public void moveRight() {
        if (activeBrick == null) return;
        Coordinate newPos = new Coordinate(activeBrickPosition.x() + 1, activeBrickPosition.y());
        if (!checkCollision(newPos)) {
            activeBrickPosition = newPos;
        }
    }

    public boolean moveDown() {
        if (activeBrick == null) return false;
        Coordinate newPos = new Coordinate(activeBrickPosition.x(), activeBrickPosition.y() + 1);
        if (!checkCollision(newPos)) {
            activeBrickPosition = newPos;
            return true;
        }
        return false;
    }
    
    public void drop() {
        while(moveDown());
    }

    public void lockBrick() {
        if (activeBrick == null) return;
        List<Coordinate> coords = activeBrick.getOccupiedCoordinates(activeBrickPosition);
        List<Block> blocks = activeBrick.blocks();
        
        for (int i = 0; i < coords.size(); i++) {
            field.setBlock(coords.get(i), blocks.get(i));
        }
        activeBrick = null;
    }

    private boolean checkCollision(Coordinate center) {
        if (activeBrick == null) return false;
        List<Coordinate> coords = activeBrick.getOccupiedCoordinates(center);
        for (Coordinate coord : coords) {
            if (!field.isValidCoordinate(coord) || !field.isEmpty(coord)) {
                return true;
            }
        }
        return false;
    }

    public int checkAndClearMatches() {
        Set<Coordinate> matchedCoordinates = new HashSet<>();

        // Check horizontal matches
        for (int y = 0; y < field.height(); y++) {
            for (int x = 0; x < field.width() - 2; x++) {
                checkLineMatch(x, y, 1, 0, matchedCoordinates);
            }
        }

        // Check vertical matches
        for (int x = 0; x < field.width(); x++) {
            for (int y = 0; y < field.height() - 2; y++) {
                checkLineMatch(x, y, 0, 1, matchedCoordinates);
            }
        }

        if (!matchedCoordinates.isEmpty()) {
            for (Coordinate coord : matchedCoordinates) {
                field.removeBlock(coord);
            }
            return 1; // Return 1 if any matches found (simplified for now)
        }
        return 0;
    }

    private void checkLineMatch(int startX, int startY, int dx, int dy, Set<Coordinate> matchedCoordinates) {
        Coordinate c1 = new Coordinate(startX, startY);
        Coordinate c2 = new Coordinate(startX + dx, startY + dy);
        Coordinate c3 = new Coordinate(startX + 2 * dx, startY + 2 * dy);

        Optional<Block> b1 = field.getBlock(c1);
        Optional<Block> b2 = field.getBlock(c2);
        Optional<Block> b3 = field.getBlock(c3);

        if (b1.isPresent() && b2.isPresent() && b3.isPresent()) {
            if (b1.get().symbol() == b2.get().symbol() && b2.get().symbol() == b3.get().symbol()) {
                matchedCoordinates.add(c1);
                matchedCoordinates.add(c2);
                matchedCoordinates.add(c3);
                
                // Check for more than 3
                int k = 3;
                while (true) {
                    Coordinate next = new Coordinate(startX + k * dx, startY + k * dy);
                    if (!field.isValidCoordinate(next)) break;
                    Optional<Block> bn = field.getBlock(next);
                    if (bn.isPresent() && bn.get().symbol() == b1.get().symbol()) {
                        matchedCoordinates.add(next);
                        k++;
                    } else {
                        break;
                    }
                }
            }
        }
    }
}