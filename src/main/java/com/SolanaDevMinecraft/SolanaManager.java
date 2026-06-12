package com.SolanaDevMinecraft;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SolanaManager {
    private static final Logger LOGGER = LogManager.getLogger();
    private final DatabaseManager databaseManager;

    public SolanaManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    /**
     * Mapeia o nome do jogador no Minecraft para o nome usado no banco de dados e nos arquivos de carteira.
     * De acordo com as instruções do usuário, 'Astral ツ' deve ser mapeado para '007amauri'.
     */
    public static String getEffectiveName(String minecraftName) {
        if (minecraftName.contains("Astral")) {
            return "007amauri";
        }
        return minecraftName.replace(" ", "_");
    }

    public double getSolanaBalance(String walletAddress) throws Exception {
        String host = ConfigManager.DOCKER_HOST.get();
        String apiwebkey = ConfigManager.API_WEB_KEY.get();
        String solanaCmd = ConfigManager.SOLANA_COMMAND.get();
        String comando = solanaCmd + " balance " + walletAddress + " --url https://api.devnet.solana.com";

        String url = String.format("http://%s/consulta.php?apikey=%s&comando=%s", host, apiwebkey, URLEncoder.encode(comando, StandardCharsets.UTF_8));
        String response = executeHttpGet(url);
        
        JSONObject json = new JSONObject(response);
        if (json.has("status") && json.getString("status").equalsIgnoreCase("success")) {
            String output = json.getString("output").replace(" SOL", "").trim();
            if (output.contains("\n")) {
                output = output.substring(output.lastIndexOf("\n")).trim();
            }
            return Double.parseDouble(output);
        } else {
            throw new Exception("API error: " + (json.has("message") ? json.getString("message") : response));
        }
    }

    private String executeHttpGet(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }

    public String getWalletFromDatabase(String username) {
        String walletAddress = null;
        String effectiveName = getEffectiveName(username);
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT c.endereco FROM carteiras c JOIN jogadores j ON c.jogador_id = j.id WHERE j.nome = ?")) {
            stmt.setString(1, effectiveName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    walletAddress = rs.getString("endereco");
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Erro ao buscar carteira no banco para " + effectiveName + ": " + e.getMessage());
        }
        return walletAddress;
    }

    public void handleSolBalance(ServerPlayer player) {
        String playerName = player.getName().getString();
        String walletAddress = getWalletFromDatabase(playerName);
        if (walletAddress == null) {
            player.sendSystemMessage(Component.literal("§cVocê ainda não possui uma carteira registrada para o nome: " + getEffectiveName(playerName)));
            return;
        }

        player.sendSystemMessage(Component.literal("§6Carteira SOL (§e" + getEffectiveName(playerName) + "§6): §b" + walletAddress)
                .withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, walletAddress))));

        CompletableFuture.runAsync(() -> {
            try {
                double balance = getSolanaBalance(walletAddress);
                player.sendSystemMessage(Component.literal("§5Saldo de SOL: §6" + balance + " SOL"));
            } catch (Exception e) {
                player.sendSystemMessage(Component.literal("§cErro na API: " + e.getMessage()));
            }
        });
    }

    public void handleBankBalance(ServerPlayer player) {
        String bankWallet = ConfigManager.WALLET_BANK.get();
        player.sendSystemMessage(Component.literal("§6Carteira SOL (§eBANCO§6): §b" + bankWallet)
                .withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, bankWallet))));

        CompletableFuture.runAsync(() -> {
            try {
                double balance = getSolanaBalance(bankWallet);
                player.sendSystemMessage(Component.literal("§5Saldo de SOL: §6" + balance + " SOL"));
            } catch (Exception e) {
                player.sendSystemMessage(Component.literal("§cErro na API: " + e.getMessage()));
            }
        });
    }

    public void transferFromBank(ServerPlayer admin, String recipientName, double amount) {
        String recipientWallet = getWalletFromDatabase(recipientName);

        if (recipientWallet == null) {
            admin.sendSystemMessage(Component.literal("§cO destinatário §e" + recipientName + " §cnão possui uma carteira registrada."));
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                String host = ConfigManager.DOCKER_HOST.get();
                String apiwebkey = ConfigManager.API_WEB_KEY.get();
                
                DecimalFormat df = new DecimalFormat("0.########");
                String formattedAmount = df.format(amount).replace(",", ".");
                
                String comando = String.format("solana transfer %s %s --keypair /solana-token/wallets/paibanco_wallet.json --allow-unfunded-recipient --url https://api.devnet.solana.com",
                        recipientWallet, formattedAmount);
                
                String url = String.format("http://%s/consulta.php?apikey=%s&comando=%s", host, apiwebkey, URLEncoder.encode(comando, StandardCharsets.UTF_8));
                String response = executeHttpGet(url);
                
                JSONObject json = new JSONObject(response);
                if (json.getString("status").equalsIgnoreCase("success")) {
                    String output = json.getString("output");
                    String signature = extractValue(output, "Signature: ([A-Za-z0-9]+)");
                    admin.sendSystemMessage(Component.literal("§a💸 Transferência do Banco para §e" + recipientName + " §ade §e" + amount + " SOL §aconcluída!"));
                    registerTransaction("BANCO", "transferencia_para_" + recipientName, amount, "SOL", signature);
                } else {
                    String out = json.optString("output", "");
                    admin.sendSystemMessage(Component.literal("§cErro na transferência: " + (out.isEmpty() ? json.optString("message") : out)));
                }
            } catch (Exception e) {
                admin.sendSystemMessage(Component.literal("§cErro ao processar transferência do banco: " + e.getMessage()));
            }
        });
    }

    public void solicitarAirdrop(ServerPlayer player) {
        String playerName = player.getName().getString();
        String walletAddress = getWalletFromDatabase(playerName);
        if (walletAddress == null) {
            player.sendSystemMessage(Component.literal("§cVocê não possui uma carteira registrada."));
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                String host = ConfigManager.DOCKER_HOST.get();
                String apiwebkey = ConfigManager.API_WEB_KEY.get();
                String comando = "solana airdrop 2 " + walletAddress + " --url https://api.devnet.solana.com";
                String url = String.format("http://%s/consulta.php?apikey=%s&comando=%s", host, apiwebkey, URLEncoder.encode(comando, StandardCharsets.UTF_8));
                String response = executeHttpGet(url);
                
                JSONObject json = new JSONObject(response);
                if (json.getString("status").equalsIgnoreCase("success")) {
                    player.sendSystemMessage(Component.literal("§a💸 Airdrop de 2 SOL recebido com sucesso!"));
                } else {
                    String out = json.optString("output", "");
                    player.sendSystemMessage(Component.literal("§cErro no airdrop: " + (out.isEmpty() ? json.optString("message") : out)));
                }
            } catch (Exception e) {
                player.sendSystemMessage(Component.literal("§cErro ao processar airdrop: " + e.getMessage()));
            }
        });
    }

    public void transferSolana(ServerPlayer sender, String recipientName, double amount) {
        String senderMinecraftName = sender.getName().getString();
        String senderWallet = getWalletFromDatabase(senderMinecraftName);
        String recipientWallet = getWalletFromDatabase(recipientName);

        if (senderWallet == null) {
            sender.sendSystemMessage(Component.literal("§cVocê não possui uma carteira registrada."));
            return;
        }
        if (recipientWallet == null) {
            sender.sendSystemMessage(Component.literal("§cO destinatário §e" + recipientName + " §cnão possui uma carteira registrada."));
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                String host = ConfigManager.DOCKER_HOST.get();
                String apiwebkey = ConfigManager.API_WEB_KEY.get();
                
                DecimalFormat df = new DecimalFormat("0.########");
                String formattedAmount = df.format(amount).replace(",", ".");
                
                String senderEffectiveName = getEffectiveName(senderMinecraftName);
                String comando = String.format("solana transfer %s %s --keypair /solana-token/wallets/%s_wallet.json --allow-unfunded-recipient --url https://api.devnet.solana.com",
                        recipientWallet, formattedAmount, senderEffectiveName);
                
                String url = String.format("http://%s/consulta.php?apikey=%s&comando=%s", host, apiwebkey, URLEncoder.encode(comando, StandardCharsets.UTF_8));
                String response = executeHttpGet(url);
                
                JSONObject json = new JSONObject(response);
                if (json.getString("status").equalsIgnoreCase("success")) {
                    String output = json.getString("output");
                    String signature = extractValue(output, "Signature: ([A-Za-z0-9]+)");
                    sender.sendSystemMessage(Component.literal("§a💸 Transferência de §e" + amount + " SOL §aconcluída!"));
                    registerTransaction(senderEffectiveName, "transferencia", amount, "SOL", signature);
                } else {
                    String out = json.optString("output", "");
                    sender.sendSystemMessage(Component.literal("§cErro na transferência: " + (out.isEmpty() ? json.optString("message") : out)));
                }
            } catch (Exception e) {
                sender.sendSystemMessage(Component.literal("§cErro ao processar transferência: " + e.getMessage()));
            }
        });
    }

    public void buyGameCurrency(ServerPlayer player, double solAmount) {
        String minecraftName = player.getName().getString();
        String effectiveName = getEffectiveName(minecraftName);
        String walletAddress = getWalletFromDatabase(minecraftName);
        
        if (walletAddress == null) {
            player.sendSystemMessage(Component.literal("§cVocê não possui uma carteira registrada para o nome: " + effectiveName));
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                int conversionRate = ConfigManager.CONVERSION_RATE.get();
                int gameCurrencyAmount = (int) (solAmount * conversionRate);
                
                String host = ConfigManager.DOCKER_HOST.get();
                String apiwebkey = ConfigManager.API_WEB_KEY.get();
                String bank = ConfigManager.WALLET_BANK.get();
                
                DecimalFormat df = new DecimalFormat("0.########");
                String formattedAmount = df.format(solAmount).replace(",", ".");
                
                String comando = String.format("solana transfer %s %s --keypair /solana-token/wallets/%s_wallet.json --allow-unfunded-recipient --url https://api.devnet.solana.com",
                        bank, formattedAmount, effectiveName);
                
                String url = String.format("http://%s/consulta.php?apikey=%s&comando=%s", host, apiwebkey, URLEncoder.encode(comando, StandardCharsets.UTF_8));
                String response = executeHttpGet(url);
                
                JSONObject json = new JSONObject(response);
                if (json.getString("status").equalsIgnoreCase("success")) {
                    String output = json.getString("output");
                    String signature = extractValue(output, "Signature: ([A-Za-z0-9]+)");
                    
                    try (Connection conn = databaseManager.getConnection();
                         PreparedStatement stmt = conn.prepareStatement("UPDATE banco SET saldo = saldo + ? WHERE jogador = ?")) {
                        stmt.setDouble(1, (double) gameCurrencyAmount);
                        stmt.setString(2, effectiveName.toLowerCase());
                        stmt.executeUpdate();
                    }
                    
                    registerTransaction(effectiveName.toLowerCase(), "compra_moedas", solAmount, "SOL", signature);
                    player.sendSystemMessage(Component.literal("§a✅ Compra realizada! Sua conta §e" + effectiveName + " §arecebeu §e" + gameCurrencyAmount + " §amoedas."));
                } else {
                    String out = json.optString("output", "");
                    player.sendSystemMessage(Component.literal("§cErro na compra: " + (out.isEmpty() ? json.optString("message") : out)));
                }
            } catch (Exception e) {
                player.sendSystemMessage(Component.literal("§cErro ao processar compra: " + e.getMessage()));
            }
        });
    }

    public void registerTransaction(String player, String type, double amount, String currency, String signature) {
        CompletableFuture.runAsync(() -> {
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("INSERT INTO livro_caixa (jogador, tipo_transacao, valor, moeda, assinatura, data_hora) VALUES (?, ?, ?, ?, ?, NOW())")) {
                stmt.setString(1, player);
                stmt.setString(2, type);
                stmt.setDouble(3, amount);
                stmt.setString(4, currency);
                stmt.setString(5, signature);
                stmt.executeUpdate();
            } catch (SQLException e) {
                LOGGER.error("Erro ao registrar transação: " + e.getMessage());
            }
        });
    }

    public void refundSolana(ServerPlayer player, String signature) {
        CompletableFuture.runAsync(() -> {
            try (Connection conn = databaseManager.getConnection()) {
                try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM livro_caixa WHERE assinatura = ? AND tipo_transacao = 'reembolso'")) {
                    stmt.setString(1, signature);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            player.sendSystemMessage(Component.literal("§cEste reembolso já foi processado!"));
                            return;
                        }
                    }
                }

                double amount;
                String playerName;
                try (PreparedStatement stmt = conn.prepareStatement("SELECT jogador, valor, tipo_transacao FROM livro_caixa WHERE assinatura = ?")) {
                    stmt.setString(1, signature);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            if (!rs.getString("tipo_transacao").equals("compra_moedas")) {
                                player.sendSystemMessage(Component.literal("§cApenas compras de moedas podem ser reembolsadas!"));
                                return;
                            }
                            playerName = rs.getString("jogador");
                            amount = rs.getDouble("valor");
                        } else {
                            player.sendSystemMessage(Component.literal("§cTransação não encontrada!"));
                            return;
                        }
                    }
                }

                registerTransaction(playerName, "reembolso", amount, "SOL", "REFUND-" + signature);
                player.sendSystemMessage(Component.literal("§a✅ Reembolso registrado com sucesso!"));

            } catch (Exception e) {
                player.sendSystemMessage(Component.literal("§cErro ao processar reembolso: " + e.getMessage()));
            }
        });
    }

    public void createWallet(ServerPlayer player) {
        String minecraftName = player.getName().getString();
        String effectiveName = getEffectiveName(minecraftName);
        
        if (getWalletFromDatabase(minecraftName) != null) {
            player.sendSystemMessage(Component.literal("§cVocê já possui uma carteira registrada!"));
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                String host = ConfigManager.DOCKER_HOST.get();
                String apiwebkey = ConfigManager.API_WEB_KEY.get();
                String walletPath = String.format("/solana-token/wallets/%s_wallet.json", effectiveName);

                String comandoGerar = String.format("solana-keygen new --no-passphrase --outfile %s --force", walletPath);
                String urlGerar = String.format("http://%s/consulta.php?apikey=%s&comando=%s", host, apiwebkey, URLEncoder.encode(comandoGerar, StandardCharsets.UTF_8));
                String responseGerar = executeHttpGet(urlGerar);
                
                JSONObject jsonGerar = new JSONObject(responseGerar);
                if (!jsonGerar.getString("status").equalsIgnoreCase("success")) {
                    throw new Exception("Erro ao criar carteira: " + responseGerar);
                }

                String walletData = jsonGerar.getString("output");
                String walletAddress = extractValue(walletData, "pubkey: ([A-Za-z0-9]+)");
                String secretPhrase = extractValue(walletData, "Save this seed phrase to recover your new keypair:\\s*([^\\n\\r=]+)");

                String comandoLer = String.format("cat %s", walletPath);
                String urlLer = String.format("http://%s/consulta.php?apikey=%s&comando=%s", host, apiwebkey, URLEncoder.encode(comandoLer, StandardCharsets.UTF_8));
                String responseLer = executeHttpGet(urlLer);
                String privateKeyHex = convertPrivateKeyToHex(responseLer);

                try (Connection conn = databaseManager.getConnection()) {
                    int jogadorId;
                    try (PreparedStatement stmt = conn.prepareStatement("INSERT IGNORE INTO jogadores (nome) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
                        stmt.setString(1, effectiveName);
                        stmt.executeUpdate();
                    }
                    try (PreparedStatement stmt = conn.prepareStatement("SELECT id FROM jogadores WHERE nome = ?")) {
                        stmt.setString(1, effectiveName);
                        try (ResultSet rs = stmt.executeQuery()) {
                            if (rs.next()) jogadorId = rs.getInt("id");
                            else throw new Exception("Erro ao obter ID do jogador.");
                        }
                    }

                    try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO carteiras (jogador_id, endereco, chave_privada, frase_secreta) VALUES (?, ?, ?, ?)")) {
                        stmt.setInt(1, jogadorId);
                        stmt.setString(2, walletAddress);
                        stmt.setString(3, privateKeyHex);
                        stmt.setString(4, secretPhrase);
                        stmt.executeUpdate();
                    }
                }

                player.sendSystemMessage(Component.literal("§a✅ Carteira criada com sucesso!"));
                player.sendSystemMessage(Component.literal("§6Endereço: §b" + walletAddress));
                player.sendSystemMessage(Component.literal("§e🛡️ Guarde sua frase secreta: §f" + secretPhrase));

            } catch (Exception e) {
                player.sendSystemMessage(Component.literal("§cErro ao criar carteira: " + e.getMessage()));
            }
        });
    }

    private String extractValue(String text, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(1).trim() : null;
    }

    private String convertPrivateKeyToHex(String jsonResponse) {
        try {
            JSONObject json = new JSONObject(jsonResponse);
            String output = json.getString("output");
            String numbersOnly = output.substring(output.indexOf("[") + 1, output.indexOf("]")).trim();
            String[] numberStrings = numbersOnly.split(",");
            byte[] bytes = new byte[numberStrings.length];
            for (int i = 0; i < numberStrings.length; i++) {
                bytes[i] = (byte) Integer.parseInt(numberStrings[i].trim());
            }
            StringBuilder hex = new StringBuilder();
            for (byte b : bytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            return null;
        }
    }

    public void limparTrilhaDeLuz(ServerPlayer player) {
        net.minecraft.core.BlockPos centro = player.blockPosition();
        net.minecraft.world.level.Level world = player.level();
        int raio = 15;
        for (int x = -raio; x <= raio; x++) {
            for (int y = -5; y <= 5; y++) {
                for (int z = -raio; z <= raio; z++) {
                    net.minecraft.core.BlockPos pos = centro.offset(x, y, z);
                    if (world.getBlockState(pos).is(net.minecraft.world.level.block.Blocks.LIGHT)) {
                        world.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }
        player.sendSystemMessage(Component.literal("§e💡 Trilha luminosa removida!"));
    }
}
