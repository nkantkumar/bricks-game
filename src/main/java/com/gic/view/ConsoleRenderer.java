package com.gic.view;

import com.gic.engine.GameEngine;
import com.gic.model.Block;
import com.gic.model.Coordinate;
import com.gic.model.Field;

import java.util.Optional;

public class ConsoleRenderer {
    public void render(Field field, GameEngine engine) {
        StringBuilder sb = new StringBuilder();
        
        // Boundry starts here!!
        sb.append("+");
        for (int x = 0; x < field.width(); x++) sb.append("-");
        sb.append("+\n");

        for (int y = 0; y < field.height(); y++) {
            sb.append("|");
            for (int x = 0; x < field.width(); x++) {
                Coordinate coord = new Coordinate(x, y);
                char symbol = ' ';
                
                // Check active brick first
                if (engine.getActiveBrick().isPresent()) {
                    var activeBrick = engine.getActiveBrick().get();
                    var activePos = engine.getActiveBrickPosition();
                    var occupied = activeBrick.getOccupiedCoordinates(activePos);
                    int index = occupied.indexOf(coord);
                    if (index != -1) {
                        symbol = activeBrick.blocks().get(index).symbol();
                    }
                }
                
                // If not active brick, check field
                if (symbol == ' ') {
                    Optional<Block> block = field.getBlock(coord);
                    if (block.isPresent()) {
                        symbol = block.get().symbol();
                    }
                }
                
                sb.append(symbol);
            }
            sb.append("|\n");
        }

        //Boundry ends here!!
        sb.append("+");
        for (int x = 0; x < field.width(); x++) sb.append("-");
        sb.append("+\n");
        
        System.out.print(sb.toString());
    }
}