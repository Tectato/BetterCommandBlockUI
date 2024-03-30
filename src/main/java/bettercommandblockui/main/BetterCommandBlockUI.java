package bettercommandblockui.main;

import bettercommandblockui.main.config.ConfigScreen;
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
    public static final Identifier BUTTON_SIDE_WINDOW = new Identifier("bettercommandblockui","textures/gui/bettercommandblockui/button_side_window.png");
    public static final Identifier SLIDER = new Identifier("bettercommandblockui","textures/gui/bettercommandblockui/slider.png");
    public static final Identifier SLIDER_NOTCH = new Identifier("bettercommandblockui","textures/gui/bettercommandblockui/slider_notch.png");
    public static final Identifier SLIDER_PICK = new Identifier("bettercommandblockui","textures/gui/bettercommandblockui/slider_pick.png");
    public static final Identifier COMPASS_FRAME = new Identifier("bettercommandblockui","textures/gui/bettercommandblockui/compass_frame.png");
    public static final Identifier COMPASS_NEEDLE = new Identifier("bettercommandblockui","textures/gui/bettercommandblockui/compass_needle.png");

    private static SimpleConfig CONFIG = SimpleConfig.of("betterCommandBlockUI").provider(BetterCommandBlockUI::provider).request();

    public static final String VAR_SCROLL_X = "scroll_step_horizontal";
    public static final String VAR_SCROLL_Y = "scroll_step_vertical";
    public static final String VAR_INDENTATION = "indentation_spaces";
    public static final String VAR_INDENT_CHAR = "indentation_char";
    public static final String VAR_WRAPAROUND = "wraparound";
    public static final String VAR_FORMAT_STRINGS = "format_strings";
    public static final String VAR_IGNORE_ENTER = "ignore_enter";
    public static final String VAR_NEWLINE_PRE_OPEN_BRACKET = "newline_pre_open_bracket";
    public static final String VAR_NEWLINE_POST_OPEN_BRACKET = "newline_post_open_bracket";
    public static final String VAR_NEWLINE_PRE_CLOSE_BRACKET = "newline_pre_close_bracket";
    public static final String VAR_NEWLINE_POST_CLOSE_BRACKET = "newline_post_close_bracket";
    public static final String VAR_NEWLINE_POST_LAST_CLOSE_BRACKET = "newline_post_last_close_bracket";
    public static final String VAR_NEWLINE_POST_COMMA = "newline_post_comma";
    public static final String VAR_AVOID_DOUBLE_NEWLINE = "avoid_double_newline";
    public static final String VAR_BRACKET_AUTOCOMPLETE = "bracket_autocomplete";
    public static final String VAR_TRACK_OUTPUT_DEFAULT_USED = "track_output_default_used";
    public static final String VAR_TRACK_OUTPUT_DEFAULT_VALUE = "track_output_default_value";
    public static final String VAR_SHOW_OUTPUT_DEFAULT = "show_output_default";


    public static int SCROLL_STEP_X = CONFIG.getOrDefault(VAR_SCROLL_X, 4);
    public static int SCROLL_STEP_Y = CONFIG.getOrDefault(VAR_SCROLL_Y, 2);
    public static int INDENTATION_FACTOR = CONFIG.getOrDefault(VAR_INDENTATION, 2);
    public static char INDENTATION_CHAR = ' ';//CONFIG.getOrDefault(VAR_INDENT_CHAR, " ").charAt(0);
    public static int WRAPAROUND_WIDTH = CONFIG.getOrDefault(VAR_WRAPAROUND, 250);
    public static boolean FORMAT_STRINGS = CONFIG.getOrDefault(VAR_FORMAT_STRINGS, true);
    public static boolean IGNORE_ENTER = CONFIG.getOrDefault(VAR_IGNORE_ENTER, false);
    public static boolean NEWLINE_PRE_OPEN_BRACKET = CONFIG.getOrDefault(VAR_NEWLINE_PRE_OPEN_BRACKET, true);
    public static boolean NEWLINE_POST_OPEN_BRACKET = CONFIG.getOrDefault(VAR_NEWLINE_POST_OPEN_BRACKET, true);
    public static boolean NEWLINE_PRE_CLOSE_BRACKET = CONFIG.getOrDefault(VAR_NEWLINE_PRE_CLOSE_BRACKET, true);
    public static boolean NEWLINE_POST_CLOSE_BRACKET = CONFIG.getOrDefault(VAR_NEWLINE_POST_CLOSE_BRACKET, false);
    //public static boolean NEWLINE_POST_LAST_CLOSE_BRACKET = CONFIG.getOrDefault(VAR_NEWLINE_POST_LAST_CLOSE_BRACKET, true);
    public static boolean NEWLINE_POST_COMMA = CONFIG.getOrDefault(VAR_NEWLINE_POST_COMMA, true);
    public static boolean AVOID_DOUBLE_NEWLINE = CONFIG.getOrDefault(VAR_AVOID_DOUBLE_NEWLINE, true);
    public static boolean BRACKET_AUTOCOMPLETE = false;//CONFIG.getOrDefault(VAR_BRACKET_AUTOCOMPLETE, false);
    public static boolean TRACK_OUTPUT_DEFAULT_USED = CONFIG.getOrDefault(VAR_TRACK_OUTPUT_DEFAULT_USED, false);
    public static boolean TRACK_OUTPUT_DEFAULT_VALUE = CONFIG.getOrDefault(VAR_TRACK_OUTPUT_DEFAULT_VALUE, true);
    public static boolean SHOW_OUTPUT_DEFAULT = CONFIG.getOrDefault(VAR_SHOW_OUTPUT_DEFAULT, false);

    private static KeyBinding areaSelectionInput;

    public static String commandBuffer = "";

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
                client.player.sendMessage(startedSelection ? Text.translatable("bcbui.areaSelection.start") : Text.translatable("bcbui.areaSelection.end"), true);
            }
        });

        if (CONFIG.isBroken()) {
            System.out.println("[BCBUI] Config found to be corrupted or outdated, resetting...");
            CONFIG.reconstructFile();
        }

        System.out.println("[BCBUI] Initialized.");
    }

    public static void setConfig(String key, String value){
        CONFIG.set(key, value);

        if(key.equals(VAR_SCROLL_X)) SCROLL_STEP_X = CONFIG.getOrDefault(key, 4);
        if(key.equals(VAR_SCROLL_Y)) SCROLL_STEP_Y = CONFIG.getOrDefault(key, 2);
        if(key.equals(VAR_INDENTATION)) INDENTATION_FACTOR = CONFIG.getOrDefault(key, 2);
        if(key.equals(VAR_WRAPAROUND)) WRAPAROUND_WIDTH = CONFIG.getOrDefault(key, 250);
        if(key.equals(VAR_FORMAT_STRINGS)) FORMAT_STRINGS = CONFIG.getOrDefault(key, true);
        if(key.equals(VAR_IGNORE_ENTER)) IGNORE_ENTER = CONFIG.getOrDefault(key, false);
        if(key.equals(VAR_NEWLINE_PRE_OPEN_BRACKET)) NEWLINE_PRE_OPEN_BRACKET = CONFIG.getOrDefault(key, true);
        if(key.equals(VAR_NEWLINE_POST_OPEN_BRACKET)) NEWLINE_POST_OPEN_BRACKET = CONFIG.getOrDefault(key, true);
        if(key.equals(VAR_NEWLINE_PRE_CLOSE_BRACKET)) NEWLINE_PRE_CLOSE_BRACKET = CONFIG.getOrDefault(key, true);
        if(key.equals(VAR_NEWLINE_POST_CLOSE_BRACKET)) NEWLINE_POST_CLOSE_BRACKET = CONFIG.getOrDefault(key, false);
        //if(key.equals(VAR_NEWLINE_POST_LAST_CLOSE_BRACKET)) NEWLINE_POST_LAST_CLOSE_BRACKET = CONFIG.getOrDefault(key, false);
        if(key.equals(VAR_NEWLINE_POST_COMMA)) NEWLINE_POST_COMMA = CONFIG.getOrDefault(key, true);
        if(key.equals(VAR_AVOID_DOUBLE_NEWLINE)) AVOID_DOUBLE_NEWLINE = CONFIG.getOrDefault(key, true);
        //if(key.equals(VAR_BRACKET_AUTOCOMPLETE)) BRACKET_AUTOCOMPLETE = CONFIG.getOrDefault(key, false);
        if(key.equals(VAR_TRACK_OUTPUT_DEFAULT_USED)) TRACK_OUTPUT_DEFAULT_USED = CONFIG.getOrDefault(key, false);
        if(key.equals(VAR_TRACK_OUTPUT_DEFAULT_VALUE)) TRACK_OUTPUT_DEFAULT_VALUE = CONFIG.getOrDefault(key, true);
        if(key.equals(VAR_SHOW_OUTPUT_DEFAULT)) SHOW_OUTPUT_DEFAULT = CONFIG.getOrDefault(key, false);
    }

    public static void writeConfig(){
        CONFIG.writeIfModified();
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
                "format_strings=true\n\n" +
                "# Whether to ignore enter presses\n" +
                "ignore_enter=false\n\n" +
                "# Newline settings\n" +
                "newline_pre_open_bracket=true\n" +
                "newline_post_open_bracket=true\n" +
                "newline_pre_close_bracket=true\n" +
                "newline_post_close_bracket=false\n" +
                //"newline_post_last_close_bracket=true\n" +
                "newline_post_comma=true\n" +
                "avoid_double_newline=true\n\n" +
                "# Default states\n" +
                "track_output_default_used=false\n" +
                "track_output_default_value=true\n" +
                "show_output_default=false";
    }


}
