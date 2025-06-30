package bettercommandblockui.main.ui.screen;

import bettercommandblockui.main.BetterCommandBlockUI;
import bettercommandblockui.main.ChainHandler;
import bettercommandblockui.main.ui.CyclingTexturedButtonWidget;
import bettercommandblockui.main.ui.MultiLineTextFieldWidget;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.CommandBlock;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.c2s.play.UpdateCommandBlockC2SPacket;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.CommandBlockExecutor;

import java.util.LinkedList;
import java.util.List;
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
    private CommandBlockBlockEntity.Type mode = REDSTONE;

    private BlockState blockState;
    private ChainHandler chainHandler;
    private ButtonWidget chainNext;
    private List<ButtonWidget> chainPrior;
    CommandBlockBlockEntity blockEntity;
    private boolean conditional;
    private boolean autoActivate;

    public static BetterCommandBlockScreen instance;

    public BetterCommandBlockScreen(MinecraftClient client, CommandBlockBlockEntity blockEntity, CommandBlockExecutor commandExecutor) {
        this.blockEntity = blockEntity;
        this.commandExecutor = commandExecutor;
        this.client = client;
        this.chainHandler = new ChainHandler(this, this.commandExecutor);
        this.chainPrior = new LinkedList<>();
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
                        new net.minecraft.client.gui.screen.ButtonTextures[]{BUTTON_IMPULSE, BUTTON_CHAIN, BUTTON_REPEAT},
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
                        new net.minecraft.client.gui.screen.ButtonTextures[]{BUTTON_UNCONDITIONAL, BUTTON_CONDITIONAL},
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
                        new net.minecraft.client.gui.screen.ButtonTextures[]{BUTTON_POWER_INACTIVE, BUTTON_POWER_ACTIVE},
                        0,
                        new Boolean[]{false, true},
                        activeTooltips
                ));

        if(chainHandler.isInChain()){
            BlockState next = chainHandler.getNext();
            List<Pair<BlockState,Direction>> prior = chainHandler.getPrior();


            BlockPos position = BlockPos.ofFloored(commandExecutor.getPos());
            blockState = MinecraftClient.getInstance().world.getBlockState(position);

            if(next != null && next.getBlock().equals(Registries.BLOCK.get(Identifier.of("minecraft","chain_command_block")))){
                Direction dir = MinecraftClient.getInstance().world.getBlockState(position).get(CommandBlock.FACING);
                chainNext = this.addDrawableChild(
                        new TexturedButtonWidget( 49, 5, 20, 20, BlockStateToButtonTextures(next), (button -> {
                            moveAlongChain(dir);
                        }), Text.of(""))
                );
                chainNext.setTooltip(Tooltip.of(Text.translatable("bcbui.chain.next")));
            }

            if(!prior.isEmpty()){
                int y = 5;
                for(Pair<BlockState,Direction> entry : prior){
                    chainPrior.add(this.addDrawableChild(
                            new TexturedButtonWidget( 5, y, 20, 20, BlockStateToButtonTextures(entry.getLeft()), (button -> {
                                moveAlongChain(entry.getRight());
                            }), Text.of(""))
                    ));
                    chainPrior.get(chainPrior.size()-1).setTooltip(Tooltip.of(DirectionToText(entry.getRight())));
                    y += 22;
                }
            }
        }

        setButtonsActive(false);
    }

    @Override
    protected void setButtonsActive(boolean value){
        super.setButtonsActive(value);
        this.modeButton.setActive(value);
        this.conditionalModeButton.setActive(value);
        this.redstoneTriggerButton.setActive(value);
    }

    public void moveAlongChain(Direction dir){
        if(wasModified() && AUTOSAVE){
            commit();
        }
        BlockPos position = BlockPos.ofFloored(commandExecutor.getPos());
        assert MinecraftClient.getInstance().interactionManager != null;
        MinecraftClient.getInstance().interactionManager.interactBlock(MinecraftClient.getInstance().player, Hand.MAIN_HAND, new BlockHitResult(
                position.add(dir.getVector()).toCenterPos(), Direction.UP, position.add(dir.getVector()), false
        ));
    }

    @Override
    protected boolean wasModified(){
        boolean bl = super.wasModified();
        return bl
                || modeButton.getValue() != priorState.type
                || conditionalModeButton.getValue() != priorState.conditional
                || redstoneTriggerButton.getValue() != priorState.needsRedstone;
    }

    @Override
    public void returnFromConfig(){
        updateCommandBlock();
        super.returnFromConfig();
    }

    public void updateCommandBlock() {
        updated = true;
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

    @Override
    protected void commit(){
        super.commit();
        priorState.needsRedstone = redstoneTriggerButton.getValue();
        priorState.type = modeButton.getValue();
        priorState.conditional = conditional;
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
        super.render(context, mouseX, mouseY, delta);
        renderAsterisk(context, modeButton, modeButton.getValue() != priorState.type);
        renderAsterisk(context, conditionalModeButton, conditionalModeButton.getValue() != priorState.conditional);
        renderAsterisk(context, redstoneTriggerButton, redstoneTriggerButton.getValue() != priorState.needsRedstone);

        if(chainHandler.isInChain()){
            //context.fill(28,6,46,24, 0xFF000000);
            //context.drawBorder(27, 5, 20, 20, 0xFFA0A0A0);
            Identifier texture = BlockStateToButtonTextures(blockState).enabled();
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, texture, 27, 5, 20, 20);
            context.fill(27,5,46,24, 0x8F000000);
            renderAsterisk(context, 47, 1, wasModified());

            if(!updated){
                if(showOutput){
                    context.fill(
                            previousOutputTextField.getX(),
                            previousOutputTextField.getY(),
                            previousOutputTextField.getX() + previousOutputTextField.getWidth(),
                            previousOutputTextField.getY() + previousOutputTextField.getHeight(),
                            0x8F000000);
                } else {
                    context.fill(
                            consoleCommandTextField.getX(),
                            consoleCommandTextField.getY(),
                            consoleCommandTextField.getX() + consoleCommandTextField.getWidth(),
                            consoleCommandTextField.getY() + consoleCommandTextField.getHeight(),
                            0x8F000000);
                }
                context.drawCenteredTextWithShadow(textRenderer, Text.translatable("bcbui.chain.tooFar"), width/2, height/2, 0xFFA0A0A0);
            }
        }
    }
}
