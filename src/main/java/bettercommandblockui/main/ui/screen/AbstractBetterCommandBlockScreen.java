package bettercommandblockui.main.ui.screen;

import bettercommandblockui.main.BetterCommandBlockUI;
import bettercommandblockui.main.config.ConfigScreen;
import bettercommandblockui.main.ui.CyclingTexturedButtonWidget;
import bettercommandblockui.main.ui.MultiLineCommandSuggestor;
import bettercommandblockui.main.ui.MultiLineTextFieldWidget;
import bettercommandblockui.main.ui.SideWindow;
import bettercommandblockui.mixin.ScreenAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.CommandBlockExecutor;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;

import java.time.Duration;

import static bettercommandblockui.main.BetterCommandBlockUI.*;

@Environment(EnvType.CLIENT)
public abstract class AbstractBetterCommandBlockScreen extends Screen {

    protected class CommandBlockState{
        public CommandBlockBlockEntity.Type type = CommandBlockBlockEntity.Type.REDSTONE;
        boolean conditional = false;
        public boolean needsRedstone = true;
        public boolean trackOutput = true;

        public CommandBlockState(CommandBlockBlockEntity.Type type, boolean conditional, boolean needsRedstone, boolean trackOutput){
            this.type = type;
            this.conditional = conditional;
            this.needsRedstone = needsRedstone;
            this.trackOutput = trackOutput;
        }
    }

    protected static final Text SET_COMMAND_TEXT = Text.translatable("advMode.setCommand");
    protected static final Text COMMAND_TEXT = Text.translatable("advMode.command");
    protected static final Text PREVIOUS_OUTPUT_TEXT = Text.translatable("advMode.previousOutput");
    protected static final ButtonTextures SIDE_WINDOW_BUTTON_TEXTURES = new ButtonTextures(
            Identifier.of("bettercommandblockui:button_side_window_enabled"),
            Identifier.of("bettercommandblockui:button_side_window_disabled"),
            Identifier.of("bettercommandblockui:button_side_window_focused")
    );

    protected static final ButtonTextures SAVE_BUTTON_TEXTURES = new ButtonTextures(
            Identifier.of("bettercommandblockui:button_save_enabled"),
            Identifier.of("bettercommandblockui:button_save_disabled"),
            Identifier.of("bettercommandblockui:button_save_focused")
    );

    protected CommandBlockState priorState;

    protected TextFieldWidget consoleCommandTextField;
    protected TextFieldWidget previousOutputTextField;

    protected ButtonWidget doneButton;
    protected ButtonWidget cancelButton;
    //protected ButtonWidget configButton;
    protected CyclingTexturedButtonWidget<Boolean> toggleTrackingOutputButton;
    protected CheckboxWidget setTrackingOutputDefaultCheckbox;
    protected CyclingTexturedButtonWidget<Boolean> showOutputButton;
    protected CheckboxWidget setShowOutputDefaultCheckbox;
    protected TexturedButtonWidget showSideWindowButton, saveButton;

    protected SideWindow sideWindow;

    protected CommandBlockExecutor commandExecutor;
    protected ChatInputSuggestor commandSuggestor;
    protected boolean trackOutput = true;
    protected boolean showOutput = SHOW_OUTPUT_DEFAULT;
    protected boolean updated = false;
    protected boolean closing = false;
    protected static boolean showSideWindow = false;

    protected static int buttonHeight = 20;
    protected static int sliderHeight = 10;
    protected static int textHeight = 10;

    protected static int cycleButtonWidth = buttonHeight;

    protected static int buttonMargin = 10;
    protected static int textMargin = 5;
    protected static int screenMarginX = 40;
    protected static int screenMarginY = 20;

    protected AbstractBetterCommandBlockScreen() {
        super(NarratorManager.EMPTY);
    }

