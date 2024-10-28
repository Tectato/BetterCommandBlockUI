package bettercommandblockui.main.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class CyclingTexturedButtonWidget<T> extends PressableWidget {
    ButtonTextures[] textures;
    T[] values;
    CyclingTooltipSupplier tooltipSupplier;
    PressAction action;

    public CyclingTexturedButtonWidget(int x, int y, int width, int height, Text message, PressAction onPress, Screen screen, ButtonTextures[] textures, int initialIndex, T[] values) {
        super(x, y, width, height, message);
        this.textures = textures;
        this.values = values;
        this.tooltipSupplier = new CyclingTooltipSupplier(screen, initialIndex, new Text[values.length]);
        this.action = onPress;
        this.setTooltip(tooltipSupplier.getTooltip());
    }

    public CyclingTexturedButtonWidget(int x, int y, int width, int height, Text message, PressAction onPress, Screen screen, ButtonTextures[] textures, int initialIndex, T[] values, Text[] tooltips) {
        super(x, y, width, height, message);
        this.textures = textures;
        this.values = values;
        this.tooltipSupplier = new CyclingTooltipSupplier(screen, initialIndex, tooltips);
        this.action = onPress;
        this.setTooltip(tooltipSupplier.getTooltip());
    }

    @Override
    public void onPress(){
        if(!active) return;
        this.tooltipSupplier.incrementIndex();
        this.setTooltip(tooltipSupplier.getTooltip());
        action.onPress(this);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if(!active) return;
        if(mouseX > getX() && mouseX <= getX() + getWidth() && mouseY > getY() && mouseY <= getY() + getHeight()){
            this.tooltipSupplier.incrementIndex();
            this.setTooltip(tooltipSupplier.getTooltip());
            action.onPress(this);
        }
    }

    public T getValue(){
        return values[this.tooltipSupplier.getCurrentIndex()];
    }

    public void setIndex(int index){
        this.tooltipSupplier.setIndex(index);
        this.setTooltip(tooltipSupplier.getTooltip());
    }

    public void setActive(boolean value){
        this.active = value;
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        TextRenderer textRenderer = minecraftClient.textRenderer;
        int i = this.active?(this.isHovered()?2:1):0;
        context.drawGuiTexture(RenderLayer::getGuiTextured, this.textures[this.tooltipSupplier.getCurrentIndex()].get(this.active,this.isHovered()), this.getX(), this.getY(), 20, 20);
        //this.renderBackground(matrices, minecraftClient, mouseX, mouseY);
        int j = this.active ? 0xFFFFFF : 0xA0A0A0;
        context.drawCenteredTextWithShadow(textRenderer, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, j | MathHelper.ceil(this.alpha * 255.0f) << 24);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }

    public void setTooltipVisible(boolean value){
        this.setTooltip(value ? tooltipSupplier.getTooltip() : null);
    }

    public interface PressAction{
        public void onPress(CyclingTexturedButtonWidget button);
    }
}
