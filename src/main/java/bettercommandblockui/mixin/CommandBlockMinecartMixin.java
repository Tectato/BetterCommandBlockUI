package bettercommandblockui.mixin;

import bettercommandblockui.main.ui.screen.BetterMinecartCommandBlockScreen;
import bettercommandblockui.mixin.LocalPlayerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.MinecartCommandBlock;
import net.minecraft.world.level.BaseCommandBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MinecartCommandBlock.class)
public class CommandBlockMinecartMixin {
    @Redirect(method= "interact",
            at=@At(value="INVOKE", target= "Lnet/minecraft/world/entity/player/Player;openMinecartCommandBlock(Lnet/minecraft/world/entity/vehicle/minecart/MinecartCommandBlock;)V"))
    public void openBetterMinecartCommandBlockScreen(Player instance, MinecartCommandBlock minecart){
        if(instance instanceof LocalPlayer){
            Minecraft client = ((LocalPlayerAccessor)instance).getMinecraft();
            client.setScreen(new BetterMinecartCommandBlockScreen(client, minecart));
        }
    }
}
