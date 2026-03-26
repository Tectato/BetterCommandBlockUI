package bettercommandblockui.mixin;

import bettercommandblockui.main.ui.screen.BetterCommandBlockScreen;
import bettercommandblockui.mixin.LocalPlayerAccessor;
import bettercommandblockui.mixin.ServerPlayerAccessor;
import net.minecraft.world.level.block.CommandBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CommandBlock.class)
public class CommandBlockMixin {

    @Redirect(method= "useWithoutItem",
    at=@At(value="INVOKE", target= "Lnet/minecraft/world/entity/player/Player;openCommandBlock(Lnet/minecraft/world/level/block/entity/CommandBlockEntity;)V"))
    public void openBetterCommandBlockScreen(Player instance, CommandBlockEntity commandBlock){
        if(instance instanceof LocalPlayer){
            Minecraft client = ((LocalPlayerAccessor)instance).getMinecraft();
            client.setScreen(new BetterCommandBlockScreen(client, commandBlock, commandBlock.getCommandBlock()));
        } else if (instance instanceof ServerPlayer){
            ((ServerPlayerAccessor)instance).getConnection().send(ClientboundBlockEntityDataPacket.create(commandBlock, BlockEntity::saveWithoutMetadata));
        }
    }
}
