package bettercommandblockui.mixin;

import com.mojang.brigadier.suggestion.Suggestion;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.renderer.Rect2i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(CommandSuggestions.SuggestionsList.class)
public interface SuggestionWindowAccessor {
    @Accessor @Mutable
    void setRect(Rect2i area);

    @Accessor
    Rect2i getRect();

    @Accessor
    List<Suggestion> getSuggestionList();

    @Accessor
    boolean getTabCycles();

    @Accessor
    void setTabCycles(boolean val);
}
