package dev.mistix.pixelmontpa;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;

@Mod(PixelmonTpaMod.MOD_ID)
public class PixelmonTpaMod {
    public static final String MOD_ID = "mistixtpa";
    public static final Logger LOGGER = LogUtils.getLogger();

    private final TpaManager tpaManager = new TpaManager();

    public PixelmonTpaMod(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.SERVER, TpaConfig.SPEC);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
        LOGGER.info("Pixelmon TPA Brand loaded - Made by Mistix");
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        PixelmonTpaCommand.register(event.getDispatcher(), tpaManager);
    }
}
