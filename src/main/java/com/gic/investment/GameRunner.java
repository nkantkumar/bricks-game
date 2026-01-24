package com.gic.investment;

import java.util.*;
import java.util.stream.Collectors;

// Game.java - Main game controller
class GameRunner {
    private static final Set<Character> VALID_SYMBOLS = Set.of('~', '^', '*', '@');
    private static final Set<Character> VALID_COMMANDS = Set.of('L', 'R', 'D');

    private final Scanner scanner;

    public GameRunner() {
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("Welcome to the Match-3 Falling Bricks Game!");

        boolean playing = true;
        while (playing) {
            playGame();
            playing = askPlayAgain();
        }

        System.out.println("Thank you for playing Match-3 Falling Bricks Game!");
    }

    private void playGame() {
        GameState state = initializeGame();
        if (state == null) return;

        state = state.spawnNextBrick();

        while (!state.isGameOver()) {
            displayGame(state);

            List<Character> commands = getCommands();
            state = processCommands(state, commands);
            state = state.moveDownOrPlace();

            if (state.needsNewBrick()) {
                state = state.spawnNextBrick();
            }
        }
        System.out.println(state.getField().render(Optional.empty()));
        System.out.println("\nGame Over!");
    }

    private GameState initializeGame() {
        System.out.println("Please enter init string (e.g., 5 8 H^^* V*@^):");
        String line = scanner.nextLine().trim();
        if (line.isEmpty()) return null;

        String[] parts = line.split("\\s+");
        if (parts.length < 2) {
            System.out.println("Invalid input.");
            return null;
        }

        int width = 10;
        int height = 20;
        Queue<Brick> bricks = new LinkedList<>();

        try {
            width = Integer.parseInt(parts[0]);
            height = Integer.parseInt(parts[1]);

            for (int i = 2; i < parts.length; i++) {
                String token = parts[i];
                if (token.length() != 4) continue;

                char orientChar = token.charAt(0);
                String symbols = token.substring(1);

                Orientation orientation = (orientChar == 'V' || orientChar == 'v') ? Orientation.VERTICAL : Orientation.HORIZONTAL;
                
                List<Character> symbolList = symbols.chars()
                        .mapToObj(c -> (char) c)
                        .filter(VALID_SYMBOLS::contains)
                        .collect(Collectors.toList());

                if (symbolList.size() != 3) continue;

                Position startPos = orientation == Orientation.HORIZONTAL ?
                        new Position(0, (width - 3) / 2) :
                        new Position(0, width / 2);

                bricks.add(new Brick(orientation, symbolList, startPos));
            }
        } catch (Exception e) {
            System.out.println("Error parsing input: " + e.getMessage());
            return null;
        }

        if (bricks.isEmpty()) {
            System.out.println("No valid bricks provided.");
        }

        Field field = new Field(width, height);
        return new GameState(field, bricks);
    }

    private void displayGame(GameState state) {
        System.out.println("\n" + state.getField().render(state.getActiveBrick()));
    }

    private List<Character> getCommands() {
        System.out.print("Enter up to 2 commands (L/R/D): ");
        return scanner.nextLine().toUpperCase().chars()
                .mapToObj(c -> (char) c)
                .filter(VALID_COMMANDS::contains)
                .limit(2)
                .toList();
    }

    private GameState processCommands(GameState state, List<Character> commands) {
        return commands.stream()
                .reduce(state, GameState::processCommand, (s1, s2) -> s2);
    }

    private boolean askPlayAgain() {
        System.out.print("\nEnter S to start over or Q to quit: ");
        String input = scanner.nextLine().trim().toUpperCase();
        return input.equals("S");
    }

    public static void main(String[] args) {
        new GameRunner().start();
    }
}
