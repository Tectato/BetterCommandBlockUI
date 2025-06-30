package bettercommandblockui.main.ui;

import bettercommandblockui.main.BetterCommandBlockUI;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import org.joml.AxisAngle4d;
import org.joml.Quaternionf;
import org.joml.Vector2d;

public class RotationIndicator extends ClickableWidget {
    private boolean dragging = false;
    private double angle = -1.0d;
    private Vector2d midPos;
    private java.util.function.Consumer<Double> changedListener;

    public RotationIndicator(int x, int y, Text message) {
        super(x, y, 32, 32, message);
        midPos = new Vector2d(getX() + getWidth()/2.0d, getY() + getHeight()/2.0d);
    }

    public void setChangedListener(java.util.function.Consumer<Double> listener){
        changedListener = listener;
    }

    public void setAngle(double value){
        angle = (value-0.5) * 2.0;
    }

    public double getAngle(){
        return (angle + 1) / 2.0;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, BetterCommandBlockUI.COMPASS_FRAME, getX(), getY(), 32, 32);
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(getX() + 16, getY() + 16);
        context.getMatrices().rotate((float) ((angle + 1) * Math.PI));
        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, BetterCommandBlockUI.COMPASS_NEEDLE, -16, -16, 32, 32);
        context.getMatrices().popMatrix();
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {}

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isValidClickButton(button) && this.clicked(mouseX, mouseY)) {
            this.playDownSound(MinecraftClient.getInstance().getSoundManager());
            dragging = true;
            setAngleFromMousePos(mouseX, mouseY);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.isValidClickButton(button)) {
            dragging = false;
            return true;
        }
        return false;
    }

    public boolean clicked(double mouseX, double mouseY){
        return midPos.distance(new Vector2d(mouseX, mouseY)) <= getWidth() / 2.0;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY){
        if(dragging) {
            setAngleFromMousePos(mouseX, mouseY);
            return true;
        }
        return false;
    }

    private void setAngleFromMousePos(double mouseX, double mouseY){
        Vector2d toMouse = new Vector2d(mouseX, mouseY).sub(midPos);
        angle = new Vector2d(0.0,1.0).angle(toMouse) / Math.PI;
        changedListener.accept(getAngle());
    }
}
