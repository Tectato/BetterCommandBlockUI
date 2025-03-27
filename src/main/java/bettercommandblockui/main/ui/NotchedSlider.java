package bettercommandblockui.main.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import static bettercommandblockui.main.BetterCommandBlockUI.SLIDER;
import static bettercommandblockui.main.BetterCommandBlockUI.SLIDER_NOTCH;
import static bettercommandblockui.main.BetterCommandBlockUI.SLIDER_PICK;

public class NotchedSlider extends ClickableWidget {
    protected int subdivisions = 4;
    protected double pos = 0.0d;
    protected boolean dragging = false;
    protected int length;
    protected double prevMouseX = 0.0d;
    protected double prevMouseY = 0.0d;

    protected java.util.function.Consumer<Double> changedListener;

    public NotchedSlider(int x, int y, int width, int height, Text message) {
        super(x, y, width, height, message);
        this.length = width;
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        //RenderSystem.setShaderTexture(0, SLIDER);
        context.drawGuiTexture(
                RenderLayer::getGuiTextured,
                SLIDER,
                512,
                16,
                0,
                0,
                getX()-2,
                getY(),
                4,
                16
        );
        context.drawGuiTexture(
                RenderLayer::getGuiTextured,
                SLIDER,
                512,
                16,
                4,
                0,
                getX()+2,
                getY(),
                getWidth()-4,
                16
        );
        context.drawGuiTexture(
                RenderLayer::getGuiTextured,
                SLIDER,
                512,
                16,
                508,
                0,
                getX()+getWidth()-2,
                getY(),
                4,
                16
        );

        //RenderSystem.setShaderTexture(0, SLIDER_NOTCH);
        float step = 1.0f/((float)subdivisions);
        for(int i=1; i<subdivisions; i++){
            context.drawGuiTexture(
                    RenderLayer::getGuiTextured,
                    SLIDER_NOTCH,
                    4,
                    16,
                    0,
                    0,
                    (int) (getX() + (i * step * getWidth())) - 2,
                    getY(),
                    4,
                    16
            );
        }

        //RenderSystem.setShaderTexture(0, SLIDER_PICK);
        context.drawGuiTexture(
                RenderLayer::getGuiTextured,
                SLIDER_PICK.get(true, hovered),
                (int) (getX() + (pos * getWidth()) - 4),
                getY(),
                8,
                16
        );
    }

    public void setChangedListener(java.util.function.Consumer<Double> changedListener){
        this.changedListener = changedListener;
    }

    public double getValue(){
        return pos;
    }

    public int getSubdivisions(){
        return subdivisions;
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }

    public void setPos(double value){
        pos = snap(Math.min(Math.max(value, 0.0d), 1.0d), 1.0d/subdivisions);
    }

    /*@Override
    protected boolean clicked(double mouseX, double mouseY){
        if (!this.visible) {
            return false;
        }
        return checkHovered(mouseX, mouseY);
    }*/

    private boolean checkHovered(double mouseX, double mouseY){
        return mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + length && mouseY < this.getY() + this.height;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isValidClickButton(button) && checkHovered(mouseX, mouseY) && this.visible) {
            this.dragging = true;
            this.playDownSound(MinecraftClient.getInstance().getSoundManager());
            this.onClick(mouseX, mouseY);
            return true;
        }
        this.dragging = false;
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.isValidClickButton(button)) {
            this.dragging = false;
            this.onRelease(mouseX, mouseY);
            return true;
        }
        return false;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (!this.visible || !checkHovered(mouseX, mouseY)) {
            dragging = false;
            return;
        }
        dragging = true;
        prevMouseX = mouseX;
        prevMouseY = mouseY;

        pos = snap(Math.min(Math.max((mouseX - getX()) / length, 0.0d), 1.0d), 1.0d/subdivisions);
        if (changedListener != null){
            changedListener.accept(pos);
        }
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        if (!this.visible) {
            return;
        }
        dragging = false;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        super.mouseMoved(mouseX, mouseY);
        if(dragging){
            double distX = mouseX - prevMouseX;
            double distY = mouseY - prevMouseY;
            prevMouseX = mouseX;
            prevMouseY = mouseY;

            onDrag(mouseX, mouseY, distX, distY);
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY){
        if(dragging) {
            onDrag(mouseX, mouseY, deltaX, deltaY);
            return true;
        }
        return false;
    }

    @Override
    public void onDrag(double mouseX, double mouseY, double distX, double distY){
        if(dragging) {
            double posBefore = pos;
            pos = snap(Math.min(Math.max((mouseX - getX()) / length, 0.0d), 1.0d), 1.0d/subdivisions);

            if (changedListener != null && Math.abs(posBefore) > 0.0){
                changedListener.accept(pos);
            }
        }
    }

    public void setSubdivisions(int value){
        subdivisions = value;
    }

    double snap(double x, double step){
        return Math.round(x / step)*step;
    }
}
