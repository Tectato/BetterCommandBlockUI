package bettercommandblockui.main;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class BetterCommandBlockUI implements ClientModInitializer {
    public static final Identifier BUTTON_MODE = new Identifier("bettercommandblockui","textures/gui/bettercommandblockui/button_mode.png");
    public static final Identifier BUTTON_CONDITIONAL = new Identifier("bettercommandblockui","textures/gui/bettercommandblockui/button_conditional.png");
    public static final Identifier BUTTON_ACTIVE = new Identifier("bettercommandblockui","textures/gui/bettercommandblockui/button_active.png");
    public static final Identifier BUTTON_OUTPUT = new Identifier("bettercommandblockui","textures/gui/bettercommandblockui/button_output.png");
    public static final Identifier BUTTON_TRACK_OUTPUT = new Identifier("bettercommandblockui","textures/gui/bettercommandblockui/button_track_output.png");
    public static final Identifier SCROLLBAR_HORIZONTAL = new Identifier("bettercommandblockui","textures/gui/bettercommandblockui/scrollbar_horizontal.png");
    public static final Identifier SCROLLBAR_VERTICAL = new Identifier("bettercommandblockui","textures/gui/bettercommandblockui/scrollbar_vertical.png");

    private static KeyBinding areaSelectionInput;

    @Override
    public void onInitializeClient() {
        areaSelectionInput = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.bcbui.areaselectioninput",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_SEMICOLON,
                "key.category.bcbui.keybinds"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (areaSelectionInput.wasPressed()) {
                boolean startedSelection = AreaSelectionHandler.areaSelectionInput();
                client.player.sendMessage(Text.literal(startedSelection ? "[Started Area Selection]" : "[Area Selector pasted to clipboard]"), true);
            }
        });

        System.out.println("[BCBUI] Initialized.");
    }
}
