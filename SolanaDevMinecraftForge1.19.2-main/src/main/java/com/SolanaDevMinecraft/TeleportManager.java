package com.SolanaDevMinecraft;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeleportManager {
    private final Map<UUID, LastLocation> lastLocations = new HashMap<>();
    private final Map<UUID, UUID> tpaRequests = new HashMap<>();

    public TeleportManager() {
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(this);
    }

    @net.minecraftforge.eventbus.api.SubscribeEvent
    public void onPlayerDeath(net.minecraftforge.event.entity.living.LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            saveLastLocation(player);
        }
    }

    public void saveLastLocation(ServerPlayer player) {
        lastLocations.put(player.getUUID(), new LastLocation(player.getLevel(), player.position(), player.getYRot(), player.getXRot()));
    }

    public void teleportBack(ServerPlayer player) {
        LastLocation loc = lastLocations.get(player.getUUID());
        if (loc != null) {
            player.teleportTo(loc.level, loc.pos.x, loc.pos.y, loc.pos.z, loc.yRot, loc.xRot);
            player.sendSystemMessage(Component.translatable("solanaforge.message.teleport_back"));
        } else {
            player.sendSystemMessage(Component.translatable("solanaforge.message.no_previous_pos"));
        }
    }

    public void sendTpaRequest(ServerPlayer sender, ServerPlayer target) {
        tpaRequests.put(target.getUUID(), sender.getUUID());
        sender.sendSystemMessage(Component.translatable("solanaforge.message.tp_sent", target.getName().getString()));
        target.sendSystemMessage(Component.translatable("solanaforge.message.tp_received", sender.getName().getString()));
    }

    public void acceptTpa(ServerPlayer target) {
        UUID requesterUUID = tpaRequests.remove(target.getUUID());
        if (requesterUUID != null) {
            ServerPlayer requester = target.getServer().getPlayerList().getPlayer(requesterUUID);
            if (requester != null) {
                saveLastLocation(requester);
                requester.teleportTo(target.getLevel(), target.getX(), target.getY(), target.getZ(), target.getYRot(), target.getXRot());
                requester.sendSystemMessage(Component.translatable("solanaforge.message.tp_accepted_requester", target.getName().getString()));
                target.sendSystemMessage(Component.translatable("solanaforge.message.tp_success"));
            }
        } else {
            target.sendSystemMessage(Component.translatable("solanaforge.message.no_pending_tp"));
        }
    }

    public void denyTpa(ServerPlayer target) {
        UUID requesterUUID = tpaRequests.remove(target.getUUID());
        if (requesterUUID != null) {
            ServerPlayer requester = target.getServer().getPlayerList().getPlayer(requesterUUID);
            if (requester != null) {
                requester.sendSystemMessage(Component.translatable("solanaforge.message.tp_denied_requester", target.getName().getString()));
            }
            target.sendSystemMessage(Component.translatable("solanaforge.message.tp_denied_target"));
        } else {
            target.sendSystemMessage(Component.translatable("solanaforge.message.no_pending_tp"));
        }
    }

    private static class LastLocation {
        final ServerLevel level;
        final Vec3 pos;
        final float yRot;
        final float xRot;

        LastLocation(ServerLevel level, Vec3 pos, float yRot, float xRot) {
            this.level = level;
            this.pos = pos;
            this.yRot = yRot;
            this.xRot = xRot;
        }
    }
}
