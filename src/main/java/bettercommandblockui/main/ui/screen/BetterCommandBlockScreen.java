package bettercommandblockui.main.ui.screen;

import bettercommandblockui.main.BetterCommandBlockUI;
import bettercommandblockui.main.ChainHandler;
import bettercommandblockui.main.ui.CyclingTexturedButtonWidget;
import bettercommandblockui.main.ui.MultiLineTextFieldWidget;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.CommandBlock;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.protocol.game.ServerboundSetCommandBlockPacket;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Tuple;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.BaseCommandBlock;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static bettercommandblockui.main.BetterCommandBlockUI.*;
import static net.minecraft.world.level.block.entity.CommandBlockEntity.Mode.REDSTONE;
import static net.minecraft.world.level.block.entity.CommandBlockEntity.Mode.SEQUENCE;
import static net.minecraft.world.level.block.entity.CommandBlockEntity.Mode.AUTO;

@Environment(value= EnvType.CLIENT)
public class BetterCommandBlockScreen extends AbstractBetterCommandBlockScreen {
    private CyclingTexturedButtonWidget<CommandBlockEntity.Mode> modeButton;
    private CyclingTexturedButtonWidget<Boolean> conditionalModeButton;
    private CyclingTexturedButtonWidget<Boolean> redstoneTriggerButton;
    private CommandBlockEntity.Mode mode = REDSTONE;

    private BlockState blockState;
    private ChainHandler chainHandler;
    private Button chainNext;
    private List<Button> chainPrior;
    CommandBlockEntity blockEntity;
    private boolean conditional;
    private boolean autoActivate;

    public static BetterCommandBlockScreen instance;

    public BetterCommandBlockScreen(Minecraft client, CommandBlockEntity blockEntity, BaseCommandBlock commandExecutor) {
        this.blockEntity = blockEntity;
        this.commandExecutor = commandExecutor;
        //this.client = client;
        this.chainHandler = new ChainHandler(this, this.blockEntity.getBlockPos());
        this.chainPrior = new LinkedList<>();
        instance = this;
    }

