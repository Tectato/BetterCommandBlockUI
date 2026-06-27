package bettercommandblockui.main.ui;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.Button;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class CyclingTooltipSupplier {
    private Component[] tooltips;
    private int currentIndex;

    public CyclingTooltipSupplier(int initialIndex, Component[] tooltips){
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
        return Tooltip.create(tooltips[currentIndex]);
    }

}
