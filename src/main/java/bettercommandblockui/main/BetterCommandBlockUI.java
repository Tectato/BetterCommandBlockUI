package bettercommandblockui.main;

import bettercommandblockui.main.config.SimpleConfig;
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

    private static SimpleConfig CONFIG = SimpleConfig.of("betterCommandBlockUI").provider(BetterCommandBlockUI::provider).request();

    public static int SCROLL_STEP_X = CONFIG.getOrDefault("scroll_step_horizontal", 4);
    public static int SCROLL_STEP_Y = CONFIG.getOrDefault("scroll_step_vertical", 2);
    public static int INDENTATION_FACTOR = CONFIG.getOrDefault("indentation_spaces", 2);

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

    private static String provider(String filename){
        return "# SimpleConfig provided by magistermaks at https://github.com/magistermaks/fabric-simplelibs\n" +
                "\n" +
                "# Number of extra spaces per level of indentation\n" +
                "indentation_spaces=2\n\n" +
                "# Number of chars scrolled per click of the mouse wheel\n" +
                "scroll_step_vertical=2\n" +
                "scroll_step_horizontal=4\n";
    }
}
