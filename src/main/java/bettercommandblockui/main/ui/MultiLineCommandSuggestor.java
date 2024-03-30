package bettercommandblockui.main.ui;

import bettercommandblockui.mixin.CommandSuggestorAccessor;
import bettercommandblockui.mixin.SuggestionWindowAccessor;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.SuggestionContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Rect2i;
import net.minecraft.command.CommandSource;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class MultiLineCommandSuggestor extends ChatInputSuggestor {
    private static final List<Style> HIGHLIGHT_STYLES = Stream.of(Formatting.RED, Formatting.GRAY, Formatting.AQUA, Formatting.YELLOW, Formatting.GREEN, Formatting.LIGHT_PURPLE, Formatting.GOLD).map(Style.EMPTY::withColor).collect(ImmutableList.toImmutableList());

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
                accessor.getTextRenderer().drawWithShadow(matrices, orderedText, this.x, (this.y + j + 2), -1);
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
        try {
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

    public Style getColor(int colorIndex){
        return HIGHLIGHT_STYLES.get(colorIndex);
    }

    /**
     * @return List of (colorIndex, startIndex)-Pairs.
     * Color Indices:
     *  0   - Error
     *  1   - Info
     *  2-7 - Highlight
     */
    public List<Pair<Integer,Integer>> getColors(String original, int firstCharacterIndex) {
        if(accessor.getParse() == null){
            return new ArrayList<Pair<Integer,Integer>>();
        }
        ParseResults<CommandSource> parse = accessor.getParse();

        int m;
        ArrayList<Pair<Integer,Integer>> list = Lists.newArrayList();
        list.add(new Pair<>(1,0));
        int colorIndex = -1;
        CommandContextBuilder<CommandSource> commandContextBuilder = parse.getContext();
        do {
            for (ParsedArgument<CommandSource, ?> parsedArgument : commandContextBuilder.getArguments().values()) {
                int k;
                colorIndex = bumpColorIndex(colorIndex);
                if ((k = Math.max(parsedArgument.getRange().getStart() - firstCharacterIndex, 0)) >= original.length())
                    break;
                int l = Math.min(parsedArgument.getRange().getEnd() - firstCharacterIndex, original.length());
                if (l <= 0) continue;
                list.add(new Pair<>(colorIndex + 2, k));
                list.add(new Pair<>(1, l));
            }
            commandContextBuilder = commandContextBuilder.getChild();
        } while (commandContextBuilder != null);


        if (parse.getReader().canRead() && (m = Math.max(parse.getReader().getCursor() - firstCharacterIndex, 0)) < original.length()) {
            int n = Math.min(m + parse.getReader().getRemainingLength(), original.length());
            list.add(new Pair<>(0, m));
        }
        return list;
    }

    public int getY(){
        return y;
    }

    public int getX(){
        return x;
    }

    public void setPos(int x, int y){
        this.x = x;
        this.y = y;
    }

    public int getHighlighColorCount(){
        return HIGHLIGHT_STYLES.size() - 2;
    }

    private int bumpColorIndex(int colorIndex){
        return (colorIndex + 1) % getHighlighColorCount();
    }
}
