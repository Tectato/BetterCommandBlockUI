package bettercommandblockui.mixin;

import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.screens.inventory.AbstractCommandBlockEditScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractCommandBlockEditScreen.class)
public interface AbstractCommandBlockEditScreenAccessor {
    @Accessor
    CommandSuggestions getCommandSuggestions();

    @Accessor("commandSuggestions")
    void setCommandSuggestions(CommandSuggestions suggestor);

    @Invoker("onEdited")
    void invokeOnEdited(String text);
}