    @Override
    public void init(){
        assert this.client != null;

        this.priorState = new CommandBlockState(CommandBlockBlockEntity.Type.REDSTONE, false, false, commandExecutor.isTrackingOutput());

        int textBoxHeight = this.height - (2*screenMarginY + textHeight + textMargin + buttonHeight + 2*buttonMargin + sliderHeight);
        int textBoxWidth = this.width - (2*screenMarginX + 2*cycleButtonWidth + 2*buttonMargin);

        int lowerButtonWidth = Math.min((textBoxWidth/2) - buttonMargin/2, 160);
        this.doneButton = this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.commitAndClose()).dimensions(this.width / 2 - (lowerButtonWidth + buttonMargin/2), this.height / 2 + (5 + buttonMargin + textBoxHeight/2), lowerButtonWidth, buttonHeight).build());
        this.cancelButton = this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.close()).dimensions(this.width / 2 + buttonMargin/2, this.height / 2 + (5 + buttonMargin + textBoxHeight/2), lowerButtonWidth, buttonHeight).build());
        //this.configButton = this.addDrawableChild(ButtonWidget.builder(Text.literal("Config"), button -> this.openConfig()).dimensions(16, 16, 2*buttonHeight, buttonHeight).build());

        this.trackOutput = this.commandExecutor.isTrackingOutput();
        if(TRACK_OUTPUT_DEFAULT_USED){
            trackOutput = TRACK_OUTPUT_DEFAULT_VALUE;
        }

        Text[] trackOutputTooltips = {
                Text.translatable("bcbui.trackOutput.true"),
                Text.translatable("bcbui.trackOutput.false")
        };

        this.toggleTrackingOutputButton = this.addDrawableChild(new CyclingTexturedButtonWidget<Boolean>(this.width / 2 + (textBoxWidth/2 + buttonMargin + buttonMargin/2), this.height/2 - (buttonHeight + buttonMargin), cycleButtonWidth, buttonHeight, Text.of(""), (button) -> {
            this.trackOutput = ((CyclingTexturedButtonWidget<Boolean>)button).getValue();
            this.commandExecutor.setTrackOutput(trackOutput);
            this.setPreviousOutputText(trackOutput);
            this.setTrackingOutputDefaultCheckbox.active =
                    (!setTrackingOutputDefaultCheckbox.isChecked()) || trackOutput == TRACK_OUTPUT_DEFAULT_VALUE;
        }, client.currentScreen, new ButtonTextures[]{BUTTON_TRACK_OUTPUT, BUTTON_IGNORE_OUTPUT}, trackOutput?0:1, new Boolean[]{true, false}, trackOutputTooltips));

        this.setTrackingOutputDefaultCheckbox = this.addDrawableChild(
                CheckboxWidget.builder(Text.of(""), textRenderer)
                        .pos(toggleTrackingOutputButton.getX() + toggleTrackingOutputButton.getWidth() + 2, toggleTrackingOutputButton.getY() + 2)
                        .checked(TRACK_OUTPUT_DEFAULT_USED)
                        .callback(new CheckboxWidget.Callback() {
                            @Override
                            public void onValueChange(CheckboxWidget checkbox, boolean checked) {
                                BetterCommandBlockUI.setConfig(VAR_TRACK_OUTPUT_DEFAULT_USED, ""+checked);
                                BetterCommandBlockUI.setConfig(VAR_TRACK_OUTPUT_DEFAULT_VALUE, ""+trackOutput);
                            }
                        })
                        .tooltip(Tooltip.of(Text.translatable("bcbui.trackOutput.setDefault")))
                        .build());
        this.setTrackingOutputDefaultCheckbox.active =
                (!setTrackingOutputDefaultCheckbox.isChecked()) || trackOutput == TRACK_OUTPUT_DEFAULT_VALUE;

        Text[] outputTooltips = {
                Text.translatable("bcbui.view.command"),
                Text.translatable("bcbui.view.output")
        };
        this.showOutputButton = this.addDrawableChild(new CyclingTexturedButtonWidget<Boolean>(this.width / 2 + (textBoxWidth/2 + buttonMargin + buttonMargin/2), this.height/2, cycleButtonWidth, buttonHeight, Text.of(""), (button) -> {
            this.showOutput = ((CyclingTexturedButtonWidget<Boolean>)button).getValue();
            this.consoleCommandTextField.setVisible(!showOutput);
            this.previousOutputTextField.setVisible(showOutput);
            this.setShowOutputDefaultCheckbox.active = showOutput;
        }, client.currentScreen, new ButtonTextures[]{BUTTON_COMMAND, BUTTON_OUTPUT}, SHOW_OUTPUT_DEFAULT?1:0, new Boolean[]{false, true}, outputTooltips));

        this.setShowOutputDefaultCheckbox = this.addDrawableChild(
                CheckboxWidget.builder(Text.of(""), textRenderer)
                        .pos(showOutputButton.getX() + showOutputButton.getWidth() + 2, showOutputButton.getY() + 2)
                        .checked(SHOW_OUTPUT_DEFAULT)
                        .callback(new CheckboxWidget.Callback() {
                            @Override
                            public void onValueChange(CheckboxWidget checkbox, boolean checked) {
                                BetterCommandBlockUI.setConfig(VAR_SHOW_OUTPUT_DEFAULT, ""+checked);
                            }
                        })
                        .tooltip(Tooltip.of(Text.translatable("bcbui.view.outputDefault")))
                        .build());
        this.setShowOutputDefaultCheckbox.active = showOutput;

        this.showSideWindowButton = this.addDrawableChild(new TexturedButtonWidget(
                this.width - (buttonMargin + cycleButtonWidth),
                this.height - (buttonMargin + cycleButtonWidth),
                cycleButtonWidth,
                buttonHeight,
                SIDE_WINDOW_BUTTON_TEXTURES,
                (button) -> {
                    showSideWindow = !showSideWindow;
                    this.sideWindow.setVisible(showSideWindow);

                    this.showOutputButton.setTooltipVisible(!showSideWindow);
                    this.setShowOutputDefaultCheckbox.setTooltipDelay(showSideWindow ? Duration.ofDays(Integer.MAX_VALUE) : Duration.ofSeconds(0));
                    this.toggleTrackingOutputButton.setTooltipVisible(!showSideWindow);
                    this.setTrackingOutputDefaultCheckbox.setTooltipDelay(showSideWindow ? Duration.ofDays(Integer.MAX_VALUE) : Duration.ofSeconds(0));
                },
                Text.translatable("bcbui.tools"))
        );
        Tooltip showSideWindowTooltip = Tooltip.of(Text.translatable("bcbui.tools"));
        this.showSideWindowButton.setTooltip(showSideWindowTooltip);

        this.consoleCommandTextField = new MultiLineTextFieldWidget(this.textRenderer, this.width/2 - textBoxWidth/2, this.height/2 - textBoxHeight/2, textBoxWidth, textBoxHeight, (Text)Text.translatable("advMode.command"), this){
            @Override
            protected MutableText getNarrationMessage() {
                return super.getNarrationMessage().append(commandSuggestor.getNarration());
            }
        };

        commandSuggestor = new MultiLineCommandSuggestor(this.client, this, this.consoleCommandTextField, this.textRenderer, true, true, 0, 7, false, Integer.MIN_VALUE);
        commandSuggestor.setWindowActive(true);
        commandSuggestor.refresh();
        ((MultiLineTextFieldWidget)this.consoleCommandTextField).setCommandSuggestor((MultiLineCommandSuggestor) commandSuggestor);

        this.consoleCommandTextField.setMaxLength(32500);
        this.consoleCommandTextField.setChangedListener(this::onCommandChanged);
        this.addSelectableChild(this.consoleCommandTextField);
        this.previousOutputTextField = new MultiLineTextFieldWidget(this.textRenderer, this.width/2 - textBoxWidth/2, this.height/2 - textBoxHeight/2, textBoxWidth, 16, Text.translatable("advMode.previousOutput"), this);
        this.previousOutputTextField.setMaxLength(32500);
        this.previousOutputTextField.setEditable(false);
        ((MultiLineTextFieldWidget)this.consoleCommandTextField).setRawText("-");
        this.addSelectableChild(this.previousOutputTextField);
        this.setPreviousOutputText(trackOutput);

        this.consoleCommandTextField.setText(commandExecutor.getCommand());

        this.sideWindow = this.addDrawable(new SideWindow(3*this.width/4, 20, this.width/4, 7*this.height/10, (MultiLineTextFieldWidget) consoleCommandTextField, this));
        this.sideWindow.setVisible(showSideWindow);
        this.sideWindow.setFocused(false);
        this.showOutputButton.setTooltipVisible(!showSideWindow);
        this.setShowOutputDefaultCheckbox.setTooltipDelay(showSideWindow ? Duration.ofDays(Integer.MAX_VALUE) : Duration.ofSeconds(0));
        this.toggleTrackingOutputButton.setTooltipVisible(!showSideWindow);
        this.setTrackingOutputDefaultCheckbox.setTooltipDelay(showSideWindow ? Duration.ofDays(Integer.MAX_VALUE) : Duration.ofSeconds(0));

        this.consoleCommandTextField.setVisible(!showOutput);
        this.previousOutputTextField.setVisible(showOutput);

        this.setInitialFocus(showOutput ? this.previousOutputTextField : this.consoleCommandTextField);
        if(showOutput) {
            this.previousOutputTextField.setFocused(true);
        } else {
            this.consoleCommandTextField.setFocused(true);
        }

        this.saveButton = this.addDrawableChild(new TexturedButtonWidget(
                27,
                27,
                cycleButtonWidth,
                buttonHeight,
                SAVE_BUTTON_TEXTURES,
                (button) -> {
                    if(wasModified()){
                        commit();
                    }
                },
                Text.translatable("bcbui.save"))
        );
        Tooltip saveTooltip = Tooltip.of(Text.translatable("bcbui.save"));
        this.saveButton.setTooltip(saveTooltip);
    }

    @Override
    public void close(){
        if (!closing && AUTOSAVE && wasModified()){
            commit();
        }
        super.close();
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        String string = this.consoleCommandTextField.getText();
        this.init(client, width, height);
        ((MultiLineTextFieldWidget)this.consoleCommandTextField).setRawText(string);
        commandSuggestor.refresh();
        setButtonsActive(true);
    }

    public void openConfig(){
        assert client != null;
        commandBuffer = this.consoleCommandTextField.getText();
        client.setScreen(new ConfigScreen(this, client, width, height));
    }

    public void returnFromConfig(){
        this.consoleCommandTextField.setText(commandBuffer);
    }

    public void sideWindowFocused(){
        consoleCommandTextField.setFocused(false);
    }

    public boolean scroll(double amount){
        return commandSuggestor.mouseScrolled(MathHelper.clamp(amount, -1.0, 1.0));
    }

    public boolean mouseClicked(Click click, boolean doubled){
        if(showSideWindow && sideWindow.mouseClicked(click, doubled)) return true;
        if(commandSuggestor.isOpen() && commandSuggestor.mouseClicked(click)) return true;
        consoleCommandTextField.onClick(click, doubled);
        /*if(consoleCommandTextField.mouseClicked(mouseX,mouseY,button)) {
            setFocused(consoleCommandTextField);
            return true;
        }*/
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY){
        if(click.button() == 0) {
            if(showSideWindow && sideWindow.mouseDragged(click, deltaX, deltaY)) return true;
            if(showOutput){
                return this.previousOutputTextField.mouseDragged(click, deltaX, deltaY);
            }
            return this.consoleCommandTextField.mouseDragged(click, deltaX, deltaY);
        }
        return false;
    }

    @Override
    public boolean mouseReleased(Click click){
        if(showSideWindow && sideWindow.mouseReleased(click)) return true;
        if(click.button() == 0){
            if(showOutput){
                return this.previousOutputTextField.mouseReleased(click);
            }
            return this.consoleCommandTextField.mouseReleased(click);
        }
        return false;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if(showSideWindow && sideWindow.isFocused() && sideWindow.keyPressed(input)) return true;
        int keyCode = input.getKeycode();
        if(keyCode == GLFW.GLFW_KEY_LEFT_SHIFT || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            if (showOutput) {
                this.previousOutputTextField.keyPressed(input);
            } else {
                this.consoleCommandTextField.keyPressed(input);
            }
        }
        if (this.commandSuggestor.keyPressed(input)) {
            return true;
        }
        if (keyCode == 258 && sideWindow.isFocused()) return true;
        if (super.keyPressed(input)) {
            return true;
        }
        if (updated && !IGNORE_ENTER && (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER)) {
            this.commitAndClose();
            return true;
        }
        if (keyCode == 83 && MinecraftClient.getInstance().isCtrlPressed() && ((MultiLineTextFieldWidget)this.consoleCommandTextField).wasModified()){ // CTRL + S
            this.commit();
        }
        return false;
    }

    @Override
    public boolean keyReleased(KeyInput input) {
        int keyCode = input.getKeycode();
        if(keyCode == 340 || keyCode == 344){
            if(showOutput){
                this.previousOutputTextField.keyReleased(input);
            } else {
                this.consoleCommandTextField.keyReleased(input);
            }
        }
        return super.keyReleased(input);
    }

    @Override
    public boolean charTyped(CharInput input){
        if(showSideWindow && sideWindow.charTyped(input)) return true;
        if(showOutput) return false;
        return this.consoleCommandTextField.charTyped(input);
    }

    protected void onCommandChanged(String s){
        commandSuggestor.refresh();
    }

    protected void setButtonsActive(boolean active) {
        this.doneButton.active = active;
        this.toggleTrackingOutputButton.active = active;
        this.consoleCommandTextField.setEditable(active);
        this.saveButton.active = active;
    }

    protected void setPreviousOutputText(boolean trackOutput) {
        ((MultiLineTextFieldWidget)this.previousOutputTextField).setRawText(trackOutput ? this.commandExecutor.getLastOutput().getString() : "-");
    }

    protected void commit(){
        trackOutput = toggleTrackingOutputButton.getValue();
        this.syncSettingsToServer(commandExecutor);
        if (!commandExecutor.isTrackingOutput()) {
            commandExecutor.setLastOutput(null);
        }
        ((MultiLineTextFieldWidget)consoleCommandTextField).resetModified();
        priorState.trackOutput = trackOutput;
    }

    protected void commitAndClose() {
        commit();
        assert this.client != null;
        closing = true;
        close();
    }

    protected boolean wasModified(){
        return ((MultiLineTextFieldWidget)consoleCommandTextField).wasModified() || toggleTrackingOutputButton.getValue() != priorState.trackOutput;
    }

    abstract protected void syncSettingsToServer(CommandBlockExecutor commandExecutor);

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        //this.renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, SET_COMMAND_TEXT, this.width / 2, 20, 0xFFFFFFFF);
        if(showOutput){
            context.drawTextWithShadow(this.textRenderer, PREVIOUS_OUTPUT_TEXT, this.previousOutputTextField.getX(), 40, 0xFFA0A0A0);
            this.previousOutputTextField.render(context, mouseX, mouseY, delta);
        } else {
            context.drawTextWithShadow(this.textRenderer, COMMAND_TEXT, this.consoleCommandTextField.getX(), 40, 0xFFA0A0A0);
            this.consoleCommandTextField.render(context, mouseX, mouseY, delta);
        }
        renderAsterisk(context, toggleTrackingOutputButton, toggleTrackingOutputButton.getValue() != priorState.trackOutput);
        for (Drawable drawable : ((ScreenAccessor)this).getDrawables()) {
            if(!drawable.equals(sideWindow)) drawable.render(context, mouseX, mouseY, delta);
        }
        darkenCheckbox(context, setTrackingOutputDefaultCheckbox);
        darkenCheckbox(context, setShowOutputDefaultCheckbox);

        /*Matrix3x2fStack matrixStack = context.getMatrices();
        matrixStack.pushMatrix();
        matrixStack.translate(0,0,1);*/
        sideWindow.render(context, mouseX, mouseY, delta);
        //matrixStack.popMatrix();
    }

    private void darkenCheckbox(DrawContext context, CheckboxWidget checkbox){
        if(checkbox.active) return;
        context.fill(checkbox.getX(), checkbox.getY(), checkbox.getX() + checkbox.getWidth() - 4, checkbox.getY() + checkbox.getHeight(), 0xA0000000);
    }

    protected void renderAsterisk(DrawContext context, Widget widget, boolean draw){
        if(!draw) return;
        renderAsterisk(context, widget.getX() + widget.getWidth(), widget.getY() - 4, draw);
    }

    protected void renderAsterisk(DrawContext context, int x, int y, boolean draw){
        if(!draw) return;
        context.drawTextWithShadow(textRenderer, "*", x, y, 0xFFFFC000);
    }
}
