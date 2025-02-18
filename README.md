# Network-Based Battleship

## Overview
Network-Based Battleship is an interactive 1v1 multiplayer game inspired by the classic Battleship. This project implements a robust client/server architecture using threads and sockets to enable real-time, bidirectional communication between players. Choose to enter a game via a matchmaking queue or by creating a private room with a join code.

## Game Modes & Features
- **Lobby & Pre-Game Phase**:  
  Once two players are paired, the game enters the **PRE_GAME** stage. Each player places five ships on a grid (default 16×16; alternatives: 12×12, 20×20, 24×24 based on majority vote) selecting from six ship types (2×1, L-shape, two 3×3 variants, 4×1, and S-shape). If a player fails to place ships within 1:30 minutes, the remaining ships are automatically positioned.

- **In-Game Mode**:  
  After setup, players assume the roles of attacker and defender. The attacker targets grid cells, with successful hits adding 15 energy and sinking ships granting 20 energy. A hit extends the turn timer by 25 seconds, and players may continue attacking within the maximum turn duration of 3 minutes.

- **Energy & Items System**:  
  Every turn begins with 15 energy. Additional energy can be earned through gameplay, and players can strategically use items:
    - **Airstrike (120 energy):** Attack an entire horizontal or vertical row.
    - **Radar (40 energy):** Reveal the number of ship parts within a 3×3 area centered on a mine.
    - **Bomb (60 energy):** Uncover a 2×2 section of the grid.

- **Turn Timers**:  
  Each turn is initially limited to 40 seconds, with time extensions on successful hits.

- **Endgame & Statistics**:  
  The match concludes when one player loses all their ships. The final screen displays the winner along with detailed statistics such as total playtime, items used, and energy accumulated.

## Technical Implementation
- **Language & IDE**: Java (developed in Eclipse)
- **Architecture**:
    - **Server:** Manages game sessions, synchronizes player actions, and communicates with clients using threads and sockets.
    - **Client:** Handles user interactions, updates the game state, and renders the interface.
- **Design Philosophy:**  
  The project emphasizes a clean separation between game logic, presentation, and network communication to ensure maintainability and scalability.

## Project Timeline & Deliverables
- **Project Start:** February 18, 2025
- **Exposé Submission:** March 2, 2025
- **First Review:** June 21, 2025
- **Colloquium & Final Revision:** July 7–11, 2025 (Final submission one week after the colloquium)

## Getting Started
1. **Clone the repository.**
2. **Import the project into Eclipse.**
3. **Build and run the project.**
4. **Follow the on-screen instructions to register and join a game session.**

## Contributing
For further development, please refer to the provided coding standards and project guidelines in the documentation.

## License
[LICENSE.md](Apache License 2.0)

## Acknowledgements
This project was developed as part of Programmierpraktikum 2, integrating network programming, multithreading, and interactive game mechanics into a challenging, didactically rich application.