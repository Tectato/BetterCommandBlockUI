package bettercommandblockui.main.ui;

import bettercommandblockui.main.ui.screen.AbstractBetterCommandBlockScreen;
import bettercommandblockui.main.ui.screen.BetterCommandBlockScreen;
import bettercommandblockui.main.BetterCommandBlockUI;
import bettercommandblockui.mixin.TextFieldWidgetAccessor;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.util.Util;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class MultiLineTextFieldWidget extends TextFieldWidget implements Element {
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
    private MultiLineCommandSuggestor suggestor;
    private TextFieldWidgetAccessor accessor = (TextFieldWidgetAccessor)this;

    public MultiLineTextFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text text, AbstractBetterCommandBlockScreen screen) {
        super(textRenderer, x, y, width, height, text);
        this.lines = new LinkedList<String>();
        this.lineOffsets = new LinkedList<Integer>();
        this.textOffsets = new LinkedList<Integer>();
        this.visibleLines = (height-4) / 10;
        this.scrolledLines = 0;
        this.screen = screen;
        this.scrollX = new TextFieldScrollbarWidget(x, y + height + 1, width, 10, Text.of(""), this, true);
        this.scrollY = new TextFieldScrollbarWidget(x + width + 1, y, 10, height, Text.of(""), this, false);
        cursorPosPreference = new Pair<>(0,0);
    }

    @Override
    public void setWidth(int width){
        this.width = width;
        scrollX.setWidth(width);
    }

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
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta){
        //TODO: render line at x of cursor if currently at parenthesis
        int color;
        if (!this.isVisible()) {
            return;
        }
        if (accessor.getDrawsBackground()) {
            color = this.isFocused() ? -1 : -6250336;
            DrawableHelper.fill(matrices, this.getX() - 1, this.getY() - 1, this.getX() + this.width + 1, this.getY() + this.height + 1, color);
            DrawableHelper.fill(matrices, this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, -16777216);
        }

        color = accessor.getEditable() ? accessor.getEditableColor() : accessor.getUneditableColor();

        if(hasCommandSuggestor) {
            if(lines.isEmpty()){
                renderSuggestor(matrices, mouseX, mouseY);
                return;
            }

            for (int i = scrolledLines; i < scrolledLines + visibleLines && i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.length() >= horizontalOffset) {
                    line = line.substring(horizontalOffset);
                    line = accessor.getTextRenderer().trimToWidth(line, this.getInnerWidth());

                    this.drawColoredLine(matrices, line, this.getX() + 5, this.getY() + 10 * (i - scrolledLines) + 5, i);
                }
            }
        } else {
            this.drawRawText(matrices, accessor.getText(), this.getX() + 5, this.getY() + 5, color);
        }

        scrollX.render(matrices, mouseX, mouseY, delta);
        scrollY.render(matrices, mouseX, mouseY, delta);

        if(!hasCommandSuggestor) return;

        int selectionStart = accessor.getSelectionStart();
        int selectionEnd = accessor.getSelectionEnd();

        boolean selectingBackwards = false;
        if(selectionStart > selectionEnd){
            int temp = selectionEnd;
            selectionEnd = selectionStart;
            selectionStart = temp;
            selectingBackwards = true;
        }

        Pair<Integer, Integer> start = indexToLineAndOffset(selectionStart);
        Pair<Integer, Integer> end = indexToLineAndOffset(selectionEnd);

        int firstSelectedLine = start.getLeft();
        int selectionStartOffset = start.getRight();
        int lastSelectedLine = end.getLeft();
        int selectionEndOffset = end.getRight();

        selectionStartOffset -= horizontalOffset;
        selectionEndOffset -= horizontalOffset;

        selectionStartOffset = Math.max(selectionStartOffset,0);
        selectionEndOffset = Math.max(selectionEndOffset,0);

        boolean renderVerticalCursor = selectionStart < accessor.getText().length();
        boolean verticalCursorVisible = this.isFocused() && accessor.getFocusedTicks() / 6 % 2 == 0;

        matrices.translate(0.0,0.0,0.1);

        RenderSystem.enableColorLogicOp();
        RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
        for (int i = firstSelectedLine; i <= lastSelectedLine; i++) {
            if(i < scrolledLines || i >= scrolledLines+visibleLines) continue;
            int x1 = this.getX() + 5;
            int x2 = x1;
            int y = this.getY() + 10 * (i - scrolledLines) + 5;

            String visibleLine = lines.get(i).substring(Math.min(horizontalOffset,lines.get(i).length()));
            if (i == firstSelectedLine) {
                x1 += accessor.getTextRenderer().getWidth(visibleLine.substring(0, Math.min(selectionStartOffset,visibleLine.length())));

                if (verticalCursorVisible && !selectingBackwards) {
                    if (renderVerticalCursor) {
                        DrawableHelper.fill(matrices, x1, y - 1, x1 + 1, y + 1 + accessor.getTextRenderer().fontHeight, -3092272);
                    } else {
                        RenderSystem.disableColorLogicOp();
                        accessor.getTextRenderer().drawWithShadow(matrices, "_", x1, y, -3092272);
                    }
                }
            }

            if (i == lastSelectedLine) {
                x2 += accessor.getTextRenderer().getWidth(visibleLine.substring(0, Math.min(selectionEndOffset,visibleLine.length())));

                if (verticalCursorVisible && renderVerticalCursor && selectingBackwards) {
                    DrawableHelper.fill(matrices, x2, y - 1, x2 + 1, y + 1 + accessor.getTextRenderer().fontHeight, -3092272);
                }

            } else {
                x2 += this.getInnerWidth();
            }
            accessor.invokeDrawSelectionHighlight(matrices, x1, y, x2, y + 10);
        }
        RenderSystem.disableColorLogicOp();

        renderSuggestor(matrices, mouseX, mouseY);
    }

    private void renderSuggestor(MatrixStack matrices, int mouseX, int mouseY){
        if(suggestor.getY() > getY() + getHeight() || suggestor.getY() < getY()) return;
        if(suggestor.getX() > getX() + getWidth() || suggestor.getX() < getX()) return;
        suggestor.render(matrices, mouseX, mouseY);
    }

    private void drawColoredLine(MatrixStack matrices, String content, int x, int y, int lineIndex){
        TextRenderer textRenderer = accessor.getTextRenderer();
        int renderOffset = 0;
        int startOffset = textOffsets.get(lineIndex); // start index of this line within command
        int currentOffset = 0;
        int numSpaces = lineOffsets.get(lineIndex); // number of spaces before this line
        if(textColors.size() > 1) {
            for (int i = 0; i < textColors.size(); i++) {
                if(currentOffset >= content.length()) break;
                int nextColorStart = (i+1)<textColors.size() ? textColors.get(i+1).getRight() : (startOffset + content.length());
                nextColorStart -= startOffset;
                nextColorStart += numSpaces-horizontalOffset;
                if (nextColorStart > currentOffset) {
                    String substring = content.substring(currentOffset, clamp(nextColorStart, currentOffset, content.length()));

                    int color;
                    try{
                        color = textColors.get(i).getLeft().getColor().getRgb();
                    } catch (IndexOutOfBoundsException e){
                        color = TextColor.fromFormatting(Formatting.GRAY).getRgb();
                    }

                    textRenderer.drawWithShadow(
                            matrices,
                            substring,
                            x + renderOffset,
                            y,
                            color
                    );
                    currentOffset += substring.length();
                    renderOffset += textRenderer.getWidth(substring);
                    //numSpaces = 0;
                }
                if(currentOffset > content.length()) break;
            }
        } else {
            int color;
            try{
                color = textColors.get(0).getLeft().getColor().getRgb();
            } catch (IndexOutOfBoundsException e){
                color = TextColor.fromFormatting(Formatting.GRAY).getRgb();
            }
            textRenderer.drawWithShadow(matrices, content, x, y, color);
        }
    }

    private void drawRawText(MatrixStack matrices, String content, int x, int y, int color){
        TextRenderer textRenderer = accessor.getTextRenderer();
        String line = content.substring(Math.max(Math.min(horizontalOffset, content.length() - 1),0));
        String trimmedLine = textRenderer.trimToWidth(line,this.getInnerWidth());
        textRenderer.drawWithShadow(matrices, trimmedLine, x, y, color);
    }

    private int pointToIndex(double x, double y){
        if(lines.size() > 0) {
            TextRenderer textRenderer = accessor.getTextRenderer();
            int lineIndex = (int) Math.floor((y - (this.getY() + 5)) / 10) + scrolledLines;
            lineIndex = Math.max(Math.min(lineIndex, lines.size() - 1), 0);
            String line = lines.get(lineIndex);
            int offset = 0;
            if(line.length() > 0) {
                String visibleLine = line.substring(Math.min(horizontalOffset, line.length()));
                String trimmedLine = textRenderer.trimToWidth(visibleLine, (int) (x - (this.getX() + 5)));
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
                output.setLeft(i);
                output.setRight((index - textOffsets.get(i)) + lineOffsets.get(i));
                return output;
            }
        }
        return output;
    }

    public Pair<Integer, Integer> getCharacterPos(int index){
        Pair<Integer,Integer> output = indexToLineAndOffset(index);
        int x,y;
        try {
            x = this.getX() + 5 + accessor.getTextRenderer().getWidth(lines.get(output.getLeft()).substring(horizontalOffset, output.getRight()));
        } catch(Exception e){
            x = 0;
        }
        y = this.getY() + 5 + 10 * (output.getLeft()-scrolledLines);
        return new Pair<>(x,y);
    }

    public void setCommandSuggestor(MultiLineCommandSuggestor newSuggestor){
        this.suggestor = newSuggestor;
        this.hasCommandSuggestor = true;
    }

    @Override
    public void write(String text) {
        String string2;
        String string;
        int l;
        int i = Math.min(accessor.getSelectionStart(), accessor.getSelectionEnd());
        int j = Math.max(accessor.getSelectionStart(), accessor.getSelectionEnd());
        int k = accessor.invokeGetMaxLength() - accessor.getText().length() - (i - j);
        if (k < (l = (string = SharedConstants.stripInvalidChars(text)).length())) {
            string = string.substring(0, k);
            l = k;
        }
        if (!accessor.getTextPredicate().test(string2 = new StringBuilder(accessor.getText()).replace(i, j, string).toString())) {
            return;
        }
        accessor.setTextVariable(string2);
        this.setSelectionStart(i + l);
        this.setSelectionEnd(accessor.getSelectionStart());
        this.onChanged(accessor.getText(), true);
        this.updateScrollPositions();
    }

    private void erase(int offset) {
        if (Screen.hasControlDown()) {
            this.eraseWords(offset);
        } else {
            this.eraseCharacters(offset);
        }
        this.onChanged(accessor.getText(), true);
        this.updateScrollPositions();
    }

    private boolean isAlphanumeric(char a){
        return Character.isLetter(a) || Character.isDigit(a) || a == '_';
    }

    private int getWordLength(int offsetDir){
        /*
         * If traversing letters or numbers, keep going until reaching a non-alphanumeric character.
         * Otherwise, only traverse chunks of the same character
         */
        int startIndex = accessor.getSelectionStart() + (offsetDir < 0 ? -1 : 0);
        String text = getText();
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

    @Override
    public void eraseWords(int wordOffset) {
        if (accessor.getText().isEmpty()) {
            return;
        }
        if (accessor.getSelectionEnd() != accessor.getSelectionStart()) {
            this.write("");
            return;
        }
        //this.eraseCharacters(this.getWordSkipPosition(wordOffset) - accessor.getSelectionStart());
        eraseCharacters(getWordLength(wordOffset));
    }

    @Override
    public void eraseCharacters(int characterOffset) {
        int k;
        if (accessor.getText().isEmpty()) {
            return;
        }
        if (accessor.getSelectionEnd() != accessor.getSelectionStart()) {
            this.write("");
            return;
        }
        int i = accessor.invokeGetCursorPosWithOffset(characterOffset);
        int j = Math.min(i, accessor.getSelectionStart());
        if (j == (k = Math.max(i, accessor.getSelectionStart()))) {
            return;
        }
        String string = new StringBuilder(accessor.getText()).delete(j, k).toString();
        if (!accessor.getTextPredicate().test(string)) {
            return;
        }
        accessor.setTextVariable(string);
        this.setCursor(j);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(keyCode == 340){
            LShiftPressed = true;
        }
        if(keyCode == 344){
            RShiftPressed = true;
        }

        if (!this.isActive()) {
            return false;
        }

        if (Screen.isSelectAll(keyCode)) {
            this.setCursorToEnd();
            this.setSelectionEnd(0);
            return true;
        }
        if (Screen.isCopy(keyCode)) {
            MinecraftClient.getInstance().keyboard.setClipboard(this.getSelectedText());
            return true;
        }
        if (Screen.isPaste(keyCode)) {
            if (accessor.getEditable()) {
                this.write(MinecraftClient.getInstance().keyboard.getClipboard());
            }
            return true;
        }
        if (Screen.isCut(keyCode)) {
            MinecraftClient.getInstance().keyboard.setClipboard(this.getSelectedText());
            if (accessor.getEditable()) {
                this.write("");
            }
            return true;
        }
        switch (keyCode) {
            case 263: {
                if (Screen.hasControlDown()) {
                    //this.setCursor(this.getWordSkipPosition(-1), Screen.hasShiftDown());
                    this.setCursor(getCursor() + getWordLength(-1));
                    updateScrollPositions();
                } else {
                    this.moveCursor(-1);
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
                if (Screen.hasControlDown()) {
                    //this.setCursor(this.getWordSkipPosition(1), Screen.hasShiftDown());
                    this.setCursor(getCursor() + getWordLength(1));
                    updateScrollPositions();
                } else {
                    this.moveCursor(1);
                }
                return true;
            }
            case 259: {
                if (accessor.getEditable()) {
                    this.erase(-1);
                }
                return true;
            }
            case 261: {
                if (accessor.getEditable()) {
                    this.erase(1);
                }
                return true;
            }
            case 268: {
                this.setCursorToStart();
                updateScrollPositions();
                return true;
            }
            case 269: {
                this.setCursorToEnd();
                updateScrollPositions();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if(keyCode == 340){
            LShiftPressed = false;
        }
        if(keyCode == 344){
            RShiftPressed = false;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (!this.isActive()) {
            return false;
        }
        if (SharedConstants.isValidChar(chr)) {
            if (accessor.getEditable()) {
                this.write(Character.toString(chr));
                if (BetterCommandBlockUI.BRACKET_AUTOCOMPLETE) {
                    // TODO: trace forward and check if needed
                    if (chr == '{') {
                        this.write(Character.toString('}'));
                    } else if (chr == '['){
                        this.write(Character.toString(']'));
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void setText(String text) {
        if (!accessor.getTextPredicate().test(text)) {
            return;
        }
        accessor.setTextVariable(text.length() > accessor.invokeGetMaxLength() ? text.substring(0, accessor.invokeGetMaxLength()) : text);
        this.setCursorToEnd();
        this.setSelectionEnd(accessor.getSelectionStart());
        this.onChanged(text, true);
    }

    public void setRawText(String text) {
        this.setText(text);
    }

    public void refreshFormatting(){
        setRawText(getText());
    }

    private void onChanged(String newText, boolean formatText) {
        if (accessor.getChangedListener() != null) {
            accessor.getChangedListener().accept(newText);
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
        textColors.add(new Pair<>(Style.EMPTY.withColor(Formatting.GRAY), 0));

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
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
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
            while(currentColorListIndex < colorIndices.size() && colorIndices.get(currentColorListIndex).getRight() < currentIndex){
                colorStack.push(colorIndices.get(currentColorListIndex).getLeft());
                currentColorListIndex++;
            }

            current = textArr[currentIndex];

            if (textRenderer.getWidth(currentLine) > BetterCommandBlockUI.WRAPAROUND_WIDTH){
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
                        currentIndex = peekResult.getLeft();
                        currentLine += peekResult.getRight();

                        submitLine(currentLine, parenthesesDepth);
                        lastWordStart = 0;
                        currentLine = "";
                        newLine = true;
                    }
                    if (newLine) {
                        parenthesesDepth++;
                    }

                    if (currentColorListIndex > 0) {
                        currentHighlightColor = colorIndices.get(currentColorListIndex - 1).getLeft();
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
                        currentIndex = peekResult.getLeft();
                        currentLine += peekResult.getRight();

                        submitLine(currentLine, parenthesesDepth);
                        lastWordStart = 0;
                        currentLine = "";
                        newLine = true;
                        if (!indentationChanged)
                            parenthesesDepth = Math.max(0,parenthesesDepth-1);
                    }

                    if (currentColorListIndex > 0 && colorIndices.get(currentColorListIndex - 1).getLeft() != 0) {
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
                    currentIndex = peekResult.getLeft();
                    currentLine += peekResult.getRight();

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
            textColors.add(new Pair<>(suggestor.getColor(p.getLeft()),p.getRight()));
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

    private boolean getHovered(double mouseX, double mouseY){
        return mouseX >= (double) this.getX() && mouseX < (double)(this.getX() + this.width) && mouseY >= (double) this.getY() && mouseY < (double)(this.getY() + this.height);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button){
        if (!this.isVisible()) {
            return false;
        }

        scrollX.mouseClicked(mouseX, mouseY, button);
        scrollY.mouseClicked(mouseX, mouseY, button);

        boolean hovered = getHovered(mouseX, mouseY);
        if (accessor.getFocusUnlocked()) {
            this.setFocused(hovered);
        }
        if(this.isFocused() && hovered && button == 0) {
            this.setCursor(pointToIndex(mouseX, mouseY));
            cursorPosPreference = new Pair<>((int)mouseX, (int)mouseY);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount){
        if (!this.isVisible()) {
            return false;
        }
        if(screen != null && screen.scroll(amount)){
            return true;
        }
        if(LShiftPressed || RShiftPressed){
            horizontalOffset = clamp(horizontalOffset-(int)amount*BetterCommandBlockUI.SCROLL_STEP_X, 0, maxLineWidth-20);
            scrollX.updatePos((double)horizontalOffset / (maxLineWidth-20));
        } else {
            scrolledLines = clamp(scrolledLines-(int)amount*BetterCommandBlockUI.SCROLL_STEP_Y, 0, lines.size()-visibleLines);
            scrollY.updatePos((double)scrolledLines / (lines.size() - visibleLines));
        }
        refreshSuggestorPos();
        return true;
    }

    public boolean isHovered(double mouseX, double mouseY){
        return (mouseX >= getX() && mouseX <= getX() + getWidth()) && (mouseY >= getY() && mouseY <= getY() + getHeight());
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
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY){
        if (!this.isVisible()) {
            return false;
        }
        scrollX.onDrag(mouseX, mouseY, deltaX, deltaY);
        scrollY.onDrag(mouseX, mouseY, deltaX, deltaY);

        if (this.isHovered(mouseX, mouseY) && this.isFocused()){
            setSelectionStart(pointToIndex(mouseX, mouseY));
        }
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button){
        if (!this.isVisible()) {
            return false;
        }
        scrollX.mouseReleased(mouseX, mouseY, button);
        scrollY.mouseReleased(mouseX, mouseY, button);
        return true;
    }

    private void moveCursorVertical(int delta){
        Pair<Integer, Integer> lineAndOffset = indexToLineAndOffset(accessor.invokeGetCursorPosWithOffset(0));
        int yPreference = getY() + 5 + (lineAndOffset.getLeft() - scrolledLines) * 10;
        cursorPosPreference.setRight(yPreference + delta * 10);
        int index = pointToIndex(cursorPosPreference.getLeft(), cursorPosPreference.getRight());
        setCursor(index);

        updateScrollPositions();
    }

    @Override
    public void moveCursor(int offset) {
        TextRenderer textRenderer = accessor.getTextRenderer();
        this.setCursor(accessor.invokeGetCursorPosWithOffset(offset));
        if(lines.isEmpty()) return;
        Pair<Integer, Integer> lineAndOffset = indexToLineAndOffset(accessor.invokeGetCursorPosWithOffset(0));
        String line = lines.get(lineAndOffset.getLeft());
        int xPreference = getX() + textRenderer.getWidth(line.substring(0,Math.min(line.length()-1,lineAndOffset.getRight())));
        cursorPosPreference = new Pair<>(xPreference, this.getY() + 10 * (lineAndOffset.getLeft() - scrolledLines));

        updateScrollPositions();
    }

    private void updateScrollPositions(){
        TextRenderer textRenderer = accessor.getTextRenderer();

        Pair<Integer, Integer> lineAndOffset = indexToLineAndOffset(accessor.invokeGetCursorPosWithOffset(0));
        if(lines.size() < 1){
            horizontalOffset = 0;
            scrollY.updatePos(0);
            scrolledLines = 0;
            scrollX.updatePos(0);
            return;
        }
        String line = lines.get(lineAndOffset.getLeft());
        int maxIndex = line.length()-1;
        boolean lineEndLeftOfWindow = horizontalOffset > lineAndOffset.getRight();
        int xPos;
        if(lineEndLeftOfWindow){
            int extension = 1 + horizontalOffset - lineAndOffset.getRight();
            line += "_".repeat(extension);
            maxIndex += extension;
            xPos = textRenderer.getWidth(line.substring(clamp(lineAndOffset.getRight(), 0, maxIndex), clamp(horizontalOffset, 0, maxIndex))) * -1;
        } else {
            xPos = textRenderer.getWidth(line.substring(clamp(horizontalOffset, 0, maxIndex), clamp(lineAndOffset.getRight(), 0, maxIndex)));
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


        int lineIndex = lineAndOffset.getLeft();
        if(lineIndex < scrolledLines){
            scrolledLines = clamp(scrolledLines - (scrolledLines - lineIndex), 0, lines.size()-visibleLines);
            scrollY.updatePos((double)scrolledLines / (lines.size() - visibleLines));
        } else if(lineIndex >= scrolledLines + visibleLines){
            scrolledLines = clamp(scrolledLines + 1 + ((lineIndex - scrolledLines) - visibleLines), 0, lines.size()-visibleLines);
            scrollY.updatePos((double)scrolledLines / (lines.size() - visibleLines));
        }
    }

    @Override
    public void setCursor(int cursor) {
        //System.out.println(cursor);
        this.setSelectionStart(cursor);
        if (!Screen.hasShiftDown()) {
            this.setSelectionEnd(accessor.getSelectionStart());
        }
        this.onChanged(accessor.getText(), false);
    }

    public void setScroll(double value){
        this.scrolledLines = (int)Math.max(Math.round((lines.size() - visibleLines) * value),0);
        refreshSuggestorPos();
    }

    public void setHorizontalOffset(double value){
        this.horizontalOffset = (int)Math.max(Math.floor((maxLineWidth - visibleChars) * value),0);
        refreshSuggestorPos();
    }

    void refreshSuggestorPos(){
        if(!hasCommandSuggestor) return;
        int selectionStart = accessor.getSelectionStart();
        int selectionEnd = accessor.getSelectionEnd();

        if(selectionStart > selectionEnd){
            selectionStart = selectionEnd;
        }
        Pair<Integer, Integer> cursor = indexToLineAndOffset(selectionStart);
        int selectionStartOffset = Math.max(cursor.getRight() - horizontalOffset, 0);
        int fontHeight = accessor.getTextRenderer().fontHeight + 1;
        if(lines.isEmpty()){
            suggestor.setPos(getX() + 5, getY() + 5 + fontHeight);
            suggestor.refreshRenderPos();
            return;
        }
        String line = lines.get(cursor.getLeft());
        line = line.substring(clamp(horizontalOffset, 0,line.length()));
        int x = this.getX() + 5 + accessor.getTextRenderer().getWidth(line.substring(0, Math.min(selectionStartOffset,line.length())));
        int y = this.getY() + 5 + fontHeight + (cursor.getLeft() - scrolledLines) * fontHeight;

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