    @Override
    public void init(){
        super.init();

        int textBoxWidth = this.width - (2*screenMarginX + 2*cycleButtonWidth + 2*buttonMargin);
        int sideButtonX = this.width / 2 - (cycleButtonWidth + buttonMargin + textBoxWidth/2);

        Component[] modeTooltips = {
                Component.translatable("advMode.mode.redstone"),
                Component.translatable("advMode.mode.sequence"),
                Component.translatable("advMode.mode.auto")};
        this.modeButton = this.addRenderableWidget(
                new CyclingTexturedButtonWidget<CommandBlockEntity.Mode>(
                        sideButtonX,
                        this.height/2 - (buttonHeight + buttonHeight/2 + buttonMargin),
                        cycleButtonWidth,
                        buttonHeight,
                        Component.nullToEmpty(""),
                        (button -> this.mode = ((CyclingTexturedButtonWidget<CommandBlockEntity.Mode>)button).getValue()),
                        minecraft.screen,
                        new net.minecraft.client.gui.components.WidgetSprites[]{BUTTON_IMPULSE, BUTTON_CHAIN, BUTTON_REPEAT},
                        0,
                        new CommandBlockEntity.Mode[]{REDSTONE, SEQUENCE, AUTO},
                        modeTooltips
                ));

        Component[] conditionalTooltips = {
                Component.translatable("advMode.mode.unconditional"),
                Component.translatable("advMode.mode.conditional")};
        this.conditionalModeButton = this.addRenderableWidget(
                new CyclingTexturedButtonWidget<Boolean>(
                        sideButtonX,
                        this.height/2 - buttonHeight/2,
                        cycleButtonWidth,
                        buttonHeight,
                        Component.nullToEmpty(""),
                        (button -> this.conditional = ((CyclingTexturedButtonWidget<Boolean>)button).getValue()),
                        minecraft.screen,
                        new net.minecraft.client.gui.components.WidgetSprites[]{BUTTON_UNCONDITIONAL, BUTTON_CONDITIONAL},
                        0,
                        new Boolean[]{false, true},
                        conditionalTooltips
                ));

        Component[] activeTooltips = {
                Component.translatable("advMode.mode.redstoneTriggered"),
                Component.translatable("advMode.mode.autoexec.bat")};
        this.redstoneTriggerButton = this.addRenderableWidget(
                new CyclingTexturedButtonWidget<Boolean>(
                        sideButtonX,
                        this.height/2 + buttonHeight/2 + buttonMargin,
                        cycleButtonWidth,
                        buttonHeight,
                        Component.nullToEmpty(""),
                        (button -> this.autoActivate = ((CyclingTexturedButtonWidget<Boolean>)button).getValue()),
                        minecraft.screen,
                        new net.minecraft.client.gui.components.WidgetSprites[]{BUTTON_POWER_INACTIVE, BUTTON_POWER_ACTIVE},
                        0,
                        new Boolean[]{false, true},
                        activeTooltips
                ));

        if(chainHandler.isInChain()){
            BlockState next = chainHandler.getNext();
            List<Tuple<BlockState, Direction>> prior = chainHandler.getPrior();


            BlockPos position = blockEntity.getBlockPos();
            blockState = Minecraft.getInstance().level.getBlockState(position);

            if(next != null && next.getBlock().equals(BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath("minecraft","chain_command_block")))){
                Direction dir = Minecraft.getInstance().level.getBlockState(position).getValue(CommandBlock.FACING);
                chainNext = this.addRenderableWidget(
                        new ImageButton( 49, 5, 20, 20, BlockStateToButtonTextures(next), (button -> {
                            moveAlongChain(dir);
                        }), Component.nullToEmpty(""))
                );
                chainNext.setTooltip(Tooltip.create(Component.translatable("bcbui.chain.next")));
            }

            if(!prior.isEmpty()){
                int y = 5;
                for(Tuple<BlockState, Direction> entry : prior){
                    chainPrior.add(this.addRenderableWidget(
                            new ImageButton( 5, y, 20, 20, BlockStateToButtonTextures(entry.getA()), (button -> {
                                moveAlongChain(entry.getB());
                            }), Component.nullToEmpty(""))
                    ));
                    chainPrior.get(chainPrior.size()-1).setTooltip(Tooltip.create(DirectionToText(entry.getB())));
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
        BlockPos position = blockEntity.getBlockPos();
        assert Minecraft.getInstance().gameMode != null;
        Minecraft.getInstance().gameMode.useItemOn(Minecraft.getInstance().player, InteractionHand.MAIN_HAND, new BlockHitResult(
                position.offset(dir.getUnitVec3i()).getCenter(), Direction.UP, position.offset(dir.getUnitVec3i()), false
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
        BaseCommandBlock commandBlockExecutor = this.blockEntity.getCommandBlock();
        ((MultiLineTextFieldWidget)this.consoleCommandTextField).setRawText(commandBlockExecutor.getCommand());
        this.trackOutput = commandBlockExecutor.isTrackOutput();
        this.mode = this.blockEntity.getMode();
        this.conditional = this.blockEntity.isConditional();
        this.autoActivate = this.blockEntity.isAutomatic();

        this.priorState = new CommandBlockState(mode, conditional, autoActivate, trackOutput);

        if(!TRACK_OUTPUT_DEFAULT_USED) {
            int trackingOutputIndex = trackOutput ? 0 : 1;
            this.toggleTrackingOutputButton.setIndex(trackingOutputIndex);
        }

        int modeIndex = 0;
        this.mode = blockEntity.getMode();
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

    protected void syncSettingsToServer(BaseCommandBlock commandExecutor) {
        assert this.minecraft != null;
        Objects.requireNonNull(this.minecraft.getConnection()).send(
                new ServerboundSetCommandBlockPacket(
                        blockEntity.getBlockPos(),
                        this.consoleCommandTextField.getValue(),
                        this.mode,
                        this.trackOutput,
                        this.conditional,
                        this.autoActivate));
    }

    @Override
    public void extractRenderState(final GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        renderAsterisk(graphics, modeButton, modeButton.getValue() != priorState.type);
        renderAsterisk(graphics, conditionalModeButton, conditionalModeButton.getValue() != priorState.conditional);
        renderAsterisk(graphics, redstoneTriggerButton, redstoneTriggerButton.getValue() != priorState.needsRedstone);

        if(chainHandler.isInChain()){
            //context.fill(28,6,46,24, 0xFF000000);
            //context.drawBorder(27, 5, 20, 20, 0xFFA0A0A0);
            Identifier texture = BlockStateToButtonTextures(blockState).enabled();
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, texture, 27, 5, 20, 20);
            graphics.fill(27,5,46,24, 0x8F000000);
            renderAsterisk(graphics, 47, 1, wasModified());

            if(!updated){
                if(showOutput){
                    graphics.fill(
                            previousOutputTextField.getX(),
                            previousOutputTextField.getY(),
                            previousOutputTextField.getX() + previousOutputTextField.getWidth(),
                            previousOutputTextField.getY() + previousOutputTextField.getHeight(),
                            0x8F000000);
                } else {
                    graphics.fill(
                            consoleCommandTextField.getX(),
                            consoleCommandTextField.getY(),
                            consoleCommandTextField.getX() + consoleCommandTextField.getWidth(),
                            consoleCommandTextField.getY() + consoleCommandTextField.getHeight(),
                            0x8F000000);
                }
                graphics.centeredText(font, Component.translatable("bcbui.chain.tooFar"), width/2, height/2, 0xFFA0A0A0);
            }
        }
    }
}
