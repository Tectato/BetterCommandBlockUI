package bettercommandblockui.main;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

public class BetterCommandBlockUI implements ModInitializer {
    public static final Identifier BUTTON_MODE = new Identifier("bettercommandblockui","textures/gui/bettercommandblockui/button_mode.png");
    public static final Identifier BUTTON_CONDITIONAL = new Identifier("bettercommandblockui","textures/gui/bettercommandblockui/button_conditional.png");
    public static final Identifier BUTTON_ACTIVE = new Identifier("bettercommandblockui","textures/gui/bettercommandblockui/button_active.png");
    public static final Identifier BUTTON_OUTPUT = new Identifier("bettercommandblockui","textures/gui/bettercommandblockui/button_output.png");
    public static final Identifier BUTTON_TRACK_OUTPUT = new Identifier("bettercommandblockui","textures/gui/bettercommandblockui/button_track_output.png");
    public static final Identifier SCROLLBAR_HORIZONTAL = new Identifier("bettercommandblockui","textures/gui/bettercommandblockui/scrollbar_horizontal.png");
    public static final Identifier SCROLLBAR_VERTICAL = new Identifier("bettercommandblockui","textures/gui/bettercommandblockui/scrollbar_vertical.png");

    @Override
    public void onInitialize() {
        System.out.println("[BCBUI] Initialized.");
    }
}
