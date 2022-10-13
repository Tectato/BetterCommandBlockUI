package bettercommandblockui.mixin;

import bettercommandblockui.main.BetterCommandBlockScreen;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.CommandBlockScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import org.checkerframework.common.reflection.qual.Invoke;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

//    @Inject(method="onBlockEntityUpdate(Lnet/minecraft/network/packet/s2c/play/BlockEntityUpdateS2CPacket;)V",
//    at=@At("TAIL"))
//    public void testInject(BlockEntityUpdateS2CPacket packet, CallbackInfo ci){
//        System.out.println("Block Entity Update packet!");
//        BlockPos blockPos = packet.getPos();
//        MinecraftClient client = ((ClientPlayNetworkHandlerAccessor)(Object)this).getClient();
//        client.world.getBlockEntity(blockPos, packet.getBlockEntityType()).ifPresent(blockEntity -> {
//            if(blockEntity instanceof CommandBlockBlockEntity){
//                System.out.println("-> Is a command block");
//            }
//            NbtCompound nbtCompound = packet.getNbt();
//            if (nbtCompound != null) {
//                blockEntity.readNbt(nbtCompound);
//            }
//            if (blockEntity instanceof CommandBlockBlockEntity && client.currentScreen instanceof CommandBlockScreen) {
//                ((CommandBlockScreen)client.currentScreen).updateCommandBlock();
//            }
//        });
//    }

    @Redirect(method="method_38542(Lnet/minecraft/network/packet/s2c/play/BlockEntityUpdateS2CPacket;Lnet/minecraft/block/entity/BlockEntity;)V",
    at=@At(value="INVOKE", target="Lnet/minecraft/client/gui/screen/ingame/CommandBlockScreen;updateCommandBlock()V"))
    public void updateCommandBlockRedirect(CommandBlockScreen screen){
        BetterCommandBlockScreen.instance.updateCommandBlock();
    }
}
