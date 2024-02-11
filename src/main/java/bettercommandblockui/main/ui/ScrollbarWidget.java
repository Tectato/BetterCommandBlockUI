package bettercommandblockui.main.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import static bettercommandblockui.main.BetterCommandBlockUI.SCROLLBAR_HORIZONTAL;
import static bettercommandblockui.main.BetterCommandBlockUI.SCROLLBAR_VERTICAL;

public class ScrollbarWidget extends ClickableWidget {
    protected boolean dragging = false;
    protected boolean horizontal = false;
    protected double prevMouseX = 0.0d;
    protected double prevMouseY = 0.0d;
    protected double pos = 0.0d;
    protected double scale;
    protected int length;
    protected int barLength;
    protected java.util.function.Consumer<Double> changedListener;

    public ScrollbarWidget(int x, int y, int width, int height, Text message, boolean horizontal) {
        super(x, y, width, height, message);
        this.horizontal = horizontal;
        this.scale = 1.0d;
        this.length = horizontal?width:height;
        this.barLength =  (int) ((double)length / scale);
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!this.visible) {
            return;
        }
        this.hovered = checkHovered(mouseX, mouseY);

        this.renderFrame(context);
        this.renderSlider(context, mouseX, mouseY, delta);
    }

    protected void renderFrame(DrawContext context){
        if(horizontal){
            RenderSystem.setShaderTexture(0, SCROLLBAR_HORIZONTAL);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

            context.drawTexture(SCROLLBAR_HORIZONTAL, this.getX(), this.getY(), 0, 0, 0, this.width / 2, this.height, 256, 30);
            context.drawTexture(SCROLLBAR_HORIZONTAL, this.getX() + this.width / 2, this.getY(), 0, 256 - this.width / 2, 0, this.width / 2, this.height, 256, 30);
        } else {
            RenderSystem.setShaderTexture(0, SCROLLBAR_VERTICAL);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

            context.drawTexture(SCROLLBAR_VERTICAL, this.getX(), this.getY(), 0, 0, 0, this.width, this.height / 2, 30 , 256);
            context.drawTexture(SCROLLBAR_VERTICAL, this.getX(), this.getY() + height / 2, 0, 0, 256 - this.height / 2, this.width, this.height / 2, 30 , 256);
        }
    }

    protected void renderSlider(DrawContext context, int mouseX, int mouseY, float delta) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        int i = (this.hovered || this.dragging)?1:0;

        if(horizontal){
            RenderSystem.setShaderTexture(0, SCROLLBAR_HORIZONTAL);

            context.drawTexture(SCROLLBAR_HORIZONTAL, this.getX() + (int)(pos * (length - barLength)), this.getY(), 0, 0, 10 + i * 10, barLength / 2, this.height, 256, 30);
            context.drawTexture(SCROLLBAR_HORIZONTAL, this.getX() + (int)(pos * (length - barLength)) +  barLength / 2, this.getY(), 0, 256 - barLength / 2, 10 + i * 10,  barLength / 2, this.height, 256, 30);
        } else {
            RenderSystem.setShaderTexture(0, SCROLLBAR_VERTICAL);

            context.drawTexture(SCROLLBAR_VERTICAL, this.getX(), this.getY() + (int)(pos * (length - barLength)), 0, 10 + i * 10, 0, this.width, barLength / 2, 30, 256);
            context.drawTexture(SCROLLBAR_VERTICAL, this.getX(), this.getY() + (int)(pos * (length - barLength)) +  barLength / 2, 0, 10 + i * 10, 256 - barLength / 2,  this.width, barLength / 2, 30, 256);
        }
    }

    public void setChangedListener(java.util.function.Consumer<Double> changedListener){
        this.changedListener = changedListener;
    }

    @Override
    protected boolean clicked(double mouseX, double mouseY){
        if (!this.visible) {
            return false;
        }
        return checkHovered(mouseX, mouseY);
    }

    protected boolean checkHovered(double mouseX, double mouseY){
        if(horizontal){
            return mouseX >= this.getX() + pos * (length-barLength) && mouseY >= this.getY() && mouseX < this.getX() + pos * (length-barLength) + barLength && mouseY < this.getY() + this.height;
        } else {
            return mouseX >= this.getX() && mouseY >= this.getY() + pos * (length-barLength) && mouseX < this.getX() + this.width && mouseY < this.getY() + pos * (length-barLength) + barLength;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isValidClickButton(button) && this.clicked(mouseX, mouseY)) {
            this.playDownSound(MinecraftClient.getInstance().getSoundManager());
            this.onClick(mouseX, mouseY);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.isValidClickButton(button)) {
            this.onRelease(mouseX, mouseY);
            return true;
        }
        return false;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (!this.visible) {
            return;
        }
        dragging = true;
        prevMouseX = mouseX;
        prevMouseY = mouseY;
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
    public void onDrag(double mouseX, double mouseY, double distX, double distY){
        if(dragging) {
            double posBefore = pos;
            if (horizontal) {
                pos = Math.min(Math.max(pos + distX/(length-barLength), 0), 1);
            } else {
                pos = Math.min(Math.max(pos + distY/(length-barLength), 0), 1);
            }
            if ((changedListener != null) && (Math.abs(posBefore-pos) > 0.0d)){
                changedListener.accept(pos);
            }
        }
    }

    public void setScale(double newScale){
        this.scale = Math.max(newScale,1);
        this.barLength = (int) ((double)length / Math.min(scale,8));
    }

    public void updatePos(double newPos){
        this.pos = Math.max(Math.min(newPos,1),0);
    }

    public double getPos(){
        return pos;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return super.charTyped(chr, modifiers);
    }

    @Override
    public void appendClickableNarrations(NarrationMessageBuilder builder) {

    }
}
