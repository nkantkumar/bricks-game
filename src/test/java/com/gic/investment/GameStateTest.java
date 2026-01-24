package com.gic.investment;

import org.junit.jupiter.api.Test;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import static org.junit.jupiter.api.Assertions.*;

class GameStateTest {

    @Test
    void testSpawnNextBrick() {
        Field field = new Field(10, 20);
        Queue<Brick> bricks = new LinkedList<>();
        Brick brick = new Brick(Orientation.HORIZONTAL, List.of('A', 'B', 'C'), new Position(0, 0));
        bricks.add(brick);
        
        GameState state = new GameState(field, bricks);
        GameState nextState = state.spawnNextBrick();
        
        assertTrue(nextState.getActiveBrick().isPresent());
        assertEquals(brick, nextState.getActiveBrick().get());
        assertFalse(nextState.isGameOver());
    }

    @Test
    void testGameOverWhenNoBricks() {
        Field field = new Field(10, 20);
        Queue<Brick> bricks = new LinkedList<>();
        
        GameState state = new GameState(field, bricks);
        GameState nextState = state.spawnNextBrick();
        
        assertTrue(nextState.isGameOver());
    }

    @Test
    void testGameOverWhenBlocked() {
        Field field = new Field(10, 20);
        // Fill the top row
        Brick blocker = new Brick(Orientation.HORIZONTAL, List.of('X', 'X', 'X'), new Position(0, 0));
        field = field.placeBrick(blocker);
        
        Queue<Brick> bricks = new LinkedList<>();
        Brick brick = new Brick(Orientation.HORIZONTAL, List.of('A', 'B', 'C'), new Position(0, 0));
        bricks.add(brick);
        
        GameState state = new GameState(field, bricks);
        GameState nextState = state.spawnNextBrick();
        
        assertTrue(nextState.isGameOver());
    }

    @Test
    void testProcessCommandMove() {
        Field field = new Field(10, 20);
        Queue<Brick> bricks = new LinkedList<>();
        Brick brick = new Brick(Orientation.HORIZONTAL, List.of('A', 'B', 'C'), new Position(5, 5));
        bricks.add(brick);
        
        GameState state = new GameState(field, bricks).spawnNextBrick();
        
        GameState movedLeft = state.processCommand('L');
        assertEquals(new Position(5, 4), movedLeft.getActiveBrick().get().getPosition());
        
        GameState movedRight = state.processCommand('R');
        assertEquals(new Position(5, 6), movedRight.getActiveBrick().get().getPosition());
    }

    @Test
    void testProcessCommandDrop() {
        Field field = new Field(10, 20);
        Queue<Brick> bricks = new LinkedList<>();
        Brick brick = new Brick(Orientation.HORIZONTAL, List.of('A', 'B', 'C'), new Position(0, 0));
        bricks.add(brick);
        
        GameState state = new GameState(field, bricks).spawnNextBrick();
        
        GameState dropped = state.processCommand('D');
        // Should drop to the bottom (row 19)
        assertEquals(new Position(19, 0), dropped.getActiveBrick().get().getPosition());
    }

    @Test
    void testMoveDownOrPlace_Move() {
        Field field = new Field(10, 20);
        Queue<Brick> bricks = new LinkedList<>();
        Brick brick = new Brick(Orientation.HORIZONTAL, List.of('A', 'B', 'C'), new Position(0, 0));
        bricks.add(brick);
        
        GameState state = new GameState(field, bricks).spawnNextBrick();
        GameState nextState = state.moveDownOrPlace();
        
        assertEquals(new Position(1, 0), nextState.getActiveBrick().get().getPosition());
    }

    @Test
    void testMoveDownOrPlace_Place() {
        Field field = new Field(10, 20);
        Queue<Brick> bricks = new LinkedList<>();
        Brick brick = new Brick(Orientation.HORIZONTAL, List.of('A', 'B', 'C'), new Position(19, 0));
        bricks.add(brick);
        GameState state = new GameState(field, bricks).spawnNextBrick();
        state = state.processCommand('D'); // At 19,0
        
        GameState placedState = state.moveDownOrPlace();
        
        assertTrue(placedState.getActiveBrick().isEmpty());
        assertTrue(placedState.getField().isOccupied(new Position(19, 0)));
    }

    @Test
    void testProcessCommandNoActiveBrick() {
        Field field = new Field(10, 20);
        Queue<Brick> bricks = new LinkedList<>();
        GameState state = new GameState(field, bricks);
        
        GameState sameState = state.processCommand('L');
        assertEquals(state, sameState);
    }

    @Test
    void testProcessCommandInvalidMove() {
        Field field = new Field(10, 20);
        Queue<Brick> bricks = new LinkedList<>();
        Brick brick = new Brick(Orientation.HORIZONTAL, List.of('A', 'B', 'C'), new Position(0, 0));
        bricks.add(brick);
        
        GameState state = new GameState(field, bricks).spawnNextBrick();

        GameState sameState = state.processCommand('L');
        assertEquals(new Position(0, 0), sameState.getActiveBrick().get().getPosition());
    }

    @Test
    void testProcessCommandUnknown() {
        Field field = new Field(10, 20);
        Queue<Brick> bricks = new LinkedList<>();
        Brick brick = new Brick(Orientation.HORIZONTAL, List.of('A', 'B', 'C'), new Position(5, 5));
        bricks.add(brick);
        
        GameState state = new GameState(field, bricks).spawnNextBrick();
        
        GameState sameState = state.processCommand('X');
        assertEquals(new Position(5, 5), sameState.getActiveBrick().get().getPosition());
    }

    @Test
    void testMoveDownOrPlaceNoActiveBrick() {
        Field field = new Field(10, 20);
        Queue<Brick> bricks = new LinkedList<>();
        GameState state = new GameState(field, bricks);
        
        GameState sameState = state.moveDownOrPlace();
        assertEquals(state, sameState);
    }
    
    @Test
    void testNeedsNewBrick() {
        Field field = new Field(10, 20);
        Queue<Brick> bricks = new LinkedList<>();
        GameState state = new GameState(field, bricks);

        bricks.add(new Brick(Orientation.HORIZONTAL, List.of('A', 'B', 'C'), new Position(19, 0)));
        state = state.spawnNextBrick();
        state = state.processCommand('D');
        state = state.moveDownOrPlace();
        
        assertFalse(state.needsNewBrick());
        assertTrue(state.isGameOver());
    }
}