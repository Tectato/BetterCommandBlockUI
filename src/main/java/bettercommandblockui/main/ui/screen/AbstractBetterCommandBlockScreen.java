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
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.GameNarrator;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BaseCommandBlock;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;

import java.time.Duration;

import static bettercommandblockui.main.BetterCommandBlockUI.*;

@Environment(EnvType.CLIENT)
public abstract class AbstractBetterCommandBlockScreen extends Screen {

    protected class CommandBlockState{
        public CommandBlockEntity.Mode type = CommandBlockEntity.Mode.REDSTONE;
        boolean conditional = false;
        public boolean needsRedstone = true;
        public boolean trackOutput = true;

        public CommandBlockState(CommandBlockEntity.Mode type, boolean conditional, boolean needsRedstone, boolean trackOutput){
            this.type = type;
            this.conditional = conditional;
            this.needsRedstone = needsRedstone;
            this.trackOutput = trackOutput;
        }
    }

    protected static final Component SET_COMMAND_TEXT = Component.translatable("advMode.setCommand");
    protected static final Component COMMAND_TEXT = Component.translatable("advMode.command");
    protected static final Component PREVIOUS_OUTPUT_TEXT = Component.translatable("advMode.previousOutput");
    protected static final WidgetSprites SIDE_WINDOW_BUTTON_TEXTURES = new WidgetSprites(
            Identifier.parse("bettercommandblockui:button_side_window_enabled"),
            Identifier.parse("bettercommandblockui:button_side_window_disabled"),
            Identifier.parse("bettercommandblockui:button_side_window_focused")
    );

    protected static final WidgetSprites SAVE_BUTTON_TEXTURES = new WidgetSprites(
            Identifier.parse("bettercommandblockui:button_save_enabled"),
            Identifier.parse("bettercommandblockui:button_save_disabled"),
            Identifier.parse("bettercommandblockui:button_save_focused")
    );

    protected CommandBlockState priorState;

    protected EditBox consoleCommandTextField;
    protected EditBox previousOutputTextField;

    protected Button doneButton;
    protected Button cancelButton;
    //protected ButtonWidget configButton;
    protected CyclingTexturedButtonWidget<Boolean> toggleTrackingOutputButton;
    protected Checkbox setTrackingOutputDefaultCheckbox;
    protected CyclingTexturedButtonWidget<Boolean> showOutputButton;
    protected Checkbox setShowOutputDefaultCheckbox;
    protected ImageButton showSideWindowButton, saveButton;

    protected SideWindow sideWindow;

