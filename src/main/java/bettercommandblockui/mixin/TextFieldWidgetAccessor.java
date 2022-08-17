package bettercommandblockui.mixin;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.OrderedText;
import org.checkerframework.checker.units.qual.A;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Mixin(TextFieldWidget.class)
public interface TextFieldWidgetAccessor {
    @Invoker("drawsBackground")
    boolean invokeDrawsBackground();

    @Invoker("drawSelectionHighlight")
    void invokeDrawSelectionHighlight(int x1, int y1, int x2, int y2);

    @Invoker("getMaxLength")
    int invokeGetMaxLength();

    @Invoker
    void invokeErase(int i);

    @Invoker
    int invokeGetCursorPosWithOffset(int characterOffset);

    @Accessor
    boolean getEditable();

    @Accessor
    boolean getSelecting();

    @Accessor
    boolean getFocusUnlocked();

    @Accessor
    void setSelecting(boolean val);

    @Accessor
    int getEditableColor();

    @Accessor
    int getUneditableColor();

    @Accessor
    int getSelectionStart();

    @Accessor
    int getSelectionEnd();

    @Accessor
    int getFirstCharacterIndex();

    @Accessor
    int getFocusedTicks();

    @Accessor
    String getText();

    @Accessor("text")
    void setTextVariable(String text);

    @Accessor
    String getSuggestion();

    @Accessor
    String getHORIZONTAL_CURSOR();

    @Accessor
    Predicate<String> getTextPredicate();

    @Accessor
    TextRenderer getTextRenderer();

    @Accessor
    Consumer<String> getChangedListener();

    @Accessor
    BiFunction<String, Integer, OrderedText> getRenderTextProvider();
}
