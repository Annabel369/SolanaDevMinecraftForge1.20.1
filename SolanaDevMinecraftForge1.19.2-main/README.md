# SolanaDevMinecraftForge1.19.2 🚀⛏️

This repository contains an integration between the **Solana** blockchain and **Minecraft (Java Edition)** using **Minecraft Forge 1.19.2**. The goal of this mod is to allow developers and players to connect their Solana wallets to the game, interact with smart contracts (programs), or manage in-game items based on tokens or NFTs from the Solana network.

---

#✨ Key Features & Commands
All commands work in both English and Portuguese!

#💰 Economy & Wallet

    /createwallet or /criarcarteira: Generate your test Solana wallet.
    /balance or /saldo: Check your in-game coin balance (PandaCoins).
    /solbalance or /solsaldo: View your Solana address and SOL balance.
    /airdrop: Receive 2 test SOL (Devnet) to start playing.
    /buycoins [amount] or /comprarmoedas: Convert SOL to in-game currency.
    /transfersol [player] [amount] or /transferirsol: Send test SOL to another player.
#🔒 Protection & Utilities

    /lockchest [password] or /trancarbau: Secure a chest with a password.
    /unlockchest [password] or /destrancarbau: Remove protection from a chest.
    /sethome [name]: Set your home position.
    /home [name]: Teleport back to your home.
    /tpa [player]: Send a teleport request to a friend.
    /tpaccept or /tpaceitar: Accept a teleport request.
    /back: Return to your previous location (or death point).
#🛒 Special Store

    /buy_boots or /comprar_botas: Get the Celestial Relic Boots.
    /buy_wings or /comprar_asas: Get the Amauris Relic Wings.
    /buy_pants or /comprar_calca: Get the Absolute Shield Pants.


## 📋 Prerequisites

Before you begin, ensure you have the following tools installed on your computer:

1. **Java Development Kit (JDK) 17**: Required for Minecraft 1.19.2 and Forge.
   * [Download JDK 17 (Oracle)](https://www.oracle.com/java/technologies/downloads/#java17) or use OpenJDK (Adoptium Temurin).
2. **Minecraft Launcher**: The official launcher or an alternative of your choice (Prism Launcher, CurseForge App, etc.).
3. **Minecraft Account**: A legitimate Java Edition account to load the game.
4. **Solana Environment (For Developers)**:
   * Solana CLI installed ([Official Instructions](https://docs.solanalabs.com/cli/install)).
   * A test wallet (File-system wallet or Phantom configured for Devnet/Testnet).
5. **Git**: To clone the repository.
   * [Download Git](https://git-scm.com/)

---

## 🌍 Multi-language Support
The mod now automatically detects your Minecraft language settings and supports:
*   **English (US)**
*   **Portuguese (Brazil)**

All in-game messages, item names, and command feedbacks are fully localized.

---

## 🛠️ Installation and Configuration (Development)

If you are a developer and wish to compile, modify, or test the mod locally, follow the steps below:

### 1. Clone the Repository
Open your terminal (Command Prompt, PowerShell, or Git Bash) and run:
```bash
git clone https://github.com/Annabel369/SolanaDevMinecraftForge1.19.2.git
cd SolanaDevMinecraftForge1.19.2
```

### 2. Configure the Development Environment
The project uses Gradle to manage Forge dependencies. In the terminal, run the command corresponding to your operating system to download the Forge MDK and generate the necessary files:

**Windows (PowerShell or CMD):**
```bash
./gradlew genEclipseRuns  # If using Eclipse
# OR
./gradlew genIntellijRuns # If using IntelliJ IDEA (Recommended)
```

**Linux / macOS:**
```bash
chmod +x gradlew
./gradlew genIntellijRuns
```

### 3. Opening the Project in your IDE
1. Open IntelliJ IDEA (or Eclipse).
2. Select the **Open** option and choose the root folder of the cloned project.
3. Wait for Gradle to import all Minecraft and Forge dependencies (this may take a few minutes the first time).
4. In IntelliJ, reload the Gradle project if the run buttons do not appear automatically.

### 4. Running the Game in Test Mode
To run Minecraft with the mod loaded directly from the IDE:

* **Via IDE:** Look for the run configuration named `runClient` and click **Run**.
* **Via Terminal:**
```bash
# Windows
./gradlew runClient

# Linux / macOS
./gradlew runClient
```

---

## 📦 How to Build the Mod (.jar)
If you have finished your modifications and want to generate the mod file to install in your regular `.minecraft` folder or upload to a server:

In the terminal, run:
```bash
./gradlew build
```

After the process finishes (**BUILD SUCCESSFUL**), the compiled `.jar` file will be located in:
`build/libs/SolanaDevMinecraftForge1.19.2-[version].jar`

---

## 🎮 How to Install the Mod (Players)
If you just want to play/test the compiled mod in your standard Minecraft:

1. Make sure you have **Minecraft Forge 1.19.2** installed in your Minecraft Launcher.
   * If not, download the installer from [files.minecraftforge.net](https://files.minecraftforge.net/) (Recommended or Latest version) and run it.
2. Download or take the `.jar` file generated in the `build/libs/` folder (or from the GitHub Releases tab, if available).
3. Open the Windows "Run" menu (`Win + R`), type `%appdata%` and click OK.
4. Navigate to the `.minecraft` folder and then open the **mods** folder (if it doesn't exist, create a folder named `mods` in lowercase).
5. Paste the `.jar` file inside the **mods** folder.
6. Open the Minecraft Launcher, select the Forge 1.19.2 profile, and click Play.

---

## 🌐 Solana Connection Configuration
*(Adjust this section based on your mod's specific logic, such as RPC config files or keys)*

https://github.com/Annabel369/WebSolana

By default, the mod is configured to connect to the Solana **Devnet**.

Make sure to change the RPC URL in the mod's configuration file (usually generated at `.minecraft/config/solanadevmod-client.toml` after the first run) if you want to use Mainnet or a local Testnet.

Default test RPC: `https://api.devnet.solana.com`

---

## 🤝 Contributing
Contributions are very welcome! If you find a bug or have ideas for new integration features (such as SPL-Token support, NFTs, or wallet authentication via QR Code):

1. **Fork** the project.
2. Create a **Branch** for your modification (`git checkout -b feature/NewFeature`).
3. **Commit** your changes (`git commit -m 'Added feature X'`).
4. Push to the remote repository (`git push origin feature/NewFeature`).
5. Open a **Pull Request**.

---

## 📄 License
This project is under the MIT license. See the [LICENSE](LICENSE) file for details.
