# 🎲 RandomTrivia

A high-performance, modular chat trivia plugin for Minecraft servers running **Paper 1.21.4+**. Engage your community with automated trivia sessions, reward systems, and extensible question modules.

---

## 🚀 Quick Start

### 📥 Download the JAR
To get the latest version of RandomTrivia:
1. Go to the **[Releases](https://github.com/Jaxonyuill/RandomTrivia/releases)** page.
2. Download the latest `RandomTrivia-1.0.0.jar` file.
3. Drop the JAR file into your server's `plugins` folder.
4. Restart your server or use a plugin loader to enable it.

### 🛠️ Building from Source
If you prefer to build the plugin yourself:
1. Clone the repository:
   ```bash
   git clone https://github.com/Jaxonyuill/RandomTrivia.git
   ```
2. Navigate to the directory:
   ```bash
   cd RandomTrivia
   ```
3. Build using Gradle:
   ```bash
   ./gradlew build
   ```
4. Find the compiled JAR in `build/libs/`.

---

## 🎮 Commands & Permissions

| Command | Description | Permission |
| :--- | :--- | :--- |
| `/trivia start` | Start a new trivia session | `randomtrivia.admin` |
| `/trivia stop` | Force stop the current session | `randomtrivia.admin` |
| `/trivia skip` | Skip the current question | `randomtrivia.admin` |
| `/trivia score` | View your current score | `randomtrivia.play` |
| `/trivia top` | View the leaderboard | `randomtrivia.play` |
| `/trivia modules` | List active trivia modules | `randomtrivia.admin` |

---

## ⚙️ Configuration

The plugin generates a `config.yml` in the `plugins/RandomTrivia` folder. You can customize:
- **Rewards**: Integrated with **Vault** for economy rewards.
- **Timing**: Question intervals and answer window duration.
- **Messaging**: Full support for MiniMessage formatting.

---

## 🧩 Features
- **Modular Architecture**: Easily add new trivia modules (e.g., Anime, History, Math).
- **Vault Integration**: Automatic economy rewards for winners.
- **Asynchronous Processing**: Designed to never impact server TPS.
- **Modern Tech Stack**: Built with Kotlin and Paper API.

---

## 📄 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
