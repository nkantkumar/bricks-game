package com.gic.investment;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PositionTest {

    @Test
    void testPositionCreation() {
        Position pos = new Position(5, 10);
        assertEquals(5, pos.row());
        assertEquals(10, pos.col());
    }

    @Test
    void testMove() {
        Position pos = new Position(5, 10);
        Position newPos = pos.move(1, -1);
        assertEquals(6, newPos.row());
        assertEquals(9, newPos.col());
    }
}