package com.i114rk.minecraftoverlay;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MinecraftOverlay implements ModInitializer {
    public static final String MOD_ID = "minecraftoverlay";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("MinecraftOverlay initialized");
    }
}
