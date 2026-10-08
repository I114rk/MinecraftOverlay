package com.i114rk.minecraftoverlay;

import com.mojang.blaze3d.platform.InputConstants;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import org.lwjgl.glfw.GLFW;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;

public final class MinecraftOverlayClient implements ClientModInitializer {
    private static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.register(
            MinecraftOverlay.id("steam_overlay")
    );

    private static KeyMapping key1;
    private static KeyMapping key2;
    private static boolean shortcutDown;
    private static boolean settingsLoaded;
    private static boolean fpsPinned;
    private static int fpsX = SteamOverlayScreen.DEFAULT_X;
    private static int fpsY = SteamOverlayScreen.DEFAULT_Y;

    @Override
    public void onInitializeClient() {
        key1 = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.minecraftoverlay.steam_overlay_key_1",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT_SHIFT,
                KEY_CATEGORY
        ));
        key2 = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.minecraftoverlay.steam_overlay_key_2",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_TAB,
                KEY_CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(MinecraftOverlayClient::handleShortcut);
        HudElementRegistry.addLast(MinecraftOverlay.id("pinned_fps"), (graphics, deltaTracker) -> {
            Minecraft client = Minecraft.getInstance();
            if (isFpsPinned(client) && client.level != null && client.gui.screen() == null) {
                SteamOverlayScreen.renderFps(graphics, client, fpsX, fpsY, false, false);
            }
        });
    }

    private static void handleShortcut(Minecraft client) {
        boolean key1Bound = !key1.isUnbound();
        boolean key2Bound = !key2.isUnbound();
        boolean shortcutPressed = switch ((key1Bound ? 1 : 0) | (key2Bound ? 2 : 0)) {
            case 1 -> isPhysicallyDown(client, key1);
            case 2 -> isPhysicallyDown(client, key2);
            case 3 -> isPhysicallyDown(client, key1) && isPhysicallyDown(client, key2);
            default -> false;
        };

        // KeyMapping.isDown() is cleared when a screen opens. Read the physical
        // inputs so the same shortcut works while the overlay is already open.
        if (shortcutPressed && !shortcutDown && client.getWindow().isFocused()
                && client.gui.overlay() == null
                && !(client.gui.screen() instanceof KeyBindsScreen)) {
            toggleOverlay(client);
        }

        shortcutDown = shortcutPressed;
    }

    private static boolean isPhysicallyDown(Minecraft client, KeyMapping mapping) {
        InputConstants.Key key = KeyMappingHelper.getBoundKeyOf(mapping);
        return switch (key.getType()) {
            case KEYSYM -> InputConstants.isKeyDown(client.getWindow(), key.getValue());
            case MOUSE -> GLFW.glfwGetMouseButton(client.getWindow().handle(), key.getValue()) == GLFW.GLFW_PRESS;
            case SCANCODE -> mapping.isDown();
        };
    }

    private static void toggleOverlay(Minecraft client) {
        if (client.gui.screen() instanceof SteamOverlayScreen overlay) {
            overlay.onClose();
        } else {
            client.gui.setScreen(new SteamOverlayScreen(client.gui.screen()));
        }
    }

    static boolean isFpsPinned(Minecraft client) {
        loadSettings(client);
        return fpsPinned;
    }

    static int getFpsX(Minecraft client) {
        loadSettings(client);
        return fpsX;
    }

    static int getFpsY(Minecraft client) {
        loadSettings(client);
        return fpsY;
    }

    static void setFpsPinned(Minecraft client, boolean pinned) {
        loadSettings(client);
        fpsPinned = pinned;
        saveSettings(client);
    }

    static void setFpsPosition(Minecraft client, int x, int y, int maxWidth, int maxHeight) {
        loadSettings(client);
        fpsX = Math.max(4, Math.min(x, Math.max(4, maxWidth - SteamOverlayScreen.METRICS_WIDTH - 4)));
        fpsY = Math.max(4, Math.min(y, Math.max(4, maxHeight - SteamOverlayScreen.METRICS_HEIGHT - 4)));
        saveSettings(client);
    }

    private static void loadSettings(Minecraft client) {
        if (settingsLoaded) {
            return;
        }

        settingsLoaded = true;
        Path path = settingsPath(client);
        if (!Files.isRegularFile(path)) {
            return;
        }

        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(path)) {
            properties.load(reader);
            fpsPinned = Boolean.parseBoolean(properties.getProperty("fps.pinned", "false"));
            fpsX = parseCoordinate(properties.getProperty("fps.x"), fpsX);
            fpsY = parseCoordinate(properties.getProperty("fps.y"), fpsY);
        } catch (IOException exception) {
            MinecraftOverlay.LOGGER.warn("Could not load MinecraftOverlay settings", exception);
        }
    }

    private static int parseCoordinate(String value, int fallback) {
        if (value == null) {
            return fallback;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private static void saveSettings(Minecraft client) {
        Path path = settingsPath(client);
        try {
            Files.createDirectories(path.getParent());
            Properties properties = new Properties();
            properties.setProperty("fps.pinned", Boolean.toString(fpsPinned));
            properties.setProperty("fps.x", Integer.toString(fpsX));
            properties.setProperty("fps.y", Integer.toString(fpsY));
            try (Writer writer = Files.newBufferedWriter(path)) {
                properties.store(writer, "MinecraftOverlay settings");
            }
        } catch (IOException exception) {
            MinecraftOverlay.LOGGER.warn("Could not save MinecraftOverlay settings", exception);
        }
    }

    private static Path settingsPath(Minecraft client) {
        return client.gameDirectory.toPath().resolve("config").resolve("minecraftoverlay.properties");
    }
}
