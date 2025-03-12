[![Java Build](https://github.com/robert-kratz/battleship-game/actions/workflows/java-ci.yml/badge.svg)](https://github.com/robert-kratz/battleship-game/actions/workflows/java-ci.yml)

# Network-Based Battleship

<img src="https://raw.githubusercontent.com/robert-kratz/battleship-game/fe8ee47b24cbdbf348aa2b1d02f404d0bce8b43e/resource/battle_ships_landing.gif" width="70%" style="max-width: 200px; text-align: center" alt="Logo"/>

Network-Based Battleship is a fully featured, real-time multiplayer game inspired by the classic Battleship. Built using Java with a robust client/server architecture based on threads and sockets, this project supports both matchmaking and private games with customizable game settings. The project was developed as part of a programming practicum to demonstrate advanced network programming, multithreading, and interactive GUI design.


---

## Table of Contents

- [Overview](#overview)
- [Key Features](#key-features)
- [Game Flow and Phases](#game-flow-and-phases)
  - [Connection & Registration](#connection--registration)
  - [Lobby and Game Creation](#lobby-and-game-creation)
  - [Build Phase (Ship Placement)](#build-phase-ship-placement)
  - [In-Game Phase (Battle)](#in-game-phase-battle)
  - [Game Over and Statistics](#game-over-and-statistics)
- [Protocol and Messaging](#protocol-and-messaging)
- [Technical Implementation](#technical-implementation)
- [Installation and Setup](#installation-and-setup)
- [Seminar Context & Documentation Requirements](#seminar-context--documentation-requirements)
- [Timeline and Project Milestones](#timeline-and-project-milestones)
- [Contributing](#contributing)
- [Acknowledgements](#acknowledgements)

## Overview

Network-Based Battleship is a competitive 1v1 game where players place a set of ships on a grid and then take turns attacking their opponent’s board. The game supports a customizable board size, configurable timers, energy management, and an item system that adds strategic depth to each turn. The client and server communicate via a set of well-defined messages that maintain a snapshot of the game state throughout the match.

## Key Features

- **Real-Time Multiplayer:** Players connect to a central server via sockets on a default port (12345), with the port configurable via command-line arguments.
- **Matchmaking & Private Games:** Users can enter a matchmaking queue or create/join games using unique session codes.
- **Customizable Game Options:** In the lobby, players can adjust game settings including board size, build time, move time, bonus time, and energy parameters using an intuitive Swing-based GUI.
- **Dynamic Ship Placement:** In the build phase, players select ships from a left-hand panel and place them on the board via mouse interaction. Ships must fit completely on the board and must not overlap.
- **Energy and Item System:** Players earn energy on every move and from hitting opponent ships. They can spend energy on items:
  - **Sea Bomb:** Attacks a 2×2 area.
  - **Radar:** Reveals the number of ship parts in a 3×3 area.
  - **Air Strike:** Destroys an entire row or column.
- **Turn-Based Gameplay:** Alternating turns with a dynamic timer that can be extended when a move is successful.
- **Comprehensive Messaging Protocol:** A set of messages (e.g., RegisterMessage, QueueUpdateMessage, CreateGameMessage, GameBuildingStartMessage, PlayerMoveMessage, MoveMadeMessage, PlayerTurnChangeMessage, GameOverMessage) ensures synchronized game state updates.
- **Automatic Fallback:** If a player does not finish placing ships within the build phase, the server randomizes the remaining ship placements.

## Game Flow and Phases

### Connection & Registration

- **Server Startup:**  
  The server automatically starts a `ServerSocket` on port **12345** (modifiable using the `--p PORT` argument). When a new client connects, a `ServerPlayer` instance is created. This object handles the client’s communication on a dedicated thread.

- **Registration:**  
  Immediately after connection, the server sends a **RegisterMessage** containing the generated username and unique user ID. A **QueueUpdateMessage** is also sent to inform the client about the current waiting queue.

### Lobby and Game Creation

- **Lobby Options:**  
  On the client lobby screen, players are offered three options:
  - **Enter Queue:** Sends a **JoinQueueMessage** to be matched automatically when another player is waiting.
  - **Create:** Opens a custom game creation screen where players can configure game options.
  - **Join:** Allows joining a game by entering a session code provided by a waiting game.

- **Game Creation GUI (CreateGameScene):**  
  Players can choose:
  - **Board Size:** Options like 8×8, 10×10 (default), or 12×12.
  - **Timers:** Adjust build time (ship placement time), move time (per turn), and bonus time for successful moves.
  - **Energy Settings:** Set the starting energy, energy reward per hit, and energy bonus per turn.

  Once the player clicks **Create**, a **CreateGameMessage** is sent with the chosen **GameOptions**. The server validates the options (e.g., board size must be within an allowed range) and starts a new `BattleShipGame` thread if valid.

### Build Phase (Ship Placement)

- **GUI Layout (GameBuildScene):**  
  The build phase GUI is divided into three parts:
  - **Left Panel:** Displays available ships as selectable buttons. Clicking a ship selects it for placement.
  - **Center Panel (Game Board):** The grid where ships are placed. Ships must be positioned fully on the board without overlapping.
  - **Right Panel:** Shows player and opponent status, a timer, and additional controls (e.g., randomize placement, ready button).

- **Interaction:**  
  When a player selects a ship from the left panel and clicks on the board, the ship is placed. Each placement triggers a **PlayerUpdateShipPlacement** message to the server. Once all ships are placed, the player can click **Ready**. This locks the board and sends a **PlayerReadyMessage**. If both players are ready before the build timer expires, the server skips the remaining build time; otherwise, the server randomizes the remaining placements and informs players via a **GameInGameStartMessage**.

### In-Game Phase (Battle)

- **Turn Structure:**  
  The game transitions into the in-game phase where players alternate turns. At the start of a turn, the server sends a **PlayerTurnChangeMessage** along with a snapshot of the current **GameState**. The active player sees the opponent’s board with hidden cells, and can click on cells to reveal them.

- **Making a Move:**  
  When a move is made:
  - The client sends a **PlayerMoveMessage** containing a **Move** object.
  - The **Move** is validated by the server. If valid:
    - The server broadcasts a **MoveMadeMessage** with the updated **GameState**.
    - If the move is a hit, energy is awarded (and the turn timer extended) so the player can continue attacking.
    - If the move is a miss, the turn ends and the server issues a new **PlayerTurnChangeMessage**.

- **Item Usage:**  
  Players may use items by selecting the corresponding button (Bomb, Radar, or Air Strike) from the left panel:
  - **Sea Bomb:** Targets a 2×2 area.
  - **Radar:** Reveals the number of ship parts in a 3×3 area (with the selected cell at the center).
  - **Air Strike:** Eliminates an entire row or column; can be toggled between horizontal and vertical using keyboard inputs (e.g., R or SPACE).

  Item moves are sent as part of the **PlayerMoveMessage** and are validated for energy cost before execution.

- **Hover Effects:**  
  When hovering over the board, the client sends a **PlayerHoverMessage**. The server relays this to the opponent so that a ghost overlay (e.g., highlighted cells or preview of item effects) is displayed.

### Game Over and Statistics

- **Ending Conditions:**  
  The game ends when:
  - One player sinks all opponent ships.
  - No valid moves remain.
  - A player gives up via the **Give Up** button.
  - A player disconnects.

  Upon game over, the server sends a **GameOverMessage** with the final **GameState**.

- **Game Over Screen (GameOverScene):**  
  The GUI displays:
  - The result ("You won" or "You lost").
  - The winner and loser names.
  - Detailed statistics (energy accumulated, number of moves, etc.).
  - A button to return to the lobby.

## Protocol and Messaging

The communication between the client and server is facilitated by a series of serializable messages. Key message types include:

- **Registration and Lobby:**
  - `RegisterMessage`: Sent by the server upon client connection.
  - `QueueUpdateMessage`: Notifies clients about their position in the matchmaking queue.
  - `CreateGameMessage` & `JoinGameWithCodeMessage`: For creating and joining games.

- **Build Phase:**
  - `PlayerUpdateShipPlacement`: Sent when the client updates ship placement.
  - `PlayerReadyMessage` & `BuildReadyStateChangeMessage`: For updating and broadcasting ready states.
  - `GameBuildingStartMessage`: Signals the start of the build phase.

- **In-Game Phase:**
  - `GameInGameStartMessage`: Initiates the in-game phase once ship placement is finalized.
  - `PlayerTurnChangeMessage`: Notifies clients when the active turn changes.
  - `PlayerMoveMessage`: Sent by the client when a move is made.
  - `MoveMadeMessage`: Broadcast by the server after a move is validated and applied.
  - `PlayerHoverMessage`: Transmits hover events for visual feedback.
  - `LeaveGameMessage`: Sent when a player exits the game.
  - `GameOverMessage`: Broadcast when the game ends, detailing winners, losers, and statistics.

- **Game State Snapshot:**  
  The `GameState` object contains all relevant data such as board size, available ships, player details (using `ClientPlayer`), session code, timers, moves, energy levels, and turn information.

## Technical Implementation

- **Language & Frameworks:**
  - Developed in Java (Eclipse IDE recommended).
  - GUI implemented using Swing.

- **Architecture:**
  - **Server:**
    - Listens for incoming connections on a configurable port (default **12345**).
    - Each client connection is managed by a dedicated `ServerPlayer` instance running on its own thread.
    - Maintains a matchmaking queue and a registry of active games using a `GameContainer` that bundles each game’s thread and instance.
    - Handles game logic in the `BattleShipGame` class, which governs the build phase, in-game mechanics, move validation, and game termination.

  - **Client:**
    - Connects to the server using default host (`localhost`) and port (modifiable via command-line arguments).
    - Displays various scenes (Lobby, Create Game, Build Phase, In-Game, Game Over) managed by different GUI classes.
    - Communicates with the server by sending and receiving serializable message objects.

- **Game Options & Parameters:**  
  Default parameters such as build time, move time, energy rewards, and item costs are configured in the `Parameters` class. These can be adjusted to modify the game experience.

- **Item Mechanics:**  
  Items (Sea Bomb, Radar, Air Strike) extend the base `Item` class and define specific methods to compute affected board cells. Energy costs for these items are enforced and integrated into the move validation process.

## Installation and Setup

1. **Clone the Repository:**

   ```bash
   git clone https://github.com/robert-kratz/battleship-game.git
   cd battleship-game
   ```

2.	Server Setup:
-	Import the project into Eclipse (or your preferred Java IDE).
-	Run the main method in the Server class.
-	Optionally, specify a custom port using the --p PORT argument.
3.	Client Setup:
-	Ensure the server is running.
-	Run the client application.
-	Optionally, specify host and port using --h HOST and --p PORT (default: localhost and 12345).
4. Playing the Game:
-	In the lobby, choose to enter the queue, create a new game, or join an existing game using a session code.
-	Configure game options if creating a game.
-	In the build phase, place your ships by selecting them from the left panel and clicking on the board.
-	Click Ready when finished (or wait for the server to randomize placements).
-	Engage in turn-based play in the in-game phase. Use items and strategize to hit your opponent’s ships.
-	The game ends when one player sinks all enemy ships or a player gives up.

## Seminar Context & Documentation Requirements

This project is part of the “Entwicklung eines Projektes für Programmierpraktikum 2” seminar at the University of Mannheim. The aim was to develop a project that later forms the basis for an assignment sheet for Programmierpraktikum 2. The primary focus was on implementing a client/server architecture using threads and sockets, with the classic Battleship game as the application domain.

Key aspects include:
-	Client/Server Architecture: Clear separation of client and server identities using two-way socket communication.
-	Documentation Requirements:
1.	Exposé (Due 2nd March): A 0.5-1.5 page project description.
2.	Complete Eclipse Project: Full solution implementation.
3.	Eclipse Project with Templates: Starter code for participants.
4.	Assignment Sheet: Created in LaTeX.
5.	Presentation Slides: Provided in PDF format.

## Timeline and Project Milestones

The project followed a structured timeline as part of the seminar requirements:
-	**18 February 2025**: Kick-off event and initial topic briefing.
-	**18 February – 28 February 2025**: Preparation phase and individual topic discussions.
-	**2 March 2025**: Submission of the project exposé (0.5-1.5 pages describing the project).
-	**21 June 2025**: First project submission for review.
-	**28 June 2025**: Submission of review materials.
-	**7 July – 11 July 2025**: Possible revision phase and final project adjustments during the colloquium.
-	Final Submission: One week after the colloquium.
-	Additional Events:
-	Kick-off presentations.
-	Individual topic meetings.
-	Submission of exposés.

## Screenshots
Building phase and in-game phase screenshots are provided below to illustrate the GUI and game mechanics.

![Game Build Phase](https://raw.githubusercontent.com/robert-kratz/battleship-game/99b9c71c95a21f7a104709145f2f8044d7bb878d/resource/build_phase.gif)

In-game phase with item usage and turn-based mechanics.

![Game In-Game Phase](https://raw.githubusercontent.com/robert-kratz/battleship-game/99b9c71c95a21f7a104709145f2f8044d7bb878d/resource/ingame_phase.gif)

## Acknowledgements

This project was developed as part of Programmierpraktikum 2 in the “Entwicklung eines Projektes für Programmierpraktikum 2” seminar at the University of Mannheim and was developed by [Robert Julian Kratz](https://rjks.us)

