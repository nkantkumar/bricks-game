package com.gic.engine;

import com.gic.model.*;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.List;
import java.util.Optional;

public class GameEngineTest {

    @Test
    public void testSpawnBrick() {
        Field field = new Field(10, 20);
        Brick brick = new Brick(List.of(new Block('@'), new Block('@'), new Block('@')), Orientation.HORIZONTAL);
        GameEngine engine = new GameEngine(field, List.of(brick));
        
        engine.spawnNextBrick();
        
        assertTrue(engine.getActiveBrick().isPresent());
        assertEquals(new Coordinate(5, 0), engine.getActiveBrickPosition());
    }

    @Test
    public void testGameOverWhenNoMoreBricks() {
        Field field = new Field(10, 20);
        Brick brick = new Brick(List.of(new Block('@'), new Block('@'), new Block('@')), Orientation.HORIZONTAL);
        GameEngine engine = new GameEngine(field, List.of(brick));
        
        engine.spawnNextBrick(); // Uses the only brick
        engine.lockBrick();
        engine.spawnNextBrick(); // Should trigger game over
        
        assertTrue(engine.isGameOver());
        assertFalse(engine.getActiveBrick().isPresent());
    }

    @Test
    public void testMoveLeft() {
        Field field = new Field(10, 20);
        Brick brick = new Brick(List.of(new Block('@'), new Block('@'), new Block('@')), Orientation.HORIZONTAL);
        GameEngine engine = new GameEngine(field, List.of(brick));
        engine.spawnNextBrick(); // Spawns at 5,0
        
        engine.moveLeft();
        
        assertEquals(new Coordinate(4, 0), engine.getActiveBrickPosition());
    }
    
    @Test
    public void testMoveRight() {
        Field field = new Field(10, 20);
        Brick brick = new Brick(List.of(new Block('@'), new Block('@'), new Block('@')), Orientation.HORIZONTAL);
        GameEngine engine = new GameEngine(field, List.of(brick));
        engine.spawnNextBrick(); // Spawns at 5,0
        
        engine.moveRight();
        
        assertEquals(new Coordinate(6, 0), engine.getActiveBrickPosition());
    }
    
    @Test
    public void testMoveDown() {
        Field field = new Field(10, 20);
        Brick brick = new Brick(List.of(new Block('@'), new Block('@'), new Block('@')), Orientation.HORIZONTAL);
        GameEngine engine = new GameEngine(field, List.of(brick));
        engine.spawnNextBrick(); // Spawns at 5,0
        
        boolean moved = engine.moveDown();
        
        assertTrue(moved);
        assertEquals(new Coordinate(5, 1), engine.getActiveBrickPosition());
    }
    
    @Test
    public void testCollisionWithWall() {
        Field field = new Field(10, 20);
        Brick brick = new Brick(List.of(new Block('@'), new Block('@'), new Block('@')), Orientation.HORIZONTAL);
        GameEngine engine = new GameEngine(field, List.of(brick));
        engine.spawnNextBrick(); // Spawns at 5,0. Occupies 4,0; 5,0; 6,0
        
        // Move to left wall
        engine.moveLeft(); // 4,0
        engine.moveLeft(); // 3,0
        engine.moveLeft(); // 2,0
        engine.moveLeft(); // 1,0. Occupies 0,0; 1,0; 2,0
        
        engine.moveLeft(); // Should fail
        assertEquals(new Coordinate(1, 0), engine.getActiveBrickPosition());
    }
    
    @Test
    public void testCollisionWithBlock() {
        Field field = new Field(10, 20);
        field.setBlock(new Coordinate(5, 2), new Block('#'));
        
        Brick brick = new Brick(List.of(new Block('@'), new Block('@'), new Block('@')), Orientation.HORIZONTAL);
        GameEngine engine = new GameEngine(field, List.of(brick));
        engine.spawnNextBrick(); // Spawns at 5,0
        
        engine.moveDown(); // 5,1
        boolean moved = engine.moveDown(); // Should fail because 5,2 is occupied
        
        assertFalse(moved);
        assertEquals(new Coordinate(5, 1), engine.getActiveBrickPosition());
    }

    @Test
    public void testMatchDetection() {
        Field field = new Field(10, 20);
        // Create a horizontal match
        field.setBlock(new Coordinate(0, 19), new Block('@'));
        field.setBlock(new Coordinate(1, 19), new Block('@'));
        field.setBlock(new Coordinate(2, 19), new Block('@'));
        
        GameEngine engine = new GameEngine(field, List.of());
        int matches = engine.checkAndClearMatches();
        
        assertEquals(1, matches);
        assertTrue(field.isEmpty(new Coordinate(0, 19)));
        assertTrue(field.isEmpty(new Coordinate(1, 19)));
        assertTrue(field.isEmpty(new Coordinate(2, 19)));
    }
}