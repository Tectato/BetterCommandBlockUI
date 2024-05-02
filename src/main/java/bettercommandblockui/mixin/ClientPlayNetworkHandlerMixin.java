package bettercommandblockui.mixin;

import bettercommandblockui.main.ui.screen.BetterCommandBlockScreen;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    @Inject(method="onBlockEntityUpdate(Lnet/minecraft/network/packet/s2c/play/BlockEntityUpdateS2CPacket;)V",
    at=@At("TAIL"))
    public void testInject(BlockEntityUpdateS2CPacket packet, CallbackInfo ci){
        BlockPos blockPos = packet.getPos();
        MinecraftClient client = ((ClientCommonNetworkHandlerAccessor)(Object)this).getClient();
        client.world.getBlockEntity(blockPos, packet.getBlockEntityType()).ifPresent(blockEntity -> {
            NbtCompound nbtCompound = packet.getNbt();
            /*if (nbtCompound != null) {
                blockEntity.readNbt(nbtCompound);
            }*/
            if (blockEntity instanceof CommandBlockBlockEntity && client.currentScreen instanceof BetterCommandBlockScreen) {
                ((BetterCommandBlockScreen)client.currentScreen).updateCommandBlock();
            }
        });
    }
}
