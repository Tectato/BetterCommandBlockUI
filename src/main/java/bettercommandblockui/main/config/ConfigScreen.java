package bettercommandblockui.main.config;

import bettercommandblockui.main.BetterCommandBlockUI;
import bettercommandblockui.main.ui.MultiLineCommandSuggestor;
import bettercommandblockui.main.ui.MultiLineTextFieldWidget;
import bettercommandblockui.main.ui.screen.AbstractBetterCommandBlockScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class ConfigScreen extends Screen {

    private Screen parent;
    private final String sampleText = "setblock ~ ~1 ~ oak_hanging_sign{front_text:{messages:['[\"[Brackets within a String]\"]','[\"Some more text\"]','[\"A really long example text to demonstrate text wraparound. Remember to stay hydrated and take care of yourself, I hope you have a lovely day :)\"]','[\"\"]']}}";
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
    private static int screenMarginY = 12;
    private static int numberInputWidth = 40;
    private Checkbox newLinePreOpen, newLinePostOpen, newLinePreClose, newLinePostClose, //newLinePostLastClose,
            newLinePostComma, avoidDoubleNewline, formatStrings, ignoreEnter, autosave;//, bracketAutocomplete;
    private EditBox indentationFac, wraparound, scrollSpeedX, scrollSpeedY;
    private Button back;

    public ConfigScreen() {
        super(Component.translatable("bcbui.config.title"));
        setup();
    }

    public ConfigScreen(Screen parent, Minecraft client, int width, int height){
        super(Component.translatable("bcbui.config.title"));
        this.parent = parent;
        this.width = width;
        this.height = height;
        setup();
    }

    private void setup(){
        if (parent != null){
            back = Button.builder(CommonComponents.GUI_BACK,
                button -> onClose())
                .bounds(8, 8, 50, 20)
                .build();
        }

        int textBoxWidth = this.width - (2*screenMarginX + 2*buttonHeight + 2*buttonMargin);
        int textBoxHeight = this.height / 2;// - (2*screenMarginY + textHeight + textMargin + buttonHeight + 2*buttonMargin + sliderHeight);
        textField = new MultiLineTextFieldWidget(this.font, this.width/2 - textBoxWidth/2,screenMarginY,textBoxWidth,textBoxHeight, Component.literal(sampleText), null);
        textField.setMaxLength(32500);

        commandSuggestor = new MultiLineCommandSuggestor(this.minecraft, this, this.textField, this.font, true, true, 0, 7, false, Integer.MIN_VALUE);
        commandSuggestor.setAllowSuggestions(true);
        if (minecraft != null && minecraft.player != null) commandSuggestor.updateCommandInfo();

        textField.setCommandSuggestor(commandSuggestor);
        textField.setResponder(this::onCommandChanged);
        textField.setRawText(sampleText);

        Checkbox.OnValueChange callback = new Checkbox.OnValueChange() {
            @Override
            public void onValueChange(Checkbox checkbox, boolean checked) {
                checkboxCallback(checkbox, checked);
            }
        };
        newLinePreOpen = Checkbox.builder(Component.literal(""), font).selected(BetterCommandBlockUI.NEWLINE_PRE_OPEN_BRACKET).onValueChange(callback).build();
        newLinePostOpen = Checkbox.builder(Component.translatable("bcbui.config.after"), font).selected(BetterCommandBlockUI.NEWLINE_POST_OPEN_BRACKET).onValueChange(callback).build();
        newLinePreClose = Checkbox.builder(Component.literal(""), font).selected(BetterCommandBlockUI.NEWLINE_PRE_CLOSE_BRACKET).onValueChange(callback).build();
        newLinePostClose = Checkbox.builder(Component.literal(""), font).selected(BetterCommandBlockUI.NEWLINE_POST_CLOSE_BRACKET).onValueChange(callback).build();
        //newLinePostLastClose = CheckboxWidget.builder(Text.literal("After last closing bracket"), textRenderer).checked(BetterCommandBlockUI.NEWLINE_POST_LAST_CLOSE_BRACKET).callback(callback).build();
        newLinePostComma = Checkbox.builder(Component.literal(""), font).selected(BetterCommandBlockUI.NEWLINE_POST_COMMA).onValueChange(callback).build();
        avoidDoubleNewline = Checkbox.builder(Component.translatable("bcbui.config.avoidEmpty"), font).selected(BetterCommandBlockUI.AVOID_DOUBLE_NEWLINE).onValueChange(callback).build();
        //bracketAutocomplete = CheckboxWidget.builder(Text.literal("Bracket autocomplete"), textRenderer).checked(BetterCommandBlockUI.BRACKET_AUTOCOMPLETE).callback(callback).build();
        formatStrings = Checkbox.builder(Component.translatable("bcbui.config.formatStrings"), font).selected(BetterCommandBlockUI.FORMAT_STRINGS).onValueChange(callback).build();
        ignoreEnter = Checkbox.builder(Component.translatable("bcbui.config.ignoreEnter"), font).selected(BetterCommandBlockUI.IGNORE_ENTER).onValueChange(callback).build();
        autosave = Checkbox.builder(Component.translatable("bcbui.config.autosave"), font).selected(BetterCommandBlockUI.AUTOSAVE).onValueChange(callback).build();

        indentationFac = new EditBox(font, 0, 0, numberInputWidth, 10, Component.translatable("bcbui.config.indentation"));
        indentationFac.setValue(String.valueOf(BetterCommandBlockUI.INDENTATION_FACTOR));
        indentationFac.setResponder((input) -> {
            try {
                int inputInt = Math.min(Math.max(Integer.parseInt(input),1),16);
                BetterCommandBlockUI.setConfig(BetterCommandBlockUI.VAR_INDENTATION, String.valueOf(inputInt));
                this.textField.refreshFormatting();
            } catch (NumberFormatException e){
                BetterCommandBlockUI.INDENTATION_FACTOR = 2;
            }
        });
        wraparound = new EditBox(font, 0, 0, numberInputWidth, 10, Component.translatable("bcbui.config.wraparoundWidth"));
        wraparound.setValue(String.valueOf(BetterCommandBlockUI.WRAPAROUND_WIDTH));
        wraparound.setResponder((input) -> {
            try {
                int inputInt = Math.min(Math.max(Integer.parseInt(input),10),6400);
                BetterCommandBlockUI.setConfig(BetterCommandBlockUI.VAR_WRAPAROUND, String.valueOf(inputInt));
                this.textField.refreshFormatting();
            } catch (NumberFormatException e){
                BetterCommandBlockUI.WRAPAROUND_WIDTH = 200;
            }
        });
        scrollSpeedX = new EditBox(font, 0, 0, numberInputWidth, 10, Component.translatable("bcbui.config.scrollX"));
        scrollSpeedX.setValue(String.valueOf(BetterCommandBlockUI.SCROLL_STEP_X));
        scrollSpeedX.setResponder((input) -> {
            try {
                int inputInt = Math.min(Math.max(Integer.parseInt(input),1),64);
                BetterCommandBlockUI.setConfig(BetterCommandBlockUI.VAR_SCROLL_X, String.valueOf(inputInt));
            } catch (NumberFormatException e){
                BetterCommandBlockUI.SCROLL_STEP_X = 4;
            }
        });
        scrollSpeedY = new EditBox(font, 0, 0, numberInputWidth, 10, Component.translatable("bcbui.config.scrollY"));
        scrollSpeedY.setValue(String.valueOf(BetterCommandBlockUI.SCROLL_STEP_Y));
        scrollSpeedY.setResponder((input) -> {
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
    public void onClose() {
        if (parent != null && minecraft != null) {
            BetterCommandBlockUI.writeConfig();
            minecraft.setScreen(parent);
            if (parent instanceof AbstractBetterCommandBlockScreen) {
                ((AbstractBetterCommandBlockScreen) parent).returnFromConfig();
            }
        }
    }

    @Override
    protected void init(){
        if (minecraft != null && font == null){
            super.init(width, height);
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
        autosave.setPosition(newLinePreOpen.getX() + 80, newLinePostComma.getY() + checkboxDistY);
        avoidDoubleNewline.setPosition(formatStrings.getX(), formatStrings.getY() + checkboxDistY);
        //bracketAutocomplete.setPosition(newLinePostLastClose.getX(), avoidDoubleNewline.getY() + checkboxDistY*2);
        indentationFac.setPosition(formatStrings.getX() + 130, newLinePreOpen.getY() + 2);
        wraparound.setPosition(indentationFac.getX(), indentationFac.getY() + 12);
        scrollSpeedX.setPosition(wraparound.getX(), wraparound.getY() + 12);
        scrollSpeedY.setPosition(scrollSpeedX.getX(), scrollSpeedX.getY() + 12);

        if (parent != null){
            addRenderableWidget(back);
        }
        addRenderableWidget(textField);
        this.setInitialFocus(this.textField);
        textField.setFocused(true);
        addRenderableWidget(newLinePreOpen);
        addRenderableWidget(newLinePostOpen);
        addRenderableWidget(newLinePreClose);
        addRenderableWidget(newLinePostClose);
        //addDrawableChild(newLinePostLastClose);
        addRenderableWidget(newLinePostComma);
        addRenderableWidget(formatStrings);
        addRenderableWidget(avoidDoubleNewline);
        addRenderableWidget(ignoreEnter);
        addRenderableWidget(autosave);
        //addDrawableChild(bracketAutocomplete);
        addRenderableWidget(indentationFac);
        addRenderableWidget(wraparound);
        addRenderableWidget(scrollSpeedX);
        addRenderableWidget(scrollSpeedY);
    }

    protected void onCommandChanged(String s){
        if (minecraft != null && minecraft.player != null) commandSuggestor.updateCommandInfo();
    }

    @Override
    public void extractRenderState(final GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        if(parent != null){
            back.extractRenderState(graphics, mouseX, mouseY, delta);
        }
        if(minecraft == null || minecraft.player == null){
            graphics.centeredText(font, Component.translatable("bcbui.config.colorError"), width/2, 4, 0xffe0e0e0);
        }
        graphics.centeredText(font, Component.translatable("bcbui.config.lineBreaks"), newLinePreOpen.getX() + 20, newLinePreOpen.getY() - 12, 0xffffff);
        String beforeText = Component.translatable("bcbui.config.before").getString();
        int beforeTextWidth = font.width(Component.translatable("bcbui.config.before").getString());
        graphics.text(font, Component.translatable("bcbui.config.before"), newLinePreOpen.getX() - (beforeTextWidth + 2), newLinePreOpen.getY() + 4, 0xFFE0E0E0);
        graphics.text(font, Component.literal("{"), newLinePreOpen.getX() + 20, newLinePreOpen.getY() + 6, 0xffffff);
        graphics.text(font, Component.literal("}"), newLinePreClose.getX() + 20, newLinePreClose.getY() + 6, 0xffffff);
        graphics.text(font, Component.literal(","), newLinePreClose.getX() + 20, newLinePostComma.getY() + 6, 0xffffff);
        graphics.text(font, Component.translatable("bcbui.config.indentation"), indentationFac.getX() + numberInputWidth + 4, indentationFac.getY() + 2, 0xFFE0E0E0);
        graphics.text(font, Component.translatable("bcbui.config.wraparoundWidth"), wraparound.getX() + numberInputWidth + 4, wraparound.getY() + 2, 0xFFE0E0E0);
        graphics.text(font, Component.translatable("bcbui.config.scrollX"), scrollSpeedX.getX() + numberInputWidth + 4, scrollSpeedX.getY() + 2, 0xFFE0E0E0);
        graphics.text(font, Component.translatable("bcbui.config.scrollY"), scrollSpeedY.getX() + numberInputWidth + 4, scrollSpeedY.getY() + 2, 0xFFE0E0E0);
        textField.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    public void checkboxCallback(Checkbox source, boolean checked){
        if (source.equals(newLinePreOpen)) BetterCommandBlockUI.setConfig(BetterCommandBlockUI.VAR_NEWLINE_PRE_OPEN_BRACKET, String.valueOf(checked));
        if (source.equals(newLinePostOpen)) BetterCommandBlockUI.setConfig(BetterCommandBlockUI.VAR_NEWLINE_POST_OPEN_BRACKET, String.valueOf(checked));
        if (source.equals(newLinePreClose)) BetterCommandBlockUI.setConfig(BetterCommandBlockUI.VAR_NEWLINE_PRE_CLOSE_BRACKET, String.valueOf(checked));
        if (source.equals(newLinePostClose)) BetterCommandBlockUI.setConfig(BetterCommandBlockUI.VAR_NEWLINE_POST_CLOSE_BRACKET, String.valueOf(checked));
        //if (source.equals(newLinePostLastClose)) BetterCommandBlockUI.setConfig(BetterCommandBlockUI.VAR_NEWLINE_POST_LAST_CLOSE_BRACKET, String.valueOf(checked));
        if (source.equals(newLinePostComma)) BetterCommandBlockUI.setConfig(BetterCommandBlockUI.VAR_NEWLINE_POST_COMMA, String.valueOf(checked));
        if (source.equals(formatStrings)) BetterCommandBlockUI.setConfig(BetterCommandBlockUI.VAR_FORMAT_STRINGS, String.valueOf(checked));
        if (source.equals(ignoreEnter)) BetterCommandBlockUI.setConfig(BetterCommandBlockUI.VAR_IGNORE_ENTER, String.valueOf(checked));
        if (source.equals(autosave)) BetterCommandBlockUI.setConfig(BetterCommandBlockUI.VAR_AUTOSAVE, String.valueOf(checked));
        if (source.equals(avoidDoubleNewline)) BetterCommandBlockUI.setConfig(BetterCommandBlockUI.VAR_AVOID_DOUBLE_NEWLINE, String.valueOf(checked));
        //if (source.equals(bracketAutocomplete)) BetterCommandBlockUI.setConfig(BetterCommandBlockUI.VAR_BRACKET_AUTOCOMPLETE, String.valueOf(checked));
        textField.refreshFormatting();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled){
        if(commandSuggestor.mouseClicked(click)) return true;
        if(textField.mouseClicked(click, doubled)) {
            setFocused(textField);
            return true;
        }
        return super.mouseClicked(click, doubled);
    }
    @Override
    public boolean mouseDragged(MouseButtonEvent click, double deltaX, double deltaY){
        if(click.button() == 0) {
            return this.textField.mouseDragged(click, deltaX, deltaY);
        }
        return false;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click){
        if(click.button() == 0){
            return this.textField.mouseReleased(click);
        }
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        int keyCode = input.input();
        if(keyCode == GLFW.GLFW_KEY_LEFT_SHIFT || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            this.textField.keyPressed(input);
        }
        if (this.commandSuggestor.keyPressed(input)) {
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean keyReleased(KeyEvent input) {
        int keyCode = input.input();
        if(keyCode == 340 || keyCode == 344){
            this.textField.keyReleased(input);
        }
        return super.keyReleased(input);
    }
}
