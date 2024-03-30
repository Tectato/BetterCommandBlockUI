package bettercommandblockui.main.ui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;
import org.joml.AxisAngle4d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

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
    protected void renderFrame(MatrixStack matrices){
        matrices.push();
        matrices.translate(getX(), getY(), 0);
        matrices.multiply(new Quaternionf(new AxisAngle4d(-0.5f * Math.PI, 0, 0, 1)));
        DrawableHelper.fillGradient(matrices, -getHeight(), 0, 0, getWidth(), 0xFF000000, color);
        matrices.pop();
    }

    @Override
    protected void renderSlider(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        matrices.push();
        matrices.translate(0,0,1f);
        boolean highlighted = (this.checkHovered(mouseX, mouseY) || this.dragging);
        int posX = this.getX() + (int)(pos * (length - barLength));
        int posY = this.getY();
        DrawableHelper.fill(matrices, posX, posY, posX + 1, posY + 9, highlighted ? 0xFFFFFFFF : 0xFFA0A0A0);
        DrawableHelper.fill(matrices, posX + 1, posY + 1, posX + 2, posY + 10, 0xFF000000);
        matrices.pop();
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
