package com.gic.model;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class FieldTest {

    @Test
    public void testFieldCreation() {
        Field field = new Field(10, 20);
        assertEquals(10, field.width());
        assertEquals(20, field.height());
        assertTrue(field.isEmpty(new Coordinate(0, 0)));
    }

    @Test
    public void testPlaceBlock() {
        Field field = new Field(10, 20);
        Coordinate coord = new Coordinate(5, 5);
        Block block = new Block('@');
        
        field.setBlock(coord, block);
        
        assertFalse(field.isEmpty(coord));
        assertEquals(Optional.of(block), field.getBlock(coord));
    }

    @Test
    public void testOutOfBounds() {
        Field field = new Field(5, 5);
        assertFalse(field.isValidCoordinate(new Coordinate(-1, 0)));
        assertFalse(field.isValidCoordinate(new Coordinate(0, -1)));
        assertFalse(field.isValidCoordinate(new Coordinate(5, 0)));
        assertFalse(field.isValidCoordinate(new Coordinate(0, 5)));
        assertTrue(field.isValidCoordinate(new Coordinate(0, 0)));
        assertTrue(field.isValidCoordinate(new Coordinate(4, 4)));
    }
}