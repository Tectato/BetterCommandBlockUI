package bettercommandblockui.main;

import bettercommandblockui.main.config.ConfigScreen;
import bettercommandblockui.main.config.SimpleConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.block.CommandBlock;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import org.lwjgl.glfw.GLFW;

public class BetterCommandBlockUI implements ClientModInitializer {
    public static final Identifier SLIDER = Identifier.of("bettercommandblockui:slider");
    public static final Identifier SLIDER_NOTCH = Identifier.of("bettercommandblockui:slider_notch");
    public static final Identifier COMPASS_FRAME = Identifier.of("bettercommandblockui:compass_frame");
    public static final Identifier COMPASS_NEEDLE = Identifier.of("bettercommandblockui:compass_needle");

    public static final Identifier ID_BLOCK_IMPULSE = Identifier.of("bettercommandblockui:block_impulse");
    public static final Identifier ID_BLOCK_IMPULSE_FOCUSED = Identifier.of("bettercommandblockui:block_impulse_focused");
    public static final Identifier ID_BLOCK_IMPULSE_CONDITIONAL = Identifier.of("bettercommandblockui:block_impulse_conditional");
    public static final Identifier ID_BLOCK_IMPULSE_CONDITIONAL_FOCUSED = Identifier.of("bettercommandblockui:block_impulse_conditional_focused");
    public static final Identifier ID_BLOCK_CHAIN = Identifier.of("bettercommandblockui:block_chain");
    public static final Identifier ID_BLOCK_CHAIN_FOCUSED = Identifier.of("bettercommandblockui:block_chain_focused");
    public static final Identifier ID_BLOCK_CHAIN_CONDITIONAL = Identifier.of("bettercommandblockui:block_chain_conditional");
    public static final Identifier ID_BLOCK_CHAIN_CONDITIONAL_FOCUSED = Identifier.of("bettercommandblockui:block_chain_conditional_focused");
    public static final Identifier ID_BLOCK_REPEAT = Identifier.of("bettercommandblockui:block_repeat");
    public static final Identifier ID_BLOCK_REPEAT_FOCUSED = Identifier.of("bettercommandblockui:block_repeat_focused");
    public static final Identifier ID_BLOCK_REPEAT_CONDITIONAL = Identifier.of("bettercommandblockui:block_repeat_conditional");
    public static final Identifier ID_BLOCK_REPEAT_CONDITIONAL_FOCUSED = Identifier.of("bettercommandblockui:block_repeat_conditional_focused");
    public static final Identifier ID_BUTTON_IMPULSE_DISABLED = Identifier.of("bettercommandblockui:button_impulse_disabled");
    public static final Identifier ID_BUTTON_IMPULSE_ENABLED = Identifier.of("bettercommandblockui:button_impulse_enabled");
    public static final Identifier ID_BUTTON_IMPULSE_FOCUSED = Identifier.of("bettercommandblockui:button_impulse_focused");
    public static final Identifier ID_BUTTON_CHAIN_DISABLED = Identifier.of("bettercommandblockui:button_chain_disabled");
    public static final Identifier ID_BUTTON_CHAIN_ENABLED = Identifier.of("bettercommandblockui:button_chain_enabled");
    public static final Identifier ID_BUTTON_CHAIN_FOCUSED = Identifier.of("bettercommandblockui:button_chain_focused");
    public static final Identifier ID_BUTTON_REPEAT_DISABLED = Identifier.of("bettercommandblockui:button_repeat_disabled");
    public static final Identifier ID_BUTTON_REPEAT_ENABLED = Identifier.of("bettercommandblockui:button_repeat_enabled");
    public static final Identifier ID_BUTTON_REPEAT_FOCUSED = Identifier.of("bettercommandblockui:button_repeat_focused");
    public static final Identifier ID_BUTTON_COMMAND_DISABLED = Identifier.of("bettercommandblockui:button_command_disabled");
    public static final Identifier ID_BUTTON_COMMAND_ENABLED = Identifier.of("bettercommandblockui:button_command_enabled");
    public static final Identifier ID_BUTTON_COMMAND_FOCUSED = Identifier.of("bettercommandblockui:button_command_focused");
    public static final Identifier ID_BUTTON_OUTPUT_DISABLED = Identifier.of("bettercommandblockui:button_output_disabled");
    public static final Identifier ID_BUTTON_OUTPUT_ENABLED = Identifier.of("bettercommandblockui:button_output_enabled");
    public static final Identifier ID_BUTTON_OUTPUT_FOCUSED = Identifier.of("bettercommandblockui:button_output_focused");
    public static final Identifier ID_BUTTON_POWER_INACTIVE_DISABLED = Identifier.of("bettercommandblockui:button_power_inactive_disabled");
    public static final Identifier ID_BUTTON_POWER_INACTIVE_ENABLED = Identifier.of("bettercommandblockui:button_power_inactive_enabled");
    public static final Identifier ID_BUTTON_POWER_INACTIVE_FOCUSED = Identifier.of("bettercommandblockui:button_power_inactive_focused");
    public static final Identifier ID_BUTTON_POWER_ACTIVE_DISABLED = Identifier.of("bettercommandblockui:button_power_active_disabled");
    public static final Identifier ID_BUTTON_POWER_ACTIVE_ENABLED = Identifier.of("bettercommandblockui:button_power_active_enabled");
    public static final Identifier ID_BUTTON_POWER_ACTIVE_FOCUSED = Identifier.of("bettercommandblockui:button_power_active_focused");
    public static final Identifier ID_BUTTON_IGNORE_OUTPUT_DISABLED = Identifier.of("bettercommandblockui:button_ignore_output_disabled");
    public static final Identifier ID_BUTTON_IGNORE_OUTPUT_ENABLED = Identifier.of("bettercommandblockui:button_ignore_output_enabled");
    public static final Identifier ID_BUTTON_IGNORE_OUTPUT_FOCUSED = Identifier.of("bettercommandblockui:button_ignore_output_focused");
    public static final Identifier ID_BUTTON_TRACK_OUTPUT_DISABLED = Identifier.of("bettercommandblockui:button_track_output_disabled");
    public static final Identifier ID_BUTTON_TRACK_OUTPUT_ENABLED = Identifier.of("bettercommandblockui:button_track_output_enabled");
    public static final Identifier ID_BUTTON_TRACK_OUTPUT_FOCUSED = Identifier.of("bettercommandblockui:button_track_output_focused");
    public static final Identifier ID_BUTTON_UNCONDITIONAL_DISABLED = Identifier.of("bettercommandblockui:button_unconditional_disabled");
    public static final Identifier ID_BUTTON_UNCONDITIONAL_ENABLED = Identifier.of("bettercommandblockui:button_unconditional_enabled");
    public static final Identifier ID_BUTTON_UNCONDITIONAL_FOCUSED = Identifier.of("bettercommandblockui:button_unconditional_focused");
    public static final Identifier ID_BUTTON_CONDITIONAL_DISABLED = Identifier.of("bettercommandblockui:button_conditional_disabled");
    public static final Identifier ID_BUTTON_CONDITIONAL_ENABLED = Identifier.of("bettercommandblockui:button_conditional_enabled");
    public static final Identifier ID_BUTTON_CONDITIONAL_FOCUSED = Identifier.of("bettercommandblockui:button_conditional_focused");
    public static final Identifier ID_SCROLLBAR_HORIZONTAL_DISABLED = Identifier.of("bettercommandblockui:scrollbar_horizontal_disabled");
    public static final Identifier ID_SCROLLBAR_HORIZONTAL_ENABLED = Identifier.of("bettercommandblockui:scrollbar_horizontal_enabled");
    public static final Identifier ID_SCROLLBAR_HORIZONTAL_FOCUSED = Identifier.of("bettercommandblockui:scrollbar_horizontal_focused");
    public static final Identifier ID_SCROLLBAR_VERTICAL_DISABLED = Identifier.of("bettercommandblockui:scrollbar_vertical_disabled");
    public static final Identifier ID_SCROLLBAR_VERTICAL_ENABLED = Identifier.of("bettercommandblockui:scrollbar_vertical_enabled");
    public static final Identifier ID_SCROLLBAR_VERTICAL_FOCUSED = Identifier.of("bettercommandblockui:scrollbar_vertical_focused");
    public static final Identifier ID_SLIDER_PICK_ENABLED = Identifier.of("bettercommandblockui:slider_pick_enabled");
    public static final Identifier ID_SLIDER_PICK_FOCUSED = Identifier.of("bettercommandblockui:slider_pick_focused");

