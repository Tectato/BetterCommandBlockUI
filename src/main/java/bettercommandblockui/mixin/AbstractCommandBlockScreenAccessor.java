package bettercommandblockui.mixin;

import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.screen.ingame.AbstractCommandBlockScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractCommandBlockScreen.class)
public interface AbstractCommandBlockScreenAccessor {
    @Accessor
    ChatInputSuggestor getCommandSuggestor();

    @Accessor("commandSuggestor")
    void setCommandSuggestor(ChatInputSuggestor suggestor);

    @Invoker("onCommandChanged")
    void invokeOnCommandChanged(String text);
}
