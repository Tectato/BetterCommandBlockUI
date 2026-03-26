package bettercommandblockui.main.ui.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.vehicle.minecart.MinecartCommandBlock;
import net.minecraft.network.protocol.game.ServerboundSetCommandMinecartPacket;
import net.minecraft.world.level.BaseCommandBlock;

public class BetterMinecartCommandBlockScreen extends AbstractBetterCommandBlockScreen{

    public static BetterMinecartCommandBlockScreen instance;
    private MinecartCommandBlock minecart;

    public BetterMinecartCommandBlockScreen(Minecraft client, MinecartCommandBlock minecart){
        this.commandExecutor = minecart.getCommandBlock();
        this.minecart = minecart;
        //this.client = client;
        instance = this;
        updated = true;
    }

    public void init(){
        super.init();
        consoleCommandTextField.setValue(commandExecutor.getCommand());
    }

    @Override
    protected void syncSettingsToServer(BaseCommandBlock commandExecutor) {
        this.minecraft.getConnection().send(new ServerboundSetCommandMinecartPacket(minecart.getId(), this.consoleCommandTextField.getValue(), commandExecutor.isTrackOutput()));
    }
}
