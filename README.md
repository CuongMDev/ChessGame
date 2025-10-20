# ♟️ Chess Game (JavaFX GUI)

A modern **chess application** built with **JavaFX**, featuring a graphical interface that supports both **2-player mode** and **Play vs Bot** mode.  
The bot is powered by an **external MCTS (Monte Carlo Tree Search) engine**, developed as a separate project.

---

## 🚀 Features

### 🎮 Game Modes
- **Player vs Player**  
  Two players can play locally on the same board.

- **Player vs Bot**
    - Choose to play as **White** or **Black** before starting.
    - The bot uses a **Monte Carlo Tree Search (MCTS)** engine for decision-making.
    - Adjustable **Thinking Ability**, **Game Music**, and **Search Thread** in the Settings menu.

---

## ⚙️ Settings

Customize your gameplay and fine-tune the bot’s performance:

| Setting | Description | Notes |
|----------|--------------|-------|
| 🧠 Bot Thinking | Controls the **Thinking Ability**. | Higher values = stronger but slower AI. |
| 🔊 Sound | Toggle move and game-end sounds on/off. | Helps reduce distractions. |
| 🧵 Search Thread | Number of threads the bot uses during search. | Recommended: **32 threads**. Setting too high may reduce search efficiency. |

---

## 🧩 Architecture Overview

This project consists of **two main repositories**:

| Component | Description | Repository |
|------------|--------------|-------------|
| 🎨 **ChessGame (GUI)** | JavaFX graphical interface, user interaction, and match control. | [👉 View on GitHub](https://github.com/CuongMDev/ChessGame) |
| 🧠 **ChessAI (Engine)** | AI engine using **Monte Carlo Tree Search (MCTS)** for move evaluation and decision-making. | [👉 View on GitHub](https://github.com/CuongMDev/ChessAI) |

The GUI communicates with the AI engine through a **socket connection**, sending board positions and receiving the best move suggested by the AI.

---

## 🖼️ User Interface

- Built with **JavaFX** and **FXML**.
- Clean, modern layout with interactive chessboard.
- Integrated **menu**, **settings**, and **gameplay** screens.

---

## 🧠 AI Engine (MCTS)

The external AI engine implements the **Monte Carlo Tree Search** algorithm with four main phases:

1. **Selection** – Selects the most promising node based on the UCT (Upper Confidence bound for Trees) formula.
2. **Expansion** – Expands new nodes by exploring possible moves.
3. **Simulation** – Runs random playouts to estimate outcome probabilities.
4. **Backpropagation** – Updates statistics along the search path.

You can find detailed information and source code in the [ChessAI repository](https://github.com/CuongMDev/ChessAI).
