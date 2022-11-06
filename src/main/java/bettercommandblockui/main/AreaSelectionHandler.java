package bettercommandblockui.main;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public class AreaSelectionHandler {
    private static boolean selecting;
    private static Vec3i startPos, endPos;

    public static boolean areaSelectionInput(){
        BlockPos currentPos = MinecraftClient.getInstance().player.getBlockPos();
        if(!selecting){
            startPos = currentPos;
        } else {
            endPos = currentPos;
            Vec3i difference = endPos.subtract(startPos);

            String output = "";
            output += "x="+startPos.getX()+",";
            output += "y="+startPos.getY()+",";
            output += "z="+startPos.getZ()+",";

            output += "dx="+difference.getX()+",";
            output += "dy="+difference.getY()+",";
            output += "dz="+difference.getZ()+",";

            MinecraftClient.getInstance().keyboard.setClipboard(output);
        }

        selecting = !selecting;
        return selecting;
    }
}
