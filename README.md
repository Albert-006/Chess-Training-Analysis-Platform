# Chess Training & Analysis Platform

A modern, full-featured desktop chess platform built with **JavaFX** and **Java 17/21**. The application provides an interactive environment for playing against an adaptive chess AI engine, solving tactical puzzles with progressive hints, analyzing games with move evaluations and graphs, exploring chess openings, tracking performance statistics, and replaying saved matches.

---

## Key Features

### 1. Interactive Chess Gameplay (Player vs AI)
- **Engine Rules & Validation**: Complete legal move validation, check/checkmate/stalemate detection, castling (kingside/queenside), en passant captures, and pawn promotions.
- **AI Opponent**: Powered by **Minimax with Alpha-Beta Pruning** and positional evaluation (material, center control, king safety, pawn structure, piece activity, and mobility) across Easy, Medium, and Hard depths.
- **Move Controls**: Supports both **click-to-move** and **drag-and-drop** piece interaction.
- **Real-Time Board Feedback**:
  - **King in Check Highlighting**: The square beneath an attacked king dynamically highlights red (`#dc2626`) without modifying the piece artwork, automatically clearing when out of check.
  - **Checkmate Screen Overlay**: An overlay appears directly over the board when the game ends, announcing whether the player won or lost (`🏆 Checkmate — You Won!` / `💀 Checkmate — You Lost!` / `⚖ Stalemate — Draw!`), with instant options to **Play Again** or **Review Board**.
  - **Highlights**: Legal destination squares, selected pieces, last moves played, and invalid move indicators.
- **Controls**: Undo, Redo, Hint, New Game, and Game Saving with accuracy calculation.

### 2. Tactical Puzzle Mode
- **Puzzle Progression**: Multi-ply tactical puzzle trainer with automatic opponent counter-moves.
- **Move Validation**: Strictly validates moves against the puzzle's expected solution sequence while verifying board legality.
- **Progressive Hint System**:
  - **1st click**: Highlights and indicates the source square/piece to play.
  - **2nd click**: Highlights both the source square and target destination square.
  - Generates hints without modifying the position, advancing plies, or corrupting state.
- **Controls & Ratings**: Success and error feedback banners, puzzle reset, random puzzle navigation, and player puzzle rating updates.

### 3. Game Analysis & Evaluation
- **Move Classification**: Evaluates every ply and classifies moves into **Best**, **Good**, **Inaccuracy**, **Mistake**, and **Blunder**.
- **Accuracy Estimation**: Computes an overall game accuracy percentage.
- **Visual Evaluation Graph**: Interactive JavaFX line chart plotting centipawn advantage across move numbers alongside a detailed analysis table.

### 4. Opening Explorer & Game Replay
- **Opening Identification**: Real-time opening detection and naming (Sicilian Defense, French Defense, Ruy Lopez, Italian Game, Queen's Gambit, King's Indian Defense, etc.) with key strategic concepts.
- **Game Replay**: Step forwards and backwards through move histories of previously saved matches with board position synchronization.

### 5. Authentication & User Profile
- **Login-First Flow**: Unauthenticated users start on the login/registration screen; main dashboard features are locked until valid authentication.
- **Security**: Passwords hashed using **SHA-256** with salt.
- **User Isolation**: Dedicated game histories, statistics (wins, losses, draws, standard Elo, puzzle rating), and preferences per user.
- **Session Management**: Dedicated logout function that clears session memory and protects user data.

### 6. Customizable Settings & Modern UI
- **Custom Staunton Pieces**: High-resolution piece artwork matching traditional Staunton design with transparent backgrounds, dark charcoal black pieces, light white pieces, and distinct outlines for high contrast across all squares.
- **Appearance Settings**:
  - **Themes**: Dark Mode and Light Mode.
  - **Chessboard Themes**: Classic Green, Ocean Blue, Wood / Walnut, and Monochrome.
  - **Piece Styles**: Staunton Vector / Reference Graphics and Unicode Symbols fallback.
- **Gameplay Toggles**: Highlight legal moves, highlight last move, and show board coordinates (`a-h`, `1-8`).
- **Persistent Preferences**: Settings are stored in `data/settings/<username>.settings` and applied immediately without requiring a restart.

---

## Tech Stack & Architecture

- **Language**: Java 17 / 21
- **UI Framework**: JavaFX 21 (Controls, Graphics, FXML-free pure code architecture)
- **Build System**: Apache Maven
- **Testing**: JUnit 5 (Surefire)
- **Persistence**: File-based Java object serialization (`data/`)
- **Architecture**: MVC (Model-View-Controller) with cleanly separated domain models, engine, AI, analysis, and views.

---

## Project Structure

```text
ChessTrainingAnalysisPlatform/
├── data/                               # Local persistent storage
│   ├── games/                          # Saved games (.game)
│   ├── openings/                       # Opening database
│   ├── puzzles/                        # Puzzle definitions
│   ├── settings/                       # User preferences (.settings)
│   └── users/                          # User credentials and profiles (.user)
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── ai/                     # Minimax, Alpha-Beta pruning, evaluation heuristics
│   │   │   ├── analysis/               # Game analyzer, blunder detector, move classifications
│   │   │   ├── controller/             # GameController application coordinator
│   │   │   ├── engine/                 # Board representation, legal move generator, check detection
│   │   │   ├── model/                  # Piece subclasses, Position, Move, User
│   │   │   ├── opening/                # Opening book and line describer
│   │   │   ├── puzzle/                 # Puzzle, PuzzleService, PuzzleSession, PuzzleValidator
│   │   │   ├── storage/                # FileManager, GameStorage, UserStorage, SettingsStorage, UserSettings
│   │   │   ├── util/                   # EloCalculator, PasswordHasher
│   │   │   └── view/                   # MainApp, BoardView, PieceViewFactory
│   │   └── resources/
│   │       ├── pieces/                 # High-resolution transparent Staunton piece graphics
│   │       └── style.css               # JavaFX CSS theme definitions and styling
│   └── test/
│       └── java/
│           ├── ai/                     # AI depth and move decision tests
│           ├── controller/             # Authentication flow and user isolation tests
│           ├── engine/                 # Board legality and checkmate tests
│           ├── puzzle/                 # Puzzle solving, hint, and reset tests
│           ├── storage/                # Storage serialization and settings tests
│           └── util/                   # Elo rating formula tests
├── pom.xml
└── README.md
```

---

## Getting Started

### Prerequisites
- **JDK 17** or **JDK 21** installed and configured on your `PATH`.
- **Apache Maven 3.8+** installed.

### Build and Test
Run all JUnit 5 test suites to verify engine integrity, puzzle validation, and authentication:
```bash
mvn clean test
```

### Run the Application
Launch the JavaFX platform:
```bash
mvn javafx:run
```

---

## Testing Coverage

The project includes unit and integration tests across all core systems:
1. **Engine & Rules**: Legal move generation, illegal move rejection, castling, en passant, check and checkmate detection.
2. **AI & Heuristics**: Alpha-Beta pruning search depth and move selection.
3. **Puzzles**: Solution sequence validation, multi-ply progression, invalid move rejection, non-mutating hint generation, and board reset.
4. **Authentication & Session**: User registration, login verification, password hash checks, logout session clearing, and cross-user data isolation.
5. **Settings Persistence**: Custom theme, board pattern, and piece style serialization round-trips.
