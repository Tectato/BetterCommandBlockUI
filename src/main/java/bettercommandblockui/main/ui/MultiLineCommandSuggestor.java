package bettercommandblockui.main.ui;

import bettercommandblockui.main.util.Pair;
import bettercommandblockui.mixin.CommandSuggestorAccessor;
import bettercommandblockui.mixin.SuggestionWindowAccessor;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.SuggestionContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.EditBox;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.Style;
import net.minecraft.ChatFormatting;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class MultiLineCommandSuggestor extends CommandSuggestions {
    private static final List<Style> HIGHLIGHT_STYLES = Stream.of(ChatFormatting.RED, ChatFormatting.GRAY, ChatFormatting.AQUA, ChatFormatting.YELLOW, ChatFormatting.GREEN, ChatFormatting.LIGHT_PURPLE, ChatFormatting.GOLD).map(Style.EMPTY::withColor).collect(ImmutableList.toImmutableList());

    private CommandSuggestorAccessor accessor;
    private Pair<Integer, Integer> startPos;
    private int x, y;

    public MultiLineCommandSuggestor(Minecraft client, Screen owner, EditBox textField, Font textRenderer, boolean slashOptional, boolean suggestingWhenEmpty, int inWindowIndexOffset, int maxSuggestionSize, boolean chatScreenSized, int color) {
        super(client, owner, textField, textRenderer, slashOptional, suggestingWhenEmpty, inWindowIndexOffset, maxSuggestionSize, chatScreenSized, color);
        accessor = (CommandSuggestorAccessor) this;
        startPos = ((MultiLineTextFieldWidget)accessor.getInput()).getCharacterPos(0);
    }

    @Override
    public void extractRenderState(final GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.fill(this.x - 1, this.y - 1, this.x + 1, this.y + 1, 0xff8000);
        if (accessor.getSuggestions() != null) {
            accessor.getSuggestions().extractRenderState(graphics, mouseX, mouseY);
        } else {
            int i = 0;
            for (FormattedCharSequence orderedText : accessor.getCommandUsage()) {
                int j = i * 10;
                graphics.fill(this.x - 1, j + this.y, this.x + accessor.getCommandUsageWidth() + 1, j + 12 + this.y, accessor.getFillColor());
                graphics.text(accessor.getFont(), orderedText, this.x, (this.y + j + 2), -1);
                ++i;
            }
        }
    }

    @Override
    public void updateCommandInfo(){
        super.updateCommandInfo();
        this.refreshRenderPos();
    }

    public void refreshRenderPos(){
        try {
            SuggestionWindowAccessor window = (SuggestionWindowAccessor) accessor.getSuggestions();
            if(window != null){
                Rect2i area = new Rect2i(this.x, this.y, window.getRect().getWidth(), window.getRect().getHeight());
                window.setRect(area);
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
        if(accessor.getCurrentParse() == null){
            return new ArrayList<Pair<Integer,Integer>>();
        }
        ParseResults<SharedSuggestionProvider> parse = accessor.getCurrentParse();

        int m;
        ArrayList<Pair<Integer,Integer>> list = Lists.newArrayList();
        list.add(new Pair<>(1,0));
        int colorIndex = -1;
        CommandContextBuilder<SharedSuggestionProvider> commandContextBuilder = parse.getContext();
        do {
            for (ParsedArgument<SharedSuggestionProvider, ?> parsedArgument : commandContextBuilder.getArguments().values()) {
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
