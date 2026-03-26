package bettercommandblockui.mixin;

import bettercommandblockui.main.BetterCommandBlockUI;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(method= "close()V", at=@At("HEAD"))
    public void closeMixin(CallbackInfo ci){
        BetterCommandBlockUI.writeConfig();
    }
}
