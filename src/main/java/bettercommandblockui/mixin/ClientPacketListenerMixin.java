package bettercommandblockui.mixin;

import bettercommandblockui.main.ui.screen.BetterCommandBlockScreen;
import bettercommandblockui.mixin.ClientCommonPacketListenerImplAccessor;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    @Inject(method= "handleBlockEntityData(Lnet/minecraft/network/protocol/game/ClientboundBlockEntityDataPacket;)V",
    at=@At("TAIL"))
    public void blockEntityUpdateInject(ClientboundBlockEntityDataPacket packet, CallbackInfo ci){
        BlockPos blockPos = packet.getPos();
        Minecraft client = ((ClientCommonPacketListenerImplAccessor)(Object)this).getMinecraft();
        client.level.getBlockEntity(blockPos, packet.getType()).ifPresent(blockEntity -> {
            CompoundTag nbtCompound = packet.getTag();
            /*if (nbtCompound != null) {
                blockEntity.readNbt(nbtCompound);
            }*/
            if (blockEntity instanceof CommandBlockEntity && client.screen instanceof BetterCommandBlockScreen) {
                ((BetterCommandBlockScreen)client.screen).updateCommandBlock();
            }
        });
    }
}
