package bettercommandblockui.mixin;

import net.minecraft.client.gui.screens.inventory.CommandBlockEditScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(CommandBlockEditScreen.class)
public class CommandBlockEditScreenMixin {
    @Inject(method= "updateGui()V", at=@At("HEAD"))
    public void updateCommandBlockMixin(CallbackInfo ci){
        try{
            int[] egh = {};
            egh[0] = 1;
        } catch(Exception e){
            System.out.print(Arrays.toString(e.getStackTrace()));
        }
    }
}
