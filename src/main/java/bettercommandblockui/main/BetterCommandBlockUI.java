package bettercommandblockui.main;

import bettercommandblockui.main.config.ConfigScreen;
import bettercommandblockui.main.config.SimpleConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.CommandBlock;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.core.Direction;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BetterCommandBlockUI implements ClientModInitializer {
    public static final Identifier SLIDER = Identifier.parse("bettercommandblockui:slider");
    public static final Identifier SLIDER_NOTCH = Identifier.parse("bettercommandblockui:slider_notch");
    public static final Identifier COMPASS_FRAME = Identifier.parse("bettercommandblockui:compass_frame");
    public static final Identifier COMPASS_NEEDLE = Identifier.parse("bettercommandblockui:compass_needle");

    public static final Identifier ID_BLOCK_IMPULSE = Identifier.parse("bettercommandblockui:block_impulse");
    public static final Identifier ID_BLOCK_IMPULSE_FOCUSED = Identifier.parse("bettercommandblockui:block_impulse_focused");
    public static final Identifier ID_BLOCK_IMPULSE_CONDITIONAL = Identifier.parse("bettercommandblockui:block_impulse_conditional");
    public static final Identifier ID_BLOCK_IMPULSE_CONDITIONAL_FOCUSED = Identifier.parse("bettercommandblockui:block_impulse_conditional_focused");
    public static final Identifier ID_BLOCK_CHAIN = Identifier.parse("bettercommandblockui:block_chain");
    public static final Identifier ID_BLOCK_CHAIN_FOCUSED = Identifier.parse("bettercommandblockui:block_chain_focused");
    public static final Identifier ID_BLOCK_CHAIN_CONDITIONAL = Identifier.parse("bettercommandblockui:block_chain_conditional");
    public static final Identifier ID_BLOCK_CHAIN_CONDITIONAL_FOCUSED = Identifier.parse("bettercommandblockui:block_chain_conditional_focused");
    public static final Identifier ID_BLOCK_REPEAT = Identifier.parse("bettercommandblockui:block_repeat");
    public static final Identifier ID_BLOCK_REPEAT_FOCUSED = Identifier.parse("bettercommandblockui:block_repeat_focused");
    public static final Identifier ID_BLOCK_REPEAT_CONDITIONAL = Identifier.parse("bettercommandblockui:block_repeat_conditional");
    public static final Identifier ID_BLOCK_REPEAT_CONDITIONAL_FOCUSED = Identifier.parse("bettercommandblockui:block_repeat_conditional_focused");
    public static final Identifier ID_BUTTON_IMPULSE_DISABLED = Identifier.parse("bettercommandblockui:button_impulse_disabled");
    public static final Identifier ID_BUTTON_IMPULSE_ENABLED = Identifier.parse("bettercommandblockui:button_impulse_enabled");
    public static final Identifier ID_BUTTON_IMPULSE_FOCUSED = Identifier.parse("bettercommandblockui:button_impulse_focused");
    public static final Identifier ID_BUTTON_CHAIN_DISABLED = Identifier.parse("bettercommandblockui:button_chain_disabled");
    public static final Identifier ID_BUTTON_CHAIN_ENABLED = Identifier.parse("bettercommandblockui:button_chain_enabled");
    public static final Identifier ID_BUTTON_CHAIN_FOCUSED = Identifier.parse("bettercommandblockui:button_chain_focused");
    public static final Identifier ID_BUTTON_REPEAT_DISABLED = Identifier.parse("bettercommandblockui:button_repeat_disabled");
    public static final Identifier ID_BUTTON_REPEAT_ENABLED = Identifier.parse("bettercommandblockui:button_repeat_enabled");
    public static final Identifier ID_BUTTON_REPEAT_FOCUSED = Identifier.parse("bettercommandblockui:button_repeat_focused");
    public static final Identifier ID_BUTTON_COMMAND_DISABLED = Identifier.parse("bettercommandblockui:button_command_disabled");
    public static final Identifier ID_BUTTON_COMMAND_ENABLED = Identifier.parse("bettercommandblockui:button_command_enabled");
    public static final Identifier ID_BUTTON_COMMAND_FOCUSED = Identifier.parse("bettercommandblockui:button_command_focused");
    public static final Identifier ID_BUTTON_OUTPUT_DISABLED = Identifier.parse("bettercommandblockui:button_output_disabled");
    public static final Identifier ID_BUTTON_OUTPUT_ENABLED = Identifier.parse("bettercommandblockui:button_output_enabled");
    public static final Identifier ID_BUTTON_OUTPUT_FOCUSED = Identifier.parse("bettercommandblockui:button_output_focused");
    public static final Identifier ID_BUTTON_POWER_INACTIVE_DISABLED = Identifier.parse("bettercommandblockui:button_power_inactive_disabled");
    public static final Identifier ID_BUTTON_POWER_INACTIVE_ENABLED = Identifier.parse("bettercommandblockui:button_power_inactive_enabled");
    public static final Identifier ID_BUTTON_POWER_INACTIVE_FOCUSED = Identifier.parse("bettercommandblockui:button_power_inactive_focused");
    public static final Identifier ID_BUTTON_POWER_ACTIVE_DISABLED = Identifier.parse("bettercommandblockui:button_power_active_disabled");
    public static final Identifier ID_BUTTON_POWER_ACTIVE_ENABLED = Identifier.parse("bettercommandblockui:button_power_active_enabled");
    public static final Identifier ID_BUTTON_POWER_ACTIVE_FOCUSED = Identifier.parse("bettercommandblockui:button_power_active_focused");
    public static final Identifier ID_BUTTON_IGNORE_OUTPUT_DISABLED = Identifier.parse("bettercommandblockui:button_ignore_output_disabled");
    public static final Identifier ID_BUTTON_IGNORE_OUTPUT_ENABLED = Identifier.parse("bettercommandblockui:button_ignore_output_enabled");
    public static final Identifier ID_BUTTON_IGNORE_OUTPUT_FOCUSED = Identifier.parse("bettercommandblockui:button_ignore_output_focused");
    public static final Identifier ID_BUTTON_TRACK_OUTPUT_DISABLED = Identifier.parse("bettercommandblockui:button_track_output_disabled");
    public static final Identifier ID_BUTTON_TRACK_OUTPUT_ENABLED = Identifier.parse("bettercommandblockui:button_track_output_enabled");
    public static final Identifier ID_BUTTON_TRACK_OUTPUT_FOCUSED = Identifier.parse("bettercommandblockui:button_track_output_focused");
    public static final Identifier ID_BUTTON_UNCONDITIONAL_DISABLED = Identifier.parse("bettercommandblockui:button_unconditional_disabled");
    public static final Identifier ID_BUTTON_UNCONDITIONAL_ENABLED = Identifier.parse("bettercommandblockui:button_unconditional_enabled");
    public static final Identifier ID_BUTTON_UNCONDITIONAL_FOCUSED = Identifier.parse("bettercommandblockui:button_unconditional_focused");
    public static final Identifier ID_BUTTON_CONDITIONAL_DISABLED = Identifier.parse("bettercommandblockui:button_conditional_disabled");
    public static final Identifier ID_BUTTON_CONDITIONAL_ENABLED = Identifier.parse("bettercommandblockui:button_conditional_enabled");
    public static final Identifier ID_BUTTON_CONDITIONAL_FOCUSED = Identifier.parse("bettercommandblockui:button_conditional_focused");
    public static final Identifier ID_SCROLLBAR_HORIZONTAL_DISABLED = Identifier.parse("bettercommandblockui:scrollbar_horizontal_disabled");
    public static final Identifier ID_SCROLLBAR_HORIZONTAL_ENABLED = Identifier.parse("bettercommandblockui:scrollbar_horizontal_enabled");
    public static final Identifier ID_SCROLLBAR_HORIZONTAL_FOCUSED = Identifier.parse("bettercommandblockui:scrollbar_horizontal_focused");
    public static final Identifier ID_SCROLLBAR_VERTICAL_DISABLED = Identifier.parse("bettercommandblockui:scrollbar_vertical_disabled");
    public static final Identifier ID_SCROLLBAR_VERTICAL_ENABLED = Identifier.parse("bettercommandblockui:scrollbar_vertical_enabled");
    public static final Identifier ID_SCROLLBAR_VERTICAL_FOCUSED = Identifier.parse("bettercommandblockui:scrollbar_vertical_focused");
    public static final Identifier ID_SLIDER_PICK_ENABLED = Identifier.parse("bettercommandblockui:slider_pick_enabled");
    public static final Identifier ID_SLIDER_PICK_FOCUSED = Identifier.parse("bettercommandblockui:slider_pick_focused");

    public static final WidgetSprites BLOCK_IMPULSE = new WidgetSprites(ID_BLOCK_IMPULSE, ID_BLOCK_IMPULSE_FOCUSED);
    public static final WidgetSprites BLOCK_IMPULSE_CONDITIONAL = new WidgetSprites(ID_BLOCK_IMPULSE_CONDITIONAL, ID_BLOCK_IMPULSE_CONDITIONAL_FOCUSED);
    public static final WidgetSprites BLOCK_CHAIN = new WidgetSprites(ID_BLOCK_CHAIN, ID_BLOCK_CHAIN_FOCUSED);
    public static final WidgetSprites BLOCK_CHAIN_CONDITIONAL = new WidgetSprites(ID_BLOCK_CHAIN_CONDITIONAL, ID_BLOCK_CHAIN_CONDITIONAL_FOCUSED);
    public static final WidgetSprites BLOCK_REPEAT = new WidgetSprites(ID_BLOCK_REPEAT, ID_BLOCK_REPEAT_FOCUSED);
    public static final WidgetSprites BLOCK_REPEAT_CONDITIONAL = new WidgetSprites(ID_BLOCK_REPEAT_CONDITIONAL, ID_BLOCK_REPEAT_CONDITIONAL_FOCUSED);

    public static final WidgetSprites BUTTON_IMPULSE = new WidgetSprites(ID_BUTTON_IMPULSE_ENABLED, ID_BUTTON_IMPULSE_DISABLED, ID_BUTTON_IMPULSE_FOCUSED);
    public static final WidgetSprites BUTTON_CHAIN = new WidgetSprites(ID_BUTTON_CHAIN_ENABLED, ID_BUTTON_CHAIN_DISABLED, ID_BUTTON_CHAIN_FOCUSED);
    public static final WidgetSprites BUTTON_REPEAT = new WidgetSprites(ID_BUTTON_REPEAT_ENABLED, ID_BUTTON_REPEAT_DISABLED, ID_BUTTON_REPEAT_FOCUSED);
    public static final WidgetSprites BUTTON_POWER_INACTIVE = new WidgetSprites(ID_BUTTON_POWER_INACTIVE_ENABLED, ID_BUTTON_POWER_INACTIVE_DISABLED, ID_BUTTON_POWER_INACTIVE_FOCUSED);
    public static final WidgetSprites BUTTON_POWER_ACTIVE = new WidgetSprites(ID_BUTTON_POWER_ACTIVE_ENABLED, ID_BUTTON_POWER_ACTIVE_DISABLED, ID_BUTTON_POWER_ACTIVE_FOCUSED);
    public static final WidgetSprites BUTTON_UNCONDITIONAL = new WidgetSprites(ID_BUTTON_UNCONDITIONAL_ENABLED, ID_BUTTON_UNCONDITIONAL_DISABLED, ID_BUTTON_UNCONDITIONAL_FOCUSED);
    public static final WidgetSprites BUTTON_CONDITIONAL = new WidgetSprites(ID_BUTTON_CONDITIONAL_ENABLED, ID_BUTTON_CONDITIONAL_DISABLED, ID_BUTTON_CONDITIONAL_FOCUSED);
    public static final WidgetSprites BUTTON_COMMAND = new WidgetSprites(ID_BUTTON_COMMAND_ENABLED, ID_BUTTON_COMMAND_DISABLED, ID_BUTTON_COMMAND_FOCUSED);
    public static final WidgetSprites BUTTON_OUTPUT = new WidgetSprites(ID_BUTTON_OUTPUT_ENABLED, ID_BUTTON_OUTPUT_DISABLED, ID_BUTTON_OUTPUT_FOCUSED);
    public static final WidgetSprites BUTTON_TRACK_OUTPUT = new WidgetSprites(ID_BUTTON_TRACK_OUTPUT_ENABLED, ID_BUTTON_TRACK_OUTPUT_DISABLED, ID_BUTTON_TRACK_OUTPUT_FOCUSED);
    public static final WidgetSprites BUTTON_IGNORE_OUTPUT = new WidgetSprites(ID_BUTTON_IGNORE_OUTPUT_ENABLED, ID_BUTTON_IGNORE_OUTPUT_DISABLED, ID_BUTTON_IGNORE_OUTPUT_FOCUSED);
    public static final WidgetSprites SLIDER_PICK = new WidgetSprites(ID_SLIDER_PICK_ENABLED, ID_SLIDER_PICK_FOCUSED);
    public static final WidgetSprites SCROLLBAR_HORIZONTAL = new WidgetSprites(ID_SCROLLBAR_HORIZONTAL_ENABLED, ID_SCROLLBAR_HORIZONTAL_DISABLED, ID_SCROLLBAR_HORIZONTAL_FOCUSED);
    public static final WidgetSprites SCROLLBAR_VERTICAL = new WidgetSprites(ID_SCROLLBAR_VERTICAL_ENABLED, ID_SCROLLBAR_VERTICAL_DISABLED, ID_SCROLLBAR_VERTICAL_FOCUSED);

    public static WidgetSprites BlockStateToButtonTextures(BlockState state){
        boolean conditional = state.getValue(CommandBlock.CONDITIONAL);
        if(state.getBlock().equals(BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath("minecraft","command_block")))){
            return conditional ? BLOCK_IMPULSE_CONDITIONAL : BLOCK_IMPULSE;
        }
        if(state.getBlock().equals(BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath("minecraft","chain_command_block")))){
            return conditional ? BLOCK_CHAIN_CONDITIONAL : BLOCK_CHAIN;
        }
        if(state.getBlock().equals(BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath("minecraft","repeating_command_block")))){
            return conditional ? BLOCK_REPEAT_CONDITIONAL : BLOCK_REPEAT;
        }
        return null;
    }

    public static Component DirectionToText(Direction dir){
        return switch(dir){
            case UP -> Component.translatable("bcbui.direction.up");
            case DOWN -> Component.translatable("bcbui.direction.down");
            case NORTH -> Component.translatable("bcbui.direction.north");
            case SOUTH -> Component.translatable("bcbui.direction.south");
            case EAST -> Component.translatable("bcbui.direction.east");
            case WEST -> Component.translatable("bcbui.direction.west");
        };
    }

    private static SimpleConfig CONFIG = SimpleConfig.of("betterCommandBlockUI").provider(BetterCommandBlockUI::provider).request();
    private static final Logger LOGGER = LoggerFactory.getLogger("betterCommandBlockUI");

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

    private static KeyMapping.Category keybindCategory;
    private static KeyMapping areaSelectionInput;

    public static String commandBuffer = "";

    @Override
    public void onInitializeClient() {

        keybindCategory = KeyMapping.Category.register(Identifier.parse("key.category.bcbui.keybinds"));
        areaSelectionInput = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.bcbui.areaselectioninput",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_SEMICOLON,
                keybindCategory));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (areaSelectionInput.consumeClick()) {
                boolean startedSelection = AreaSelectionHandler.areaSelectionInput();
                client.player.sendOverlayMessage(startedSelection ? Component.translatable("bcbui.areaSelection.start") : Component.translatable("bcbui.areaSelection.end"));
            }
        });

        if (CONFIG.isBroken()) {
            LOGGER.info("[BCBUI] Config found to be corrupted or outdated, resetting...");
            CONFIG.reconstructFile();
        }

        LOGGER.info("[BCBUI] Initialized.");
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
