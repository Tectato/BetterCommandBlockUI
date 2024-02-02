package bettercommandblockui.main.ui;

import bettercommandblockui.main.BetterCommandBlockUI;
import bettercommandblockui.main.ui.screen.AbstractBetterCommandBlockScreen;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.LinkedList;
import java.util.List;

import static bettercommandblockui.main.BetterCommandBlockUI.*;

public class SideWindow implements Drawable, Element {
    protected static final ButtonTextures COPY_BUTTON_TEXTURES = new ButtonTextures(
            new Identifier("bettercommandblockui:button_copy_enabled"),
            new Identifier("bettercommandblockui:button_copy_disabled"),
            new Identifier("bettercommandblockui:button_copy_focused")
    );

    int x, y, width, height;
    int topMargin = 5;
    int leftMargin = 5;
    boolean visible = false;

    MultiLineTextFieldWidget commandField;
    Screen screen;

    String piFractionInputText = "2π / ";
    TextFieldWidget piFractionInput;
    NotchedSlider piSlider;
    TexturedButtonWidget piCopyButton;
    ButtonWidget configButton;
    TextRenderer textRenderer;

    List<ClickableWidget> widgets;
    List<Element> elements;

    public SideWindow(int x, int y, int width, int height, MultiLineTextFieldWidget commandField, Screen screen){
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        this.commandField = commandField;
        this.screen = screen;

        this.widgets = new LinkedList<>();

        int posY = y + topMargin;
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        this.textRenderer = minecraftClient.textRenderer;
        this.piFractionInput = (TextFieldWidget) addWidget(
                new TextFieldWidget(
                        textRenderer,
                        x + leftMargin + textRenderer.getWidth(piFractionInputText),
                        posY,
                        60,
                        10,
                        Text.of("")
                )
        );
        this.piFractionInput.setChangedListener((input)->{
            try {
                int inputInt = Math.min(Math.max(Integer.parseInt(input),1),16);
                this.piSlider.setSubdivisions(inputInt);
            } catch (NumberFormatException e){
                this.piSlider.setSubdivisions(2);
            }
        });

        posY += 12;
        this.piSlider = (NotchedSlider) addWidget(new NotchedSlider(x + leftMargin, posY, width - 30, 16, Text.of("")));
        this.piFractionInput.setText(String.valueOf(this.piSlider.getSubdivisions()));
        this.piCopyButton = (TexturedButtonWidget) addWidget(new TexturedButtonWidget(
                x + leftMargin + width - 25,
                posY - 2,
                20,
                20,
                COPY_BUTTON_TEXTURES,
                (button)->{
                    MinecraftClient.getInstance().keyboard.setClipboard(String.valueOf(piSlider.getValue() * 2 * Math.PI));
                }
        ));

        posY += 20;
        this.configButton = (ButtonWidget) addWidget(ButtonWidget
                .builder(Text.literal("Config"), button -> ((AbstractBetterCommandBlockScreen)screen).openConfig())
                .dimensions(x+leftMargin, posY, width - leftMargin*2, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if(!visible) return;
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        context.fill(x, y, x+width, y+height, 0xB0000000);

        context.drawBorder(x-1, y-1, width+4, height+2, 0xFFFFFFFF);

        context.drawTextWithShadow(this.textRenderer, "2π / ", x + leftMargin, piFractionInput.getY(), 0xFFFFFFFF);
        this.piFractionInput.render(context, mouseX, mouseY, delta);
        this.piSlider.render(context, mouseX, mouseY, delta);
        this.piCopyButton.render(context, mouseX, mouseY, delta);
        this.configButton.render(context, mouseX, mouseY, delta);
    }

    Widget addWidget(ClickableWidget widget){
        widgets.add(widget);
        return widget;
    }

    public void setVisible(boolean value){
        this.visible = value;
        this.piFractionInput.setFocused(value);
    }

    @Override
    public void setFocused(boolean focused) {
        if(!focused){
            this.piFractionInput.setFocused(false);
            this.configButton.setFocused(false);
        }
    }

    @Override
    public boolean isFocused() {
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(!visible) return false;
        boolean widgetClicked = false;
        for(ClickableWidget w : widgets){
            if(!widgetClicked && w.mouseClicked(mouseX, mouseY, button)){
                w.setFocused(true);
                widgetClicked = true;
            } else {
                w.setFocused(false);
            }
        }
        if(widgetClicked) return true;
        if(mouseX > x && mouseY > y && mouseY < y + height) return true;
        setFocused(false);
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if(!visible) return false;
        if(piFractionInput.mouseReleased(mouseX,mouseY,button)) return true;
        return piSlider.mouseReleased(mouseX,mouseY,button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY){
        if(!visible) return false;
        return piSlider.mouseDragged(mouseX,mouseY,button,deltaX,deltaY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers){
        if(!visible) return false;
        for(ClickableWidget w : widgets){
            if(w.keyPressed(keyCode,scanCode,modifiers)) return true;
        }
        return false;
    }

    @Override
    public boolean charTyped(char c, int modifiers){
        if(!visible) return false;
        for(ClickableWidget w : widgets){
            if(w.charTyped(c,modifiers)) return true;
        }
        return false;
    }
}
