package bettercommandblockui.main;

import bettercommandblockui.main.ui.CyclingTexturedButtonWidget;
import bettercommandblockui.main.ui.MultiLineCommandSuggestor;
import bettercommandblockui.main.ui.MultiLineTextFieldWidget;
import bettercommandblockui.mixin.AbstractCommandBlockScreenAccessor;
import bettercommandblockui.mixin.ScreenAccessor;
import com.mojang.datafixers.kinds.IdF;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.CommandSuggestor;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractCommandBlockScreen;
import net.minecraft.client.gui.screen.ingame.CommandBlockScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.c2s.play.UpdateCommandBlockC2SPacket;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.CommandBlockExecutor;

import java.util.Objects;

import static bettercommandblockui.main.BetterCommandBlockUI.*;
import static net.minecraft.block.entity.CommandBlockBlockEntity.Type.REDSTONE;
import static net.minecraft.block.entity.CommandBlockBlockEntity.Type.SEQUENCE;
import static net.minecraft.block.entity.CommandBlockBlockEntity.Type.AUTO;

@Environment(value= EnvType.CLIENT)
public class BetterCommandBlockScreen extends CommandBlockScreen {
    private static final Text SET_COMMAND_TEXT = Text.translatable("advMode.setCommand");
    private static final Text COMMAND_TEXT = Text.translatable("advMode.command");
    private static final Text PREVIOUS_OUTPUT_TEXT = Text.translatable("advMode.previousOutput");
    protected ButtonWidget doneButton;
    protected ButtonWidget cancelButton;
    private CyclingTexturedButtonWidget<CommandBlockBlockEntity.Type> modeButton;
    private CyclingTexturedButtonWidget<Boolean> conditionalModeButton;
    private CyclingTexturedButtonWidget<Boolean> redstoneTriggerButton;
    private CommandBlockBlockEntity.Type mode = CommandBlockBlockEntity.Type.REDSTONE;
    protected CyclingTexturedButtonWidget<Boolean> toggleTrackingOutputButton;
    protected CyclingTexturedButtonWidget<Boolean> showOutputButton;
    CommandBlockBlockEntity blockEntity;
    CommandBlockExecutor commandExecutor;
    private boolean conditional;
    private boolean autoActivate;
    private boolean showOutput = false;
    private boolean trackOutput = true;
    private final boolean minecart;

    private final AbstractCommandBlockScreenAccessor accessor = (AbstractCommandBlockScreenAccessor) this;

    public static BetterCommandBlockScreen instance;

    public BetterCommandBlockScreen(MinecraftClient client, CommandBlockBlockEntity blockEntity, CommandBlockExecutor commandExecutor) {
        super(blockEntity);
        this.blockEntity = blockEntity;
        this.commandExecutor = commandExecutor;
        this.client = client;
        this.minecart = (blockEntity == null);
        instance = this;
    }

    @Override
    public void tick() {
        this.consoleCommandTextField.tick();
    }

