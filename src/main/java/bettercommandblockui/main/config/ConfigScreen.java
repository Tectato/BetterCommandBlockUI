package bettercommandblockui.main.config;

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

    private CheckboxWidget newLinePre, newLinePost;

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

        newLinePre = CheckboxWidget.builder(Text.literal("Before brackets"), textRenderer).checked(true).build();
        newLinePost = CheckboxWidget.builder(Text.literal("After brackets"), textRenderer).checked(true).build();

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

        newLinePre.setPosition(textField.getX() + textField.getWidth() + 20, 64);
        newLinePost.setPosition(textField.getX() + textField.getWidth() + 20, 86);

        addDrawableChild(textField);
        this.setInitialFocus(this.textField);
        textField.setFocused(true);
        addDrawableChild(newLinePre);
        addDrawableChild(newLinePost);
    }

    protected void onCommandChanged(String s){
        commandSuggestor.refresh();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawTextWithShadow(textRenderer, Text.literal("Line breaks"), newLinePre.getX(), newLinePre.getY() - 20, 0xffffff);
        textField.render(context, mouseX, mouseY, delta);
    }
}
