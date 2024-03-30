package bettercommandblockui.main.ui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class CyclingTooltipSupplier {
    private Screen screen;
    private Text[] tooltips;
    private int currentIndex;

    public CyclingTooltipSupplier(Screen screen, int initialIndex, Text[] tooltips){
        this.screen = screen;
        this.tooltips = tooltips;
        this.currentIndex = initialIndex;
    }

    public void incrementIndex(){
        currentIndex = (currentIndex+1)%tooltips.length;
    }

    public void setIndex(int index){
        currentIndex = index;
    }

    public int getCurrentIndex(){
        return currentIndex;
    }

    public Tooltip getTooltip() {
        return Tooltip.of(tooltips[currentIndex]);
    }
}
