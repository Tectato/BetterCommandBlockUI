package bettercommandblockui.main.ui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
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
    protected void renderFrame(DrawContext context){
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(getX() + getWidth()/2.0f, getY() + getHeight()/2.0f);
        context.getMatrices().rotate((float) (0.5f * Math.PI));
        context.fillGradient(-getHeight()/2, -getWidth()/2, getHeight()/2, getWidth()/2, color, 0xFF000000);
        context.getMatrices().popMatrix();
    }

    @Override
    protected void renderSlider(DrawContext context, int mouseX, int mouseY, float delta) {
        boolean highlighted = (this.checkHovered(mouseX, mouseY) || this.dragging);
        int posX = this.getX() + (int)(pos * (length - barLength));
        int posY = this.getY();
        context.fill(posX, posY, posX + 1, posY + 9, highlighted ? 0xFFFFFFFF : 0xFFA0A0A0);
        context.fill(posX + 1, posY + 1, posX + 2, posY + 10, 0xFF000000);
    }

    @Override
    protected boolean checkHovered(double mouseX, double mouseY){
        if (!visible) return false;
        return mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + width && mouseY < this.getY() + this.height;
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (this.isValidClickButton(click.buttonInfo()) && this.checkHovered(click.x(), click.y())) {
            this.playDownSound(MinecraftClient.getInstance().getSoundManager());
            this.onClick(click, doubled);
            return true;
        }
        return false;
    }

    @Override
    public void onClick(Click click, boolean doubled) {
        if (!this.visible) {
            return;
        }
        pos = Math.min(Math.max((click.x() - getX())/width, 0), 1);
        if ((changedListener != null)){
            changedListener.accept(pos);
        }
        dragging = true;
        prevMouseX = click.x();
        prevMouseY = click.y();
    }

    @Override
    public void onDrag(Click click, double distX, double distY){
        if(dragging) {
            double posBefore = pos;
            pos = Math.min(Math.max(pos + distX/(length-barLength), 0), 1);
            if ((changedListener != null) && (Math.abs(posBefore-pos) > 0.0d)){
                changedListener.accept(pos);
            }
        }
    }
}
