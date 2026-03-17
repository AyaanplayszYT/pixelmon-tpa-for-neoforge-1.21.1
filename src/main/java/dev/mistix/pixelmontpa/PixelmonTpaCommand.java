package dev.mistix.pixelmontpa;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.mistix.pixelmontpa.TpaRequest.RequestType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class PixelmonTpaCommand {
    private PixelmonTpaCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, TpaManager manager) {
        dispatcher.register(Commands.literal("tpa")
                .then(Commands.argument("target", EntityArgument.player())
                        .executes(ctx -> sendRequest(ctx, manager, RequestType.TPA)))
                .then(Commands.literal("accept").executes(ctx -> acceptRequest(ctx, manager)))
                .then(Commands.literal("deny").executes(ctx -> denyRequest(ctx, manager)))
                .then(Commands.literal("toggle").executes(ctx -> toggleTpa(ctx, manager)))
            .then(Commands.literal("reload")
                .requires(source -> source.hasPermission(2))
                .executes(PixelmonTpaCommand::reloadConfig))
                .then(Commands.literal("help").executes(PixelmonTpaCommand::showHelp))
        );

        dispatcher.register(Commands.literal("tpahere")
                .then(Commands.argument("target", EntityArgument.player())
                        .executes(ctx -> sendRequest(ctx, manager, RequestType.TPA_HERE))));

        dispatcher.register(Commands.literal("tpaccept").executes(ctx -> acceptRequest(ctx, manager)));
        dispatcher.register(Commands.literal("tpdeny").executes(ctx -> denyRequest(ctx, manager)));
    }

    private static int sendRequest(CommandContext<CommandSourceStack> context, TpaManager manager, RequestType requestType) throws CommandSyntaxException {
        ServerPlayer requester = context.getSource().getPlayerOrException();
        ServerPlayer target = EntityArgument.getPlayer(context, "target");

        var error = manager.createRequest(requester, target, requestType);
        if (error.isPresent()) {
            requester.sendSystemMessage(prefix().append(Component.literal(error.get()).withStyle(ChatFormatting.RED)));
            return 0;
        }

        if (requestType == RequestType.TPA) {
            requester.sendSystemMessage(prefix().append(Component.literal("TPA request sent to " + target.getName().getString() + ".").withStyle(ChatFormatting.GREEN)));
        } else {
            requester.sendSystemMessage(prefix().append(Component.literal("TPAHERE request sent to " + target.getName().getString() + ".").withStyle(ChatFormatting.GREEN)));
        }

        MutableComponent acceptButton = Component.literal("[ACCEPT]")
                .withStyle(style -> style
                        .withColor(ChatFormatting.GREEN)
                        .withBold(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept"))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Accept teleport request"))));

        MutableComponent denyButton = Component.literal("[DENY]")
                .withStyle(style -> style
                        .withColor(ChatFormatting.RED)
                        .withBold(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpdeny"))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Deny teleport request"))));

        if (requestType == RequestType.TPA) {
            target.sendSystemMessage(prefix().append(Component.literal(requester.getName().getString() + " wants to teleport to you.").withStyle(ChatFormatting.GOLD)));
        } else {
            target.sendSystemMessage(prefix().append(Component.literal(requester.getName().getString() + " wants you to teleport to them.").withStyle(ChatFormatting.GOLD)));
        }
        target.sendSystemMessage(Component.literal(" ").append(acceptButton).append(Component.literal(" ")).append(denyButton));
        target.sendSystemMessage(Component.literal("Made by Mistix").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC));

        return 1;
    }

    private static int acceptRequest(CommandContext<CommandSourceStack> context, TpaManager manager) throws CommandSyntaxException {
        ServerPlayer target = context.getSource().getPlayerOrException();
        var request = manager.acceptRequest(target.getUUID());

        if (request.isEmpty()) {
            target.sendSystemMessage(prefix().append(Component.literal("No pending TPA request.").withStyle(ChatFormatting.RED)));
            return 0;
        }

        MinecraftServer server = context.getSource().getServer();
        ServerPlayer requester = server.getPlayerList().getPlayer(request.get().requesterId());
        if (requester == null) {
            target.sendSystemMessage(prefix().append(Component.literal("Requester is offline.").withStyle(ChatFormatting.RED)));
            return 0;
        }

        if (request.get().requestType() == RequestType.TPA_HERE) {
            target.teleportTo(requester.serverLevel(), requester.getX(), requester.getY(), requester.getZ(), requester.getYRot(), requester.getXRot());
            requester.sendSystemMessage(prefix().append(Component.literal(target.getName().getString() + " accepted your TPAHERE request.").withStyle(ChatFormatting.GREEN)));
            target.sendSystemMessage(prefix().append(Component.literal("Teleported to " + requester.getName().getString() + ".").withStyle(ChatFormatting.GREEN)));
        } else {
            requester.teleportTo(target.serverLevel(), target.getX(), target.getY(), target.getZ(), target.getYRot(), target.getXRot());
            requester.sendSystemMessage(prefix().append(Component.literal("Teleport accepted by " + target.getName().getString() + ".").withStyle(ChatFormatting.GREEN)));
            target.sendSystemMessage(prefix().append(Component.literal("Accepted request from " + requester.getName().getString() + ".").withStyle(ChatFormatting.GREEN)));
        }
        return 1;
    }

    private static int denyRequest(CommandContext<CommandSourceStack> context, TpaManager manager) throws CommandSyntaxException {
        ServerPlayer target = context.getSource().getPlayerOrException();
        var request = manager.denyRequest(target.getUUID());

        if (request.isEmpty()) {
            target.sendSystemMessage(prefix().append(Component.literal("No pending TPA request.").withStyle(ChatFormatting.RED)));
            return 0;
        }

        MinecraftServer server = context.getSource().getServer();
        ServerPlayer requester = server.getPlayerList().getPlayer(request.get().requesterId());
        if (requester != null) {
            requester.sendSystemMessage(prefix().append(Component.literal(target.getName().getString() + " denied your TPA request.").withStyle(ChatFormatting.RED)));
        }

        target.sendSystemMessage(prefix().append(Component.literal("Request denied.").withStyle(ChatFormatting.YELLOW)));
        return 1;
    }

    private static int toggleTpa(CommandContext<CommandSourceStack> context, TpaManager manager) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        boolean enabled = manager.toggleTpa(player.getUUID());
        if (enabled) {
            player.sendSystemMessage(prefix().append(Component.literal("TPA requests enabled.").withStyle(ChatFormatting.GREEN)));
        } else {
            player.sendSystemMessage(prefix().append(Component.literal("TPA requests disabled.").withStyle(ChatFormatting.RED)));
        }
        return 1;
    }

    private static int showHelp(CommandContext<CommandSourceStack> context) {
        var source = context.getSource();
        source.sendSuccess(() -> prefix().append(Component.literal("Commands: /tpa <player>, /tpahere <player>, /tpaccept, /tpdeny, /tpa toggle, /tpa reload").withStyle(ChatFormatting.AQUA)), false);
        source.sendSuccess(() -> Component.literal("Made by Mistix").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC), false);
        return 1;
    }

    private static int reloadConfig(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        source.getServer().getCommands().performPrefixedCommand(source, "reload");
        source.sendSuccess(() -> prefix().append(Component.literal("Reload triggered. TPA config values were refreshed.").withStyle(ChatFormatting.GREEN)), true);
        return 1;
    }

    private static MutableComponent prefix() {
        return Component.literal("[PixelmonTPA] ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD);
    }
}
