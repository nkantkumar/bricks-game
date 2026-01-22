package com.gic.investment;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BlockTest {

    @Test
    void testBlockCreation() {
        Position pos = new Position(1, 2);
        Block block = new Block(pos, 'A');
        assertEquals(pos, block.pos());
        assertEquals('A', block.symbol());
    }
}