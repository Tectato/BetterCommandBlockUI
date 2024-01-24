package bettercommandblockui.mixin;

import bettercommandblockui.main.BetterCommandBlockUI;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Inject(method="close()V",
            at=@At("HEAD"))
    public void close(CallbackInfo ci){
        BetterCommandBlockUI.writeConfig();
    }

}
