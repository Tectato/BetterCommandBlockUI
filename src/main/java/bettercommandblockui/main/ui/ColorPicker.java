package bettercommandblockui.main.ui;

import net.minecraft.text.Text;

public class ColorPicker {

    public enum COLOR{
        RED,
        GREEN,
        BLUE
    }

    private static int r = 255, g = 255, b = 255;

    public static void setColor(COLOR color, int value){
        value = Math.min(Math.max(0,value),255);
        switch(color){
            case RED:
                r = value;
                break;
            case GREEN:
                g = value;
                break;
            case BLUE:
                b = value;
                break;
        }
    }

    public static int getColor(COLOR color){
        return switch (color) {
            case RED -> r;
            case GREEN -> g;
            case BLUE -> b;
        };
    }

    public static int getInteger(){
        return (r << 16) | (g << 8) | (b);
    }

    public static String getHexString(){
        return Integer.toHexString(getInteger());
    }

}
