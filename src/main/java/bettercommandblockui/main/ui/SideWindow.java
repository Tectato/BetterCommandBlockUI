package bettercommandblockui.main.ui;

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
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.LinkedList;
import java.util.List;

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
    TextFieldWidget piFractionInput, colorTextR, colorTextG, colorTextB, colorHex, colorInt;
    ColorScrollbarWidget colorSliderR, colorSliderG, colorSliderB;
    NotchedSlider piSlider;
    TexturedButtonWidget piCopyButton;
    ButtonWidget configButton;
    TextRenderer textRenderer;

    List<ClickableWidget> widgets;
    int focusedWidget = -1;

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

        posY += 32;
        this.colorTextR = (TextFieldWidget) addWidget(
                new TextFieldWidget(
                        textRenderer,
                        x + leftMargin + textRenderer.getWidth("R:"),
                        posY,
                        30,
                        10,
                        Text.of("")
                )
        );
        this.colorSliderR = (ColorScrollbarWidget) addWidget(
                new ColorScrollbarWidget(
                        x + leftMargin + 50,
                        posY,
                        64,
                        10,
                        Text.of(""),
                        this.colorTextR,
                        ColorPicker.COLOR.RED
                )
        );
        this.colorSliderR.setChangedListener((pos)->{
            updateColorOutputs();
        });
        posY += 14;
        this.colorTextG = (TextFieldWidget) addWidget(
                new TextFieldWidget(
                        textRenderer,
                        x + leftMargin + textRenderer.getWidth("R:"),
                        posY,
                        30,
                        10,
                        Text.of("")
                )
        );
        this.colorSliderG = (ColorScrollbarWidget) addWidget(
                new ColorScrollbarWidget(
                        x + leftMargin + 50,
                        posY,
                        64,
                        10,
                        Text.of(""),
                        this.colorTextG,
                        ColorPicker.COLOR.GREEN
                )
        );
        this.colorSliderG.setChangedListener((pos)->{
            updateColorOutputs();
        });
        posY += 14;
        this.colorTextB = (TextFieldWidget) addWidget(
                new TextFieldWidget(
                        textRenderer,
                        x + leftMargin + textRenderer.getWidth("R:"),
                        posY,
                        30,
                        10,
                        Text.of("")
                )
        );
        this.colorSliderB = (ColorScrollbarWidget) addWidget(
                new ColorScrollbarWidget(
                        x + leftMargin + 50,
                        posY,
                        64,
                        10,
                        Text.of(""),
                        this.colorTextB,
                        ColorPicker.COLOR.BLUE
                )
        );
        this.colorSliderB.setChangedListener((pos)->{
            updateColorOutputs();
        });
        posY += 16;
        this.colorHex = (TextFieldWidget) addWidget(
                new TextFieldWidget(
                        textRenderer,
                        x + leftMargin + textRenderer.getWidth("R:"),
                        posY,
                        60,
                        10,
                        Text.of("")
                )
        );
        this.colorHex.setEditable(false);
        posY += 14;
        this.colorInt = (TextFieldWidget) addWidget(
                new TextFieldWidget(
                        textRenderer,
                        x + leftMargin + textRenderer.getWidth("R:"),
                        posY,
                        60,
                        10,
                        Text.of("")
                )
        );
        this.colorInt.setEditable(false);

        posY += 20;
        this.configButton = (ButtonWidget) addWidget(ButtonWidget
                .builder(Text.literal("Config"), button -> ((AbstractBetterCommandBlockScreen)screen).openConfig())
                .dimensions(x+leftMargin, posY, width - leftMargin*2, 20).build());
    }

    public void updateColorOutputs(){
        colorHex.setText(""+ColorPicker.getInteger());
        colorInt.setText("#"+ColorPicker.getHexString().toUpperCase());
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
        context.drawTextWithShadow(this.textRenderer, "R:", x + leftMargin, colorTextR.getY(), 0xFFFF0000);
        this.colorTextR.render(context, mouseX, mouseY, delta);
        this.colorSliderR.render(context, mouseX, mouseY, delta);
        context.drawTextWithShadow(this.textRenderer, "G:", x + leftMargin, colorTextG.getY(), 0xFF00FF00);
        this.colorTextG.render(context, mouseX, mouseY, delta);
        this.colorSliderG.render(context, mouseX, mouseY, delta);
        context.drawTextWithShadow(this.textRenderer, "B:", x + leftMargin, colorTextB.getY(), 0xFF0000FF);
        this.colorTextB.render(context, mouseX, mouseY, delta);
        this.colorSliderB.render(context, mouseX, mouseY, delta);
        context.fill(x + leftMargin, colorHex.getY(), x + leftMargin + 7, colorHex.getY() + 26, 0xFF000000 | ColorPicker.getInteger());
        this.colorHex.render(context, mouseX, mouseY, delta);
        this.colorInt.render(context, mouseX, mouseY, delta);
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
        for(ClickableWidget w : widgets){
            if (w.isFocused()) return true;
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(!visible) return false;
        boolean widgetClicked = false;
        int index = 0;
        for(ClickableWidget w : widgets){
            if(!widgetClicked && w.mouseClicked(mouseX, mouseY, button)){
                w.setFocused(true);
                widgetClicked = true;
                focusedWidget = index;
            } else {
                w.setFocused(false);
            }
            index++;
        }
        boolean returnVal = widgetClicked;
        if(mouseX > x && mouseY > y && mouseY < y + height) returnVal = true;
        if (returnVal){
            ((AbstractBetterCommandBlockScreen)screen).sideWindowFocused();
            return returnVal;
        }
        setFocused(false);
        System.out.println("Not clicked");
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if(!visible) return false;
        for(ClickableWidget w : widgets){
            if(w.mouseReleased(mouseX,mouseY,button)) return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY){
        if(!visible) return false;
        for(ClickableWidget w : widgets){ // TODO: Sliders aren't sliding
            if(w.mouseDragged(mouseX,mouseY,button,deltaX,deltaY)) return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers){
        if(!visible) return false;
        for(ClickableWidget w : widgets){
            if(w.keyPressed(keyCode,scanCode,modifiers)) return true;
        }
        if (keyCode == 258) {
            focusedWidget += Screen.hasShiftDown() ? -1 : 1;
            focusedWidget %= widgets.size();
            if (focusedWidget < 0) focusedWidget = widgets.size() + focusedWidget;

            int index = 0;
            for(ClickableWidget w : widgets){
                w.setFocused(index == focusedWidget);
                index++;
            }
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
