package bettercommandblockui.main.ui;

import bettercommandblockui.main.ui.screen.AbstractBetterCommandBlockScreen;
import bettercommandblockui.main.ui.screen.BetterCommandBlockScreen;
import bettercommandblockui.main.BetterCommandBlockUI;
import bettercommandblockui.main.util.Pair;
import bettercommandblockui.mixin.EditBoxAccessor;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import me.shurik.betterhighlighting.api.BuiltinGrammar;
import me.shurik.betterhighlighting.api.TextMateRegistry;
import me.shurik.betterhighlighting.api.syntax.Tokenizer;
import static me.shurik.betterhighlighting.api.syntax.Tokenizer.command;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.ChatFormatting;
import net.minecraft.util.StringUtil;
import net.minecraft.util.Util;
//import org.eclipse.tm4e.core.grammar.IToken;
//import org.eclipse.tm4e.core.grammar.ITokenizeLineResult;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;


public class MultiLineTextFieldWidget extends EditBox implements GuiEventListener {
    private final int visibleChars = 20;

    private AbstractBetterCommandBlockScreen screen;
    private ScrollbarWidget scrollX, scrollY;
    private List<String> lines;
    private List<Integer> lineOffsets, textOffsets;
    private List<Pair<Style, Integer>> textColors;
    private int visibleLines = 11;
    private int scrolledLines = 0;
    private int horizontalOffset = 0;
    private int maxLineWidth = 30;
    private Pair<Integer, Integer> cursorPosPreference;
    private boolean LShiftPressed, RShiftPressed = false;
    private boolean hasCommandSuggestor = false;
    private boolean textModified = false;
    private MultiLineCommandSuggestor suggestor;
    private EditBoxAccessor accessor = (EditBoxAccessor)this;
    private float timeSinceClick = 0.0f;

    public MultiLineTextFieldWidget(Font textRenderer, int x, int y, int width, int height, Component text, AbstractBetterCommandBlockScreen screen) {
        super(textRenderer, x, y, width, height, text);
        this.lines = new LinkedList<String>();
        this.lineOffsets = new LinkedList<Integer>();
        this.textOffsets = new LinkedList<Integer>();
        this.visibleLines = (height-4) / 10;
        this.scrolledLines = 0;
        this.screen = screen;
        this.scrollX = new TextFieldScrollbarWidget(x, y + height + 1, width, 10, Component.nullToEmpty(""), this, true);
        this.scrollY = new TextFieldScrollbarWidget(x + width + 1, y, 10, height, Component.nullToEmpty(""), this, false);
        cursorPosPreference = new Pair<>(0,0);
    }

    @Override
    public void setWidth(int width){
        this.width = width;
        scrollX.setWidth(width);
    }

    @Override
    public void setHeight(int height){
        this.height = height;
        this.visibleLines = (height-4) / 10;
        scrollY.setHeight(height);
    }

    @Override
    public void setX(int x){
        super.setX(x);
        scrollX.setX(x);
        scrollY.setX(x + width + 1);
    }

    @Override
    public void setY(int y){
        super.setY(y);
        scrollX.setY(y + height + 1);
        scrollY.setY(y);
    }

    @Override
    public void extractWidgetRenderState(final GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta){
        //TODO: render line at x of cursor if currently at parenthesis
        timeSinceClick += delta/20.0f;
        int color;
        if (!this.isVisible()) {
            return;
        }
        if (accessor.getBordered()) {
            color = this.isFocused() ? -1 : -6250336;
            graphics.fill(this.getX() - 1, this.getY() - 1, this.getX() + this.width + 1, this.getY() + this.height + 1, color);
            graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, -16777216);
        }

        color = accessor.getIsEditable() ? accessor.getTextColor() : accessor.getTextColorUneditable();

