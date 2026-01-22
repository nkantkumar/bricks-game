package com.gic.investment;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class BrickTest {

    @Test
    void testHorizontalBrickCreation() {
        Position pos = new Position(0, 0);
        List<Character> symbols = List.of('A', 'B', 'C');
        Brick brick = new Brick(Orientation.HORIZONTAL, symbols, pos);

        List<Block> blocks = brick.getBlocks();
        assertEquals(3, blocks.size());
        
        assertEquals(new Position(0, 0), blocks.get(0).pos());
        assertEquals('A', blocks.get(0).symbol());
        
        assertEquals(new Position(0, 1), blocks.get(1).pos());
        assertEquals('B', blocks.get(1).symbol());
        
        assertEquals(new Position(0, 2), blocks.get(2).pos());
        assertEquals('C', blocks.get(2).symbol());
    }

    @Test
    void testVerticalBrickCreation() {
        Position pos = new Position(0, 0);
        List<Character> symbols = List.of('A', 'B', 'C');
        Brick brick = new Brick(Orientation.VERTICAL, symbols, pos);

        List<Block> blocks = brick.getBlocks();
        assertEquals(3, blocks.size());
        
        assertEquals(new Position(0, 0), blocks.get(0).pos());
        assertEquals('A', blocks.get(0).symbol());
        
        assertEquals(new Position(1, 0), blocks.get(1).pos());
        assertEquals('B', blocks.get(1).symbol());
        
        assertEquals(new Position(2, 0), blocks.get(2).pos());
        assertEquals('C', blocks.get(2).symbol());
    }

    @Test
    void testMoveLeft() {
        Position pos = new Position(5, 5);
        Brick brick = new Brick(Orientation.HORIZONTAL, List.of('A', 'B', 'C'), pos);
        Brick moved = brick.moveLeft();
        assertEquals(new Position(5, 4), moved.getPosition());
    }

    @Test
    void testMoveRight() {
        Position pos = new Position(5, 5);
        Brick brick = new Brick(Orientation.HORIZONTAL, List.of('A', 'B', 'C'), pos);
        Brick moved = brick.moveRight();
        assertEquals(new Position(5, 6), moved.getPosition());
    }

    @Test
    void testMoveDown() {
        Position pos = new Position(5, 5);
        Brick brick = new Brick(Orientation.HORIZONTAL, List.of('A', 'B', 'C'), pos);
        Brick moved = brick.moveDown();
        assertEquals(new Position(6, 5), moved.getPosition());
    }
}