package bettercommandblockui.main.ui;

import bettercommandblockui.mixin.CommandSuggestorAccessor;
import bettercommandblockui.mixin.SuggestionWindowAccessor;
import com.google.common.collect.Lists;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.SuggestionContext;
import com.mojang.brigadier.suggestion.Suggestion;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.CommandSuggestor;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Rect2i;
import net.minecraft.command.CommandSource;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class MultiLineCommandSuggestor extends CommandSuggestor {
    private CommandSuggestorAccessor accessor;
    private Pair<Integer, Integer> startPos;
    private int x, y;

    public MultiLineCommandSuggestor(MinecraftClient client, Screen owner, TextFieldWidget textField, TextRenderer textRenderer, boolean slashOptional, boolean suggestingWhenEmpty, int inWindowIndexOffset, int maxSuggestionSize, boolean chatScreenSized, int color) {
        super(client, owner, textField, textRenderer, slashOptional, suggestingWhenEmpty, inWindowIndexOffset, maxSuggestionSize, chatScreenSized, color);
        accessor = (CommandSuggestorAccessor) this;
        startPos = ((MultiLineTextFieldWidget)accessor.getTextField()).getCharacterPos(0);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY) {
        DrawableHelper.fill(matrices, this.x - 1, this.y - 1, this.x + 1, this.y + 1, 0xff8000);
        if (accessor.getWindow() != null) {
            accessor.getWindow().render(matrices, mouseX, mouseY);
        } else {
            int i = 0;
            for (OrderedText orderedText : accessor.getMessages()) {
                int j = i * 10;
                DrawableHelper.fill(matrices, this.x - 1, j + this.y, this.x + accessor.getWidth() + 1, j + 12 + this.y, accessor.getColor());
                accessor.getTextRenderer().drawWithShadow(matrices, orderedText, this.x, (float)(this.y + j + 2), -1);
                ++i;
            }
        }
    }

    @Override
    public void refresh(){
        super.refresh();
        this.refreshRenderPos();
    }

    public void refreshRenderPos(){
        MultiLineTextFieldWidget textField = (MultiLineTextFieldWidget) accessor.getTextField();
        int cursor = Math.max(textField.getCursor(),1);
        try {
            SuggestionContext<CommandSource> suggestionContext = accessor.getParse().getContext().findSuggestionContext(cursor);
            Pair<Integer, Integer> selectionPos = textField.getCharacterPos(suggestionContext.startPos);
            this.x = selectionPos.getLeft() - 1;
            this.y = selectionPos.getRight() + 9;

            SuggestionWindowAccessor window = (SuggestionWindowAccessor) accessor.getWindow();
            if(window != null){
                Rect2i area = new Rect2i(this.x, this.y, window.getArea().getWidth(), window.getArea().getHeight());
                window.setArea(area);
            }

        } catch (Exception e){
            System.out.println("[MLCS::refreshRenderPos] Error:");
            for (StackTraceElement el : e.getStackTrace()){
                System.out.println(el);
            }
        }
    }

    public List<Pair<Style,Integer>> getColors(String original, int firstCharacterIndex) {
        if(accessor.getParse() == null){
            return new ArrayList<Pair<Style,Integer>>();
        }
        ParseResults<CommandSource> parse = accessor.getParse();

        int m;
        ArrayList<Pair<Style,Integer>> list = Lists.newArrayList();
        int i = 0;
        int colorIndex = -1;
        CommandContextBuilder<CommandSource> commandContextBuilder = parse.getContext().getLastChild();
        for (ParsedArgument<CommandSource, ?> parsedArgument : commandContextBuilder.getArguments().values()) {
            int k;
            if (++colorIndex >= accessor.getHIGHLIGHT_STYLES().size()) {
                colorIndex = 0;
            }
            if ((k = Math.max(parsedArgument.getRange().getStart() - firstCharacterIndex, 0)) >= original.length()) break;
            int l = Math.min(parsedArgument.getRange().getEnd() - firstCharacterIndex, original.length());
            if (l <= 0) continue;
            list.add(new Pair<>(accessor.getINFO_STYLE(), i));
            list.add(new Pair<>(accessor.getHIGHLIGHT_STYLES().get(colorIndex), k));
            i = l;
        }
        if (parse.getReader().canRead() && (m = Math.max(parse.getReader().getCursor() - firstCharacterIndex, 0)) < original.length()) {
            int n = Math.min(m + parse.getReader().getRemainingLength(), original.length());
            list.add(new Pair<>(accessor.getINFO_STYLE(), i));
            list.add(new Pair<>(accessor.getERROR_STYLE(), m));
            i = n;
        }
        list.add(new Pair<>(accessor.getINFO_STYLE(), i));
        return list;
    }

    public void setY(int newY){
        this.y = newY;
    }
}