        if(hasCommandSuggestor) {
            if(lines.isEmpty()){
                renderSuggestor(graphics, mouseX, mouseY);
                return;
            }

            for (int i = scrolledLines; i < scrolledLines + visibleLines && i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.length() >= horizontalOffset) {
                    line = line.substring(horizontalOffset);
                    line = accessor.getFont().plainSubstrByWidth(line, this.getInnerWidth());

                    this.drawColoredLine(graphics, line, this.getX() + 5, this.getY() + 10 * (i - scrolledLines) + 5, i);
                }
            }
        } else {
            this.drawRawText(graphics, accessor.getValue(), this.getX() + 5, this.getY() + 5, color);
        }

        scrollX.extractRenderState(graphics, mouseX, mouseY, delta);
        scrollY.extractRenderState(graphics, mouseX, mouseY, delta);

        if(!hasCommandSuggestor) return;

        int selectionStart = accessor.getCursorPos();
        int selectionEnd = accessor.getHighlightPos();

        boolean selectingBackwards = false;
        if(selectionStart > selectionEnd){
            int temp = selectionEnd;
            selectionEnd = selectionStart;
            selectionStart = temp;
            selectingBackwards = true;
        }

        Pair<Integer, Integer> start = indexToLineAndOffset(selectionStart);
        Pair<Integer, Integer> end = indexToLineAndOffset(selectionEnd);

        int firstSelectedLine = start.getA();
        int selectionStartOffset = start.getB();
        int lastSelectedLine = end.getA();
        int selectionEndOffset = end.getB();

        selectionStartOffset -= horizontalOffset;
        selectionEndOffset -= horizontalOffset;

        selectionStartOffset = Math.max(selectionStartOffset,0);
        selectionEndOffset = Math.max(selectionEndOffset,0);

        boolean renderVerticalCursor = selectionStart < accessor.getValue().length();
        boolean verticalCursorVisible = this.isFocused() && (Util.getMillis() - accessor.getFocusedTime()) / 300L % 2L == 0L;

        for (int i = firstSelectedLine; i <= lastSelectedLine; i++) {
            if(i < scrolledLines || i >= scrolledLines+visibleLines) continue;
            int x1 = this.getX() + 5;
            int x2 = x1;
            int y = this.getY() + 10 * (i - scrolledLines) + 5;

            String visibleLine = lines.get(i).substring(Math.min(horizontalOffset,lines.get(i).length()));
            if (i == firstSelectedLine) {
                x1 += accessor.getFont().width(visibleLine.substring(0, Math.min(selectionStartOffset,visibleLine.length())));

                if (verticalCursorVisible && !selectingBackwards) {
                    if (renderVerticalCursor) {
                        graphics.fill(x1, y - 1, x1 + 1, y + 1 + accessor.getFont().lineHeight, -3092272);
                    } else {
                        graphics.text(accessor.getFont(), "_", x1, y, -3092272);
                    }
                }
            }

            if (i == lastSelectedLine) {
                x2 += accessor.getFont().width(visibleLine.substring(0, Math.min(selectionEndOffset,visibleLine.length())));

                if (verticalCursorVisible && renderVerticalCursor && selectingBackwards) {
                    graphics.fill(x2, y - 1, x2 + 1, y + 1 + accessor.getFont().lineHeight, -3092272);
                }

            } else {
                x2 += this.getInnerWidth();
            }
            graphics.textHighlight(x1, y, x2, y + 10, true);
        }

        renderSuggestor(graphics, mouseX, mouseY);
    }

    private void renderSuggestor(GuiGraphicsExtractor graphics, int mouseX, int mouseY){
        if(suggestor.getY() > getY() + getHeight() || suggestor.getY() < getY()) return;
        if(suggestor.getX() > getX() + getWidth() || suggestor.getX() < getX()) return;
        suggestor.extractRenderState(graphics, mouseX, mouseY);
    }

    private void drawColoredLine(GuiGraphicsExtractor graphics, String content, int x, int y, int lineIndex){
        Font textRenderer = accessor.getFont();
        int renderOffset = 0;
        int startOffset = textOffsets.get(lineIndex); // start index of this line within command
        int currentOffset = 0;
        int numSpaces = lineOffsets.get(lineIndex); // number of spaces before this line
        if(textColors.size() > 1) {
            for (int i = 0; i < textColors.size(); i++) {
                if(currentOffset >= content.length()) break;
                int nextColorStart = (i+1)<textColors.size() ? textColors.get(i+1).getB() : (startOffset + content.length());
                nextColorStart -= startOffset;
                nextColorStart += numSpaces-horizontalOffset;
                if (nextColorStart > currentOffset) {
                    String substring = content.substring(currentOffset, clamp(nextColorStart, currentOffset, content.length()));

                    int color;
                    try{
                        color = textColors.get(i).getA().getColor().getValue();
                    } catch (IndexOutOfBoundsException e){
                        color = TextColor.fromLegacyFormat(ChatFormatting.GRAY).getValue();
                    }
                    color |= 0xFF000000;

                    graphics.text(
                            textRenderer,
                            substring,
                            x + renderOffset,
                            y,
                            color
                    );
                    currentOffset += substring.length();
                    renderOffset += textRenderer.width(substring);
                    //numSpaces = 0;
                }
                if(currentOffset > content.length()) break;
            }
        } else {
            int color;
            try{
                color = textColors.get(0).getA().getColor().getValue();
            } catch (IndexOutOfBoundsException e){
                color = TextColor.fromLegacyFormat(ChatFormatting.GRAY).getValue();
            }
            color |= 0xFF000000;
            graphics.text(textRenderer, content, x, y, color);
        }
    }

    private void drawRawText(GuiGraphicsExtractor graphics, String content, int x, int y, int color){
        Font textRenderer = accessor.getFont();
        String line = content.substring(Math.max(Math.min(horizontalOffset, content.length() - 1),0));
        String trimmedLine = textRenderer.plainSubstrByWidth(line,this.getInnerWidth());
        graphics.text(textRenderer, trimmedLine, x, y, color);
    }

    private int pointToIndex(double x, double y){
        if(lines.size() > 0) {
            Font textRenderer = accessor.getFont();
            int lineIndex = (int) Math.floor((y - (this.getY() + 5)) / 10) + scrolledLines;
            lineIndex = Math.max(Math.min(lineIndex, lines.size() - 1), 0);
            String line = lines.get(lineIndex);
            int offset = 0;
            if(line.length() > 0) {
                String visibleLine = line.substring(Math.min(horizontalOffset, line.length()));
                String trimmedLine = textRenderer.plainSubstrByWidth(visibleLine, (int) (x - (this.getX() + 5)));
                boolean characterClicked = trimmedLine.length() < visibleLine.length();
                boolean lineEndLeftOfWindow = horizontalOffset > line.length()-1;
                int lineOffset = lineOffsets.get(lineIndex);
                boolean offsetClicked = trimmedLine.length() < (lineOffset-horizontalOffset);
                offset = Math.min(horizontalOffset + trimmedLine.length() - lineOffset, (line.length() - lineOffset));
                offset = Math.max(offset, 0);
                offset += offsetClicked ? 1 : 0;
            }

            return textOffsets.get(lineIndex) + Math.max(offset, 0);
        }
        return 0;
    }

    private Pair<Integer, Integer> indexToLineAndOffset(int index){
        Pair<Integer, Integer> output = new Pair<>(0,0);
        for(int i=0; i<lines.size(); i++){
            if(textOffsets.get(i)+(lines.get(i).length()-lineOffsets.get(i))+1>index){
                output.setA(i);
                output.setB((index - textOffsets.get(i)) + lineOffsets.get(i));
                return output;
            }
        }
        return output;
    }

    public Pair<Integer, Integer> getCharacterPos(int index){
        Pair<Integer,Integer> output = indexToLineAndOffset(index);
        int x,y;
        try {
            x = this.getX() + 5 + accessor.getFont().width(lines.get(output.getA()).substring(horizontalOffset, output.getB()));
        } catch(Exception e){
            x = 0;
        }
        y = this.getY() + 5 + 10 * (output.getA()-scrolledLines);
        return new Pair<>(x,y);
    }

    public void setCommandSuggestor(MultiLineCommandSuggestor newSuggestor){
        this.suggestor = newSuggestor;
        this.hasCommandSuggestor = true;
    }

    @Override
    public void insertText(String text) {
        String string;
        String string2;
        int l;
        int start = Math.min(accessor.getCursorPos(), accessor.getHighlightPos());
        int end = Math.max(accessor.getCursorPos(), accessor.getHighlightPos());
        int k = accessor.invokeGetMaxLength() - accessor.getValue().length() - (start - end);
        if (k < (l = (string = StringUtil.filterText(text)).length())) {
            string = string.substring(0, k);
            l = k;
        }
        /*if (!accessor.getFilter().test(string2 = new StringBuilder(accessor.getValue()).replace(start, end, string).toString())) {
            return;
        }*/
        string2 = new StringBuilder(accessor.getValue()).replace(start, end, string).toString();
        accessor.setTextVariable(string2);
        this.setCursorPosition(start + l);
        this.setHighlightPos(accessor.getCursorPos());
        this.onChanged(accessor.getValue(), true);
        this.updateScrollPositions();
        textModified = true;
    }

    private void erase(int offset) {
        if (Minecraft.getInstance().hasControlDown()) {
            this.deleteWords(offset);
        } else {
            this.deleteChars(offset);
        }
        this.onChanged(accessor.getValue(), true);
        this.updateScrollPositions();
        textModified = true;
    }

    private boolean isAlphanumeric(char a){
        return Character.isLetter(a) || Character.isDigit(a) || a == '_';
    }

    private int getWordLength(int offsetDir){
        /*
         * If traversing letters or numbers, keep going until reaching a non-alphanumeric character.
         * Otherwise, only traverse chunks of the same character
         */
        int startIndex = accessor.getCursorPos() + (offsetDir < 0 ? -1 : 0);
        String text = getValue();
        if(text.isEmpty() || startIndex < 0 || startIndex >= text.length()) return 0;
        char current = text.charAt(startIndex);
        char startChar = current;
        //System.out.println("Starting at: " + startChar + ", wordOffset = " + offsetDir);
        boolean erasingAlphanumeric = isAlphanumeric(startChar);
        int endIndex = startIndex;
        do{
            endIndex += offsetDir;
            if (endIndex < 0 || endIndex >= text.length()) break;
            current = text.charAt(endIndex);
        } while (
                (erasingAlphanumeric && isAlphanumeric(current))
                        || (!erasingAlphanumeric && current == startChar)
        );
        endIndex -= offsetDir;
        int min = Math.min(startIndex, endIndex);
        int max = Math.max(startIndex, endIndex);
        //System.out.println("Word: " + text.substring(min, max + 1));
        return ((max-min) + 1) * offsetDir;
    }

    private void selectWord(){
        int forward = getWordLength(1);
        int backward = getWordLength(-1);
        accessor.setCursorPos(accessor.getCursorPos()+forward);
        accessor.setHighlightPos((accessor.getCursorPos()-forward)+backward);
    }

    @Override
    public void deleteWords(int wordOffset) {
        if (accessor.getValue().isEmpty()) {
            return;
        }
        if (accessor.getHighlightPos() != accessor.getCursorPos()) {
            this.insertText("");
            return;
        }
        //this.eraseCharacters(this.getWordSkipPosition(wordOffset) - accessor.getSelectionStart());
        deleteChars(getWordLength(wordOffset));
    }

    @Override
    public void deleteChars(int characterOffset) {
        int k;
        if (accessor.getValue().isEmpty()) {
            return;
        }
        if (accessor.getHighlightPos() != accessor.getCursorPos()) {
            this.insertText("");
            return;
        }
        int i = accessor.invokeGetCursorPos(characterOffset);
        int j = Math.min(i, accessor.getCursorPos());
        if (j == (k = Math.max(i, accessor.getCursorPos()))) {
            return;
        }
        String string = new StringBuilder(accessor.getValue()).delete(j, k).toString();
        /*if (!accessor.getFilter().test(string)) {
            return;
        }*/
        accessor.setTextVariable(string);
        this.moveCursorTo(j, false);
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        int keyCode = input.input();
        if(keyCode == 340){
            LShiftPressed = true;
        }
        if(keyCode == 344){
            RShiftPressed = true;
        }

        if (!this.canConsumeInput()) {
            return false;
        }

        if (input.isSelectAll()) {
            this.moveCursorToEnd(Minecraft.getInstance().hasShiftDown());
            this.setHighlightPos(0);
            return true;
        }
        if (input.isCopy()) {
            Minecraft.getInstance().keyboardHandler.setClipboard(this.getHighlighted());
            return true;
        }
        if (input.isPaste()) {
            if (accessor.getIsEditable()) {
                this.insertText(Minecraft.getInstance().keyboardHandler.getClipboard());
            }
            return true;
        }
        if (input.isCut()) {
            Minecraft.getInstance().keyboardHandler.setClipboard(this.getHighlighted());
            if (accessor.getIsEditable()) {
                this.insertText("");
            }
            return true;
        }
        switch (keyCode) {
            case 263: {
                if (Minecraft.getInstance().hasControlDown()) {
                    //this.setCursor(this.getWordSkipPosition(-1), Screen.hasShiftDown());
                    this.moveCursorTo(getCursorPosition() + getWordLength(-1), Minecraft.getInstance().hasShiftDown());
                    updateScrollPositions();
                } else {
                    this.moveCursor(-1, Minecraft.getInstance().hasShiftDown());
                }
                return true;
            }
            case 264:{//DOWN
                this.moveCursorVertical(1);
                return true;
            }
            case 265:{//UP
                this.moveCursorVertical(-1);
                return true;
            }
            case 262: {
                if (Minecraft.getInstance().hasControlDown()) {
                    //this.setCursor(this.getWordSkipPosition(1), Screen.hasShiftDown());
                    this.moveCursorTo(getCursorPosition() + getWordLength(1), Minecraft.getInstance().hasShiftDown());
                    updateScrollPositions();
                } else {
                    this.moveCursor(1, Minecraft.getInstance().hasShiftDown());
                }
                return true;
            }
            case 259: {
                if (accessor.getIsEditable()) {
                    this.erase(-1);
                }
                return true;
            }
            case 261: {
                if (accessor.getIsEditable()) {
                    this.erase(1);
                }
                return true;
            }
            case 268: {
                this.moveCursorToStart(Minecraft.getInstance().hasShiftDown());
                updateScrollPositions();
                return true;
            }
            case 269: {
                this.moveCursorToEnd(Minecraft.getInstance().hasShiftDown());
                updateScrollPositions();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyReleased(KeyEvent input) {
        int keyCode = input.input();
        if(keyCode == 340){
            LShiftPressed = false;
        }
        if(keyCode == 344){
            RShiftPressed = false;
        }
        return super.keyReleased(input);
    }

    @Override
    public boolean charTyped(CharacterEvent input) {
        if (!this.canConsumeInput()) {
            return false;
        }
        if (input.isAllowedChatCharacter()) {
            if (accessor.getIsEditable()) {
                String chr = input.codepointAsString();
                this.insertText(chr);
                if (BetterCommandBlockUI.BRACKET_AUTOCOMPLETE) {
                    // TODO: trace forward and check if needed
                    if (chr.equals("{")) {
                        this.insertText(Character.toString('}'));
                    } else if (chr.equals("[")){
                        this.insertText(Character.toString(']'));
                    }
                }
            }
            textModified = true;
            return true;
        }
        return false;
    }

    @Override
    public void setValue(String text) {
        /*if (!accessor.getFilter().test(text)) {
            return;
        }*/
        accessor.setTextVariable(text.length() > accessor.invokeGetMaxLength() ? text.substring(0, accessor.invokeGetMaxLength()) : text);
        this.moveCursorToEnd(Minecraft.getInstance().hasShiftDown());
        this.setHighlightPos(accessor.getCursorPos());
        this.onChanged(text, true);
    }

    public void setRawText(String text) {
        this.setValue(text);
    }

    public void refreshFormatting(){
        setRawText(getValue());
    }

    private void onChanged(String newText, boolean formatText) {
        if (accessor.getResponder() != null) {
            accessor.getResponder().accept(newText);
        }
        if(hasCommandSuggestor) {
            if (formatText) this.formatText(newText);
        } else {
            this.setUnformattedText(newText);
        }
        scrollY.setScale((lines.size()) / (double)visibleLines);
        scrollX.setScale((double)(maxLineWidth) / visibleChars);
        refreshSuggestorPos();
    }

    private void setUnformattedText(String text){
        textColors = new LinkedList<>();
        textColors.add(new Pair<>(Style.EMPTY.withColor(ChatFormatting.GRAY), 0));

        lines = new LinkedList<>();
        lines.add(text);
        lineOffsets = new LinkedList<>();
        lineOffsets.add(0);
        textOffsets = new LinkedList<>();
        textOffsets.add(0);

        maxLineWidth = 0;
        for(String line : lines){
            maxLineWidth = Math.max(line.length(), maxLineWidth);
        }
    }

    private interface SpacePeeker {
        Pair<Integer,String> run(int startIndex);
    }

    private void submitLine(String line, int indent){
        submitLine(line, indent, false);
    }

    private void submitLine(String line, int indent, boolean lastLine){
        if (BetterCommandBlockUI.AVOID_DOUBLE_NEWLINE && !lastLine) {
            String trimmedLine = line.replace(BetterCommandBlockUI.INDENTATION_CHAR+"", "");
            if (trimmedLine.isEmpty()) {
                return;
            }
        }
        String indentChar = "" + BetterCommandBlockUI.INDENTATION_CHAR;
        lines.add(indentChar.repeat(indent * BetterCommandBlockUI.INDENTATION_FACTOR) + line);
        lineOffsets.add(indent * BetterCommandBlockUI.INDENTATION_FACTOR);
        if (textOffsets.isEmpty()){
            textOffsets.add(0);
        } else {
            int index = textOffsets.size()-1;
            textOffsets.add(
                    textOffsets.get(index) + (lines.get(index).length() - lineOffsets.get(index))
            );
        }
    }

    private void formatText(String text) {
        Font textRenderer = Minecraft.getInstance().font;
        textColors = new LinkedList<>();
        List<Pair<Integer,Integer>> colorIndices = suggestor.getColors(text, 0);
        Stack<Integer> colorStack = new Stack<>();
        int currentColorListIndex = 0;
        int currentHighlightColor = 0;

        lines = new LinkedList<String>();
        lineOffsets = new LinkedList<Integer>();
        textOffsets = new LinkedList<Integer>();
        char[] textArr = text.toCharArray();
        int linestart = 0;
        int parenthesesDepth = 0;
        int currentIndex = 0;
        boolean singleQuoteString = false;
        boolean doubleQuoteString = false;
        boolean escapeChar = false;

        char current;
        String currentLine = "";
        boolean newLine = false;
        int lastWordStart = 0;

        SpacePeeker peeker = new SpacePeeker() {
            @Override
            public Pair<Integer,String> run(int startIndex) {
                // Check for following spaces, makes for consistent indentation
                StringBuilder outputLine = new StringBuilder();
                int tempCurrentIndex = startIndex + 1;
                if (tempCurrentIndex < textArr.length) {
                    char tempCurrent = textArr[tempCurrentIndex];
                    while (tempCurrentIndex < textArr.length - 1 && tempCurrent == ' ') {
                        ++tempCurrentIndex;
                        outputLine.append(tempCurrent);
                        tempCurrent = textArr[tempCurrentIndex];
                    }
                    startIndex = tempCurrentIndex - 1;
                }

                return new Pair<>(startIndex, outputLine.toString());
            }
        };

        while(currentIndex < textArr.length) {
            while(currentColorListIndex < colorIndices.size() && colorIndices.get(currentColorListIndex).getB() < currentIndex){
                colorStack.push(colorIndices.get(currentColorListIndex).getA());
                currentColorListIndex++;
            }

            current = textArr[currentIndex];

            if (textRenderer.width(currentLine) > BetterCommandBlockUI.WRAPAROUND_WIDTH){
                if(lastWordStart == 0){
                    lastWordStart = currentLine.length();
                }
                String truncatedLine = currentLine.substring(0, Math.min(lastWordStart, currentLine.length()));
                submitLine(truncatedLine, parenthesesDepth);
                if(truncatedLine.length() < currentLine.length()) {
                    currentLine = currentLine.substring(lastWordStart);
                } else {
                    currentLine = "";
                }
                lastWordStart = 0;
            }

            int colorStartIndex = currentIndex; // Necessary to make color start in right spot when peeking ahead
            switch(current){
                case '{':
                case '[':
                    escapeChar = false;
                    if (singleQuoteString || doubleQuoteString){
                        currentLine += current;
                        newLine = false;
                        break;
                    }
                    if (BetterCommandBlockUI.NEWLINE_PRE_OPEN_BRACKET){
                        submitLine(currentLine, parenthesesDepth);
                        lastWordStart = 0;
                        currentLine = "";
                        newLine = true;
                    }
                    currentLine += current;
                    if (BetterCommandBlockUI.NEWLINE_POST_OPEN_BRACKET){
                        // Peek ahead for spaces
                        Pair<Integer,String> peekResult = peeker.run(currentIndex);
                        currentIndex = peekResult.getA();
                        currentLine += peekResult.getB();

                        submitLine(currentLine, parenthesesDepth);
                        lastWordStart = 0;
                        currentLine = "";
                        newLine = true;
                    }
                    if (newLine) {
                        parenthesesDepth++;
                    }

                    if (currentColorListIndex > 0) {
                        currentHighlightColor = colorIndices.get(currentColorListIndex - 1).getA();
                        colorStack.push(currentHighlightColor);
                        if (currentHighlightColor != 0) {
                            colorIndices.add(currentColorListIndex, new Pair<>(getHighlightColorIndex(currentHighlightColor + 1), colorStartIndex));
                            currentColorListIndex++;
                        }
                    }
                    break;
                case '}':
                case ']':
                    escapeChar = false;
                    if (singleQuoteString || doubleQuoteString){
                        currentLine += current;
                        newLine = false;
                        break;
                    }
                    boolean indentationChanged = false;
                    if (BetterCommandBlockUI.NEWLINE_PRE_CLOSE_BRACKET){
                        submitLine(currentLine, parenthesesDepth);
                        lastWordStart = 0;
                        currentLine = "";
                        newLine = true;
                        parenthesesDepth = Math.max(0,parenthesesDepth-1);
                        indentationChanged = true;
                    }
                    currentLine += current;
                    if (BetterCommandBlockUI.NEWLINE_POST_CLOSE_BRACKET){
                        // Peek ahead for spaces
                        Pair<Integer,String> peekResult = peeker.run(currentIndex);
                        currentIndex = peekResult.getA();
                        currentLine += peekResult.getB();

                        submitLine(currentLine, parenthesesDepth);
                        lastWordStart = 0;
                        currentLine = "";
                        newLine = true;
                        if (!indentationChanged)
                            parenthesesDepth = Math.max(0,parenthesesDepth-1);
                    }

                    if (currentColorListIndex > 0 && colorIndices.get(currentColorListIndex - 1).getA() != 0) {
                        colorIndices.add(currentColorListIndex, new Pair<>(colorStack.pop(), colorStartIndex + 1));
                        currentColorListIndex++;
                    }
                    break;
                case ',':
                    escapeChar = false;
                    currentLine += current;
                    lastWordStart = currentLine.length();

                    // Peek ahead for spaces
                    Pair<Integer,String> peekResult = peeker.run(currentIndex);
                    currentIndex = peekResult.getA();
                    currentLine += peekResult.getB();

                    if (BetterCommandBlockUI.NEWLINE_POST_COMMA){
                        submitLine(currentLine, parenthesesDepth);
                        lastWordStart = 0;
                        currentLine = "";
                        //newLine = true;
                    }
                    break;
                case ' ':
                    currentLine += current;
                    newLine = false;
                    escapeChar = false;
                    lastWordStart = currentLine.length();
                    break;
                case '\\':
                    escapeChar = !escapeChar;
                    currentLine += current;
                    newLine = false;
                    break;
                case '"':
                    if (!BetterCommandBlockUI.FORMAT_STRINGS && !singleQuoteString && !escapeChar){
                        doubleQuoteString = !doubleQuoteString;
                        escapeChar = false;
                    }
                    currentLine += current;
                    break;
                case '\'':
                    if (!BetterCommandBlockUI.FORMAT_STRINGS && !escapeChar){
                        singleQuoteString = !singleQuoteString;
                        escapeChar = false;
                    }
                    currentLine += current;
                    break;
                default:
                    currentLine += current;
                    newLine = false;
                    escapeChar = false;
                    break;
            }

            currentIndex++;
        }
        if (!currentLine.isEmpty()){
            submitLine(currentLine, parenthesesDepth, true);
        }

        for(Pair<Integer,Integer> p : colorIndices){
            textColors.add(new Pair<>(suggestor.getColor(p.getA()),p.getB()));
        }

        if(!text.isEmpty() && FabricLoader.getInstance().isModLoaded("better-highlighting")){
            transformTokenOutput(Tokenizer.tokenizeAndFormatComponent(text, BuiltinGrammar.mcfunction(), TextMateRegistry.instance().getCurrentTheme()).getSiblings());
        }

        maxLineWidth = 0;
        for(String line : lines){
            maxLineWidth = Math.max(line.length(), maxLineWidth);
        }
    }

    private int getHighlightColorIndex(int i){
        int index = i - 2;
        int count = suggestor.getHighlighColorCount();
        if(index < 0) index+= count;
        return (index % count) + 2;
    }

    private void transformTokenOutput(List<Component> output){
        textColors.clear();
        int index = 0;
        for(Component text : output) {
            if (text.getStyle().getColor() != null) {
                textColors.add(new Pair<>(text.getStyle(), index));
            }
            index += text.getString().length();
        }
    }

    private boolean getHovered(double mouseX, double mouseY){
        return mouseX >= (double) this.getX() && mouseX < (double)(this.getX() + this.width) && mouseY >= (double) this.getY() && mouseY < (double)(this.getY() + this.height);
    }

    @Override
    public void onClick(MouseButtonEvent click, boolean doubled){
        if (!this.isVisible()) {
            return;
        }

        double mouseX = click.x();
        double mouseY = click.y();
        scrollX.mouseClicked(click, doubled);
        scrollY.mouseClicked(click, doubled);

        boolean hovered = getHovered(mouseX, mouseY);
        if (accessor.getCanLoseFocus()) {
            this.setFocused(hovered);
        }
        if(this.isFocused() && hovered && click.button() == 0) {
            int previousIndex = accessor.getCursorPos();
            this.moveCursorTo(pointToIndex(mouseX, mouseY), Minecraft.getInstance().hasShiftDown());
            cursorPosPreference = new Pair<>((int)mouseX, (int)mouseY);
            if(doubled){
                selectWord();
            }
            timeSinceClick = 0.0f;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount){
        if (!this.isVisible()) {
            return false;
        }
        if(screen != null && screen.scroll(verticalAmount)){
            return true;
        }
        if(LShiftPressed || RShiftPressed){
            horizontalOffset = clamp(horizontalOffset-(int)verticalAmount*BetterCommandBlockUI.SCROLL_STEP_X, 0, maxLineWidth-20);
            scrollX.updatePos((double)horizontalOffset / (maxLineWidth-20));
        } else {
            scrolledLines = clamp(scrolledLines-(int)verticalAmount*BetterCommandBlockUI.SCROLL_STEP_Y, 0, lines.size()-visibleLines);
            scrollY.updatePos((double)scrolledLines / (lines.size() - visibleLines));
        }
        refreshSuggestorPos();
        return true;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (!this.isVisible()) {
            return;
        }
        scrollX.mouseMoved(mouseX, mouseY);
        scrollY.mouseMoved(mouseX, mouseY);
    }

    @Override
    public void onDrag(MouseButtonEvent click, double offsetX, double offsetY){
        if (!this.isVisible()) {
            return;
        }
        double mouseX = click.x();
        double mouseY = click.y();
        scrollX.onDrag(click, offsetX, offsetY);
        scrollY.onDrag(click, offsetX, offsetY);

        if (this.isHovered() && this.isFocused()){
            setCursorPosition(pointToIndex(mouseX, mouseY));
        }
    }

    @Override
    public void onRelease(MouseButtonEvent click){
        if (!this.isVisible()) {
            return;
        }
        scrollX.mouseReleased(click);
        scrollY.mouseReleased(click);
    }

    private void moveCursorVertical(int delta){
        Pair<Integer, Integer> lineAndOffset = indexToLineAndOffset(accessor.invokeGetCursorPos(0));
        int yPreference = getY() + 5 + (lineAndOffset.getA() - scrolledLines) * 10;
        cursorPosPreference.setB(yPreference + delta * 10);
        int index = pointToIndex(cursorPosPreference.getA(), cursorPosPreference.getB());
        moveCursorTo(index, Minecraft.getInstance().hasShiftDown());

        updateScrollPositions();
    }

    @Override
    public void moveCursor(int offset, boolean shiftKeyPressed) {
        Font textRenderer = accessor.getFont();
        this.moveCursorTo(accessor.invokeGetCursorPos(offset), shiftKeyPressed);
        if(lines.isEmpty()) return;
        Pair<Integer, Integer> lineAndOffset = indexToLineAndOffset(accessor.invokeGetCursorPos(0));
        String line = lines.get(lineAndOffset.getA());
        int xPreference = getX() + textRenderer.width(line.substring(0,Math.min(line.length()-1,lineAndOffset.getB())));
        cursorPosPreference = new Pair<>(xPreference, this.getY() + 10 * (lineAndOffset.getA() - scrolledLines));

        updateScrollPositions();
    }

    private void updateScrollPositions(){
        Font textRenderer = accessor.getFont();

        Pair<Integer, Integer> lineAndOffset = indexToLineAndOffset(accessor.invokeGetCursorPos(0));
        if(lines.size() < 1){
            horizontalOffset = 0;
            scrollY.updatePos(0);
            scrolledLines = 0;
            scrollX.updatePos(0);
            return;
        }
        String line = lines.get(lineAndOffset.getA());
        int maxIndex = line.length()-1;
        boolean lineEndLeftOfWindow = horizontalOffset > lineAndOffset.getB();
        int xPos;
        if(lineEndLeftOfWindow){
            int extension = 1 + horizontalOffset - lineAndOffset.getB();
            line += "_".repeat(extension);
            maxIndex += extension;
            xPos = textRenderer.width(line.substring(clamp(lineAndOffset.getB(), 0, maxIndex), clamp(horizontalOffset, 0, maxIndex))) * -1;
        } else {
            xPos = textRenderer.width(line.substring(clamp(horizontalOffset, 0, maxIndex), clamp(lineAndOffset.getB(), 0, maxIndex)));
        }

        int textWidth = getWidth() - 8;
        if(xPos <= 5){
            //Not exact, but I won't go 1 character at a time and check if it's far enough
            horizontalOffset = clamp(horizontalOffset + (xPos-10)/5, 0, maxLineWidth-20);
            scrollX.updatePos((double)horizontalOffset / (maxLineWidth-20));
        } else if (xPos >= textWidth){
            horizontalOffset = clamp(horizontalOffset + (xPos-textWidth)/5, 0, maxLineWidth-20);
            scrollX.updatePos((double)horizontalOffset / (maxLineWidth-20));
        }


        int lineIndex = lineAndOffset.getA();
        if(lineIndex < scrolledLines){
            scrolledLines = clamp(scrolledLines - (scrolledLines - lineIndex), 0, lines.size()-visibleLines);
            scrollY.updatePos((double)scrolledLines / (lines.size() - visibleLines));
        } else if(lineIndex >= scrolledLines + visibleLines){
            scrolledLines = clamp(scrolledLines + 1 + ((lineIndex - scrolledLines) - visibleLines), 0, lines.size()-visibleLines);
            scrollY.updatePos((double)scrolledLines / (lines.size() - visibleLines));
        }
    }

    @Override
    public void moveCursorTo(int cursor, boolean shiftKeyPressed) {
        //System.out.println(cursor);
        this.setCursorPosition(cursor);
        if (!shiftKeyPressed) {
            this.setHighlightPos(accessor.getCursorPos());
        }
        this.onChanged(accessor.getValue(), false);
    }

    public void setScroll(double value){
        this.scrolledLines = (int)Math.max(Math.round((lines.size() - visibleLines) * value),0);
        refreshSuggestorPos();
    }

    public void setHorizontalOffset(double value){
        this.horizontalOffset = (int)Math.max(Math.floor((maxLineWidth - visibleChars) * value),0);
        refreshSuggestorPos();
    }

    @Override
    public void setEditable(boolean value){
        super.setEditable(value);
        if(this.suggestor != null) this.suggestor.setAllowSuggestions(value);
    }

    public boolean wasModified(){
        return textModified;
    }

    public void resetModified(){
        textModified = false;
    }

    void refreshSuggestorPos(){
        if(!hasCommandSuggestor) return;
        int selectionStart = accessor.getCursorPos();
        int selectionEnd = accessor.getHighlightPos();

        if(selectionStart > selectionEnd){
            selectionStart = selectionEnd;
        }
        Pair<Integer, Integer> cursor = indexToLineAndOffset(selectionStart);
        int selectionStartOffset = Math.max(cursor.getB() - horizontalOffset, 0);
        int fontHeight = accessor.getFont().lineHeight + 1;
        if(lines.isEmpty()){
            suggestor.setPos(getX() + 5, getY() + 5 + fontHeight);
            suggestor.refreshRenderPos();
            return;
        }
        String line = lines.get(cursor.getA());
        line = line.substring(clamp(horizontalOffset, 0,line.length()));
        int x = this.getX() + 5 + accessor.getFont().width(line.substring(0, Math.min(selectionStartOffset,line.length())));
        int y = this.getY() + 5 + fontHeight + (cursor.getA() - scrolledLines) * fontHeight;

        suggestor.setPos(x, y);
        suggestor.refreshRenderPos();
    }

    private int clamp(int i, int min, int max){
        return Math.max(Math.min(i,max),min);
    }

    private double clamp(double i, double min, double max){
        return Math.max(Math.min(i,max),min);
    }

    private void printStackTrace(){
        try{
            int[] i = {};
            i[1] = 0;
        } catch (Exception e){
            System.out.println("=================");
            StackTraceElement[] stacktrace = e.getStackTrace();
            for(StackTraceElement element : stacktrace){
                System.out.println(element);
            }
            System.out.println("=================");
        }
    }
}
