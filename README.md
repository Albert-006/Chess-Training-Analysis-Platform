# Chess Training & Analysis Platform

JavaFX desktop application for playing chess against an AI, reviewing mistakes, solving tactical puzzles, exploring openings, tracking Elo progress, and saving/replaying games.

## Tech Stack

- Java 17 or Java 21
- JavaFX
- Maven
- Java serialization for file handling
- JUnit 5
- MVC-style package organization

## Run

Install JDK 17+ and Maven, then run:

```bash
mvn clean test
mvn javafx:run
```

The application stores runtime data under:

```text
data/
├── users/
├── games/
├── puzzles/
├── openings/
└── settings/
```

The folders are created automatically when data is saved.

## Packages

```text
src/main/java/
├── model/       chess pieces, moves, positions, users
├── engine/      board state, legal move generation, check/checkmate/stalemate
├── ai/          minimax, alpha-beta pruning, evaluation function
├── analysis/    move classification, blunder/mistake detection, accuracy
├── puzzle/      tactical puzzle models, built-in puzzles, validation
├── opening/     opening explorer and move-prefix matching
├── storage/     serializable user/game persistence
├── util/        Elo and password hashing
├── controller/  application coordination
└── view/        JavaFX screens and chess board
```

## Implemented Features

- Full legal move validation
- Check, checkmate, and stalemate detection
- Castling, en passant, and promotion
- Drag-and-drop and click-to-move JavaFX board
- Move highlighting
- Undo and redo
- AI opponent using Minimax and Alpha-Beta pruning
- Easy, Medium, and Hard difficulty depths
- Evaluation using material, center control, king safety, pawn structure, mobility, and activity
- Post-game analysis with Best, Good, Inaccuracy, Mistake, and Blunder labels
- Evaluation graph
- Tactical puzzle mode
- Opening explorer with Sicilian, French, Ruy Lopez, Italian, Queen's Gambit, and King's Indian Defense
- Elo update formula
- User storage with SHA-256 password hashing
- Save and replay games

## Tests

JUnit tests cover:

- Legal move generation
- Illegal move rejection
- En passant target creation
- Undo and redo
- Checkmate detection
- AI move selection
- Elo calculation
- File saving/loading
- Puzzle validation
