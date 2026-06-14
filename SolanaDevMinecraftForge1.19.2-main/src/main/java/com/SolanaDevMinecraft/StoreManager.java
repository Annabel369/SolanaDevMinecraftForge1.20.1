package com.SolanaDevMinecraft;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class StoreManager {
    private final DatabaseManager databaseManager;

    public StoreManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void giveStarterBalance(Player player) {
        String playerName = SolanaManager.getEffectiveName(player.getName().getString());
        try (Connection conn = databaseManager.getConnection()) {
            try (PreparedStatement checkStmt = conn.prepareStatement("SELECT saldo FROM banco WHERE LOWER(jogador) = LOWER(?)")) {
                checkStmt.setString(1, playerName);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (!rs.next()) {
                        try (PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO banco (jogador, saldo) VALUES (?, ?)")) {
                            insertStmt.setString(1, playerName);
                            insertStmt.setInt(2, 500);
                            insertStmt.executeUpdate();
                            // Envia a mensagem apenas se for a primeira vez
                            player.sendSystemMessage(Component.translatable("solanaforge.message.welcome_bonus"));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getBalance(String playerName) {
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT saldo FROM banco WHERE LOWER(jogador) = LOWER(?)")) {
            stmt.setString(1, playerName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("saldo");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void invest(Player player, double amount) {
        String playerName = SolanaManager.getEffectiveName(player.getName().getString());
        CompletableFuture.runAsync(() -> {
            try (Connection conn = databaseManager.getConnection()) {
                int currentBalance = getBalance(playerName);
                if (currentBalance >= amount) {
                    try (PreparedStatement stmt = conn.prepareStatement(
                            "UPDATE banco SET investimento = investimento + ?, saldo = saldo - ? WHERE LOWER(jogador) = LOWER(?)")) {
                        stmt.setDouble(1, amount);
                        stmt.setDouble(2, amount);
                        stmt.setString(3, playerName);
                        stmt.executeUpdate();
                        player.sendSystemMessage(Component.translatable("solanaforge.message.investment_success", amount));
                    }
                } else {
                    player.sendSystemMessage(Component.translatable("solanaforge.message.insufficient_balance_invest"));
                }
            } catch (SQLException e) {
                player.sendSystemMessage(Component.translatable("solanaforge.message.db_error"));
            }
        });
    }

    public void takeLoan(Player player, double amount) {
        String playerName = SolanaManager.getEffectiveName(player.getName().getString());
        CompletableFuture.runAsync(() -> {
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "UPDATE banco SET divida = divida + ?, saldo = saldo + ? WHERE LOWER(jogador) = LOWER(?)")) {
                stmt.setDouble(1, amount * 1.1); // 10% interest
                stmt.setDouble(2, amount);
                stmt.setString(3, playerName);
                stmt.executeUpdate();
                player.sendSystemMessage(Component.translatable("solanaforge.message.loan_approved", amount, (amount * 1.1)));
            } catch (SQLException e) {
                player.sendSystemMessage(Component.translatable("solanaforge.message.db_error"));
            }
        });
    }

    public boolean processPurchase(Player player, int price) {
        String playerName = SolanaManager.getEffectiveName(player.getName().getString());
        try (Connection conn = databaseManager.getConnection()) {
            int currentBalance = getBalance(playerName);
            if (currentBalance >= price) {
                try (PreparedStatement updateStmt = conn.prepareStatement("UPDATE banco SET saldo = saldo - ? WHERE LOWER(jogador) = LOWER(?)")) {
                    updateStmt.setInt(1, price);
                    updateStmt.setString(2, playerName);
                    updateStmt.executeUpdate();
                    return true;
                }
            } else {
                player.sendSystemMessage(Component.translatable("solanaforge.message.insufficient_balance_buy", (price - currentBalance)));
                return false;
            }
        } catch (SQLException e) {
            player.sendSystemMessage(Component.translatable("solanaforge.message.db_error"));
            e.printStackTrace();
            return false;
        }
    }

    public void buyEnchantedApple(Player player) {
        int price = ConfigManager.PRICE_APPLE.get();
        if (processPurchase(player, price)) {
            player.getInventory().add(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE));
            player.sendSystemMessage(Component.translatable("solanaforge.message.buy_apple", price));
        }
    }

    public void buyEmerald(Player player) {
        int price = ConfigManager.PRICE_EMERALD.get();
        if (processPurchase(player, price)) {
            player.getInventory().add(new ItemStack(Items.EMERALD));
            player.sendSystemMessage(Component.translatable("solanaforge.message.buy_emerald", price));
        }
    }

    public void buyNetherRelic(Player player) {
        int price = ConfigManager.PRICE_NETHER_RELIC.get();
        if (processPurchase(player, price)) {
            // Implementation of Nether Relic item depends on ItemInit
            player.getInventory().add(new ItemStack(ItemInit.NETHER_RELIC.get()));
            player.sendSystemMessage(Component.translatable("solanaforge.message.buy_nether_relic", price));
        }
    }

    public void buyBootRelic(Player player) {
        int price = ConfigManager.PRICE_BOOT_RELIC.get();
        if (processPurchase(player, price)) {
            ItemStack boots = new ItemStack(ItemInit.BOOT_RELIC.get());
            boots.getOrCreateTag().putBoolean("Unbreakable", true);
            net.minecraft.network.chat.MutableComponent customName = Component.translatable("solanaforge.message.relic_boots_name")
                    .withStyle(style -> style.withColor(net.minecraft.network.chat.TextColor.parseColor("#FF55FF")).withItalic(false));
            boots.setHoverName(customName);
            boots.enchant(net.minecraft.world.item.enchantment.Enchantments.DEPTH_STRIDER, 3);
            boots.enchant(net.minecraft.world.item.enchantment.Enchantments.MENDING, 1);
            boots.enchant(net.minecraft.world.item.enchantment.Enchantments.UNBREAKING, 3);
            player.getInventory().add(boots);
            player.sendSystemMessage(Component.translatable("solanaforge.message.buy_relic_boots", price));
        }
    }

    public void buyWingRelic(Player player) {
        int price = ConfigManager.PRICE_WING_RELIC.get();
        if (processPurchase(player, price)) {
            ItemStack wings = new ItemStack(ItemInit.WING_RELIC.get());
            wings.getOrCreateTag().putBoolean("Unbreakable", true);
            net.minecraft.network.chat.MutableComponent customName = Component.translatable("solanaforge.message.relic_wing_name")
                    .withStyle(style -> style.withColor(net.minecraft.network.chat.TextColor.parseColor("#FFAA00")).withItalic(false));
            wings.setHoverName(customName);
            wings.enchant(net.minecraft.world.item.enchantment.Enchantments.MENDING, 1);
            wings.enchant(net.minecraft.world.item.enchantment.Enchantments.UNBREAKING, 3);
            wings.enchant(net.minecraft.world.item.enchantment.Enchantments.BINDING_CURSE, 1);
            player.getInventory().add(wings);
            player.sendSystemMessage(Component.translatable("solanaforge.message.buy_relic_wing", price));
        }
    }

    public void buyLegRelic(Player player) {
        int price = 250; // Usando valor fixo ou pode vir do config
        if (processPurchase(player, price)) {
            ItemStack legs = new ItemStack(ItemInit.LEG_RELIC.get());
            legs.getOrCreateTag().putBoolean("Unbreakable", true);
            net.minecraft.network.chat.MutableComponent customName = Component.translatable("solanaforge.message.shield_legs_name")
                    .withStyle(style -> style.withColor(net.minecraft.network.chat.TextColor.parseColor("#00FFFF")).withItalic(false));
            legs.setHoverName(customName);
            legs.enchant(net.minecraft.world.item.enchantment.Enchantments.ALL_DAMAGE_PROTECTION, 10);
            legs.enchant(net.minecraft.world.item.enchantment.Enchantments.MENDING, 1);
            legs.enchant(net.minecraft.world.item.enchantment.Enchantments.UNBREAKING, 3);
            player.getInventory().add(legs);
            player.sendSystemMessage(Component.translatable("solanaforge.message.buy_shield_legs", price));
        }
    }
}
