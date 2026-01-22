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

*****************************************************
Key Points to Emphasize:

Immutability everywhere - this is your strongest design choice
Functional programming - shows modern Java knowledge
Testability - every design decision supports TDD
Thoughtful trade-offs - you can justify every choice

*****************************************************
Quick Summary of Alternatives:
1. Event Sourcing - Store all events, replay for state (great for audit trails)
2. Mutable OOP - Traditional approach, best raw performance
3. Entity Component System (ECS) - Game engine pattern, scales to thousands of entities
4. Command Pattern + Memento - Built-in undo/redo from the start
5. Reactive Programming - Event streams, great for async/multiplayer
6. State Machine - Explicit phases, clear game flow
   How to Use This in Interview:
   If they ask "Why this approach?"

"I chose functional immutable for testability and thread-safety"
"For a performance-critical 3D game, I'd use ECS"
"For multiplayer with replay, I'd use Event Sourcing"

If they ask "What would you change?"

"If we needed undo/redo, I'd add Command Pattern"
"For more complex state transitions, I'd use State Machine"
"These approaches aren't mutually exclusive - I could combine them"

Key Interview Win:
Show you understand:

Multiple valid solutions exist
Each has clear trade-offs
Choice depends on requirements
You can reason about when to use each

The comparison matrix and decision guide are especially valuable for showing systematic thinking!