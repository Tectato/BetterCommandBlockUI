package bettercommandblockui.main.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import static bettercommandblockui.main.BetterCommandBlockUI.SLIDER;
import static bettercommandblockui.main.BetterCommandBlockUI.SLIDER_NOTCH;
import static bettercommandblockui.main.BetterCommandBlockUI.SLIDER_PICK;

public class NotchedSlider extends ClickableWidget {
    static int subdivisions = 4;
    static double pos = 0.0d;
    boolean dragging = false;
    int length;
    double prevMouseX = 0.0d;
    double prevMouseY = 0.0d;

    public NotchedSlider(int x, int y, int width, int height, Text message) {
        super(x, y, width, height, message);
        this.length = width;
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        //RenderSystem.setShaderTexture(0, SLIDER);
        context.drawTexture(
                SLIDER,
                getX()-2,
                getY(),
                0,
                0,
                4,
                16,
                512,
                16
        );
        context.drawTexture(
                SLIDER,
                getX()+2,
                getY(),
                4,
                0,
                getWidth()-4,
                16,
                512,
                16
        );
        context.drawTexture(
                SLIDER,
                getX()+getWidth()-2,
                getY(),
                508,
                0,
                4,
                16,
                512,
                16
        );

        //RenderSystem.setShaderTexture(0, SLIDER_NOTCH);
        float step = 1.0f/((float)subdivisions);
        for(int i=1; i<subdivisions; i++){
            context.drawTexture(
                    SLIDER_NOTCH,
                    (int) (getX() + (i * step * getWidth())) - 2,
                    getY(),
                    0,
                    0,
                    4,
                    16,
                    4,
                    16
            );
        }

        //RenderSystem.setShaderTexture(0, SLIDER_PICK);
        context.drawTexture(
                SLIDER_PICK,
                (int) (getX() + (pos * getWidth()) - 4),
                getY(),
                0,
                hovered ? 16 : 0,
                8,
                16,
                8,
                32
        );
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

    @Override
    protected boolean clicked(double mouseX, double mouseY){
        if (!this.visible) {
            return false;
        }
        return checkHovered(mouseX, mouseY);
    }

    private boolean checkHovered(double mouseX, double mouseY){
        return mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + length && mouseY < this.getY() + this.height;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isValidClickButton(button) && this.clicked(mouseX, mouseY)) {
            this.dragging = true;
            this.playDownSound(MinecraftClient.getInstance().getSoundManager());
            this.onClick(mouseX, mouseY);
            return true;
        }
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
            return;
        }
        dragging = true;
        prevMouseX = mouseX;
        prevMouseY = mouseY;

        pos = snap(Math.min(Math.max((mouseX - getX()) / length, 0.0d), 1.0d), 1.0d/subdivisions);
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
            pos = snap(Math.min(Math.max((mouseX - getX()) / length, 0.0d), 1.0d), 1.0d / subdivisions);
            return true;
        }
        return false;
    }

    @Override
    public void onDrag(double mouseX, double mouseY, double distX, double distY){
        if(dragging) {
            pos = snap(Math.min(Math.max((mouseX - getX()) / length, 0.0d), 1.0d), 1.0d/subdivisions);
        }
    }

    public void setSubdivisions(int value){
        subdivisions = value;
    }

    double snap(double x, double step){
        return Math.round(x / step)*step;
    }
}
