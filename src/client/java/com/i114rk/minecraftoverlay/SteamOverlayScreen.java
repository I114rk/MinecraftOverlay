package com.i114rk.minecraftoverlay;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public final class SteamOverlayScreen extends Screen {
    static final int DEFAULT_X = 16;
    static final int DEFAULT_Y = 16;
    static final int METRICS_WIDTH = 94;
    static final int METRICS_HEIGHT = 38;

    private static final int FPS_ROW_OFFSET = 17;
    private static final int MENU_WIDTH = 180;
    private static final int MENU_ITEM_HEIGHT = 22;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final Screen parent;
    private int menuX;
    private int menuY;
    private boolean fpsMenu;
    private boolean menuOpen;
    private boolean editingLayout;
    private boolean draggingFps;
    private int dragOffsetX;
    private int dragOffsetY;

    public SteamOverlayScreen(Screen parent) {
        super(Component.translatable("screen.minecraftoverlay.steam_overlay"));
        this.parent = parent;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(0, 0, width, height, 0x66000000);

        if (minecraft != null) {
            int fpsX = MinecraftOverlayClient.getFpsX(minecraft);
            int fpsY = MinecraftOverlayClient.getFpsY(minecraft);
            renderFps(graphics, minecraft, fpsX, fpsY, true, editingLayout);

            if (editingLayout) {
                graphics.text(
                        font,
                        Component.translatable("overlay.minecraftoverlay.editing_hint"),
                        16,
                        height - 26,
                        0xFFFFFFFF
                );
            }
        }

        if (menuOpen) {
            renderContextMenu(graphics);
        }
    }

    static void renderFps(
            GuiGraphicsExtractor graphics,
            Minecraft client,
            int x,
            int y,
            boolean includeTime,
            boolean highlight
    ) {
        int panelTop = y - 5;
        int panelBottom = y + (includeTime ? METRICS_HEIGHT : 18);
        graphics.fill(x - 5, panelTop, x + METRICS_WIDTH, panelBottom, 0x99000000);

        if (includeTime) {
            graphics.text(client.font, LocalTime.now().format(TIME_FORMAT), x, y, 0xFFFFFFFF);
            graphics.text(
                    client.font,
                    Component.translatable("hud.minecraftoverlay.fps", client.getFps()),
                    x,
                    y + FPS_ROW_OFFSET,
                    0xFFFFFFFF
            );
        } else {
            graphics.text(
                    client.font,
                    Component.translatable("hud.minecraftoverlay.fps", client.getFps()),
                    x,
                    y,
                    0xFFFFFFFF
            );
        }

        if (highlight) {
            int right = x + METRICS_WIDTH;
            int bottom = y + (includeTime ? METRICS_HEIGHT : 18);
            graphics.fill(x - 6, y - 6, right + 1, y - 5, 0xFFFFFFFF);
            graphics.fill(x - 6, bottom, right + 1, bottom + 1, 0xFFFFFFFF);
            graphics.fill(x - 6, y - 6, x - 5, bottom + 1, 0xFFFFFFFF);
            graphics.fill(right, y - 6, right + 1, bottom + 1, 0xFFFFFFFF);
        }
    }

    private void renderContextMenu(GuiGraphicsExtractor graphics) {
        int itemCount = 1;
        graphics.fill(menuX, menuY, menuX + MENU_WIDTH, menuY + itemCount * MENU_ITEM_HEIGHT, 0xEE171717);
        graphics.fill(menuX, menuY, menuX + MENU_WIDTH, menuY + 1, 0xFFFFFFFF);
        graphics.fill(menuX, menuY + itemCount * MENU_ITEM_HEIGHT - 1, menuX + MENU_WIDTH, menuY + itemCount * MENU_ITEM_HEIGHT, 0xFFFFFFFF);
        graphics.text(
                font,
                Component.translatable(fpsMenu
                        ? (MinecraftOverlayClient.isFpsPinned(minecraft)
                        ? "context.minecraftoverlay.unpin"
                        : "context.minecraftoverlay.pin")
                        : "context.minecraftoverlay.edit_layout"),
                menuX + 8,
                menuY + 6,
                0xFFFFFFFF
        );
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();

        if (menuOpen) {
            if (event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT
                    && mouseX >= menuX
                    && mouseX <= menuX + MENU_WIDTH
                    && mouseY >= menuY
                    && mouseY <= menuY + MENU_ITEM_HEIGHT) {
                menuOpen = false;
                if (fpsMenu) {
                    MinecraftOverlayClient.setFpsPinned(minecraft, !MinecraftOverlayClient.isFpsPinned(minecraft));
                } else {
                    editingLayout = true;
                }
                return true;
            }

            if (event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                menuOpen = false;
                return true;
            }
        }

        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            openContextMenu(mouseX, mouseY);
            return true;
        }

        if (editingLayout && event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT && isInsideMetrics(mouseX, mouseY)) {
            int fpsX = MinecraftOverlayClient.getFpsX(minecraft);
            int fpsY = MinecraftOverlayClient.getFpsY(minecraft);
            draggingFps = true;
            dragOffsetX = (int) mouseX - fpsX;
            dragOffsetY = (int) mouseY - fpsY;
            return true;
        }

        return true;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        if (editingLayout && draggingFps && event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            MinecraftOverlayClient.setFpsPosition(
                    minecraft,
                    (int) event.x() - dragOffsetX,
                    (int) event.y() - dragOffsetY,
                    width,
                    height
            );
            return true;
        }

        return super.mouseDragged(event, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            draggingFps = false;
        }
        return super.mouseReleased(event);
    }

    private void openContextMenu(double mouseX, double mouseY) {
        int fpsX = MinecraftOverlayClient.getFpsX(minecraft);
        int fpsY = MinecraftOverlayClient.getFpsY(minecraft);
        fpsMenu = isInsideFps(mouseX, mouseY, fpsX, fpsY);
        menuX = Math.max(4, Math.min((int) mouseX, width - MENU_WIDTH - 4));
        menuY = Math.max(4, Math.min((int) mouseY, height - MENU_ITEM_HEIGHT - 4));
        menuOpen = true;
    }

    private boolean isInsideMetrics(double mouseX, double mouseY) {
        int fpsX = MinecraftOverlayClient.getFpsX(minecraft);
        int fpsY = MinecraftOverlayClient.getFpsY(minecraft);
        return mouseX >= fpsX - 6
                && mouseX <= fpsX + METRICS_WIDTH
                && mouseY >= fpsY - 6
                && mouseY <= fpsY + METRICS_HEIGHT;
    }

    private boolean isInsideFps(double mouseX, double mouseY, int fpsX, int fpsY) {
        return mouseX >= fpsX - 6
                && mouseX <= fpsX + METRICS_WIDTH
                && mouseY >= fpsY + FPS_ROW_OFFSET - 6
                && mouseY <= fpsY + FPS_ROW_OFFSET + 12;
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.gui.setScreen(parent);
        }
    }
}
