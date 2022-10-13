package bettercommandblockui.mixin;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.SuggestionContext;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.CommandSuggestor;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.command.CommandSource;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(CommandSuggestor.class)
public interface CommandSuggestorAccessor{
    @Invoker("provideRenderText")
    OrderedText invokeProvideRenderText(String original, int firstCharacterIndex);

    @Accessor
    CommandSuggestor.SuggestionWindow getWindow();

    @Accessor
    List<OrderedText> getMessages();

    @Accessor
    List<Style> getHIGHLIGHT_STYLES();

    @Accessor
    Style getINFO_STYLE();

    @Accessor
    Style getERROR_STYLE();

    @Accessor
    Screen getOwner();

    @Accessor
    TextRenderer getTextRenderer();

    @Accessor
    TextFieldWidget getTextField();

    @Accessor
    ParseResults<CommandSource> getParse();

    @Accessor
    boolean getChatScreenSized();

    @Accessor
    boolean getCompletingSuggestions();

    @Accessor
    int getColor();

    @Accessor
    int getWidth();
}
