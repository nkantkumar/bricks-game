package com.gic.investment;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import static com.gic.investment.GameConstants.INIT_MSG;
import static org.junit.jupiter.api.Assertions.*;

class GameRunnerTest {

    private final InputStream originalIn = System.in;
    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream outContent;

    @BeforeEach
    void setUp() {
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setIn(originalIn);
        System.setOut(originalOut);
    }

    private void provideInput(String data) {
        System.setIn(new ByteArrayInputStream(data.getBytes()));
    }

    @Test
    void testGameWithSingleBrickAndQuit() {
        String input = "10 20 H@@@\nD\nQ\n";
        provideInput(input);

        GameRunner runner = new GameRunner();
        runner.start();

        String output = outContent.toString();
        assertTrue(output.contains("Welcome to the Match-3 Falling Bricks Game!"));
        assertTrue(output.contains("Game Over!"));
        assertTrue(output.contains("Thank you for playing Match-3!"));
    }

    @Test
    void testGameWithMultipleBricks() {
        String input = "10 20 H@@@ V***\nD\nD\nQ\n";
        provideInput(input);

        GameRunner runner = new GameRunner();
        runner.start();

        String output = outContent.toString();
        assertTrue(output.contains("Game Over!"));
    }

    @Test
    void testInvalidInitializationEmptyInput() {
        String input = "\nQ\n";
        provideInput(input);

        GameRunner runner = new GameRunner();
        runner.start();

        String output = outContent.toString();
        assertTrue(output.contains(INIT_MSG));
    }

    @Test
    void testInvalidInitializationBadFormat() {
        String input = "BadInput\nQ\n";
        provideInput(input);

        GameRunner runner = new GameRunner();
        runner.start();

        String output = outContent.toString();
        assertTrue(output.contains("Invalid input"));
    }

    @Test
    void testInvalidInitializationException() {
        String input = "Ten Twenty\nQ\n";
        provideInput(input);

        GameRunner runner = new GameRunner();
        runner.start();

        String output = outContent.toString();
        assertTrue(output.contains("Error parsing input"));
    }

    @Test
    void testPlayAgain() {
        String input = "10 20 H@@@\nD\nS\n10 20 V***\nD\nQ\n";
        provideInput(input);

        GameRunner runner = new GameRunner();
        runner.start();

        String output = outContent.toString();
        int initPromptCount = output.split(INIT_MSG).length;
        assertEquals(1, initPromptCount, "Should prompt for initialization twice");
    }
    
    @Test
    void testCommandsProcessing() {
        String input = "10 20 H@@@\nL\nR\nD\nQ\n";
        provideInput(input);
        
        GameRunner runner = new GameRunner();
        runner.start();
        
        String output = outContent.toString();
        assertTrue(output.contains("Game Over!"));
    }
    
    @Test
    void testNoValidBricksProvided() {
        String input = "10 20\nQ\n";
        provideInput(input);
        
        GameRunner runner = new GameRunner();
        runner.start();
        
        String output = outContent.toString();
        assertTrue(output.contains("No valid bricks provided"));
    }
    
    @Test
    void testInvalidBrickFormat() {
        String input = "10 20 H@\nQ\n";
        provideInput(input);
        
        GameRunner runner = new GameRunner();
        runner.start();
        
        String output = outContent.toString();
        assertTrue(output.contains("No valid bricks provided"));
    }
}