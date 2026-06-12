# SolanaDevMinecraftForge1.20.1 🚀⛏️

Este repositório contém uma integração entre a blockchain **Solana** e o **Minecraft (Java Edition)** utilizando o **Minecraft Forge 1.20.1**. O objetivo deste mod é permitir que desenvolvedores e jogadores conectem suas carteiras Solana ao jogo, interajam com smart contracts (programs), ou gerenciem itens dentro do jogo baseados em tokens ou NFTs da rede Solana.

---

## 📋 Pré-requisitos

Antes de começar, certifique-se de ter as seguintes ferramentas instaladas no seu computador:

1. **Java Development Kit (JDK) 17**: Necessário para o Minecraft 1.20.1 e Forge.
   * [Baixar JDK 17 (Oracle)](https://www.oracle.com/java/technologies/downloads/#java17) ou usar o OpenJDK (Adoptium Temurin).
2. **Minecraft Launcher**: O launcher oficial ou um alternativo de sua preferência (Prism Launcher, CurseForge App, etc.).
3. **Conta Minecraft**: Uma conta original (Java Edition) para carregar o jogo.
4. **Ambiente Solana (Para Desenvolvedores)**:
   * Solana CLI instalada ([Instruções oficiais](https://docs.solanalabs.com/cli/install)).
   * Uma carteira de testes (File-system wallet ou Phantom configurada na Devnet/Testnet).
5. **Git**: Para clonar o repositório.
   * [Baixar Git](https://git-scm.com/)

---

## 🛠️ Como Instalar e Configurar (Desenvolvimento)

Se você é um desenvolvedor e deseja compilar, modificar ou testar o mod localmente, siga os passos abaixo:

### 1. Clonar o Repositório
Abra o seu terminal (Prompt de Comando, PowerShell ou Git Bash) e execute:
```bash
git clone https://github.com/Annabel369/SolanaDevMinecraftForge1.20.1.git
cd SolanaDevMinecraftForge1.20.1
```

### 2. Configurar o Ambiente de Desenvolvimento
O projeto utiliza o Gradle para gerenciar as dependências do Forge. No terminal, execute o comando correspondente ao seu sistema operacional para baixar o Forge MDK e gerar os arquivos necessários:

**Windows (PowerShell ou CMD):**
```bash
./gradlew genEclipseRuns  # Se for usar o Eclipse
# OU
./gradlew genIntellijRuns # Se for usar o IntelliJ IDEA (Recomendado)
```

**Linux / macOS:**
```bash
chmod +x gradlew
./gradlew genIntellijRuns
```

### 3. Abrindo o Projeto na IDE
1. Abra o IntelliJ IDEA (ou Eclipse).
2. Escolha a opção **Open** (Abrir) e selecione a pasta raiz do projeto clonado.
3. Aguarde o Gradle importar todas as dependências do Minecraft e do Forge (isso pode demorar alguns minutos na primeira vez).
4. No IntelliJ, recarregue o projeto Gradle se os botões de execução não aparecerem automaticamente.

### 4. Executando o Jogo em Modo de Teste
Para rodar o Minecraft com o mod carregado diretamente da IDE:

* **Via IDE:** Procure pela configuração de execução chamada `runClient` e clique em **Run** (Executar).
* **Via Terminal:**
```bash
# Windows
./gradlew runClient

# Linux / macOS
./gradlew runClient
```

---

## 📦 Como Compilar o Mod (.jar)
Se você terminou suas modificações e quer gerar o arquivo do mod para instalar na sua pasta `.minecraft` normal ou enviar para um servidor:

No terminal, execute o comando:
```bash
./gradlew build
```

Após o término do processo (**BUILD SUCCESSFUL**), o arquivo `.jar` compilado estará localizado na pasta:
`build/libs/SolanaDevMinecraftForge1.20.1-[versão].jar`

---

## 🎮 Como Instalar o Mod no Minecraft (Jogador)
Se você quer apenas jogar/testar o mod compilado no seu Minecraft padrão:

1. Certifique-se de ter o **Minecraft Forge 1.20.1** instalado no seu Minecraft Launcher.
   * Se não tiver, baixe o instalador em [files.minecraftforge.net](https://files.minecraftforge.net/) (versão Recommended ou Latest) e execute-o.
2. Baixe ou pegue o arquivo `.jar` gerado na pasta `build/libs/` (ou na aba Releases do GitHub, se houver).
3. Abra o menu "Executar" do Windows (`Win + R`), digite `%appdata%` e clique em OK.
4. Navegue até a pasta `.minecraft` e depois abra a pasta **mods** (se ela não existir, crie uma pasta chamada `mods` em letras minúsculas).
5. Cole o arquivo `.jar` dentro da pasta **mods**.
6. Abra o Minecraft Launcher, selecione o perfil do Forge 1.20.1 e clique em Jogar.

---

## 🌐 Configuração da Conexão com a Solana
*(Ajuste esta seção de acordo com a lógica específica do seu mod, como arquivos de configuração de RPC ou chaves)*

Por padrão, o mod está configurado para se conectar à **Devnet** da Solana.

Certifique-se de alterar a URL de RPC no arquivo de configuração do mod (geralmente gerado em `.minecraft/config/solanadevmod-client.toml` após a primeira execução) se quiser utilizar a Mainnet ou uma Testnet local.

Exemplo de RPC padrão: `https://api.devnet.solana.com`

---

## 🤝 Contribuições
Contribuições são super bem-vindas! Se você encontrar algum bug ou tiver ideias de novas funções de integração (como suporte a SPL-Tokens, NFTs ou autenticação de carteira via QR Code):

1. Faça um **Fork** do projeto.
2. Crie uma **Branch** para sua modificação (`git checkout -b feature/NovaFuncionalidade`).
3. Faça o **Commit** das suas alterações (`git commit -m 'Adicionando funcionalidade X'`).
4. Envie para o repositório remoto (`git push origin feature/NovaFuncionalidade`).
5. Abra um **Pull Request**.

---

## 📄 Licença
Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.
