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
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractCommandBlockScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.CommandBlockExecutor;
import org.lwjgl.glfw.GLFW;

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
    protected TexturedButtonWidget showSideWindowButton;

    protected SideWindow sideWindow;

    protected CommandBlockExecutor commandExecutor;
    protected ChatInputSuggestor commandSuggestor;
    protected boolean trackOutput = true;
    protected boolean showOutput = SHOW_OUTPUT_DEFAULT;
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
        }, client.currentScreen, BUTTON_TRACK_OUTPUT, trackOutput?0:1, new Boolean[]{true, false}, trackOutputTooltips));

        this.setTrackingOutputDefaultCheckbox = this.addDrawableChild(
                new CheckboxWidget(
                        toggleTrackingOutputButton.getX() + toggleTrackingOutputButton.getWidth() + 2,
                        toggleTrackingOutputButton.getY(),
                        20, 20,
                        Text.empty(),
                        TRACK_OUTPUT_DEFAULT_USED,
                        false){
                    public void onPress() {
                        super.onPress();
                        BetterCommandBlockUI.setConfig(VAR_TRACK_OUTPUT_DEFAULT_USED, ""+this.isChecked());
                        BetterCommandBlockUI.setConfig(VAR_TRACK_OUTPUT_DEFAULT_VALUE, ""+trackOutput);
                    }
                });


        this.setTrackingOutputDefaultCheckbox.active =
                (!setTrackingOutputDefaultCheckbox.isChecked()) || trackOutput == TRACK_OUTPUT_DEFAULT_VALUE;
        this.setTrackingOutputDefaultCheckbox.setTooltip(Tooltip.of(Text.translatable("bcbui.trackOutput.setDefault")));

        Text[] outputTooltips = {
                Text.translatable("bcbui.view.command"),
                Text.translatable("bcbui.view.output")
        };
        this.showOutputButton = this.addDrawableChild(new CyclingTexturedButtonWidget<Boolean>(this.width / 2 + (textBoxWidth/2 + buttonMargin + buttonMargin/2), this.height/2, cycleButtonWidth, buttonHeight, Text.of(""), (button) -> {
            this.showOutput = ((CyclingTexturedButtonWidget<Boolean>)button).getValue();
            this.consoleCommandTextField.setVisible(!showOutput);
            this.previousOutputTextField.setVisible(showOutput);
            this.setShowOutputDefaultCheckbox.active = showOutput;
        }, client.currentScreen, BUTTON_OUTPUT, SHOW_OUTPUT_DEFAULT?1:0, new Boolean[]{false, true}, outputTooltips));

        this.setShowOutputDefaultCheckbox = this.addDrawableChild(
                new CheckboxWidget(
                        showOutputButton.getX() + showOutputButton.getWidth() + 2,
                        showOutputButton.getY(),
                        20, 20,
                        Text.empty(),
                        SHOW_OUTPUT_DEFAULT,
                        false){
                    public void onPress() {
                        super.onPress();
                        BetterCommandBlockUI.setConfig(VAR_SHOW_OUTPUT_DEFAULT, ""+this.isChecked());
                    }
                });
        this.setShowOutputDefaultCheckbox.active = showOutput;
        this.setShowOutputDefaultCheckbox.setTooltip(Tooltip.of(Text.translatable("bcbui.view.outputDefault")));

        this.showSideWindowButton = this.addDrawableChild(new TexturedButtonWidget(
                this.width - (buttonMargin + cycleButtonWidth),
                this.height - (buttonMargin + cycleButtonWidth),
                cycleButtonWidth,
                buttonHeight,
                0,
                20,
                20,
                BUTTON_SIDE_WINDOW,
                20,
                60,
                (button) -> {
                    showSideWindow = !showSideWindow;
                    this.sideWindow.setVisible(showSideWindow);
                    this.showOutputButton.setTooltipVisible(!showSideWindow);
                    this.setShowOutputDefaultCheckbox.setTooltipDelay(showSideWindow ? Integer.MAX_VALUE : 0);
                    this.toggleTrackingOutputButton.setTooltipVisible(!showSideWindow);
                    this.setTrackingOutputDefaultCheckbox.setTooltipDelay(showSideWindow ? Integer.MAX_VALUE : 0);
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
        this.setShowOutputDefaultCheckbox.setTooltipDelay(showSideWindow ? Integer.MAX_VALUE : 0);
        this.toggleTrackingOutputButton.setTooltipVisible(!showSideWindow);
        this.setTrackingOutputDefaultCheckbox.setTooltipDelay(showSideWindow ? Integer.MAX_VALUE : 0);

        this.consoleCommandTextField.setVisible(!showOutput);
        this.previousOutputTextField.setVisible(showOutput);

        this.setInitialFocus(showOutput ? this.previousOutputTextField : this.consoleCommandTextField);
        if(showOutput) {
            this.previousOutputTextField.setFocused(true);
        } else {
            this.consoleCommandTextField.setFocused(true);
        }
    }

    @Override
    public void close(){
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

    public boolean mouseClicked(double mouseX, double mouseY, int button){
        if(showSideWindow && sideWindow.mouseClicked(mouseX, mouseY, button)) return true;
        if(/*commandSuggestor.isOpen() && */commandSuggestor.mouseClicked(mouseX, mouseY, button)) return true;
        if(consoleCommandTextField.mouseClicked(mouseX,mouseY,button)) {
            setFocused(consoleCommandTextField);
            return true;
        }
        return super.mouseClicked(mouseX,mouseY,button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY){
        if(button == 0) {
            if(showSideWindow && sideWindow.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) return true;
            if(showOutput){
                return this.previousOutputTextField.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
            }
            return this.consoleCommandTextField.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button){
        if(showSideWindow && sideWindow.mouseReleased(mouseX, mouseY, button)) return true;
        if(button == 0){
            if(showOutput){
                return this.previousOutputTextField.mouseReleased(mouseX, mouseY, button);
            }
            return this.consoleCommandTextField.mouseReleased(mouseX, mouseY, button);
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(showSideWindow && sideWindow.isFocused() && sideWindow.keyPressed(keyCode,scanCode,modifiers)) return true;
        if(keyCode == GLFW.GLFW_KEY_LEFT_SHIFT || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            if (showOutput) {
                this.previousOutputTextField.keyPressed(keyCode, scanCode, modifiers);
            } else {
                this.consoleCommandTextField.keyPressed(keyCode, scanCode, modifiers);
            }
        }
        if (this.commandSuggestor.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (keyCode == 258 && sideWindow.isFocused()) return true;
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (!IGNORE_ENTER && (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER)) {
            this.commitAndClose();
            return true;
        }
        return false;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if(keyCode == 340 || keyCode == 344){
            if(showOutput){
                this.previousOutputTextField.keyReleased(keyCode, scanCode, modifiers);
            } else {
                this.consoleCommandTextField.keyReleased(keyCode, scanCode, modifiers);
            }
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char c, int modifiers){
        if(showSideWindow && sideWindow.charTyped(c, modifiers)) return true;
        if(showOutput) return false;
        return this.consoleCommandTextField.charTyped(c, modifiers);
    }

    protected void onCommandChanged(String s){
        commandSuggestor.refresh();
    }

    protected void setButtonsActive(boolean active) {
        this.doneButton.active = active;
        this.toggleTrackingOutputButton.active = active;
    }

    protected void setPreviousOutputText(boolean trackOutput) {
        ((MultiLineTextFieldWidget)this.previousOutputTextField).setRawText(trackOutput ? this.commandExecutor.getLastOutput().getString() : "-");
    }

    protected void commitAndClose() {
        trackOutput = toggleTrackingOutputButton.getValue();
        this.syncSettingsToServer(commandExecutor);
        if (!commandExecutor.isTrackingOutput()) {
            commandExecutor.setLastOutput(null);
        }
        assert this.client != null;
        close();
    }

    abstract protected void syncSettingsToServer(CommandBlockExecutor commandExecutor);

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        AbstractCommandBlockScreen.drawCenteredTextWithShadow(matrices, this.textRenderer, SET_COMMAND_TEXT, this.width / 2, 20, 0xFFFFFF);
        if(showOutput){
            AbstractCommandBlockScreen.drawTextWithShadow(matrices, this.textRenderer, PREVIOUS_OUTPUT_TEXT, this.width / 2 - 150, 40, 0xA0A0A0);
            this.previousOutputTextField.render(matrices, mouseX, mouseY, delta);
        } else {
            AbstractCommandBlockScreen.drawTextWithShadow(matrices, this.textRenderer, COMMAND_TEXT, this.width / 2 - 150, 40, 0xA0A0A0);
            this.consoleCommandTextField.render(matrices, mouseX, mouseY, delta);
        }
        matrices.push();
        matrices.translate(0, 0, 0.5f);
        renderAsterisk(matrices, toggleTrackingOutputButton, toggleTrackingOutputButton.getValue() != priorState.trackOutput);
        for (Drawable drawable : ((ScreenAccessor)this).getDrawables()) {
            if(!drawable.equals(sideWindow)) drawable.render(matrices, mouseX, mouseY, delta);
        }
        darkenCheckbox(matrices, setTrackingOutputDefaultCheckbox);
        darkenCheckbox(matrices, setShowOutputDefaultCheckbox);

        matrices.translate(0,0,1);
        sideWindow.render(matrices, mouseX, mouseY, delta);
        matrices.pop();
    }

    private void darkenCheckbox(MatrixStack matrices, CheckboxWidget checkbox){
        if(checkbox.active) return;
        fill(matrices, checkbox.getX(), checkbox.getY(), checkbox.getX() + checkbox.getWidth(), checkbox.getY() + checkbox.getHeight(), 0xA0000000);
    }

    protected void renderAsterisk(MatrixStack matrices, Widget widget, boolean draw){
        if(!draw) return;
        drawTextWithShadow(matrices, textRenderer, "*", widget.getX() + widget.getWidth(), widget.getY() - 4, 0xFFC000);
    }
}
