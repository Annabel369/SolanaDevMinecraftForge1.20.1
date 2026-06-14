package com.SolanaDevMinecraft;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class SolanaCommands {
    private final SolanaManager solanaManager;
    private final StoreManager storeManager;
    private final HomeManager homeManager;
    private final TeleportManager teleportManager;
    private final ChestLockManager chestLockManager;

    public SolanaCommands(SolanaManager solanaManager, StoreManager storeManager, HomeManager homeManager, TeleportManager teleportManager, ChestLockManager chestLockManager) {
        this.solanaManager = solanaManager;
        this.storeManager = storeManager;
        this.homeManager = homeManager;
        this.teleportManager = teleportManager;
        this.chestLockManager = chestLockManager;
    }

    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // Economy & Solana
        registerBalance(dispatcher);
        registerSolBalance(dispatcher);
        registerBankBalance(dispatcher);
        registerBankTransfer(dispatcher);
        registerCreateWallet(dispatcher);
        registerAirdrop(dispatcher);
        registerTransferSol(dispatcher);
        registerBuyCoins(dispatcher);
        registerRefund(dispatcher);
        registerInvest(dispatcher);
        registerLoan(dispatcher);

        // Store
        registerBuyApple(dispatcher);
        registerBuyEmerald(dispatcher);
        registerBuyNetherRelic(dispatcher);
        registerBuyBoots(dispatcher);
        registerBuyWings(dispatcher);
        registerBuyPants(dispatcher);

        // Utility
        registerSetHome(dispatcher);
        registerHome(dispatcher);
        registerResetHomes(dispatcher);
        registerBack(dispatcher);
        registerTpa(dispatcher);
        registerTpAccept(dispatcher);
        registerTpDeny(dispatcher);

        // Chest Lock
        registerLockChest(dispatcher);
        registerUnlockChest(dispatcher);
        registerResetChests(dispatcher);
        registerTrailClear(dispatcher);
    }

    private void registerBalance(CommandDispatcher<CommandSourceStack> dispatcher) {
        for (String literal : new String[]{"saldo", "balance"}) {
            dispatcher.register(Commands.literal(literal)
                    .executes(context -> {
                        ServerPlayer player = context.getSource().getPlayerOrException();
                        int balance = storeManager.getBalance(player.getName().getString());
                        player.sendSystemMessage(Component.translatable("solanaforge.message.balance", balance));
                        return 1;
                    }));
        }
    }

    private void registerSolBalance(CommandDispatcher<CommandSourceStack> dispatcher) {
        for (String literal : new String[]{"solsaldo", "solbalance"}) {
            dispatcher.register(Commands.literal(literal)
                    .executes(context -> {
                        ServerPlayer player = context.getSource().getPlayerOrException();
                        solanaManager.handleSolBalance(player);
                        return 1;
                    }));
        }
    }

    private void registerBankBalance(CommandDispatcher<CommandSourceStack> dispatcher) {
        for (String literal : new String[]{"saldobank", "bankbalance"}) {
            dispatcher.register(Commands.literal(literal)
                    .requires(source -> source.hasPermission(2))
                    .executes(context -> {
                        ServerPlayer player = context.getSource().getPlayerOrException();
                        solanaManager.handleBankBalance(player);
                        return 1;
                    }));
        }
    }

    private void registerBankTransfer(CommandDispatcher<CommandSourceStack> dispatcher) {
        for (String literal : new String[]{"transferebank", "banktransfer"}) {
            dispatcher.register(Commands.literal(literal)
                    .requires(source -> source.hasPermission(2))
                    .then(Commands.argument("quantidade", DoubleArgumentType.doubleArg(0.001))
                            .executes(context -> {
                                ServerPlayer player = context.getSource().getPlayerOrException();
                                double amount = DoubleArgumentType.getDouble(context, "quantidade");
                                solanaManager.transferFromBank(player, player.getName().getString(), amount);
                                return 1;
                            }))
                    .then(Commands.argument("jogador", StringArgumentType.string())
                            .then(Commands.argument("quantidade", DoubleArgumentType.doubleArg(0.001))
                                    .executes(context -> {
                                        ServerPlayer player = context.getSource().getPlayerOrException();
                                        String target = StringArgumentType.getString(context, "jogador");
                                        double amount = DoubleArgumentType.getDouble(context, "quantidade");
                                        solanaManager.transferFromBank(player, target, amount);
                                        return 1;
                                    }))));
        }
    }

    private void registerCreateWallet(CommandDispatcher<CommandSourceStack> dispatcher) {
        for (String literal : new String[]{"criarcarteira", "createwallet"}) {
            dispatcher.register(Commands.literal(literal)
                    .executes(context -> {
                        ServerPlayer player = context.getSource().getPlayerOrException();
                        solanaManager.createWallet(player);
                        return 1;
                    }));
        }
    }

    private void registerAirdrop(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("airdrop")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    solanaManager.solicitarAirdrop(player);
                    return 1;
                }));
    }

    private void registerTransferSol(CommandDispatcher<CommandSourceStack> dispatcher) {
        for (String literal : new String[]{"transferirsol", "transfersol"}) {
            dispatcher.register(Commands.literal(literal)
                    .then(Commands.argument("quantidade", DoubleArgumentType.doubleArg(0.001))
                            .executes(context -> {
                                ServerPlayer player = context.getSource().getPlayerOrException();
                                double amount = DoubleArgumentType.getDouble(context, "quantidade");
                                solanaManager.transferSolana(player, player.getName().getString(), amount);
                                return 1;
                            }))
                    .then(Commands.argument("jogador", StringArgumentType.string())
                            .then(Commands.argument("quantidade", DoubleArgumentType.doubleArg(0.001))
                                    .executes(context -> {
                                        ServerPlayer player = context.getSource().getPlayerOrException();
                                        String target = StringArgumentType.getString(context, "jogador");
                                        double amount = DoubleArgumentType.getDouble(context, "quantidade");
                                        solanaManager.transferSolana(player, target, amount);
                                        return 1;
                                    }))));
        }
    }

    private void registerBuyCoins(CommandDispatcher<CommandSourceStack> dispatcher) {
        for (String literal : new String[]{"comprarmoedas", "buycoins"}) {
            dispatcher.register(Commands.literal(literal)
                    .then(Commands.argument("quantidade_sol", DoubleArgumentType.doubleArg(0.001))
                            .executes(context -> {
                                ServerPlayer player = context.getSource().getPlayerOrException();
                                double amount = DoubleArgumentType.getDouble(context, "quantidade_sol");
                                solanaManager.buyGameCurrency(player, amount);
                                return 1;
                            })));
        }
    }

    private void registerRefund(CommandDispatcher<CommandSourceStack> dispatcher) {
        for (String literal : new String[]{"reembolsar", "refund"}) {
            dispatcher.register(Commands.literal(literal)
                    .then(Commands.argument("assinatura", StringArgumentType.string())
                            .executes(context -> {
                                ServerPlayer player = context.getSource().getPlayerOrException();
                                String sig = StringArgumentType.getString(context, "assinatura");
                                solanaManager.refundSolana(player, sig);
                                return 1;
                            })));
        }
    }

    private void registerInvest(CommandDispatcher<CommandSourceStack> dispatcher) {
        for (String literal : new String[]{"investir", "invest"}) {
            dispatcher.register(Commands.literal(literal)
                    .then(Commands.argument("quantidade", DoubleArgumentType.doubleArg(1))
                            .executes(context -> {
                                ServerPlayer player = context.getSource().getPlayerOrException();
                                double amount = DoubleArgumentType.getDouble(context, "quantidade");
                                storeManager.invest(player, amount);
                                return 1;
                            })));
        }
    }

    private void registerLoan(CommandDispatcher<CommandSourceStack> dispatcher) {
        for (String literal : new String[]{"emprestimo", "loan"}) {
            dispatcher.register(Commands.literal(literal)
                    .then(Commands.argument("quantidade", DoubleArgumentType.doubleArg(1))
                            .executes(context -> {
                                ServerPlayer player = context.getSource().getPlayerOrException();
                                double amount = DoubleArgumentType.getDouble(context, "quantidade");
                                storeManager.takeLoan(player, amount);
                                return 1;
                            })));
        }
    }

    private void registerBuyApple(CommandDispatcher<CommandSourceStack> dispatcher) {
        for (String literal : new String[]{"comprar_maca", "buy_apple"}) {
            dispatcher.register(Commands.literal(literal)
                    .executes(context -> {
                        ServerPlayer player = context.getSource().getPlayerOrException();
                        storeManager.buyEnchantedApple(player);
                        return 1;
                    }));
        }
    }

    private void registerBuyEmerald(CommandDispatcher<CommandSourceStack> dispatcher) {
        for (String literal : new String[]{"comprar_esmeralda", "buy_emerald"}) {
            dispatcher.register(Commands.literal(literal)
                    .executes(context -> {
                        ServerPlayer player = context.getSource().getPlayerOrException();
                        storeManager.buyEmerald(player);
                        return 1;
                    }));
        }
    }

    private void registerBuyNetherRelic(CommandDispatcher<CommandSourceStack> dispatcher) {
        for (String literal : new String[]{"comprar_reliquia_nether", "buy_nether_relic"}) {
            dispatcher.register(Commands.literal(literal)
                    .executes(context -> {
                        ServerPlayer player = context.getSource().getPlayerOrException();
                        storeManager.buyNetherRelic(player);
                        return 1;
                    }));
        }
    }

    private void registerSetHome(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("sethome")
                .then(Commands.argument("nome", StringArgumentType.string())
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            String nome = StringArgumentType.getString(context, "nome");
                            homeManager.setHome(player, nome);
                            return 1;
                        }))
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    homeManager.setHome(player, "default");
                    return 1;
                }));
    }

    private void registerHome(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("home")
                .then(Commands.argument("nome", StringArgumentType.string())
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            String nome = StringArgumentType.getString(context, "nome");
                            teleportManager.saveLastLocation(player);
                            homeManager.teleportToHome(player, nome);
                            return 1;
                        }))
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    teleportManager.saveLastLocation(player);
                    homeManager.teleportToHome(player, "default");
                    return 1;
                }));
    }

    private void registerResetHomes(CommandDispatcher<CommandSourceStack> dispatcher) {
        for (String literal : new String[]{"resetarcasas", "resethomes"}) {
            dispatcher.register(Commands.literal(literal)
                    .requires(source -> source.hasPermission(2))
                    .executes(context -> {
                        ServerPlayer player = context.getSource().getPlayerOrException();
                        homeManager.resetAllHomes(player);
                        return 1;
                    }));
        }
    }

    private void registerBack(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("back")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    teleportManager.teleportBack(player);
                    return 1;
                }));
    }

    private void registerTpa(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tpa")
                .then(Commands.argument("jogador", EntityArgument.player())
                        .executes(context -> {
                            ServerPlayer sender = context.getSource().getPlayerOrException();
                            ServerPlayer target = EntityArgument.getPlayer(context, "jogador");
                            teleportManager.sendTpaRequest(sender, target);
                            return 1;
                        })));
    }

    private void registerTpAccept(CommandDispatcher<CommandSourceStack> dispatcher) {
        for (String literal : new String[]{"tpaceitar", "tpaccept"}) {
            dispatcher.register(Commands.literal(literal)
                    .executes(context -> {
                        ServerPlayer player = context.getSource().getPlayerOrException();
                        teleportManager.acceptTpa(player);
                        return 1;
                    }));
        }
    }

    private void registerTpDeny(CommandDispatcher<CommandSourceStack> dispatcher) {
        for (String literal : new String[]{"tprecusar", "tpdeny"}) {
            dispatcher.register(Commands.literal(literal)
                    .executes(context -> {
                        ServerPlayer player = context.getSource().getPlayerOrException();
                        teleportManager.denyTpa(player);
                        return 1;
                    }));
        }
    }

    private void registerLockChest(CommandDispatcher<CommandSourceStack> dispatcher) {
        for (String literal : new String[]{"trancarbau", "lockchest"}) {
            dispatcher.register(Commands.literal(literal)
                    .then(Commands.argument("senha", StringArgumentType.string())
                            .executes(context -> {
                                ServerPlayer player = context.getSource().getPlayerOrException();
                                String senha = StringArgumentType.getString(context, "senha");
                                HitResult hit = player.pick(5.0D, 0.0F, false);
                                if (hit.getType() == HitResult.Type.BLOCK) {
                                    BlockPos pos = ((BlockHitResult) hit).getBlockPos();
                                    chestLockManager.lockChest(player, pos, senha);
                                } else {
                                    player.sendSystemMessage(Component.translatable("solanaforge.message.look_at_chest"));
                                }
                                return 1;
                            })));
        }
    }

    private void registerUnlockChest(CommandDispatcher<CommandSourceStack> dispatcher) {
        for (String literal : new String[]{"destrancarbau", "unlockchest"}) {
            dispatcher.register(Commands.literal(literal)
                    .then(Commands.argument("senha", StringArgumentType.string())
                            .executes(context -> {
                                ServerPlayer player = context.getSource().getPlayerOrException();
                                String senha = StringArgumentType.getString(context, "senha");
                                HitResult hit = player.pick(5.0D, 0.0F, false);
                                if (hit.getType() == HitResult.Type.BLOCK) {
                                    BlockPos pos = ((BlockHitResult) hit).getBlockPos();
                                    chestLockManager.unlockChest(player, pos, senha);
                                } else {
                                    player.sendSystemMessage(Component.translatable("solanaforge.message.look_at_chest"));
                                }
                                return 1;
                            })));
        }
    }

    private void registerResetChests(CommandDispatcher<CommandSourceStack> dispatcher) {
        for (String literal : new String[]{"resetarbaus", "resetchests"}) {
            dispatcher.register(Commands.literal(literal)
                    .requires(source -> source.hasPermission(2))
                    .executes(context -> {
                        ServerPlayer player = context.getSource().getPlayerOrException();
                        chestLockManager.resetAllChests(player);
                        return 1;
                    }));
        }
    }

    private void registerBuyBoots(CommandDispatcher<CommandSourceStack> dispatcher) {
        for (String literal : new String[]{"comprar_botas", "buy_boots"}) {
            dispatcher.register(Commands.literal(literal)
                    .executes(context -> {
                        ServerPlayer player = context.getSource().getPlayerOrException();
                        storeManager.buyBootRelic(player);
                        return 1;
                    }));
        }
    }

    private void registerBuyWings(CommandDispatcher<CommandSourceStack> dispatcher) {
        for (String literal : new String[]{"comprar_asas", "buy_wings"}) {
            dispatcher.register(Commands.literal(literal)
                    .executes(context -> {
                        ServerPlayer player = context.getSource().getPlayerOrException();
                        storeManager.buyWingRelic(player);
                        return 1;
                    }));
        }
    }

    private void registerBuyPants(CommandDispatcher<CommandSourceStack> dispatcher) {
        for (String literal : new String[]{"comprar_calca", "buy_pants"}) {
            dispatcher.register(Commands.literal(literal)
                    .executes(context -> {
                        ServerPlayer player = context.getSource().getPlayerOrException();
                        storeManager.buyLegRelic(player);
                        return 1;
                    }));
        }
    }

    private void registerTrailClear(CommandDispatcher<CommandSourceStack> dispatcher) {
        for (String literal : new String[]{"apagarpegadas", "limpartrilhadeluz", "trailclear", "lighttrailclear"}) {
            dispatcher.register(Commands.literal(literal)
                    .executes(context -> {
                        ServerPlayer player = context.getSource().getPlayerOrException();
                        solanaManager.limparTrilhaDeLuz(player);
                        return 1;
                    }));
        }
    }
}
