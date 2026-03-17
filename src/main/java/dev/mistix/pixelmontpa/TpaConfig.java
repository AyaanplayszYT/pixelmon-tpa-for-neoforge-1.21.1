package dev.mistix.pixelmontpa;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class TpaConfig {
    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.IntValue REQUEST_TIMEOUT_SECONDS;
    private static final ModConfigSpec.IntValue COMMAND_COOLDOWN_SECONDS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("tpa");
        REQUEST_TIMEOUT_SECONDS = builder
                .comment("How long (in seconds) a TPA request stays valid before expiring.")
                .defineInRange("requestTimeoutSeconds", 60, 5, 3600);

        COMMAND_COOLDOWN_SECONDS = builder
                .comment("How long (in seconds) a player must wait between sending TPA requests.")
                .defineInRange("commandCooldownSeconds", 5, 0, 300);
        builder.pop();

        SPEC = builder.build();
    }

    private TpaConfig() {
    }

    public static int requestTimeoutSeconds() {
        return REQUEST_TIMEOUT_SECONDS.get();
    }

    public static int commandCooldownSeconds() {
        return COMMAND_COOLDOWN_SECONDS.get();
    }
}