    public static final ButtonTextures BLOCK_IMPULSE = new ButtonTextures(ID_BLOCK_IMPULSE, ID_BLOCK_IMPULSE_FOCUSED);
    public static final ButtonTextures BLOCK_IMPULSE_CONDITIONAL = new ButtonTextures(ID_BLOCK_IMPULSE_CONDITIONAL, ID_BLOCK_IMPULSE_CONDITIONAL_FOCUSED);
    public static final ButtonTextures BLOCK_CHAIN = new ButtonTextures(ID_BLOCK_CHAIN, ID_BLOCK_CHAIN_FOCUSED);
    public static final ButtonTextures BLOCK_CHAIN_CONDITIONAL = new ButtonTextures(ID_BLOCK_CHAIN_CONDITIONAL, ID_BLOCK_CHAIN_CONDITIONAL_FOCUSED);
    public static final ButtonTextures BLOCK_REPEAT = new ButtonTextures(ID_BLOCK_REPEAT, ID_BLOCK_REPEAT_FOCUSED);
    public static final ButtonTextures BLOCK_REPEAT_CONDITIONAL = new ButtonTextures(ID_BLOCK_REPEAT_CONDITIONAL, ID_BLOCK_REPEAT_CONDITIONAL_FOCUSED);

    public static final ButtonTextures BUTTON_IMPULSE = new ButtonTextures(ID_BUTTON_IMPULSE_ENABLED, ID_BUTTON_IMPULSE_DISABLED, ID_BUTTON_IMPULSE_FOCUSED);
    public static final ButtonTextures BUTTON_CHAIN = new ButtonTextures(ID_BUTTON_CHAIN_ENABLED, ID_BUTTON_CHAIN_DISABLED, ID_BUTTON_CHAIN_FOCUSED);
    public static final ButtonTextures BUTTON_REPEAT = new ButtonTextures(ID_BUTTON_REPEAT_ENABLED, ID_BUTTON_REPEAT_DISABLED, ID_BUTTON_REPEAT_FOCUSED);
    public static final ButtonTextures BUTTON_POWER_INACTIVE = new ButtonTextures(ID_BUTTON_POWER_INACTIVE_ENABLED, ID_BUTTON_POWER_INACTIVE_DISABLED, ID_BUTTON_POWER_INACTIVE_FOCUSED);
    public static final ButtonTextures BUTTON_POWER_ACTIVE = new ButtonTextures(ID_BUTTON_POWER_ACTIVE_ENABLED, ID_BUTTON_POWER_ACTIVE_DISABLED, ID_BUTTON_POWER_ACTIVE_FOCUSED);
    public static final ButtonTextures BUTTON_UNCONDITIONAL = new ButtonTextures(ID_BUTTON_UNCONDITIONAL_ENABLED, ID_BUTTON_UNCONDITIONAL_DISABLED, ID_BUTTON_UNCONDITIONAL_FOCUSED);
    public static final ButtonTextures BUTTON_CONDITIONAL = new ButtonTextures(ID_BUTTON_CONDITIONAL_ENABLED, ID_BUTTON_CONDITIONAL_DISABLED, ID_BUTTON_CONDITIONAL_FOCUSED);
    public static final ButtonTextures BUTTON_COMMAND = new ButtonTextures(ID_BUTTON_COMMAND_ENABLED, ID_BUTTON_COMMAND_DISABLED, ID_BUTTON_COMMAND_FOCUSED);
    public static final ButtonTextures BUTTON_OUTPUT = new ButtonTextures(ID_BUTTON_OUTPUT_ENABLED, ID_BUTTON_OUTPUT_DISABLED, ID_BUTTON_OUTPUT_FOCUSED);
    public static final ButtonTextures BUTTON_TRACK_OUTPUT = new ButtonTextures(ID_BUTTON_TRACK_OUTPUT_ENABLED, ID_BUTTON_TRACK_OUTPUT_DISABLED, ID_BUTTON_TRACK_OUTPUT_FOCUSED);
    public static final ButtonTextures BUTTON_IGNORE_OUTPUT = new ButtonTextures(ID_BUTTON_IGNORE_OUTPUT_ENABLED, ID_BUTTON_IGNORE_OUTPUT_DISABLED, ID_BUTTON_IGNORE_OUTPUT_FOCUSED);
    public static final ButtonTextures SLIDER_PICK = new ButtonTextures(ID_SLIDER_PICK_ENABLED, ID_SLIDER_PICK_FOCUSED);
    public static final ButtonTextures SCROLLBAR_HORIZONTAL = new ButtonTextures(ID_SCROLLBAR_HORIZONTAL_ENABLED, ID_SCROLLBAR_HORIZONTAL_DISABLED, ID_SCROLLBAR_HORIZONTAL_FOCUSED);
    public static final ButtonTextures SCROLLBAR_VERTICAL = new ButtonTextures(ID_SCROLLBAR_VERTICAL_ENABLED, ID_SCROLLBAR_VERTICAL_DISABLED, ID_SCROLLBAR_VERTICAL_FOCUSED);