    @Override
    public void init(){
        assert this.client != null;
        this.client.keyboard.setRepeatEvents(true);
        this.doneButton = this.addDrawableChild(new ButtonWidget(this.width / 2 - 4 - 150, this.height / 4 + 120 + 12, 150, 20, ScreenTexts.DONE, button -> this.commitAndClose()));
        this.cancelButton = this.addDrawableChild(new ButtonWidget(this.width / 2 + 4, this.height / 4 + 120 + 12, 150, 20, ScreenTexts.CANCEL, button -> this.close()));
        boolean bl = this.commandExecutor.isTrackingOutput();

        Text[] trackOutputTooltips = {
                Text.of("Tracking Output"),
                Text.of("Ignoring Output")
        };
        this.toggleTrackingOutputButton = this.addDrawableChild(new CyclingTexturedButtonWidget<Boolean>(this.width / 2 + 150 + 10, this.height/2 - 30, 20, 20, Text.of(""), (button) -> {
            this.trackOutput = ((CyclingTexturedButtonWidget<Boolean>)button).getValue();
            this.commandExecutor.setTrackOutput(trackOutput);
            this.setPreviousOutputText(trackOutput);
        }, client.currentScreen, BUTTON_TRACK_OUTPUT, 0, new Boolean[]{true, false}, trackOutputTooltips));

        Text[] outputTooltips = {
                Text.of("Command"),
                Text.of("Output")
        };
        this.showOutputButton = this.addDrawableChild(new CyclingTexturedButtonWidget<Boolean>(this.width / 2 + 150 + 10, this.height/2, 20, 20, Text.of(""), (button) -> {
            this.showOutput = ((CyclingTexturedButtonWidget<Boolean>)button).getValue();
            this.consoleCommandTextField.setVisible(!showOutput);
            this.previousOutputTextField.setVisible(showOutput);
        }, client.currentScreen, BUTTON_OUTPUT, 0, new Boolean[]{false, true}, outputTooltips));

        this.consoleCommandTextField = new MultiLineTextFieldWidget(this.textRenderer, this.width / 2 - 150, 50, 300, 120, (Text)Text.translatable("advMode.command"), this){
            @Override
            protected MutableText getNarrationMessage() {
                return super.getNarrationMessage().append(accessor.getCommandSuggestor().getNarration());
            }
        };

        MultiLineCommandSuggestor commandSuggestor = new MultiLineCommandSuggestor(this.client, this, this.consoleCommandTextField, this.textRenderer, true, true, 0, 7, false, Integer.MIN_VALUE);
        commandSuggestor.setWindowActive(true);
        commandSuggestor.refresh();
        accessor.setCommandSuggestor(commandSuggestor);
        ((MultiLineTextFieldWidget)this.consoleCommandTextField).setCommandSuggestor(commandSuggestor);

        this.consoleCommandTextField.setMaxLength(32500);
        this.consoleCommandTextField.setChangedListener(((AbstractCommandBlockScreenAccessor)this)::invokeOnCommandChanged);
        this.addSelectableChild(this.consoleCommandTextField);
        this.setInitialFocus(this.consoleCommandTextField);
        this.consoleCommandTextField.setTextFieldFocused(true);
        this.previousOutputTextField = new MultiLineTextFieldWidget(this.textRenderer, this.width / 2 - 150, 50, 300, 20, Text.translatable("advMode.previousOutput"), this);
        this.previousOutputTextField.setMaxLength(32500);
        this.previousOutputTextField.setEditable(false);
        this.previousOutputTextField.setVisible(false);
        ((MultiLineTextFieldWidget)this.consoleCommandTextField).setRawText("-");
        this.addSelectableChild(this.previousOutputTextField);
        this.setPreviousOutputText(bl);

        this.consoleCommandTextField.setText(commandExecutor.getCommand());

        if(!minecart){
            Text[] modeTooltips = {
                    Text.translatable("advMode.mode.redstone"),
                    Text.translatable("advMode.mode.sequence"),
                    Text.translatable("advMode.mode.auto")};
            this.modeButton = this.addDrawableChild(
                    new CyclingTexturedButtonWidget<CommandBlockBlockEntity.Type>(
                            this.width / 2 - 175,
                            this.height/2 - 40,
                            20,
                            20,
                            Text.of(""),
                            (button -> this.mode = ((CyclingTexturedButtonWidget<CommandBlockBlockEntity.Type>)button).getValue()),
                            client.currentScreen,
                            BUTTON_MODE,
                            0,
                            new CommandBlockBlockEntity.Type[]{REDSTONE, SEQUENCE, AUTO},
                            modeTooltips
                    ));

            Text[] conditionalTooltips = {
                    Text.translatable("advMode.mode.unconditional"),
                    Text.translatable("advMode.mode.conditional")};
            this.conditionalModeButton = this.addDrawableChild(
                    new CyclingTexturedButtonWidget<Boolean>(
                            this.width / 2 - 175,
                            this.height/2 - 10,
                            20,
                            20,
                            Text.of(""),
                            (button -> this.conditional = ((CyclingTexturedButtonWidget<Boolean>)button).getValue()),
                            client.currentScreen,
                            BUTTON_CONDITIONAL,
                            0,
                            new Boolean[]{false, true},
                            conditionalTooltips
                    ));

            Text[] activeTooltips = {
                    Text.translatable("advMode.mode.redstoneTriggered"),
                    Text.translatable("advMode.mode.autoexec.bat")};
            this.redstoneTriggerButton = this.addDrawableChild(
                    new CyclingTexturedButtonWidget<Boolean>(
                            this.width / 2 - 175,
                            this.height/2 + 20,
                            20,
                            20,
                            Text.of(""),
                            (button -> this.autoActivate = ((CyclingTexturedButtonWidget<Boolean>)button).getValue()),
                            client.currentScreen,
                            BUTTON_ACTIVE,
                            0,
                            new Boolean[]{false, true},
                            activeTooltips
                    ));
        }
        setButtonsActive(false);
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        String string = this.consoleCommandTextField.getText();
        this.init(client, width, height);
        ((MultiLineTextFieldWidget)this.consoleCommandTextField).setRawText(string);
        ((AbstractCommandBlockScreenAccessor)this).getCommandSuggestor().refresh();
        setButtonsActive(true);
    }

