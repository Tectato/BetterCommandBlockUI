package bettercommandblockui.main.config;

import bettercommandblockui.main.BetterCommandBlockUI;
import bettercommandblockui.main.ui.MultiLineCommandSuggestor;
import bettercommandblockui.main.ui.MultiLineTextFieldWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class ConfigScreen extends Screen {

    private Screen parent;
    private final String sampleText = "setblock ~ ~1 ~ oak_hanging_sign{front_text:{messages:['[\"[Brackets within a String]\"]','[\"Some more text\"]','[\"A really long example text to demonstrate text wraparound. Remember to stay hydrated and take care of yourself, I hope you have a lovely day :)\"]','[\"\"]']}}";
    public MultiLineTextFieldWidget textField;
    private MultiLineCommandSuggestor commandSuggestor;

    private CheckboxWidget newLinePreOpen, newLinePostOpen, newLinePreClose, newLinePostClose, newLinePostLastClose, newLinePostComma;

    public ConfigScreen() {
        super(Text.literal("Better Command Block UI Config"));
        setup();
    }

    public ConfigScreen(Screen parent, MinecraftClient client, int width, int height){
        super(Text.literal("Better Command Block UI Config"));
        this.parent = parent;
        this.client = client;
        this.width = width;
        this.height = height;
        this.textRenderer = client.textRenderer;
        setup();
    }

    private void setup(){
        textField = new MultiLineTextFieldWidget(this.textRenderer, 8,height/4,width/2,height/2, Text.literal(sampleText), null);
        textField.setMaxLength(32500);

        commandSuggestor = new MultiLineCommandSuggestor(this.client, this, this.textField, this.textRenderer, true, true, 0, 7, false, Integer.MIN_VALUE);
        commandSuggestor.setWindowActive(true);
        commandSuggestor.refresh();

        textField.setCommandSuggestor(commandSuggestor);
        textField.setChangedListener(this::onCommandChanged);
        textField.setRawText(sampleText);

        CheckboxWidget.Callback callback = new CheckboxWidget.Callback() {
            @Override
            public void onValueChange(CheckboxWidget checkbox, boolean checked) {
                checkboxCallback(checkbox, checked);
            }
        };
        newLinePreOpen = CheckboxWidget.builder(Text.literal(""), textRenderer).checked(BetterCommandBlockUI.NEWLINE_PRE_OPEN_BRACKET).callback(callback).build();
        newLinePostOpen = CheckboxWidget.builder(Text.literal(""), textRenderer).checked(BetterCommandBlockUI.NEWLINE_POST_OPEN_BRACKET).callback(callback).build();
        newLinePreClose = CheckboxWidget.builder(Text.literal(""), textRenderer).checked(BetterCommandBlockUI.NEWLINE_PRE_CLOSE_BRACKET).callback(callback).build();
        newLinePostClose = CheckboxWidget.builder(Text.literal(""), textRenderer).checked(BetterCommandBlockUI.NEWLINE_POST_CLOSE_BRACKET).callback(callback).build();
        newLinePostLastClose = CheckboxWidget.builder(Text.literal("After last closing bracket"), textRenderer).checked(BetterCommandBlockUI.NEWLINE_POST_LAST_CLOSE_BRACKET).callback(callback).build();
        newLinePostComma = CheckboxWidget.builder(Text.literal("After commas"), textRenderer).checked(BetterCommandBlockUI.NEWLINE_POST_COMMA).callback(callback).build();

        init();
    }

    @Override
    public void close() {
        if (parent != null && client != null) {
            client.setScreen(parent);
        }
    }

    @Override
    protected void init(){
        if (client != null && textRenderer == null){
            super.init(client, width, height);
        }
        textField.setX(8);
        textField.setY(height/4);
        textField.setWidth(width/2);
        textField.setHeight(height/2);

        newLinePreOpen.setPosition(textField.getX() + textField.getWidth() + 32, 64);
        newLinePostOpen.setPosition(newLinePreOpen.getX() + 26, 64);
        newLinePreClose.setPosition(newLinePreOpen.getX(), 86);
        newLinePostClose.setPosition(newLinePostOpen.getX(), 86);
        newLinePostLastClose.setPosition(textField.getX() + textField.getWidth() + 16, 108);
        newLinePostComma.setPosition(textField.getX() + textField.getWidth() + 16, 130);

        addDrawableChild(textField);
        this.setInitialFocus(this.textField);
        textField.setFocused(true);
        addDrawableChild(newLinePreOpen);
        addDrawableChild(newLinePostOpen);
        addDrawableChild(newLinePreClose);
        addDrawableChild(newLinePostClose);
        addDrawableChild(newLinePostLastClose);
        addDrawableChild(newLinePostComma);
    }

    protected void onCommandChanged(String s){
        commandSuggestor.refresh();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawTextWithShadow(textRenderer, Text.literal("Line breaks"), newLinePostLastClose.getX(), newLinePreOpen.getY() - 10, 0xffffff);
        context.drawTextWithShadow(textRenderer, Text.literal("{"), newLinePreOpen.getX() + 20, newLinePreOpen.getY() + 6, 0xffffff);
        context.drawTextWithShadow(textRenderer, Text.literal("}"), newLinePreOpen.getX() + 20, newLinePreClose.getY() + 6, 0xffffff);
        textField.render(context, mouseX, mouseY, delta);
    }

    public void checkboxCallback(CheckboxWidget source, boolean checked){
        if (source.equals(newLinePreOpen)) BetterCommandBlockUI.setConfig(BetterCommandBlockUI.VAR_NEWLINE_PRE_OPEN_BRACKET, String.valueOf(checked));
        if (source.equals(newLinePostOpen)) BetterCommandBlockUI.setConfig(BetterCommandBlockUI.VAR_NEWLINE_POST_OPEN_BRACKET, String.valueOf(checked));
        if (source.equals(newLinePreClose)) BetterCommandBlockUI.setConfig(BetterCommandBlockUI.VAR_NEWLINE_PRE_CLOSE_BRACKET, String.valueOf(checked));
        if (source.equals(newLinePostClose)) BetterCommandBlockUI.setConfig(BetterCommandBlockUI.VAR_NEWLINE_POST_CLOSE_BRACKET, String.valueOf(checked));
        if (source.equals(newLinePostLastClose)) BetterCommandBlockUI.setConfig(BetterCommandBlockUI.VAR_NEWLINE_POST_LAST_CLOSE_BRACKET, String.valueOf(checked));
        if (source.equals(newLinePostComma)) BetterCommandBlockUI.setConfig(BetterCommandBlockUI.VAR_NEWLINE_POST_COMMA, String.valueOf(checked));
        textField.refreshFormatting();
    }
}
