package com.SolanaDevMinecraft;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;

@Mod.EventBusSubscriber(modid = SolanaForge.MODID)
public class PlayerJoinHandler {

    private static StoreManager storeManager;
    private static SolanaManager solanaManager;

    public static void init(StoreManager store, SolanaManager solana) {
        storeManager = store;
        solanaManager = solana;
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        if (storeManager != null) {
            storeManager.giveStarterBalance(serverPlayer);
        }

        if (solanaManager != null) {
            String wallet = solanaManager.getWalletFromDatabase(player.getName().getString());
            if (wallet == null) {
                serverPlayer.sendSystemMessage(Component.literal("\n§6[Solana] §eParece que você ainda não tem uma carteira Solana!"));
                serverPlayer.sendSystemMessage(Component.literal("§6[Solana] §fUse §b/criarcarteira §fou §b/createwallet §fpara começar.\n"));
            }
        }

        // 🌈 Logo "Flolia 🍁"
        MutableComponent floliaLogo = Component.empty()
            .append(Component.literal("F").withStyle(style -> style.withColor(TextColor.parseColor("#FF0000"))))
            .append(Component.literal("l").withStyle(style -> style.withColor(TextColor.parseColor("#FFA500"))))
            .append(Component.literal("o").withStyle(style -> style.withColor(TextColor.parseColor("#FFFF00"))))
            .append(Component.literal("l").withStyle(style -> style.withColor(TextColor.parseColor("#008000"))))
            .append(Component.literal("i").withStyle(style -> style.withColor(TextColor.parseColor("#00FFFF"))))
            .append(Component.literal("a").withStyle(style -> style.withColor(TextColor.parseColor("#EE82EE"))))
            .append(Component.literal(" 🍁").withStyle(style -> style.withColor(TextColor.parseColor("#FFA500"))));

        // 🌈 Nome do Jogador Colorido
        MutableComponent playerNameColored = Component.literal(player.getName().getString())
            .withStyle(style -> style.withColor(TextColor.parseColor("#FFD700")).withBold(true));

        Component welcomeMessage = Component.translatable("solanaforge.message.welcome")
            .append(playerNameColored)
            .append(Component.translatable("solanaforge.message.welcome_suffix"));

        serverPlayer.sendSystemMessage(welcomeMessage);

        // 🔹 Configurar Header e Footer do TAB
        updateTabList(serverPlayer, floliaLogo);
    }

    public static void updateTabList(ServerPlayer player, Component logo) {
        Component header = Component.literal("\n").append(logo).append(Component.translatable("solanaforge.message.header_subtitle"));
        
        String ip = player.getIpAddress();
        if (ip == null || ip.isEmpty()) ip = "Localhost";
        
        Component footer = Component.literal("\n§6Nome: §e" + player.getName().getString() + "\n§bIP: §f" + ip + "\n§7Acesse: §dhttps://solana.dev\n");

        ClientboundTabListPacket packet = new ClientboundTabListPacket(header, footer);
        player.connection.send(packet);
    }
}