    public void scroll(double amount){
        ((AbstractCommandBlockScreenAccessor)this).getCommandSuggestor().mouseScrolled(MathHelper.clamp(amount, -1.0, 1.0));
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY){
        if(button == 0) {
            return this.consoleCommandTextField.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button){
        if(button == 0){
            return this.consoleCommandTextField.mouseReleased(mouseX, mouseY, button);
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(keyCode == 340 || keyCode == 344){
            this.consoleCommandTextField.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if(keyCode == 340 || keyCode == 344){
            this.consoleCommandTextField.keyReleased(keyCode, scanCode, modifiers);
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char c, int modifiers){
        return this.consoleCommandTextField.charTyped(c, modifiers);
    }

    private void setButtonsActive(boolean active) {
        this.doneButton.active = active;
        this.toggleTrackingOutputButton.active = active;
        this.modeButton.active = active;
        this.conditionalModeButton.active = active;
        this.redstoneTriggerButton.active = active;
    }

    @Override
    protected void setPreviousOutputText(boolean trackOutput) {
        ((MultiLineTextFieldWidget)this.previousOutputTextField).setRawText(trackOutput ? this.commandExecutor.getLastOutput().getString() : "-");
    }

    public void updateCommandBlock() {
        CommandBlockExecutor commandBlockExecutor = this.blockEntity.getCommandExecutor();
        ((MultiLineTextFieldWidget)this.consoleCommandTextField).setRawText(commandBlockExecutor.getCommand());
        this.trackOutput = commandBlockExecutor.isTrackingOutput();
        this.mode = this.blockEntity.getCommandBlockType();
        this.conditional = this.blockEntity.isConditionalCommandBlock();
        this.autoActivate = this.blockEntity.isAuto();

        int trackingOutputIndex = trackOutput?0:1;
        this.toggleTrackingOutputButton.setIndex(trackingOutputIndex);

        int modeIndex = 0;
        this.mode = blockEntity.getCommandBlockType();
        switch (this.mode) {
            case SEQUENCE -> modeIndex = 1;
            case AUTO -> modeIndex = 2;
        }
        this.modeButton.setIndex(modeIndex);

        int conditionalIndex = this.conditional?1:0;
        this.conditionalModeButton.setIndex(conditionalIndex);

        int autoIndex = this.autoActivate?1:0;
        this.redstoneTriggerButton.setIndex(autoIndex);

        this.setPreviousOutputText(trackOutput);
        this.setButtonsActive(true);
    }

    protected void commitAndClose() {
        trackOutput = toggleTrackingOutputButton.getValue();
        this.syncSettingsToServer(commandExecutor);
        if (!commandExecutor.isTrackingOutput()) {
            commandExecutor.setLastOutput(null);
        }
        assert this.client != null;
        this.client.setScreen(null);
    }

    protected void syncSettingsToServer(CommandBlockExecutor commandExecutor) {
        if(!minecart){
            assert this.client != null;
            Objects.requireNonNull(this.client.getNetworkHandler()).sendPacket(
                    new UpdateCommandBlockC2SPacket(
                            new BlockPos(commandExecutor.getPos()),
                            this.consoleCommandTextField.getText(),
                            this.mode,
                            this.trackOutput,
                            this.conditional,
                            this.autoActivate));
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        AbstractCommandBlockScreen.drawCenteredText(matrices, this.textRenderer, SET_COMMAND_TEXT, this.width / 2, 20, 0xFFFFFF);
        if(showOutput){
            AbstractCommandBlockScreen.drawTextWithShadow(matrices, this.textRenderer, PREVIOUS_OUTPUT_TEXT, this.width / 2 - 150, 40, 0xA0A0A0);
            this.previousOutputTextField.render(matrices, mouseX, mouseY, delta);
        } else {
            AbstractCommandBlockScreen.drawTextWithShadow(matrices, this.textRenderer, COMMAND_TEXT, this.width / 2 - 150, 40, 0xA0A0A0);
            this.consoleCommandTextField.render(matrices, mouseX, mouseY, delta);
        }
        for (Drawable drawable : ((ScreenAccessor)this).getDrawables()) {
            drawable.render(matrices, mouseX, mouseY, delta);
        }
    }
}
