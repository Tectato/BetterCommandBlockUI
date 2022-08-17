package bettercommandblockui.mixin;

import bettercommandblockui.main.BetterCommandBlockScreen;
import net.minecraft.block.CommandBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CommandBlock.class)
public class CommandBlockMixin {

    @Redirect(method="onUse",
    at=@At(value="INVOKE", target="Lnet/minecraft/entity/player/PlayerEntity;openCommandBlockScreen(Lnet/minecraft/block/entity/CommandBlockBlockEntity;)V"))
    public void openBetterCommandBlockScreen(PlayerEntity instance, CommandBlockBlockEntity commandBlock){
        if(instance instanceof ClientPlayerEntity){
            MinecraftClient client = ((ClientPlayerEntityAccessor)instance).getClient();
            client.setScreen(new BetterCommandBlockScreen(client, commandBlock, commandBlock.getCommandExecutor()));
        } else if (instance instanceof ServerPlayerEntity){
            ((ServerPlayerEntityAccessor)instance).getNetworkHandler().sendPacket(BlockEntityUpdateS2CPacket.create(commandBlock, BlockEntity::createNbt));
        }
    }
}
