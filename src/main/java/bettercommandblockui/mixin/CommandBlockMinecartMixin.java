package bettercommandblockui.mixin;

import bettercommandblockui.main.ui.screen.BetterMinecartCommandBlockScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.CommandBlockMinecartEntity;
import net.minecraft.world.CommandBlockExecutor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CommandBlockMinecartEntity.class)
public class CommandBlockMinecartMixin {
    @Redirect(method="interact",
            at=@At(value="INVOKE", target="Lnet/minecraft/entity/player/PlayerEntity;openCommandBlockMinecartScreen(Lnet/minecraft/entity/vehicle/CommandBlockMinecartEntity;)V"))
    public void openBetterMinecartCommandBlockScreen(PlayerEntity instance, CommandBlockMinecartEntity minecart){
        if(instance instanceof ClientPlayerEntity){
            MinecraftClient client = ((ClientPlayerEntityAccessor)instance).getClient();
            client.setScreen(new BetterMinecartCommandBlockScreen(client, minecart));
        }
    }
}
