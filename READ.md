Design Patterns & Principles Used:

TDD-Ready Structure: Classes are highly testable with pure functions and immutable state
Immutable Value Objects: Position, Block, Brick using records
Immutable State Pattern: GameState and Field always return new instances
Functional Programming:

Extensive use of Stream API
Pure functions without side effects
Optional for null safety
Pattern matching with switch expressions
Method references and lambda expressions


Strategy Pattern: Command processing through functional composition
State Pattern: Game state transitions are explicit
Single Responsibility: Each class has one clear purpose