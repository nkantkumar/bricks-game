package com.gic.investment;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

class FieldTest {

    @Test
    void testFieldCreation() {
        Field field = new Field(10, 20);
        assertEquals(10, field.getWidth());
        assertEquals(20, field.getHeight());
    }

    @Test
    void testIsValidPosition() {
        Field field = new Field(10, 20);
        assertTrue(field.isValidPosition(new Position(0, 0)));
        assertTrue(field.isValidPosition(new Position(19, 9)));
        assertFalse(field.isValidPosition(new Position(-1, 0)));
        assertFalse(field.isValidPosition(new Position(0, -1)));
        assertFalse(field.isValidPosition(new Position(20, 0)));
        assertFalse(field.isValidPosition(new Position(0, 10)));
    }

    @Test
    void testPlaceBrick() {
        Field field = new Field(10, 20);
        Brick brick = new Brick(Orientation.HORIZONTAL, List.of('A', 'B', 'C'), new Position(0, 0));
        
        assertTrue(field.canPlaceBrick(brick));
        Field newField = field.placeBrick(brick);
        
        assertTrue(newField.isOccupied(new Position(0, 0)));
        assertTrue(newField.isOccupied(new Position(0, 1)));
        assertTrue(newField.isOccupied(new Position(0, 2)));
        assertFalse(newField.isOccupied(new Position(0, 3)));
    }

    @Test
    void testCannotPlaceBrickOutOfBounds() {
        Field field = new Field(10, 20);
        Brick brick = new Brick(Orientation.HORIZONTAL, List.of('A', 'B', 'C'), new Position(0, 8));
        // Occupies (0,8), (0,9), (0,10) -> (0,10) is out of bounds
        assertFalse(field.canPlaceBrick(brick));
    }

    @Test
    void testCannotPlaceBrickOnOccupied() {
        Field field = new Field(10, 20);
        Brick brick1 = new Brick(Orientation.HORIZONTAL, List.of('A', 'B', 'C'), new Position(5, 5));
        Field fieldWithBrick = field.placeBrick(brick1);
        
        Brick brick2 = new Brick(Orientation.VERTICAL, List.of('X', 'Y', 'Z'), new Position(4, 5));
        // Occupies (4,5), (5,5), (6,5) -> (5,5) is occupied
        assertFalse(fieldWithBrick.canPlaceBrick(brick2));
    }

    @Test
    void testRemoveMatchesHorizontal() {
        Field field = new Field(10, 20);
        // Create A A A horizontally
        Brick brick = new Brick(Orientation.HORIZONTAL, List.of('A', 'A', 'A'), new Position(19, 0));
        Field fieldWithMatch = field.placeBrick(brick);
        
        Field cleanedField = fieldWithMatch.removeMatches();
        assertFalse(cleanedField.isOccupied(new Position(19, 0)));
        assertFalse(cleanedField.isOccupied(new Position(19, 1)));
        assertFalse(cleanedField.isOccupied(new Position(19, 2)));
    }

    @Test
    void testRemoveMatchesVertical() {
        Field field = new Field(10, 20);
        // Create B B B vertically
        Brick brick = new Brick(Orientation.VERTICAL, List.of('B', 'B', 'B'), new Position(17, 0));
        Field fieldWithMatch = field.placeBrick(brick);
        
        Field cleanedField = fieldWithMatch.removeMatches();
        assertFalse(cleanedField.isOccupied(new Position(17, 0)));
        assertFalse(cleanedField.isOccupied(new Position(18, 0)));
        assertFalse(cleanedField.isOccupied(new Position(19, 0)));
    }
    
    @Test
    void testNoMatches() {
        Field field = new Field(10, 20);
        Brick brick = new Brick(Orientation.HORIZONTAL, List.of('A', 'B', 'C'), new Position(19, 0));
        Field fieldWithBrick = field.placeBrick(brick);
        
        Field sameField = fieldWithBrick.removeMatches();
        assertTrue(sameField.isOccupied(new Position(19, 0)));
    }

    @Test
    void testRender() {
        Field field = new Field(4, 4);
        Brick brick = new Brick(Orientation.HORIZONTAL, List.of('A', 'B', 'C'), new Position(0, 0));
        
        // Render with active brick
        String output = field.render(Optional.of(brick));
        assertTrue(output.contains("ABC."));
        
        // Render without active brick
        output = field.render(Optional.empty());
        assertFalse(output.contains("ABC"));
    }
}