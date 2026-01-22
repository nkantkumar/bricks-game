package com.gic.investment;


import java.util.List;

class Brick {
    private final Orientation orientation;
    private final List<Character> symbols;
    private Position position;

    public Brick(Orientation orientation, List<Character> symbols, Position position) {
        this.orientation = orientation;
        this.symbols = List.copyOf(symbols);
        this.position = position;
    }

    public List<Block> getBlocks() {
        return switch (orientation) {
            case HORIZONTAL -> List.of(
                    new Block(position, symbols.get(0)),
                    new Block(position.move(0, 1), symbols.get(1)),
                    new Block(position.move(0, 2), symbols.get(2))
            );
            case VERTICAL -> List.of(
                    new Block(position, symbols.get(0)),
                    new Block(position.move(1, 0), symbols.get(1)),
                    new Block(position.move(2, 0), symbols.get(2))
            );
        };
    }

    public Brick moveLeft() {
        return new Brick(orientation, symbols, position.move(0, -1));
    }

    public Brick moveRight() {
        return new Brick(orientation, symbols, position.move(0, 1));
    }

    public Brick moveDown() {
        return new Brick(orientation, symbols, position.move(1, 0));
    }

    public Position getPosition() { return position; }
    public Orientation getOrientation() { return orientation; }
}
