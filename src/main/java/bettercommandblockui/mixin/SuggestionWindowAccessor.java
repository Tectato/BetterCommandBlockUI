package bettercommandblockui.mixin;

import com.mojang.brigadier.suggestion.Suggestion;
import net.minecraft.client.gui.screen.CommandSuggestor;
import net.minecraft.client.util.math.Rect2i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(CommandSuggestor.SuggestionWindow.class)
public interface SuggestionWindowAccessor {
    @Accessor @Mutable
    void setArea(Rect2i area);

    @Accessor
    Rect2i getArea();

    @Accessor
    List<Suggestion> getSuggestions();

    @Accessor
    boolean getCompleted();

    @Accessor
    void setCompleted(boolean val);
}
