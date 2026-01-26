package com.gic.investment;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

class GameState {
    private final Field field;
    private final Queue<Brick> brickQueue;
    private final Optional<Brick> activeBrick;
    private final boolean gameOver;

    public GameState(Field field, Queue<Brick> brickQueue) {
        this.field = field;
        this.brickQueue = new LinkedList<>(brickQueue);
        this.activeBrick = Optional.empty();
        this.gameOver = false;
    }

    private GameState(Field field, Queue<Brick> brickQueue, Optional<Brick> activeBrick, boolean gameOver) {
        this.field = field;
        this.brickQueue = new LinkedList<>(brickQueue);
        this.activeBrick = activeBrick;
        this.gameOver = gameOver;
    }

    public GameState spawnNextBrick() {
        if (brickQueue.isEmpty()) {
            return new GameState(field, brickQueue, Optional.empty(), true);
        }

        Brick nextBrick = brickQueue.peek();
        if (!field.canPlaceBrick(nextBrick)) {
            return new GameState(field, brickQueue, Optional.empty(), true);
        }

        Queue<Brick> newQueue = new LinkedList<>(brickQueue);
        newQueue.poll();
        return new GameState(field, newQueue, Optional.of(nextBrick), false);
    }

    public GameState processCommand(char command) {
        if (activeBrick.isEmpty()) return this;

        Brick current = activeBrick.get();
        Brick moved = switch (command) {
            case 'L' -> current.moveLeft();
            case 'R' -> current.moveRight();
            case 'D' -> dropBrick(current);
            default -> current;
        };

        if (field.canPlaceBrick(moved)) {
            return new GameState(field, brickQueue, Optional.of(moved), gameOver);
        }

        return this;
    }

    private Brick dropBrick(Brick brick) {
        Brick dropped = brick;
        while (field.canPlaceBrick(dropped.moveDown())) {
            dropped = dropped.moveDown();
        }
        return dropped;
    }

    public GameState moveDownOrPlace() {
        if (activeBrick.isEmpty()) return this;

        Brick current = activeBrick.get();
        Brick movedDown = current.moveDown();

        if (field.canPlaceBrick(movedDown)) {
            return new GameState(field, brickQueue, Optional.of(movedDown), gameOver);
        }

        Field newField = field.placeBrick(current).removeMatches();
        return new GameState(newField, brickQueue, Optional.empty(), gameOver);
    }

    public Field getField() { return field; }
    public Optional<Brick> getActiveBrick() { return activeBrick; }
    public boolean isGameOver() { return gameOver; }
    public boolean needsNewBrick() { return activeBrick.isEmpty() && !gameOver; }
}
