package com.i114rk.minecraftoverlay;

import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;

public final class MinecraftOverlayClient implements ClientModInitializer {
    private static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.register(
            MinecraftOverlay.id("steam_overlay")
    );

    private static KeyMapping key1;
    private static KeyMapping key2;
    private static boolean shortcutDown;

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
}
