package com.gic.investment;

public final class GameConstants {
    private GameConstants() {}

    public static final String INIT_MSG = """
            Please enter field size(width and height) and upto 5 bricks set:
            Example: 5 8 H^^* V*@^
            The first two numbers are the field width and height.
            Each brick is defined by orientation ('H' for horizontal and 'v'  for vertical) folloowed by three symbols.
            Allowed symbols: '~','^','*','@'.
            Up to 5 bricks can be provided.
            """;
    public static final String INVALID_BRICK_SIZE_MSG ="Invalid input! Check input parameter like field size and brick of the game.Either no brick or more than 5 bricks has been provided";
}
