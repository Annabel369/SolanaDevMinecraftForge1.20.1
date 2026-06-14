package com.SolanaDevMinecraft;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = SolanaForge.MODID)
public class ChestLockManager {
    private final DatabaseManager databaseManager;
    private static DatabaseManager dbManagerStatic;

    public ChestLockManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        dbManagerStatic = databaseManager;
    }

    public void lockChest(ServerPlayer player, BlockPos pos, String password) {
        CompletableFuture.runAsync(() -> {
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO locked_chests (world, x, y, z, password) VALUES (?, ?, ?, ?, ?) " +
                         "ON DUPLICATE KEY UPDATE password = VALUES(password)")) {

                stmt.setString(1, player.level.dimension().location().toString());
                stmt.setDouble(2, pos.getX());
                stmt.setDouble(3, pos.getY());
                stmt.setDouble(4, pos.getZ());
                stmt.setString(5, password);

                stmt.executeUpdate();
                player.sendSystemMessage(Component.translatable("solanaforge.message.chest.locked"));

                net.minecraft.world.item.ItemStack tag = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.NAME_TAG);
                tag.setHoverName(Component.literal(password));
                player.getInventory().add(tag);
                player.sendSystemMessage(Component.translatable("solanaforge.message.chest.password_tag"));

            } catch (SQLException e) {
                player.sendSystemMessage(Component.translatable("solanaforge.message.chest.lock_error", e.getMessage()));
            }
        });
    }

    public void unlockChest(ServerPlayer player, BlockPos pos, String password) {
        CompletableFuture.runAsync(() -> {
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "SELECT password FROM locked_chests WHERE world = ? AND x = ? AND y = ? AND z = ?")) {

                stmt.setString(1, player.level.dimension().location().toString());
                stmt.setDouble(2, pos.getX());
                stmt.setDouble(3, pos.getY());
                stmt.setDouble(4, pos.getZ());

                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    if (rs.getString("password").equals(password)) {
                        try (PreparedStatement delStmt = conn.prepareStatement(
                                "DELETE FROM locked_chests WHERE world = ? AND x = ? AND y = ? AND z = ?")) {
                            delStmt.setString(1, player.level.dimension().location().toString());
                            delStmt.setDouble(2, pos.getX());
                            delStmt.setDouble(3, pos.getY());
                            delStmt.setDouble(4, pos.getZ());
                            delStmt.executeUpdate();
                            player.sendSystemMessage(Component.translatable("solanaforge.message.chest.unlocked"));
                        }
                    } else {
                        player.sendSystemMessage(Component.translatable("solanaforge.message.chest.wrong_password"));
                    }
                } else {
                    player.sendSystemMessage(Component.translatable("solanaforge.message.chest.not_locked"));
                }
            } catch (SQLException e) {
                player.sendSystemMessage(Component.translatable("solanaforge.message.chest.unlock_error"));
            }
        });
    }

    public void resetAllChests(ServerPlayer player) {
        CompletableFuture.runAsync(() -> {
            try (Connection conn = databaseManager.getConnection();
                 java.sql.Statement stmt = conn.createStatement()) {
                stmt.execute("TRUNCATE TABLE locked_chests");
                player.sendSystemMessage(Component.translatable("solanaforge.message.chest.reset_all"));
            } catch (SQLException e) {
                player.sendSystemMessage(Component.translatable("solanaforge.message.chest.reset_error"));
            }
        });
    }

    private static String getChestPassword(Level level, BlockPos pos) {
        try (Connection conn = dbManagerStatic.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT password FROM locked_chests WHERE world = ? AND x = ? AND y = ? AND z = ?")) {
            stmt.setString(1, level.dimension().location().toString());
            stmt.setDouble(2, pos.getX());
            stmt.setDouble(3, pos.getY());
            stmt.setDouble(4, pos.getZ());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("password");
        } catch (SQLException e) { }
        return null;
    }

    @SubscribeEvent
    public static void onChestOpen(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide) return;
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockEntity be = level.getBlockEntity(pos);

        if (be instanceof ChestBlockEntity) {
            String password = getChestPassword(level, pos);
            if (password == null) password = getChestPassword(level, pos.north());
            if (password == null) password = getChestPassword(level, pos.south());
            if (password == null) password = getChestPassword(level, pos.east());
            if (password == null) password = getChestPassword(level, pos.west());

            if (password != null) {
                net.minecraft.world.item.ItemStack heldItem = event.getItemStack();
                if (heldItem.is(net.minecraft.world.item.Items.NAME_TAG) && heldItem.getHoverName().getString().equals(password)) {
                    event.getEntity().sendSystemMessage(Component.translatable("solanaforge.message.chest.access_granted"));
                    return; // Permite abrir
                }

                event.setCanceled(true);
                event.getEntity().sendSystemMessage(Component.translatable("solanaforge.message.chest.locked_interact"));
                
                net.minecraft.world.entity.LightningBolt lightning = net.minecraft.world.entity.EntityType.LIGHTNING_BOLT.create(level);
                if (lightning != null) {
                    lightning.moveTo(event.getEntity().position());
                    level.addFreshEntity(lightning);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(net.minecraftforge.event.level.BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide()) return;
        Level level = (Level) event.getLevel();
        BlockPos pos = event.getPos();
        BlockEntity be = level.getBlockEntity(pos);

        if (be instanceof ChestBlockEntity) {
            String password = getChestPassword(level, pos);
            if (password == null) password = getChestPassword(level, pos.north());
            if (password == null) password = getChestPassword(level, pos.south());
            if (password == null) password = getChestPassword(level, pos.east());
            if (password == null) password = getChestPassword(level, pos.west());

            if (password != null) {
                event.setCanceled(true);
                if (event.getPlayer() != null) {
                    event.getPlayer().sendSystemMessage(Component.translatable("solanaforge.message.chest.cannot_break"));
                }
            }
        }
    }
}
