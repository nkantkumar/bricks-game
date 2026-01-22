package com.gic.investment;

record Position(int row, int col) {
    Position move(int deltaRow, int deltaCol) {
        return new Position(row + deltaRow, col + deltaCol);
    }
}
