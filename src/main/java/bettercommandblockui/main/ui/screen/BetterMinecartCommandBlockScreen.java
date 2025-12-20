package bettercommandblockui.main.ui.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.vehicle.CommandBlockMinecartEntity;
import net.minecraft.network.packet.c2s.play.UpdateCommandBlockMinecartC2SPacket;
import net.minecraft.world.CommandBlockExecutor;

public class BetterMinecartCommandBlockScreen extends AbstractBetterCommandBlockScreen{

    public static BetterMinecartCommandBlockScreen instance;
    private CommandBlockMinecartEntity minecart;

    public BetterMinecartCommandBlockScreen(MinecraftClient client, CommandBlockMinecartEntity minecart){
        this.commandExecutor = minecart.getCommandExecutor();
        this.minecart = minecart;
        //this.client = client;
        instance = this;
        updated = true;
    }

    public void init(){
        super.init();
        consoleCommandTextField.setText(commandExecutor.getCommand());
    }

    @Override
    protected void syncSettingsToServer(CommandBlockExecutor commandExecutor) {
        this.client.getNetworkHandler().sendPacket(new UpdateCommandBlockMinecartC2SPacket(minecart.getId(), this.consoleCommandTextField.getText(), commandExecutor.isTrackingOutput()));
    }
}
