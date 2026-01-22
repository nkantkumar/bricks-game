package com.gic;

import com.gic.engine.GameEngine;
import com.gic.model.*;
import com.gic.view.ConsoleRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class App {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            runGame(scanner);
            System.out.println("Enter S to start over or Q to quit");
            String choice = scanner.nextLine().trim().toUpperCase();
            if (!choice.equals("S")) {
                break;
            }
        }
        System.out.println("Thank you for playing Match-3 Falling Bricks Game!");
        scanner.close();
    }

    private static void runGame(Scanner scanner) {
        System.out.println("Welcome to the Match-3 game!");
        System.out.println("Please enter init string (e.g., 5 8 H^^* V*@^):");
        
        String line = scanner.nextLine().trim();
        if (line.isEmpty()) return;
        
        String[] parts = line.split("\\s+");
        if (parts.length < 2) {
             System.out.println("Invalid input.");
             return;
        }
        
        int width = 10;
        int height = 20;
        List<Brick> bricks = new ArrayList<>();
        
        try {
            width = Integer.parseInt(parts[0]);
            height = Integer.parseInt(parts[1]);
            
            for (int i = 2; i < parts.length; i++) {
                String token = parts[i];
                if (token.length() != 4) continue; 
                
                char orientChar = token.charAt(0);
                String symbols = token.substring(1);
                
                Orientation orientation = (orientChar == 'V' || orientChar == 'v') ? Orientation.VERTICAL : Orientation.HORIZONTAL;
                List<Block> blocks = symbols.chars()
                        .mapToObj(c -> new Block((char) c))
                        .collect(Collectors.toList());
                
                bricks.add(new Brick(blocks, orientation));
            }
        } catch (Exception e) {
            System.out.println("Error parsing input: " + e.getMessage());
            return;
        }

        if (bricks.isEmpty()) {
             System.out.println("No bricks provided.");
        }

        Field field = new Field(width, height);
        GameEngine engine = new GameEngine(field, bricks);
        ConsoleRenderer renderer = new ConsoleRenderer();

        engine.spawnNextBrick();

        while (!engine.isGameOver()) {
            renderer.render(field, engine);
            System.out.print("Enter up to 2 commands (L, R, D): ");
            String commands = scanner.nextLine().trim().toUpperCase();
            
            processCommands(commands, engine);

            if (!engine.moveDown()) {
                engine.lockBrick();
                engine.checkAndClearMatches();
                engine.spawnNextBrick();
            }
        }
        
        renderer.render(field, engine);
        System.out.println("Game over.");
    }

    private static void processCommands(String commands, GameEngine engine) {
        int count = 0;
        for (char cmd : commands.toCharArray()) {
            if (count >= 2) break;
            switch (cmd) {
                case 'L' -> {
                    engine.moveLeft();
                    count++;
                }
                case 'R' -> {
                    engine.moveRight();
                    count++;
                }
                case 'D' -> {
                    engine.drop();
                    count++;
                }
            }
        }
    }
}