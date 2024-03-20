package bettercommandblockui.main.ui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;
import org.joml.Matrix4f;

public class ColorScrollbarWidget extends ScrollbarWidget{
    int color;
    public ColorScrollbarWidget(int x, int y, int width, int height, Text message, ColorPicker.COLOR color) {
        super(x, y, width, height, message, true);
        int colorInt = switch (color) {
            case RED -> 0xFF0000;
            case GREEN -> 0x00FF00;
            case BLUE -> 0x0000FF;
        };
        this.color = 0xFF000000 | colorInt;
        setScale(width*2);
        this.barLength = 1;
    }

    @Override
    protected void renderFrame(DrawContext context){
        fillHorizontalGradient(context, getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0xFF000000, color);
    }

    @Override
    protected void renderSlider(DrawContext context, int mouseX, int mouseY, float delta) {
        boolean highlighted = (this.checkHovered(mouseX, mouseY) || this.dragging);
        int posX = this.getX() + (int)(pos * (length - barLength));
        int posY = this.getY();
        context.fill(posX, posY, posX + 1, posY + 9, highlighted ? 0xFFFFFFFF : 0xFFA0A0A0);
        context.fill(posX + 1, posY + 1, posX + 2, posY + 10, 0xFF000000);
    }

    private void fillHorizontalGradient(DrawContext context, int startX, int startY, int endX, int endY, int colorStart, int colorEnd) {
        VertexConsumer vertexConsumer = context.getVertexConsumers().getBuffer(RenderLayer.getGui());
        float startA = (float) ColorHelper.Argb.getAlpha(colorStart) / 255.0F;
        float startR = (float) ColorHelper.Argb.getRed(colorStart) / 255.0F;
        float startG = (float) ColorHelper.Argb.getGreen(colorStart) / 255.0F;
        float startB = (float) ColorHelper.Argb.getBlue(colorStart) / 255.0F;
        float endA = (float) ColorHelper.Argb.getAlpha(colorEnd) / 255.0F;
        float endR = (float) ColorHelper.Argb.getRed(colorEnd) / 255.0F;
        float endG = (float) ColorHelper.Argb.getGreen(colorEnd) / 255.0F;
        float endB = (float) ColorHelper.Argb.getBlue(colorEnd) / 255.0F;
        Matrix4f matrix4f = context.getMatrices().peek().getPositionMatrix();
        vertexConsumer.vertex(matrix4f, (float)startX, (float)startY, 0.0f).color(startR, startG, startB, startA).next();
        vertexConsumer.vertex(matrix4f, (float)startX, (float)endY, 0.0f).color(startR, startG, startB, startA).next();
        vertexConsumer.vertex(matrix4f, (float)endX, (float)endY, 0.0f).color(endR, endG, endB, endA).next();
        vertexConsumer.vertex(matrix4f, (float)endX, (float)startY, 0.0f).color(endR, endG, endB, endA).next();
    }

    @Override
    protected boolean checkHovered(double mouseX, double mouseY){
        if (!visible) return false;
        return mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + width && mouseY < this.getY() + this.height;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isValidClickButton(button) && this.checkHovered(mouseX, mouseY)) {
            this.playDownSound(MinecraftClient.getInstance().getSoundManager());
            this.onClick(mouseX, mouseY);
            return true;
        }
        return false;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (!this.visible) {
            return;
        }
        pos = Math.min(Math.max((mouseX - getX())/width, 0), 1);
        if ((changedListener != null)){
            changedListener.accept(pos);
        }
        dragging = true;
        prevMouseX = mouseX;
        prevMouseY = mouseY;
    }

    @Override
    public void onDrag(double mouseX, double mouseY, double distX, double distY){
        if(dragging) {
            double posBefore = pos;
            pos = Math.min(Math.max(pos + distX/(length-barLength), 0), 1);
            if ((changedListener != null) && (Math.abs(posBefore-pos) > 0.0d)){
                changedListener.accept(pos);
            }
        }
    }
}
