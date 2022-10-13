package bettercommandblockui.mixin;

import com.mojang.brigadier.Command;
import net.minecraft.client.gui.screen.CommandSuggestor;
import net.minecraft.client.gui.screen.ingame.AbstractCommandBlockScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractCommandBlockScreen.class)
public interface AbstractCommandBlockScreenAccessor {
    @Accessor
    CommandSuggestor getCommandSuggestor();

    @Accessor("commandSuggestor")
    public void setCommandSuggestor(CommandSuggestor suggestor);

    @Invoker("onCommandChanged")
    public void invokeOnCommandChanged(String text);
}
