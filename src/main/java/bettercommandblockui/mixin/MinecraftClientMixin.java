package bettercommandblockui.mixin;

import bettercommandblockui.main.BetterCommandBlockUI;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Inject(method="Lnet/minecraft/client/MinecraftClient;close()V", at=@At("HEAD"))
    public void closeMixin(CallbackInfo ci){
        BetterCommandBlockUI.writeConfig();
    }
}
