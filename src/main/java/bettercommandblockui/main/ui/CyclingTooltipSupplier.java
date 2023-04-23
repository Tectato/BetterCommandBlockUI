package bettercommandblockui.main.ui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class CyclingTooltipSupplier implements ButtonWidget.TooltipSupplier {
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

    @Override
    public void onTooltip(ButtonWidget button, MatrixStack matrices, int mouseX, int mouseY) {
        this.screen.renderTooltip(matrices, tooltips[currentIndex], mouseX, mouseY);
    }

    @Override
    public void supply(Consumer<Text> consumer) {
        consumer.accept(tooltips[currentIndex]);
    }
}