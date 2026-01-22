package com.gic.model;


import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BrickTest {

    @Test
    public void testBrickCreation() {
        List<Block> blocks = List.of(new Block('@'), new Block('@'), new Block('@'));
        Brick brick = new Brick(blocks, Orientation.HORIZONTAL);
        
        assertEquals(Orientation.HORIZONTAL, brick.orientation());
        assertEquals(3, brick.blocks().size());
    }
    
    @Test
    public void testBrickCoordinatesHorizontal() {
        List<Block> blocks = List.of(new Block('1'), new Block('2'), new Block('3'));
        Brick brick = new Brick(blocks, Orientation.HORIZONTAL);
        Coordinate center = new Coordinate(5, 5);
        
        List<Coordinate> coords = brick.getOccupiedCoordinates(center);
        
        assertEquals(3, coords.size());
        assertEquals(new Coordinate(4, 5), coords.get(0));
        assertEquals(new Coordinate(5, 5), coords.get(1));
        assertEquals(new Coordinate(6, 5), coords.get(2));
    }

    @Test
    public void testBrickCoordinatesVertical() {
        List<Block> blocks = List.of(new Block('1'), new Block('2'), new Block('3'));
        Brick brick = new Brick(blocks, Orientation.VERTICAL);
        Coordinate center = new Coordinate(5, 5);
        
        List<Coordinate> coords = brick.getOccupiedCoordinates(center);
        
        assertEquals(3, coords.size());
        assertEquals(new Coordinate(5, 4), coords.get(0));
        assertEquals(new Coordinate(5, 5), coords.get(1));
        assertEquals(new Coordinate(5, 6), coords.get(2));
    }
}