    public static ButtonTextures BlockStateToButtonTextures(BlockState state){
        boolean conditional = state.get(CommandBlock.CONDITIONAL);
        if(state.getBlock().equals(Registries.BLOCK.get(Identifier.of("minecraft","command_block")))){
            return conditional ? BLOCK_IMPULSE_CONDITIONAL : BLOCK_IMPULSE;
        }
        if(state.getBlock().equals(Registries.BLOCK.get(Identifier.of("minecraft","chain_command_block")))){
            return conditional ? BLOCK_CHAIN_CONDITIONAL : BLOCK_CHAIN;
        }
        if(state.getBlock().equals(Registries.BLOCK.get(Identifier.of("minecraft","repeating_command_block")))){
            return conditional ? BLOCK_REPEAT_CONDITIONAL : BLOCK_REPEAT;
        }
        return null;
    }

    public static Text DirectionToText(Direction dir){
        return switch(dir){
            case UP -> Text.translatable("bcbui.direction.up");
            case DOWN -> Text.translatable("bcbui.direction.down");
            case NORTH -> Text.translatable("bcbui.direction.north");
            case SOUTH -> Text.translatable("bcbui.direction.south");
            case EAST -> Text.translatable("bcbui.direction.east");
            case WEST -> Text.translatable("bcbui.direction.west");
        };
    }

