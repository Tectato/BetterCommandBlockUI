package bettercommandblockui.main.ui;

import bettercommandblockui.main.ui.screen.AbstractBetterCommandBlockScreen;
import bettercommandblockui.main.ui.screen.BetterCommandBlockScreen;
import bettercommandblockui.main.BetterCommandBlockUI;
import bettercommandblockui.mixin.TextFieldWidgetAccessor;
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

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class MultiLineTextFieldWidget extends TextFieldWidget implements Element {
    private final int indentationFactor = BetterCommandBlockUI.INDENTATION_FACTOR;
    private final int visibleChars = 20;

    private AbstractBetterCommandBlockScreen screen;
    private ScrollbarWidget scrollX, scrollY;
    private List<String> lines;
    private List<Integer> lineOffsets, textOffsets;
    private List<Pair<Style, Integer>> textColors;
    private boolean stringParameter = false;
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
        this.scrollX = new ScrollbarWidget(x, y + height + 1, width, 10, Text.of(""), this, true);
        this.scrollY = new ScrollbarWidget(x + width + 1, y, 10, height, Text.of(""), this, false);
        cursorPosPreference = new Pair<>(0,0);
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta){
        int color;
        if (!this.isVisible()) {
            return;
        }
        if (accessor.invokeDrawsBackground()) {
            color = this.isFocused() ? -1 : -6250336;
            TextFieldWidget.fill(matrices, this.getX() - 1, this.getY() - 1, this.getX() + this.width + 1, this.getY() + this.height + 1, color);
            TextFieldWidget.fill(matrices, this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, -16777216);
        }

        color = accessor.getEditable() ? accessor.getEditableColor() : accessor.getUneditableColor();

        if(hasCommandSuggestor) {
            if(lines.size() == 0){
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
                        accessor.getTextRenderer().drawWithShadow(matrices, "_", (float)x1, (float)y, -3092272);
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

        matrices.translate(0.0, 0.0, 0.1);
        suggestor.render(matrices, mouseX, mouseY);
    }

    private void drawColoredLine(MatrixStack matrices, String content, int x, int y, int lineIndex){
        TextRenderer textRenderer = accessor.getTextRenderer();
        int renderOffset = 0;
        int startOffset = textOffsets.get(lineIndex);
        int currentOffset = 0;
        int firstOffset = lineOffsets.get(lineIndex); // Great variable names, I know
        if(textColors.size() > 1) {
            for (int i = 0; i < textColors.size(); i++) {
                if(currentOffset >= content.length()) break;
                int nextColorStart = (i+1)<textColors.size() ? textColors.get(i+1).getRight() : Integer.MAX_VALUE;
                if (nextColorStart > startOffset+currentOffset) {
                    String substring = content.substring(currentOffset, clamp(firstOffset + (nextColorStart-(startOffset+horizontalOffset)), currentOffset,content.length()));

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
                    firstOffset = 0;
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
                String visibleLine = line.substring(Math.min(horizontalOffset, line.length() - 1));
                String trimmedLine = textRenderer.trimToWidth(visibleLine, (int) (x - (this.getX() + 5)));
                boolean characterClicked = trimmedLine.length() < visibleLine.length();
                offset = /*(characterClicked ? 1 : 0) +*/ horizontalOffset + trimmedLine.length() - lineOffsets.get(lineIndex);
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
    }

    private void erase(int offset) {
        if (Screen.hasControlDown()) {
            this.eraseWords(offset);
        } else {
            this.eraseCharacters(offset);
        }
        this.onChanged(accessor.getText(), true);
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
        this.eraseCharacters(this.getWordSkipPosition(wordOffset) - accessor.getSelectionStart());
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

        accessor.setSelecting(Screen.hasShiftDown());
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
                    this.setCursor(this.getWordSkipPosition(-1));
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
                    this.setCursor(this.getWordSkipPosition(1));
                } else {
                    this.moveCursor(1);
                }
                return true;
            }
            case 259: {
                if (accessor.getEditable()) {
                    accessor.setSelecting(false);
                    this.erase(-1);
                    accessor.setSelecting(Screen.hasShiftDown());
                }
                return true;
            }
            case 261: {
                if (accessor.getEditable()) {
                    accessor.setSelecting(false);
                    this.erase(1);
                    accessor.setSelecting(Screen.hasShiftDown());
                }
                return true;
            }
            case 268: {
                this.setCursorToStart();
                return true;
            }
            case 269: {
                this.setCursorToEnd();
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
        accessor.setSelecting(Screen.hasShiftDown());
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

    private void formatText(String text) {
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
        boolean metaString = false;
        boolean escapeChar = false;

        // Commented out code adds line breaks after every color change as well
//        int colorIndex = 1;
//        int colorStart = 0;
//        if(colorIndex >= textColors.size()){
//            colorStart = Integer.MAX_VALUE;
//        } else {
//            colorStart = textColors.get(colorIndex).getRight();
//        }
        char current;
        String currentPrefix = "";
        while(currentIndex < textArr.length){
            while(currentColorListIndex < colorIndices.size() && colorIndices.get(currentColorListIndex).getRight() < currentIndex){
                colorStack.push(colorIndices.get(currentColorListIndex).getLeft());
                currentColorListIndex++;
            }
            current = textArr[currentIndex];
//            if(currentIndex >= colorStart-1){
//                colorIndex++;
//                if(colorIndex >= textColors.size()){
//                    colorStart = Integer.MAX_VALUE;
//                } else {
//                    Pair<Style,Integer> color = textColors.get(colorIndex);
//                    if(color.getLeft().getColor().getName().equals("gray")) colorIndex++;
//                    colorStart = colorIndex<textColors.size()?textColors.get(colorIndex).getRight():Integer.MAX_VALUE;
//                }
//                lines.add((" ".repeat(parenthesesDepth)) + currentPrefix + String.copyValueOf(textArr, linestart, 1 + (currentIndex - linestart)));
//                lineOffsets.add(parenthesesDepth);
//                textOffsets.add(linestart - currentPrefix.length());
//                linestart = currentIndex + 1;
//                currentPrefix = "";
//            } else {
                if(!metaString) {
                    switch (current) {
                        case '"':
                            if(!escapeChar){
                                metaString = true;
                            }
                            break;
                        case '{':
                        case '[':
                            if (currentColorListIndex > 0) {
                                currentHighlightColor = colorIndices.get(currentColorListIndex - 1).getLeft();
                                colorStack.push(currentHighlightColor);
                                if (currentHighlightColor != 0) {
                                    colorIndices.add(currentColorListIndex, new Pair<>(getHighlightColorIndex(currentHighlightColor + 1), currentIndex));
                                    currentColorListIndex++;
                                }
                            }

                            String previousLine = currentPrefix + String.copyValueOf(textArr, linestart, (currentIndex - linestart));
                            if (previousLine.length() > 0) {
                                lines.add((" ".repeat(parenthesesDepth*indentationFactor)) + previousLine);
                                lineOffsets.add(parenthesesDepth*indentationFactor);
                                textOffsets.add(linestart - currentPrefix.length());
                            }
                            lines.add((" ".repeat(parenthesesDepth*indentationFactor)) + String.valueOf(current));
                            lineOffsets.add(parenthesesDepth*indentationFactor);
                            textOffsets.add(currentIndex);
                            parenthesesDepth++;
                            linestart = currentIndex + 1;
                            currentPrefix = "";
                            break;
                        case '}':
                        case ']':
                            if (colorIndices.get(currentColorListIndex - 1).getLeft() != 0) {
                                colorIndices.add(currentColorListIndex, new Pair<>(colorStack.pop(), currentIndex + 1));
                                currentColorListIndex++;
                            }

                            lines.add((" ".repeat(parenthesesDepth*indentationFactor)) + currentPrefix + String.copyValueOf(textArr, linestart, (currentIndex - linestart)));
                            lineOffsets.add(parenthesesDepth*indentationFactor);
                            textOffsets.add(linestart - currentPrefix.length());
                            parenthesesDepth = Math.max(parenthesesDepth - 1, 0);
                            linestart = currentIndex + 1;
                            currentPrefix = String.valueOf(current);
                            break;
                        case ',':
                            // Check for following spaces, makes for consistent indentation
                            int tempCurrentIndex = currentIndex + 1;
                            if (tempCurrentIndex < textArr.length) {
                                char tempCurrent = textArr[tempCurrentIndex];
                                while (tempCurrentIndex < textArr.length - 1 && tempCurrent == ' ') {
                                    ++tempCurrentIndex;
                                    tempCurrent = textArr[tempCurrentIndex];
                                }
                                currentIndex = tempCurrentIndex - 1;
                            }

                            lines.add((" ".repeat(parenthesesDepth*indentationFactor)) + currentPrefix + String.copyValueOf(textArr, linestart, 1 + (currentIndex - linestart)));
                            lineOffsets.add(parenthesesDepth*indentationFactor);
                            textOffsets.add(linestart - currentPrefix.length());
                            linestart = currentIndex + 1;
                            currentPrefix = "";
                            break;
                    }
                } else {
                    if(!escapeChar) {
                        if (current == '"') {
                            metaString = false;
                        } else if (current == '\\'){
                            escapeChar = true;
                        }
                    } else {
                        escapeChar = false;
                    }
                }
            //}
            currentIndex++;

            if(currentIndex >= textArr.length){ //Print missing end parentheses
                lines.add((" ".repeat(parenthesesDepth*indentationFactor)) + currentPrefix + String.copyValueOf(textArr, linestart, (currentIndex - linestart)));
                lineOffsets.add(parenthesesDepth*indentationFactor);
                textOffsets.add(linestart-currentPrefix.length());
            }
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

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button){
        if (!this.isVisible()) {
            return false;
        }
        scrollX.mouseClicked(mouseX, mouseY, button);
        scrollY.mouseClicked(mouseX, mouseY, button);

        boolean bl = mouseX >= (double) this.getX() && mouseX < (double)(this.getX() + this.width) && mouseY >= (double) this.getY() && mouseY < (double)(this.getY() + this.height);
        if (accessor.getFocusUnlocked()) {
            this.setFocused(bl);
        }
        if(this.isFocused() && bl && button == 0) {
            this.setCursor(pointToIndex(mouseX, mouseY));
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount){
        if (!this.isVisible()) {
            return false;
        }
        screen.scroll(amount);
        if(LShiftPressed || RShiftPressed){
            horizontalOffset = clamp(horizontalOffset-(int)amount*BetterCommandBlockUI.SCROLL_STEP_X, 0, maxLineWidth-20);
            scrollX.updatePos((double)horizontalOffset / (maxLineWidth-20));
        } else {
            scrolledLines = clamp(scrolledLines-(int)amount*BetterCommandBlockUI.SCROLL_STEP_Y, 0, lines.size()-visibleLines);
            scrollY.updatePos((double)scrolledLines / (lines.size() - visibleLines));
        }
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
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY){
        if (!this.isVisible()) {
            return false;
        }
        scrollX.onDrag(mouseX, mouseY, deltaX, deltaY);
        scrollY.onDrag(mouseX, mouseY, deltaX, deltaY);
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button){
        if (!this.isVisible()) {
            return false;
        }
        scrollX.onRelease(mouseX, mouseY);
        scrollY.onRelease(mouseX, mouseY);
        return true;
    }

    private void moveCursorVertical(int delta){
        TextRenderer textRenderer = accessor.getTextRenderer();
        int lineIndex = Math.max(0,Math.min(lines.size()-1,cursorPosPreference.getLeft()+delta));
        String prevLine = lines.get(cursorPosPreference.getLeft());
        String newLine = lines.get(lineIndex);
        String trimmedNewLine = textRenderer.trimToWidth(
                newLine,
                textRenderer.getWidth(
                        prevLine.substring(
                                0,
                                Math.max(0,
                                    Math.min(
                                            prevLine.length()-1,
                                            cursorPosPreference.getRight()
                                    )
                                )
                        )
                )
        );
        int offset = trimmedNewLine.length()-1;

        cursorPosPreference.setLeft(lineIndex);
        setCursor(textOffsets.get(lineIndex)+offset);
    }

    @Override
    public void moveCursor(int offset) {
        this.setCursor(accessor.invokeGetCursorPosWithOffset(offset));
        cursorPosPreference = indexToLineAndOffset(accessor.invokeGetCursorPosWithOffset(0));
    }

    @Override
    public void setCursor(int cursor) {
        this.setSelectionStart(cursor);
        if (!accessor.getSelecting()) {
            this.setSelectionEnd(accessor.getSelectionStart());
        }
        this.onChanged(accessor.getText(), false);
    }

    public void setScroll(double value){
        this.scrolledLines = (int)Math.max(Math.round((lines.size() - visibleLines) * value),0);
        if(hasCommandSuggestor) this.suggestor.refreshRenderPos();
    }

    public void setHorizontalOffset(double value){
        this.horizontalOffset = (int)Math.max(Math.floor((maxLineWidth - visibleChars) * value),0);
        if(hasCommandSuggestor) this.suggestor.refreshRenderPos();
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
