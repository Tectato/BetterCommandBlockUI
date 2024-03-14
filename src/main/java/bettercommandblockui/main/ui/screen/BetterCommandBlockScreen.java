package bettercommandblockui.main.ui.screen;

import bettercommandblockui.main.BetterCommandBlockUI;
import bettercommandblockui.main.ui.CyclingTexturedButtonWidget;
import bettercommandblockui.main.ui.MultiLineTextFieldWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.network.packet.c2s.play.UpdateCommandBlockC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.CommandBlockExecutor;

import java.util.Objects;

import static bettercommandblockui.main.BetterCommandBlockUI.*;
import static net.minecraft.block.entity.CommandBlockBlockEntity.Type.REDSTONE;
import static net.minecraft.block.entity.CommandBlockBlockEntity.Type.SEQUENCE;
import static net.minecraft.block.entity.CommandBlockBlockEntity.Type.AUTO;

@Environment(value= EnvType.CLIENT)
public class BetterCommandBlockScreen extends AbstractBetterCommandBlockScreen {
    private CyclingTexturedButtonWidget<CommandBlockBlockEntity.Type> modeButton;
    private CyclingTexturedButtonWidget<Boolean> conditionalModeButton;
    private CyclingTexturedButtonWidget<Boolean> redstoneTriggerButton;
    private CommandBlockBlockEntity.Type mode = CommandBlockBlockEntity.Type.REDSTONE;
    CommandBlockBlockEntity blockEntity;
    private boolean conditional;
    private boolean autoActivate;

    public static BetterCommandBlockScreen instance;

    public BetterCommandBlockScreen(MinecraftClient client, CommandBlockBlockEntity blockEntity, CommandBlockExecutor commandExecutor) {
        this.blockEntity = blockEntity;
        this.commandExecutor = commandExecutor;
        this.client = client;
        instance = this;
    }

    @Override
    public void init(){
        super.init();

        int textBoxWidth = this.width - (2*screenMarginX + 2*cycleButtonWidth + 2*buttonMargin);
        int sideButtonX = this.width / 2 - (cycleButtonWidth + buttonMargin + textBoxWidth/2);

        Text[] modeTooltips = {
                Text.translatable("advMode.mode.redstone"),
                Text.translatable("advMode.mode.sequence"),
                Text.translatable("advMode.mode.auto")};
        this.modeButton = this.addDrawableChild(
                new CyclingTexturedButtonWidget<CommandBlockBlockEntity.Type>(
                        sideButtonX,
                        this.height/2 - (buttonHeight + buttonHeight/2 + buttonMargin),
                        cycleButtonWidth,
                        buttonHeight,
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
                        sideButtonX,
                        this.height/2 - buttonHeight/2,
                        cycleButtonWidth,
                        buttonHeight,
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
                        sideButtonX,
                        this.height/2 + buttonHeight/2 + buttonMargin,
                        cycleButtonWidth,
                        buttonHeight,
                        Text.of(""),
                        (button -> this.autoActivate = ((CyclingTexturedButtonWidget<Boolean>)button).getValue()),
                        client.currentScreen,
                        BUTTON_ACTIVE,
                        0,
                        new Boolean[]{false, true},
                        activeTooltips
                ));

        setButtonsActive(false);
    }

    @Override
    public void returnFromConfig(){
        updateCommandBlock();
        super.returnFromConfig();
    }

    public void updateCommandBlock() {
        CommandBlockExecutor commandBlockExecutor = this.blockEntity.getCommandExecutor();
        ((MultiLineTextFieldWidget)this.consoleCommandTextField).setRawText(commandBlockExecutor.getCommand());
        this.trackOutput = commandBlockExecutor.isTrackingOutput();
        this.mode = this.blockEntity.getCommandBlockType();
        this.conditional = this.blockEntity.isConditionalCommandBlock();
        this.autoActivate = this.blockEntity.isAuto();

        this.priorState = new CommandBlockState(mode, conditional, autoActivate, trackOutput);

        if(!TRACK_OUTPUT_DEFAULT_USED) {
            int trackingOutputIndex = trackOutput ? 0 : 1;
            this.toggleTrackingOutputButton.setIndex(trackingOutputIndex);
        }

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

    protected void syncSettingsToServer(CommandBlockExecutor commandExecutor) {
        assert this.client != null;
        Objects.requireNonNull(this.client.getNetworkHandler()).sendPacket(
                new UpdateCommandBlockC2SPacket(
                        BlockPos.ofFloored(commandExecutor.getPos()),
                        this.consoleCommandTextField.getText(),
                        this.mode,
                        this.trackOutput,
                        this.conditional,
                        this.autoActivate));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderAsterisk(context, modeButton, modeButton.getValue() != priorState.type);
        renderAsterisk(context, conditionalModeButton, conditionalModeButton.getValue() != priorState.conditional);
        renderAsterisk(context, redstoneTriggerButton, redstoneTriggerButton.getValue() != priorState.needsRedstone);
        super.render(context, mouseX, mouseY, delta);
    }
}
