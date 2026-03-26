package bettercommandblockui.main;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;

public class AreaSelectionHandler {
    private static boolean selecting;
    private static Vec3i startPos, endPos;

    public static boolean areaSelectionInput(){
        BlockPos currentPos = Minecraft.getInstance().player.blockPosition();
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

            Minecraft.getInstance().keyboardHandler.setClipboard(output);
        }

        selecting = !selecting;
        return selecting;
    }
}