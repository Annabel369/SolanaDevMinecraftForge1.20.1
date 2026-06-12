package com.SolanaDevMinecraft;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.core.registries.Registries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SolanaForge.MODID)
public class HomeManager {
    private final DatabaseManager databaseManager;
    private static HomeManager instance;

    public HomeManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        instance = this;
    }

    @SubscribeEvent
    public static void onPlayerSleep(PlayerSleepInBedEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (instance != null) {
                instance.setHome(player, "default");
                player.sendSystemMessage(Component.literal("§dCama salva como sua casa padrão (/home)!"));
            }
        }
    }

    public void setHome(ServerPlayer player, String homeName) {
        CompletableFuture.runAsync(() -> {
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO homes (player_uuid, home_name, world, x, y, z) VALUES (?, ?, ?, ?, ?, ?) " +
                                 "ON DUPLICATE KEY UPDATE world = VALUES(world), x = VALUES(x), y = VALUES(y), z = VALUES(z)")) {

                stmt.setString(1, player.getUUID().toString());
                stmt.setString(2, homeName);
                stmt.setString(3, player.level().dimension().location().toString());
                stmt.setDouble(4, player.getX());
                stmt.setDouble(5, player.getY());
                stmt.setDouble(6, player.getZ());

                stmt.executeUpdate();
                player.sendSystemMessage(Component.literal("§aCasa '§e" + homeName + "§a' definida com sucesso!"));

            } catch (SQLException e) {
                player.sendSystemMessage(Component.literal("§cErro ao salvar casa no banco de dados."));
            }
        });
    }

    public void teleportToHome(ServerPlayer player, String homeName) {
        CompletableFuture.runAsync(() -> {
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "SELECT world, x, y, z FROM homes WHERE player_uuid = ? AND home_name = ?")) {

                stmt.setString(1, player.getUUID().toString());
                stmt.setString(2, homeName);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String worldName = rs.getString("world");
                    
                    // Compatibilidade com mundos do Folia/Bukkit salvos no banco
                    if (worldName.equalsIgnoreCase("world")) worldName = "minecraft:overworld";
                    else if (worldName.equalsIgnoreCase("world_nether")) worldName = "minecraft:the_nether";
                    else if (worldName.equalsIgnoreCase("world_the_end")) worldName = "minecraft:the_end";
                    else if (!worldName.contains(":")) worldName = "minecraft:" + worldName;
                    
                    final String finalWorldName = worldName;
                    double x = rs.getDouble("x");
                    double y = rs.getDouble("y");
                    double z = rs.getDouble("z");

                    player.getServer().execute(() -> {
                        ResourceKey<Level> levelKey = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(finalWorldName));
                        ServerLevel level = player.getServer().getLevel(levelKey);
                        if (level != null) {
                            player.teleportTo(level, x, y, z, player.getYRot(), player.getXRot());
                            player.sendSystemMessage(Component.literal("§6Bem-vindo à sua casa '§e" + homeName + "§6'!"));
                        } else {
                            player.sendSystemMessage(Component.literal("§cMundo '§e" + finalWorldName + "§c' não encontrado!"));
                        }
                    });
                } else {
                    player.sendSystemMessage(Component.literal("§cCasa '§e" + homeName + "§c' não encontrada!"));
                }

            } catch (SQLException e) {
                player.sendSystemMessage(Component.literal("§cErro ao consultar casa no banco de dados."));
            }
        });
    }

    public void resetAllHomes(ServerPlayer player) {
        CompletableFuture.runAsync(() -> {
            try (Connection conn = databaseManager.getConnection();
                 java.sql.Statement stmt = conn.createStatement()) {
                stmt.execute("TRUNCATE TABLE homes");
                player.sendSystemMessage(Component.literal("§c⚠️ TODAS as casas foram resetadas!"));
            } catch (SQLException e) {
                player.sendSystemMessage(Component.literal("§cErro ao resetar casas."));
            }
        });
    }
}
