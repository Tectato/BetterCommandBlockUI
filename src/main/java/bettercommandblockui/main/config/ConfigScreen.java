package bettercommandblockui.main.config;

import bettercommandblockui.main.BetterCommandBlockUI;
import bettercommandblockui.main.ui.MultiLineCommandSuggestor;
import bettercommandblockui.main.ui.MultiLineTextFieldWidget;
import bettercommandblockui.main.ui.screen.AbstractBetterCommandBlockScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class ConfigScreen extends Screen {

    private Screen parent;
    private final String sampleText = "setblock ~ ~1 ~ oak_sign{Text1:'{\"text\":\"[Brackets within a String]\"}',Text2:'{\"text\":\"Some more text\"}',Text3:'{\"text\":\"A really long example text to demonstrate text wraparound. Remember to stay hydrated and take care of yourself, I hope you have a lovely day :)\"}'}";
    public MultiLineTextFieldWidget textField;
    private MultiLineCommandSuggestor commandSuggestor;

    private static final int checkboxDistX = 26;
    private static final int checkboxDistY = 22;
    private static int buttonHeight = 20;
    private static int sliderHeight = 10;
    private static int textHeight = 10;
    private static int buttonMargin = 10;
    private static int textMargin = 5;
    private static int screenMarginX = 40;
    private static int screenMarginY = 20;
    private static int numberInputWidth = 40;
    private CheckboxWidget newLinePreOpen, newLinePostOpen, newLinePreClose, newLinePostClose, //newLinePostLastClose,
            newLinePostComma, avoidDoubleNewline, formatStrings, ignoreEnter;//, bracketAutocomplete;
    private TextFieldWidget indentationFac, wraparound, scrollSpeedX, scrollSpeedY;
    private ButtonWidget back;

    public ConfigScreen() {
        super(Text.translatable("bcbui.config.title"));
        setup();
    }

    public ConfigScreen(Screen parent, MinecraftClient client, int width, int height){
        super(Text.translatable("bcbui.config.title"));
        this.parent = parent;
        this.client = client;
        this.width = width;
        this.height = height;
        this.textRenderer = client.textRenderer;
        setup();
    }

    private void setup(){
        if (parent != null){
            back = ButtonWidget.builder(ScreenTexts.BACK,
                button -> close())
                .dimensions(8, 8, 50, 20)
                .build();
        }

        int textBoxWidth = this.width - (2*screenMarginX + 2*buttonHeight + 2*buttonMargin);
        int textBoxHeight = this.height / 2;// - (2*screenMarginY + textHeight + textMargin + buttonHeight + 2*buttonMargin + sliderHeight);
        textField = new MultiLineTextFieldWidget(this.textRenderer, this.width/2 - textBoxWidth/2,screenMarginY,textBoxWidth,textBoxHeight, Text.literal(sampleText), null);
        textField.setMaxLength(32500);

        commandSuggestor = new MultiLineCommandSuggestor(this.client, this, this.textField, this.textRenderer, true, true, 0, 7, false, Integer.MIN_VALUE);
        commandSuggestor.setWindowActive(true);
        if (client != null && client.player != null) commandSuggestor.refresh();

        textField.setCommandSuggestor(commandSuggestor);
        textField.setChangedListener(this::onCommandChanged);
        textField.setRawText(sampleText);

        //newLinePreOpen = CheckboxWidget.builder(Text.literal(""), textRenderer).checked(BetterCommandBlockUI.NEWLINE_PRE_OPEN_BRACKET).callback(callback).build();
        newLinePreOpen = new CheckboxWidget(0, 0, 20, 20, Text.literal(""), BetterCommandBlockUI.NEWLINE_PRE_OPEN_BRACKET, false){
            @Override
            public void onPress() {
                super.onPress();
                checkboxCallback(this, this.isChecked());
            }
        };
        //newLinePostOpen = CheckboxWidget.builder(Text.translatable("bcbui.config.after"), textRenderer).checked(BetterCommandBlockUI.NEWLINE_POST_OPEN_BRACKET).callback(callback).build();
        newLinePostOpen = new CheckboxWidget(0, 0, 20, 20, Text.translatable("bcbui.config.after"), BetterCommandBlockUI.NEWLINE_POST_OPEN_BRACKET, true){
            @Override
            public void onPress() {
                super.onPress();
                checkboxCallback(this, this.isChecked());
            }
        };
        //newLinePreClose = CheckboxWidget.builder(Text.literal(""), textRenderer).checked(BetterCommandBlockUI.NEWLINE_PRE_CLOSE_BRACKET).callback(callback).build();
        newLinePreClose = new CheckboxWidget(0, 0, 20, 20, Text.literal(""), BetterCommandBlockUI.NEWLINE_PRE_CLOSE_BRACKET, false){
            @Override
            public void onPress() {
                super.onPress();
                checkboxCallback(this, this.isChecked());
            }
        };
        //newLinePostClose = CheckboxWidget.builder(Text.literal(""), textRenderer).checked(BetterCommandBlockUI.NEWLINE_POST_CLOSE_BRACKET).callback(callback).build();
        newLinePostClose = new CheckboxWidget(0, 0, 20, 20, Text.literal(""), BetterCommandBlockUI.NEWLINE_POST_CLOSE_BRACKET, false){
            @Override
            public void onPress() {
                super.onPress();
                checkboxCallback(this, this.isChecked());
            }
        };
        //newLinePostLastClose = CheckboxWidget.builder(Text.literal("After last closing bracket"), textRenderer).checked(BetterCommandBlockUI.NEWLINE_POST_LAST_CLOSE_BRACKET).callback(callback).build();
        //newLinePostComma = CheckboxWidget.builder(Text.literal(""), textRenderer).checked(BetterCommandBlockUI.NEWLINE_POST_COMMA).callback(callback).build();
        newLinePostComma = new CheckboxWidget(0, 0, 20, 20, Text.literal(""), BetterCommandBlockUI.NEWLINE_POST_COMMA, false){
            @Override
            public void onPress() {
                super.onPress();
                checkboxCallback(this, this.isChecked());
            }
        };
        //avoidDoubleNewline = CheckboxWidget.builder(Text.translatable("bcbui.config.avoidEmpty"), textRenderer).checked(BetterCommandBlockUI.AVOID_DOUBLE_NEWLINE).callback(callback).build();
        avoidDoubleNewline = new CheckboxWidget(0, 0, 20, 20, Text.translatable("bcbui.config.avoidEmpty"), BetterCommandBlockUI.AVOID_DOUBLE_NEWLINE, true){
            @Override
            public void onPress() {
                super.onPress();
                checkboxCallback(this, this.isChecked());
            }
        };
        //bracketAutocomplete = CheckboxWidget.builder(Text.literal("Bracket autocomplete"), textRenderer).checked(BetterCommandBlockUI.BRACKET_AUTOCOMPLETE).callback(callback).build();
        //formatStrings = CheckboxWidget.builder(Text.translatable("bcbui.config.formatStrings"), textRenderer).checked(BetterCommandBlockUI.FORMAT_STRINGS).callback(callback).build();
        formatStrings = new CheckboxWidget(0, 0, 20, 20, Text.translatable("bcbui.config.formatStrings"), BetterCommandBlockUI.FORMAT_STRINGS, true){
            @Override
            public void onPress() {
                super.onPress();
                checkboxCallback(this, this.isChecked());
            }
        };
        ignoreEnter = new CheckboxWidget(0, 0, 20, 20, Text.translatable("bcbui.config.ignoreEnter"), BetterCommandBlockUI.IGNORE_ENTER, true){
            @Override
            public void onPress() {
                super.onPress();
                checkboxCallback(this, this.isChecked());
            }
        };

        indentationFac = new TextFieldWidget(textRenderer, 0, 0, numberInputWidth, 10, Text.translatable("bcbui.config.indentation"));
        indentationFac.setText(String.valueOf(BetterCommandBlockUI.INDENTATION_FACTOR));
        indentationFac.setChangedListener((input) -> {
            try {
                int inputInt = Math.min(Math.max(Integer.parseInt(input),1),16);
                BetterCommandBlockUI.setConfig(BetterCommandBlockUI.VAR_INDENTATION, String.valueOf(inputInt));
                this.textField.refreshFormatting();
            } catch (NumberFormatException e){
                BetterCommandBlockUI.INDENTATION_FACTOR = 2;
            }
        });
        wraparound = new TextFieldWidget(textRenderer, 0, 0, numberInputWidth, 10, Text.translatable("bcbui.config.wraparoundWidth"));
        wraparound.setText(String.valueOf(BetterCommandBlockUI.WRAPAROUND_WIDTH));
        wraparound.setChangedListener((input) -> {
            try {
                int inputInt = Math.min(Math.max(Integer.parseInt(input),10),6400);
                BetterCommandBlockUI.setConfig(BetterCommandBlockUI.VAR_WRAPAROUND, String.valueOf(inputInt));
                this.textField.refreshFormatting();
            } catch (NumberFormatException e){
                BetterCommandBlockUI.WRAPAROUND_WIDTH = 200;
            }
        });
        scrollSpeedX = new TextFieldWidget(textRenderer, 0, 0, numberInputWidth, 10, Text.translatable("bcbui.config.scrollX"));
        scrollSpeedX.setText(String.valueOf(BetterCommandBlockUI.SCROLL_STEP_X));
        scrollSpeedX.setChangedListener((input) -> {
            try {
                int inputInt = Math.min(Math.max(Integer.parseInt(input),1),64);
                BetterCommandBlockUI.setConfig(BetterCommandBlockUI.VAR_SCROLL_X, String.valueOf(inputInt));
            } catch (NumberFormatException e){
                BetterCommandBlockUI.SCROLL_STEP_X = 4;
            }
        });
        scrollSpeedY = new TextFieldWidget(textRenderer, 0, 0, numberInputWidth, 10, Text.translatable("bcbui.config.scrollY"));
        scrollSpeedY.setText(String.valueOf(BetterCommandBlockUI.SCROLL_STEP_Y));
        scrollSpeedY.setChangedListener((input) -> {
            try {
                int inputInt = Math.min(Math.max(Integer.parseInt(input),1),64);
                BetterCommandBlockUI.setConfig(BetterCommandBlockUI.VAR_SCROLL_Y, String.valueOf(inputInt));
            } catch (NumberFormatException e){
                BetterCommandBlockUI.SCROLL_STEP_Y = 2;
            }
        });

        init();
    }

    @Override
    public void close() {
        if (parent != null && client != null) {
            BetterCommandBlockUI.writeConfig();
            client.setScreen(parent);
            if (parent instanceof AbstractBetterCommandBlockScreen) {
                ((AbstractBetterCommandBlockScreen) parent).returnFromConfig();
            }
        }
    }

    @Override
    protected void init(){
        if (client != null && textRenderer == null){
            super.init(client, width, height);
        }

        int textBoxWidth = this.width - (2*screenMarginX + 2*buttonHeight + 2*buttonMargin);
        int textBoxHeight = this.height / 2;//- (2*screenMarginY + textHeight + textMargin + buttonHeight + 2*buttonMargin + sliderHeight);
        textField.setX(this.width/2 - textBoxWidth/2);
        textField.setY(screenMarginY);
        textField.setWidth(textBoxWidth);
        textField.setHeight(textBoxHeight);

        newLinePreOpen.setPosition(textField.getX(), textField.getY() + textField.getHeight() + sliderHeight + textHeight + 4);
        newLinePostOpen.setPosition(newLinePreOpen.getX() + checkboxDistX, newLinePreOpen.getY());
        newLinePreClose.setPosition(newLinePreOpen.getX(), newLinePreOpen.getY() + checkboxDistY);
        newLinePostClose.setPosition(newLinePostOpen.getX(), newLinePreClose.getY());
        //newLinePostLastClose.setPosition(textField.getX() + textField.getWidth() + 16, newLinePreClose.getY() + checkboxDistY);
        newLinePostComma.setPosition(newLinePostOpen.getX(), newLinePreClose.getY() + checkboxDistY);
        formatStrings.setPosition(newLinePreOpen.getX() + 80, newLinePreOpen.getY());
        ignoreEnter.setPosition(newLinePreOpen.getX() + 80, newLinePostComma.getY());
        avoidDoubleNewline.setPosition(formatStrings.getX(), formatStrings.getY() + checkboxDistY);
        //bracketAutocomplete.setPosition(newLinePostLastClose.getX(), avoidDoubleNewline.getY() + checkboxDistY*2);
        indentationFac.setPosition(formatStrings.getX() + 130, newLinePreOpen.getY() + 2);
        wraparound.setPosition(indentationFac.getX(), indentationFac.getY() + 12);
        scrollSpeedX.setPosition(wraparound.getX(), wraparound.getY() + 12);
        scrollSpeedY.setPosition(scrollSpeedX.getX(), scrollSpeedX.getY() + 12);

        if (parent != null){
            addDrawableChild(back);
        }
        addDrawableChild(textField);
        this.setInitialFocus(this.textField);
        textField.setFocused(true);
        addDrawableChild(newLinePreOpen);
        addDrawableChild(newLinePostOpen);
        addDrawableChild(newLinePreClose);
        addDrawableChild(newLinePostClose);
        //addDrawableChild(newLinePostLastClose);
        addDrawableChild(newLinePostComma);
        addDrawableChild(formatStrings);
        addDrawableChild(avoidDoubleNewline);
        addDrawableChild(ignoreEnter);
        //addDrawableChild(bracketAutocomplete);
        addDrawableChild(indentationFac);
        addDrawableChild(wraparound);
        addDrawableChild(scrollSpeedX);
        addDrawableChild(scrollSpeedY);
    }

    protected void onCommandChanged(String s){
        if (client != null && client.player != null) commandSuggestor.refresh();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        matrices.push();
        matrices.translate(0,0,1f);
        if(parent != null){
            back.render(matrices, mouseX, mouseY, delta);
        }
        if(client == null || client.player == null){
            int textWidth = textRenderer.getWidth(Text.translatable("bcbui.config.colorError"));
            textRenderer.drawWithShadow(matrices, Text.translatable("bcbui.config.colorError"), width/2 - textWidth/2, 4, 0xffe0e0e0);
        }
        int textWidth = textRenderer.getWidth(Text.translatable("bcbui.config.lineBreaks"));
        textRenderer.drawWithShadow(matrices, Text.translatable("bcbui.config.lineBreaks"), newLinePreOpen.getX() + 20 - textWidth/2, newLinePreOpen.getY() - 12, 0xffffff);
        int beforeTextWidth = textRenderer.getWidth(Text.translatable("bcbui.config.before").getString());
        textRenderer.drawWithShadow(matrices, Text.translatable("bcbui.config.before"), newLinePreOpen.getX() - (beforeTextWidth + 2), newLinePreOpen.getY() + 4, 0xFFE0E0E0);
        textRenderer.drawWithShadow(matrices, Text.literal("{"), newLinePreOpen.getX() + 20, newLinePreOpen.getY() + 6, 0xffffff);
        textRenderer.drawWithShadow(matrices, Text.literal("}"), newLinePreClose.getX() + 20, newLinePreClose.getY() + 6, 0xffffff);
        textRenderer.drawWithShadow(matrices, Text.literal(","), newLinePreClose.getX() + 20, newLinePostComma.getY() + 6, 0xffffff);
        textRenderer.drawWithShadow(matrices, Text.translatable("bcbui.config.indentation"), indentationFac.getX() + numberInputWidth + 4, indentationFac.getY() + 2, 0xFFE0E0E0);
        textRenderer.drawWithShadow(matrices, Text.translatable("bcbui.config.wraparoundWidth"), wraparound.getX() + numberInputWidth + 4, wraparound.getY() + 2, 0xFFE0E0E0);
        textRenderer.drawWithShadow(matrices, Text.translatable("bcbui.config.scrollX"), scrollSpeedX.getX() + numberInputWidth + 4, scrollSpeedX.getY() + 2, 0xFFE0E0E0);
        textRenderer.drawWithShadow(matrices, Text.translatable("bcbui.config.scrollY"), scrollSpeedY.getX() + numberInputWidth + 4, scrollSpeedY.getY() + 2, 0xFFE0E0E0);
        textField.render(matrices, mouseX, mouseY, delta);
        matrices.pop();
    }

    public void checkboxCallback(CheckboxWidget source, boolean checked){
        if (source.equals(newLinePreOpen)) BetterCommandBlockUI.setConfig(BetterCommandBlockUI.VAR_NEWLINE_PRE_OPEN_BRACKET, String.valueOf(checked));
        if (source.equals(newLinePostOpen)) BetterCommandBlockUI.setConfig(BetterCommandBlockUI.VAR_NEWLINE_POST_OPEN_BRACKET, String.valueOf(checked));
        if (source.equals(newLinePreClose)) BetterCommandBlockUI.setConfig(BetterCommandBlockUI.VAR_NEWLINE_PRE_CLOSE_BRACKET, String.valueOf(checked));
        if (source.equals(newLinePostClose)) BetterCommandBlockUI.setConfig(BetterCommandBlockUI.VAR_NEWLINE_POST_CLOSE_BRACKET, String.valueOf(checked));
        //if (source.equals(newLinePostLastClose)) BetterCommandBlockUI.setConfig(BetterCommandBlockUI.VAR_NEWLINE_POST_LAST_CLOSE_BRACKET, String.valueOf(checked));
        if (source.equals(newLinePostComma)) BetterCommandBlockUI.setConfig(BetterCommandBlockUI.VAR_NEWLINE_POST_COMMA, String.valueOf(checked));
        if (source.equals(formatStrings)) BetterCommandBlockUI.setConfig(BetterCommandBlockUI.VAR_FORMAT_STRINGS, String.valueOf(checked));
        if (source.equals(ignoreEnter)) BetterCommandBlockUI.setConfig(BetterCommandBlockUI.VAR_IGNORE_ENTER, String.valueOf(checked));
        if (source.equals(avoidDoubleNewline)) BetterCommandBlockUI.setConfig(BetterCommandBlockUI.VAR_AVOID_DOUBLE_NEWLINE, String.valueOf(checked));
        //if (source.equals(bracketAutocomplete)) BetterCommandBlockUI.setConfig(BetterCommandBlockUI.VAR_BRACKET_AUTOCOMPLETE, String.valueOf(checked));
        textField.refreshFormatting();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button){
        if(commandSuggestor.mouseClicked(mouseX, mouseY, button)) return true;
        if(textField.mouseClicked(mouseX,mouseY,button)) {
            setFocused(textField);
            return true;
        }
        return super.mouseClicked(mouseX,mouseY,button);
    }
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY){
        if(button == 0) {
            return this.textField.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button){
        if(button == 0){
            return this.textField.mouseReleased(mouseX, mouseY, button);
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(keyCode == GLFW.GLFW_KEY_LEFT_SHIFT || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            this.textField.keyPressed(keyCode, scanCode, modifiers);
        }
        if (this.commandSuggestor.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if(keyCode == 340 || keyCode == 344){
            this.textField.keyReleased(keyCode, scanCode, modifiers);
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }
}
