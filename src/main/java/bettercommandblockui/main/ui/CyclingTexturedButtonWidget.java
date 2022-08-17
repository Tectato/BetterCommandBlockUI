package bettercommandblockui.main.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class CyclingTexturedButtonWidget<T> extends ButtonWidget {
    Identifier textures;
    T[] values;

    public CyclingTexturedButtonWidget(int x, int y, int width, int height, Text message, PressAction onPress, Screen screen, Identifier textures, int initialIndex, T[] values) {
        super(x, y, width, height, message, onPress, new CyclingTooltipSupplier(screen, initialIndex, new Text[values.length]));
        this.textures = textures;
        this.values = values;
    }

    public CyclingTexturedButtonWidget(int x, int y, int width, int height, Text message, PressAction onPress, Screen screen, Identifier textures, int initialIndex, T[] values, Text[] tooltips) {
        super(x, y, width, height, message, onPress, new CyclingTooltipSupplier(screen, initialIndex, tooltips));
        this.textures = textures;
        this.values = values;
    }

    @Override
    public void onPress(){
        ((CyclingTooltipSupplier)this.tooltipSupplier).incrementIndex();
        super.onPress();
    }

    public T getValue(){
        return values[((CyclingTooltipSupplier)this.tooltipSupplier).getCurrentIndex()];
    }

    public void setIndex(int index){
        ((CyclingTooltipSupplier)this.tooltipSupplier).setIndex(index);
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        TextRenderer textRenderer = minecraftClient.textRenderer;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, this.textures);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, this.alpha);
        int i = this.active?(this.isHovered()?2:1):0;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        this.drawTexture(matrices, this.x, this.y, this.width, this.height, ((CyclingTooltipSupplier)this.tooltipSupplier).getCurrentIndex()*20, i*20, 20, 20, 20 * this.values.length, 60);
        this.renderBackground(matrices, minecraftClient, mouseX, mouseY);
        int j = this.active ? 0xFFFFFF : 0xA0A0A0;
        ClickableWidget.drawCenteredText(matrices, textRenderer, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, j | MathHelper.ceil(this.alpha * 255.0f) << 24);
        if (this.isHovered()) {
            this.renderTooltip(matrices, mouseX, mouseY);
        }
    }
}
