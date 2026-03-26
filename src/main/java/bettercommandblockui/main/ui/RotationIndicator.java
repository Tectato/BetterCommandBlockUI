package bettercommandblockui.main.ui;

import bettercommandblockui.main.BetterCommandBlockUI;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.network.chat.Component;
import org.joml.AxisAngle4d;
import org.joml.Quaternionf;
import org.joml.Vector2d;

public class RotationIndicator extends AbstractWidget {
    private boolean dragging = false;
    private double angle = -1.0d;
    private Vector2d midPos;
    private java.util.function.Consumer<Double> changedListener;

    public RotationIndicator(int x, int y, Component message) {
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
    protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
        context.blitSprite(RenderPipelines.GUI_TEXTURED, BetterCommandBlockUI.COMPASS_FRAME, getX(), getY(), 32, 32);
        context.pose().pushMatrix();
        context.pose().translate(getX() + 16, getY() + 16);
        context.pose().rotate((float) ((angle + 1) * Math.PI));
        context.blitSprite(RenderPipelines.GUI_TEXTURED, BetterCommandBlockUI.COMPASS_NEEDLE, -16, -16, 32, 32);
        context.pose().popMatrix();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {}

    @Override
    public void onClick(MouseButtonEvent click, boolean doubled) {
        if (this.isValidClickButton(click.buttonInfo()) && this.clicked(click.x(), click.y())) {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            dragging = true;
            setAngleFromMousePos(click.x(), click.y());
        }
    }

    @Override
    public void onRelease(MouseButtonEvent click) {
        if (this.isValidClickButton(click.buttonInfo())) {
            dragging = false;
        }
    }

    public boolean clicked(double mouseX, double mouseY){
        return midPos.distance(new Vector2d(mouseX, mouseY)) <= getWidth() / 2.0;
    }

    @Override
    public void onDrag(MouseButtonEvent click, double distX, double distY){
        if(dragging) {
            setAngleFromMousePos(click.x(), click.y());
        }
    }

    private void setAngleFromMousePos(double mouseX, double mouseY){
        Vector2d toMouse = new Vector2d(mouseX, mouseY).sub(midPos);
        angle = new Vector2d(0.0,1.0).angle(toMouse) / Math.PI;
        changedListener.accept(getAngle());
    }
}