    private static SimpleConfig CONFIG = SimpleConfig.of("betterCommandBlockUI").provider(BetterCommandBlockUI::provider).request();

    public static final String VAR_SCROLL_X = "scroll_step_horizontal";
    public static final String VAR_SCROLL_Y = "scroll_step_vertical";
    public static final String VAR_INDENTATION = "indentation_spaces";
    public static final String VAR_INDENT_CHAR = "indentation_char";
    public static final String VAR_WRAPAROUND = "wraparound";
    public static final String VAR_FORMAT_STRINGS = "format_strings";
    public static final String VAR_IGNORE_ENTER = "ignore_enter";
    public static final String VAR_AUTOSAVE = "autosave";
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
    public static boolean AUTOSAVE = CONFIG.getOrDefault(VAR_AUTOSAVE, false);
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

    private static KeyBinding.Category keybindCategory;
    private static KeyBinding areaSelectionInput;

    public static String commandBuffer = "";

    @Override
    public void onInitializeClient() {

        keybindCategory = KeyBinding.Category.create(Identifier.of("key.category.bcbui.keybinds"));
        areaSelectionInput = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.bcbui.areaselectioninput",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_SEMICOLON,
                keybindCategory));

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
        if(key.equals(VAR_AUTOSAVE)) AUTOSAVE = CONFIG.getOrDefault(key, false);
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
                "# Whether to save automatically when closing the screen or moving through the chain\n" +
                "autosave=false\n\n" +
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
