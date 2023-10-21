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

    public static final Identifier SLIDER = new Identifier("bettercommandblockui","textures/gui/bettercommandblockui/slider.png");
    public static final Identifier SLIDER_NOTCH = new Identifier("bettercommandblockui","textures/gui/bettercommandblockui/slider_notch.png");
    public static final Identifier SLIDER_PICK = new Identifier("bettercommandblockui","textures/gui/bettercommandblockui/slider_pick.png");
    public static final Identifier BUTTON_CHECKBOX = new Identifier("bettercommandblockui","textures/gui/bettercommandblockui/button_checkbox.png");

    private static SimpleConfig CONFIG = SimpleConfig.of("betterCommandBlockUI").provider(BetterCommandBlockUI::provider).request();

    public static final String VAR_SCROLL_X = "scroll_step_horizontal";
    public static final String VAR_SCROLL_Y = "scroll_step_vertical";
    public static final String VAR_INDENTATION = "indentation_spaces";
    public static final String VAR_WRAPAROUND = "wraparound";
    public static final String VAR_FORMAT_STRINGS = "format_strings";

    public static int SCROLL_STEP_X = CONFIG.getOrDefault(VAR_SCROLL_X, 4);
    public static int SCROLL_STEP_Y = CONFIG.getOrDefault(VAR_SCROLL_Y, 2);
    public static int INDENTATION_FACTOR = CONFIG.getOrDefault(VAR_INDENTATION, 2);
    public static int WRAPAROUND_WIDTH = CONFIG.getOrDefault(VAR_WRAPAROUND, 250);
    public static boolean FORMAT_STRINGS = CONFIG.getOrDefault(VAR_FORMAT_STRINGS, true);

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

        CONFIG.reconstructFile();

        System.out.println("[BCBUI] Initialized.");
    }

    public static void setConfig(String key, String value){
        CONFIG.set(key, value);

        if(key.equals(VAR_INDENTATION)) INDENTATION_FACTOR = CONFIG.getOrDefault(key, 2);
        if(key.equals(VAR_WRAPAROUND)) WRAPAROUND_WIDTH = CONFIG.getOrDefault(key, 250);
        if(key.equals(VAR_SCROLL_X)) SCROLL_STEP_X = CONFIG.getOrDefault(key, 4);
        if(key.equals(VAR_SCROLL_Y)) SCROLL_STEP_Y = CONFIG.getOrDefault(key, 2);
        if(key.equals(VAR_FORMAT_STRINGS)) FORMAT_STRINGS = CONFIG.getOrDefault(key, true);
    }

    public static void writeConfig(){
        CONFIG.writeToFile();
    }

    private static String provider(String filename){
        return "# SimpleConfig provided by magistermaks at https://github.com/magistermaks/fabric-simplelibs\n" +
                "\n" +
                "# Number of extra spaces per level of indentation\n" +
                "indentation_spaces=2\n\n" +
                "# Number of chars scrolled per click of the mouse wheel\n" +
                "scroll_step_vertical=2\n" +
                "scroll_step_horizontal=4\n\n" +
                "# Maximum line length, lines longer than this get wrapped around to the next\n" +
                "wraparound=250\n\n" +
                "# Whether to apply parentheses-based formatting to string arguments\n" +
                "format_strings=true";
    }
}
