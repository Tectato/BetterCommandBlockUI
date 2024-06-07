package bettercommandblockui.main.ui.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.vehicle.CommandBlockMinecartEntity;
import net.minecraft.network.packet.c2s.play.UpdateCommandBlockMinecartC2SPacket;
import net.minecraft.world.CommandBlockExecutor;

public class BetterMinecartCommandBlockScreen extends AbstractBetterCommandBlockScreen{

    public static BetterMinecartCommandBlockScreen instance;

    public BetterMinecartCommandBlockScreen(MinecraftClient client, CommandBlockExecutor commandExecutor){
        this.commandExecutor = commandExecutor;
        this.client = client;
        instance = this;
        updated = true;
    }

    public void init(){
        super.init();
        consoleCommandTextField.setText(commandExecutor.getCommand());
    }

    @Override
    protected void syncSettingsToServer(CommandBlockExecutor commandExecutor) {
        if (commandExecutor instanceof CommandBlockMinecartEntity.CommandExecutor) {
            CommandBlockMinecartEntity.CommandExecutor commandExecutor2 = (CommandBlockMinecartEntity.CommandExecutor)commandExecutor;
            this.client.getNetworkHandler().sendPacket(new UpdateCommandBlockMinecartC2SPacket(commandExecutor2.getMinecart().getId(), this.consoleCommandTextField.getText(), commandExecutor.isTrackingOutput()));
        }
    }
}
