package bettercommandblockui.main.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
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
    protected int frameRepeatLength;
    protected int barRepeatLength;
    protected final int textureLength = 256;
    protected java.util.function.Consumer<Double> changedListener;

    public ScrollbarWidget(int x, int y, int width, int height, Text message, boolean horizontal) {
        super(x, y, width, height, message);
        this.horizontal = horizontal;
        this.scale = 1.0d;
        this.length = horizontal?width:height;
        this.barLength =  (int) ((double)length / scale);
        this.frameRepeatLength = length - textureLength;
        this.barRepeatLength = barLength - textureLength;
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
        renderLongBox(context, false, false, 0, horizontal ? width : height, frameRepeatLength);
    }

    protected void renderSlider(DrawContext context, int mouseX, int mouseY, float delta) {
        renderLongBox(context, true, hovered, (int)(pos * (length - barLength)), barLength, barRepeatLength);
    }

    protected void renderLongBox(DrawContext context, boolean enabled, boolean hovered, int position, int boxLength, int repeatLength){
        if(horizontal){
            Identifier textures = SCROLLBAR_HORIZONTAL.get(enabled,hovered);
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, textures, textureLength, 10, 0, 0, this.getX() + position, this.getY(), Math.min(boxLength / 2, textureLength / 2), this.height);
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, textures, textureLength, 10, Math.max(textureLength/2, textureLength - boxLength / 2), 0, Math.max(this.getX() + position + boxLength/2, this.getX() + position + boxLength - textureLength/2), this.getY(), Math.min(boxLength / 2, textureLength / 2), this.height);
            int drawX = this.getX() + position + textureLength/2;
            for (int i=0; i<(repeatLength/(textureLength/2))+1; i++){
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, textures, textureLength, 10, textureLength/4, 0, drawX, this.getY(), Math.min((repeatLength - i*textureLength/2), textureLength/2), this.height);
                drawX += textureLength/2;
            }
        } else {
            Identifier textures = SCROLLBAR_VERTICAL.get(enabled,hovered);
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, textures, 10 , textureLength, 0, 0, this.getX(), this.getY() + position,  this.width, Math.min(boxLength / 2, textureLength / 2));
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, textures, 10 , textureLength, 0, Math.max(textureLength/2, textureLength - boxLength / 2), this.getX(), Math.max(this.getY() + position + boxLength/2, this.getY() + position + boxLength - textureLength/2), this.width, Math.min(boxLength / 2, textureLength / 2));
            int drawY = this.getY() + textureLength/2;
            for (int i=0; i<(repeatLength/(textureLength/2))+1; i++){
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, textures, 10, textureLength, 0, textureLength/4, this.getX(), drawY, this.width, Math.min((repeatLength - i*textureLength/2), textureLength/2));
                drawY += textureLength/2;
            }
        }
    }

    public void setChangedListener(java.util.function.Consumer<Double> changedListener){
        this.changedListener = changedListener;
    }

    /*@Override
    protected boolean clicked(double mouseX, double mouseY){
        if (!this.visible) {
            return false;
        }
        return checkHovered(mouseX, mouseY);
    }*/

    protected boolean checkHovered(double mouseX, double mouseY){
        if(horizontal){
            return mouseX >= this.getX() + pos * (length-barLength) && mouseY >= this.getY() && mouseX < this.getX() + pos * (length-barLength) + barLength && mouseY < this.getY() + this.height;
        } else {
            return mouseX >= this.getX() && mouseY >= this.getY() + pos * (length-barLength) && mouseX < this.getX() + this.width && mouseY < this.getY() + pos * (length-barLength) + barLength;
        }
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
        dragging = true;
        prevMouseX = click.x();
        prevMouseY = click.y();
    }

    @Override
    public void onRelease(Click click) {
        if (!this.visible) {
            return;
        }
        dragging = false;
    }

    @Override
    public void onDrag(Click click, double distX, double distY){
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
        this.barRepeatLength = barLength - textureLength;
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
    public boolean charTyped(CharInput input) {
        return super.charTyped(input);
    }

    @Override
    public void appendClickableNarrations(NarrationMessageBuilder builder) {

    }
}