    protected BaseCommandBlock commandExecutor;
    protected CommandSuggestions commandSuggestor;
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
        super(GameNarrator.NO_TITLE);
    }

    @Override
    public void init(){
        assert this.minecraft != null;

        this.priorState = new CommandBlockState(CommandBlockEntity.Mode.REDSTONE, false, false, commandExecutor.isTrackOutput());

        int textBoxHeight = this.height - (2*screenMarginY + textHeight + textMargin + buttonHeight + 2*buttonMargin + sliderHeight);
        int textBoxWidth = this.width - (2*screenMarginX + 2*cycleButtonWidth + 2*buttonMargin);

        int lowerButtonWidth = Math.min((textBoxWidth/2) - buttonMargin/2, 160);
        this.doneButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.commitAndClose()).bounds(this.width / 2 - (lowerButtonWidth + buttonMargin/2), this.height / 2 + (5 + buttonMargin + textBoxHeight/2), lowerButtonWidth, buttonHeight).build());
        this.cancelButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.onClose()).bounds(this.width / 2 + buttonMargin/2, this.height / 2 + (5 + buttonMargin + textBoxHeight/2), lowerButtonWidth, buttonHeight).build());
        //this.configButton = this.addDrawableChild(ButtonWidget.builder(Text.literal("Config"), button -> this.openConfig()).dimensions(16, 16, 2*buttonHeight, buttonHeight).build());

        this.trackOutput = this.commandExecutor.isTrackOutput();
        if(TRACK_OUTPUT_DEFAULT_USED){
            trackOutput = TRACK_OUTPUT_DEFAULT_VALUE;
        }

        Component[] trackOutputTooltips = {
                Component.translatable("bcbui.trackOutput.true"),
                Component.translatable("bcbui.trackOutput.false")
        };

        this.toggleTrackingOutputButton = this.addRenderableWidget(new CyclingTexturedButtonWidget<Boolean>(this.width / 2 + (textBoxWidth/2 + buttonMargin + buttonMargin/2), this.height/2 - (buttonHeight + buttonMargin), cycleButtonWidth, buttonHeight, Component.nullToEmpty(""), (button) -> {
            this.trackOutput = ((CyclingTexturedButtonWidget<Boolean>)button).getValue();
            this.commandExecutor.setTrackOutput(trackOutput);
            this.setPreviousOutputText(trackOutput);
            this.setTrackingOutputDefaultCheckbox.active =
                    (!setTrackingOutputDefaultCheckbox.selected()) || trackOutput == TRACK_OUTPUT_DEFAULT_VALUE;
        }, new WidgetSprites[]{BUTTON_TRACK_OUTPUT, BUTTON_IGNORE_OUTPUT}, trackOutput?0:1, new Boolean[]{true, false}, trackOutputTooltips));

        this.setTrackingOutputDefaultCheckbox = this.addRenderableWidget(
                Checkbox.builder(Component.nullToEmpty(""), font)
                        .pos(toggleTrackingOutputButton.getX() + toggleTrackingOutputButton.getWidth() + 2, toggleTrackingOutputButton.getY() + 2)
                        .selected(TRACK_OUTPUT_DEFAULT_USED)
                        .onValueChange(new Checkbox.OnValueChange() {
                            @Override
                            public void onValueChange(Checkbox checkbox, boolean checked) {
                                BetterCommandBlockUI.setConfig(VAR_TRACK_OUTPUT_DEFAULT_USED, ""+checked);
                                BetterCommandBlockUI.setConfig(VAR_TRACK_OUTPUT_DEFAULT_VALUE, ""+trackOutput);
                            }
                        })
                        .tooltip(Tooltip.create(Component.translatable("bcbui.trackOutput.setDefault")))
                        .build());
        this.setTrackingOutputDefaultCheckbox.active =
                (!setTrackingOutputDefaultCheckbox.selected()) || trackOutput == TRACK_OUTPUT_DEFAULT_VALUE;

        Component[] outputTooltips = {
                Component.translatable("bcbui.view.command"),
                Component.translatable("bcbui.view.output")
        };
        this.showOutputButton = this.addRenderableWidget(new CyclingTexturedButtonWidget<Boolean>(this.width / 2 + (textBoxWidth/2 + buttonMargin + buttonMargin/2), this.height/2, cycleButtonWidth, buttonHeight, Component.nullToEmpty(""), (button) -> {
            this.showOutput = ((CyclingTexturedButtonWidget<Boolean>)button).getValue();
            this.consoleCommandTextField.setVisible(!showOutput);
            this.previousOutputTextField.setVisible(showOutput);
            this.setShowOutputDefaultCheckbox.active = showOutput;
        }, new WidgetSprites[]{BUTTON_COMMAND, BUTTON_OUTPUT}, SHOW_OUTPUT_DEFAULT?1:0, new Boolean[]{false, true}, outputTooltips));

        this.setShowOutputDefaultCheckbox = this.addRenderableWidget(
                Checkbox.builder(Component.nullToEmpty(""), font)
                        .pos(showOutputButton.getX() + showOutputButton.getWidth() + 2, showOutputButton.getY() + 2)
                        .selected(SHOW_OUTPUT_DEFAULT)
                        .onValueChange(new Checkbox.OnValueChange() {
                            @Override
                            public void onValueChange(Checkbox checkbox, boolean checked) {
                                BetterCommandBlockUI.setConfig(VAR_SHOW_OUTPUT_DEFAULT, ""+checked);
                            }
                        })
                        .tooltip(Tooltip.create(Component.translatable("bcbui.view.outputDefault")))
                        .build());
        this.setShowOutputDefaultCheckbox.active = showOutput;

        this.showSideWindowButton = this.addRenderableWidget(new ImageButton(
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
                Component.translatable("bcbui.tools"))
        );
        Tooltip showSideWindowTooltip = Tooltip.create(Component.translatable("bcbui.tools"));
        this.showSideWindowButton.setTooltip(showSideWindowTooltip);

        this.consoleCommandTextField = new MultiLineTextFieldWidget(this.font, this.width/2 - textBoxWidth/2, this.height/2 - textBoxHeight/2, textBoxWidth, textBoxHeight, (Component) Component.translatable("advMode.command"), this){
            @Override
            protected MutableComponent createNarrationMessage() {
                return super.createNarrationMessage().append(commandSuggestor.getNarrationMessage());
            }
        };

        commandSuggestor = new MultiLineCommandSuggestor(this.minecraft, this, this.consoleCommandTextField, this.font, true, true, 0, 7, false, Integer.MIN_VALUE);
        commandSuggestor.setAllowSuggestions(true);
        commandSuggestor.updateCommandInfo();
        ((MultiLineTextFieldWidget)this.consoleCommandTextField).setCommandSuggestor((MultiLineCommandSuggestor) commandSuggestor);

        this.consoleCommandTextField.setMaxLength(32500);
        this.consoleCommandTextField.setResponder(this::onCommandChanged);
        this.addWidget(this.consoleCommandTextField);
        this.previousOutputTextField = new MultiLineTextFieldWidget(this.font, this.width/2 - textBoxWidth/2, this.height/2 - textBoxHeight/2, textBoxWidth, 16, Component.translatable("advMode.previousOutput"), this);
        this.previousOutputTextField.setMaxLength(32500);
        this.previousOutputTextField.setEditable(false);
        ((MultiLineTextFieldWidget)this.consoleCommandTextField).setRawText("-");
        this.addWidget(this.previousOutputTextField);
        this.setPreviousOutputText(trackOutput);

        this.consoleCommandTextField.setValue(commandExecutor.getCommand());

        this.sideWindow = this.addRenderableOnly(new SideWindow(3*this.width/4, 20, this.width/4, 7*this.height/10, (MultiLineTextFieldWidget) consoleCommandTextField, this));
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

        this.saveButton = this.addRenderableWidget(new ImageButton(
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
                Component.translatable("bcbui.save"))
        );
        Tooltip saveTooltip = Tooltip.create(Component.translatable("bcbui.save"));
        this.saveButton.setTooltip(saveTooltip);
    }

    @Override
    public void onClose(){
        if (!closing && AUTOSAVE && wasModified()){
            commit();
        }
        super.onClose();
    }

    @Override
    public void resize(int width, int height) {
        String string = this.consoleCommandTextField.getValue();
        this.init(width, height);
        ((MultiLineTextFieldWidget)this.consoleCommandTextField).setRawText(string);
        commandSuggestor.updateCommandInfo();
        setButtonsActive(true);
    }

    public void openConfig(){
        assert minecraft != null;
        commandBuffer = this.consoleCommandTextField.getValue();
        minecraft.gui.setScreen(new ConfigScreen(this, minecraft, width, height));
    }

    public void returnFromConfig(){
        this.consoleCommandTextField.setValue(commandBuffer);
    }

    public void sideWindowFocused(){
        consoleCommandTextField.setFocused(false);
    }

    public boolean scroll(double amount){
        return commandSuggestor.mouseScrolled(Mth.clamp(amount, -1.0, 1.0));
    }

    public boolean mouseClicked(MouseButtonEvent click, boolean doubled){
        if(showSideWindow && sideWindow.mouseClicked(click, doubled)) return true;
        if(commandSuggestor.isVisible() && commandSuggestor.mouseClicked(click)) return true;
        consoleCommandTextField.onClick(click, doubled);
        /*if(consoleCommandTextField.mouseClicked(mouseX,mouseY,button)) {
            setFocused(consoleCommandTextField);
            return true;
        }*/
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent click, double deltaX, double deltaY){
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
    public boolean mouseReleased(MouseButtonEvent click){
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
    public boolean keyPressed(KeyEvent input) {
        if(showSideWindow && sideWindow.isFocused() && sideWindow.keyPressed(input)) return true;
        int keyCode = input.input();
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
        if (keyCode == 83 && Minecraft.getInstance().hasControlDown() && ((MultiLineTextFieldWidget)this.consoleCommandTextField).wasModified()){ // CTRL + S
            this.commit();
        }
        return false;
    }

    @Override
    public boolean keyReleased(KeyEvent input) {
        int keyCode = input.input();
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
    public boolean charTyped(CharacterEvent input){
        if(showSideWindow && sideWindow.charTyped(input)) return true;
        if(showOutput) return false;
        return this.consoleCommandTextField.charTyped(input);
    }

    protected void onCommandChanged(String s){
        commandSuggestor.updateCommandInfo();
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
        if (!commandExecutor.isTrackOutput()) {
            commandExecutor.setLastOutput(null);
        }
        ((MultiLineTextFieldWidget)consoleCommandTextField).resetModified();
        priorState.trackOutput = trackOutput;
    }

    protected void commitAndClose() {
        commit();
        assert this.minecraft != null;
        closing = true;
        onClose();
    }

    protected boolean wasModified(){
        return ((MultiLineTextFieldWidget)consoleCommandTextField).wasModified() || toggleTrackingOutputButton.getValue() != priorState.trackOutput;
    }

    abstract protected void syncSettingsToServer(BaseCommandBlock commandExecutor);

    @Override
    public void extractRenderState(final GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        //this.renderBackground(context, mouseX, mouseY, delta);
        graphics.centeredText(this.font, SET_COMMAND_TEXT, this.width / 2, 20, 0xFFFFFFFF);
        if(showOutput){
            graphics.text(this.font, PREVIOUS_OUTPUT_TEXT, this.previousOutputTextField.getX(), 40, 0xFFA0A0A0);
            this.previousOutputTextField.extractRenderState(graphics, mouseX, mouseY, delta);
        } else {
            graphics.text(this.font, COMMAND_TEXT, this.consoleCommandTextField.getX(), 40, 0xFFA0A0A0);
            this.consoleCommandTextField.extractRenderState(graphics, mouseX, mouseY, delta);
        }
        renderAsterisk(graphics, toggleTrackingOutputButton, toggleTrackingOutputButton.getValue() != priorState.trackOutput);
        for (Renderable drawable : ((ScreenAccessor)this).getRenderables()) {
            if(!drawable.equals(sideWindow)) drawable.extractRenderState(graphics, mouseX, mouseY, delta);
        }
        darkenCheckbox(graphics, setTrackingOutputDefaultCheckbox);
        darkenCheckbox(graphics, setShowOutputDefaultCheckbox);

        /*Matrix3x2fStack matrixStack = context.getMatrices();
        matrixStack.pushMatrix();
        matrixStack.translate(0,0,1);*/
        sideWindow.extractRenderState(graphics, mouseX, mouseY, delta);
        //matrixStack.popMatrix();
    }

    private void darkenCheckbox(final GuiGraphicsExtractor graphics, Checkbox checkbox){
        if(checkbox.active) return;
        graphics.fill(checkbox.getX(), checkbox.getY(), checkbox.getX() + checkbox.getWidth() - 4, checkbox.getY() + checkbox.getHeight(), 0xA0000000);
    }

    protected void renderAsterisk(final GuiGraphicsExtractor graphics, LayoutElement widget, boolean draw){
        if(!draw) return;
        renderAsterisk(graphics, widget.getX() + widget.getWidth(), widget.getY() - 4, draw);
    }

    protected void renderAsterisk(final GuiGraphicsExtractor graphics, int x, int y, boolean draw){
        if(!draw) return;
        graphics.text(font, "*", x, y, 0xFFFFC000);
    }
}
