package bettercommandblockui.main.ui;

import bettercommandblockui.mixin.TextFieldWidgetAccessor;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

public class OutputTextFieldWidget extends TextFieldWidget {

    private TextFieldWidgetAccessor accessor = (TextFieldWidgetAccessor)this;

    public OutputTextFieldWidget(TextRenderer textRenderer, int width, int height, Text text) {
        super(textRenderer, width, height, text);
    }

    public OutputTextFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text text) {
        super(textRenderer, x, y, width, height, text);
    }

    public OutputTextFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, @Nullable TextFieldWidget copyFrom, Text text) {
        super(textRenderer, x, y, width, height, copyFrom, text);
    }

    @Override
    public void onClick(Click click, boolean doubled) {
        super.onClick(click, doubled);
    }

    @Override
    public void setFocused(boolean focused) {
        if (accessor.getFocusUnlocked() || focused) {
            super.setFocused(focused);
            if (focused) {
                accessor.setLastSwitchFocusTime(Util.getMeasuringTimeMs());
                accessor.setSelectionStart(0);
                accessor.setSelectionEnd(accessor.getText().length());
            }
        }
        if (!this.isFocused()/* || !this.isNarratable()*/){
            accessor.setSelectionStart(0);
            accessor.setSelectionEnd(0);
        }
    }
}
