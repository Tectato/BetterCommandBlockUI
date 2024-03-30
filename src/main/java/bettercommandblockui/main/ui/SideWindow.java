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
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.AxisAngle4d;
import org.joml.Quaternionf;

import java.util.LinkedList;
import java.util.List;

public class SideWindow implements Drawable, Element {
    protected static final ButtonTextures COPY_BUTTON_TEXTURES = new ButtonTextures(
            new Identifier("bettercommandblockui:button_copy_enabled"),
            new Identifier("bettercommandblockui:button_copy_disabled"),
            new Identifier("bettercommandblockui:button_copy_focused")
    );

    private static int piFraction = 4;
    private static double piSetting = 0.0;

    int x, y, width, height;
    int topMargin = 5;
    int leftMargin = 5;
    boolean visible = false;

    MultiLineTextFieldWidget commandField;
    Screen screen;

    String piFractionInputText = "2π / ";
    TextFieldWidget piFractionInput, piOutput, colorTextR, colorTextG, colorTextB, colorHex, colorInt;
    ColorScrollbarWidget colorSliderR, colorSliderG, colorSliderB;
    NotchedSlider piSlider;
    RotationIndicator piRotationIndicator;
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
                piFraction = Math.min(Math.max(Integer.parseInt(input),1),16);
                this.piSlider.setSubdivisions(piFraction);
            } catch (NumberFormatException e){
                this.piSlider.setSubdivisions(4);
            }
        });

        posY += 12;
        this.piSlider = (NotchedSlider) addWidget(new NotchedSlider(x + leftMargin, posY, width - 30, 16, Text.of("")));
        this.piSlider.setSubdivisions(piFraction);
        this.piSlider.setPos(piSetting);
        this.piFractionInput.setText(String.valueOf(piFraction));
        /*this.piCopyButton = (TexturedButtonWidget) addWidget(new TexturedButtonWidget(
                x + leftMargin + width - 25,
                posY - 2,
                20,
                20,
                COPY_BUTTON_TEXTURES,
                (button)->{
                    MinecraftClient.getInstance().keyboard.setClipboard(String.valueOf(piSlider.getValue() * 2 * Math.PI));
                }
        ));*/
        posY += 18;
        this.piOutput = (TextFieldWidget) addWidget(
                new OutputTextFieldWidget(
                        textRenderer,
                        x + leftMargin,
                        posY,
                        60,
                        10,
                        Text.of("")
                )
        );
        this.piOutput.setEditable(false);
        String piOutputText = Double.toString(piSetting * 2*Math.PI);
        this.piOutput.setText(piOutputText.substring(0, Math.min(8,piOutputText.length())));
        this.piSlider.setChangedListener((value)->{
            piSetting = value;
            this.piRotationIndicator.setAngle(value);
            String text = Double.toString(piSetting * 2*Math.PI);
            this.piOutput.setText(text.substring(0, Math.min(8,text.length())));
        });

        this.piRotationIndicator = (RotationIndicator) addWidget(
                new RotationIndicator(
                        piOutput.getX() + piOutput.getWidth() + 8,
                        piOutput.getY() - 2,
                        Text.of("")
                )
        );
        this.piRotationIndicator.setChangedListener((value)->{
            this.piSlider.setPos(value);
            piSetting = value;
            String text = Double.toString(piSetting * 2*Math.PI);
            this.piOutput.setText(text.substring(0, Math.min(8,text.length())));
        });
        this.piRotationIndicator.setAngle(piSetting);

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
        this.colorTextR.setMaxLength(3);
        this.colorTextR.setChangedListener((input)->{
            int value = 0;
            try {
                value = Integer.parseInt(input);
            } catch (NumberFormatException ignored){}
            ColorPicker.setColor(ColorPicker.COLOR.RED, value);
            this.colorSliderR.updatePos(value/255.0);
        });
        this.colorSliderR = (ColorScrollbarWidget) addWidget(
                new ColorScrollbarWidget(
                        x + leftMargin + 40,
                        posY,
                        48,
                        10,
                        Text.of(""),
                        ColorPicker.COLOR.RED
                )
        );
        this.colorSliderR.setChangedListener((pos)->{
            this.colorTextR.setText(""+((int)(Math.round(pos * 255.0))));
            this.updateColorOutputs();
        });
        this.colorTextR.setText("" + ColorPicker.getColor(ColorPicker.COLOR.RED));

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
        this.colorTextG.setMaxLength(3);
        this.colorTextG.setChangedListener((input)->{
            int value = 0;
            try {
                value = Integer.parseInt(input);
            } catch (NumberFormatException ignored){}
            ColorPicker.setColor(ColorPicker.COLOR.GREEN, value);
            this.colorSliderG.updatePos(value/255.0);
        });
        this.colorSliderG = (ColorScrollbarWidget) addWidget(
                new ColorScrollbarWidget(
                        x + leftMargin + 40,
                        posY,
                        48,
                        10,
                        Text.of(""),
                        ColorPicker.COLOR.GREEN
                )
        );
        this.colorSliderG.setChangedListener((pos)->{
            this.colorTextG.setText(""+((int)(Math.round(pos * 255.0))));
            this.updateColorOutputs();
        });
        this.colorTextG.setText("" + ColorPicker.getColor(ColorPicker.COLOR.GREEN));

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
        this.colorTextB.setMaxLength(3);
        this.colorTextB.setChangedListener((input)->{
            int value = 0;
            try {
                value = Integer.parseInt(input);
            } catch (NumberFormatException ignored){}
            ColorPicker.setColor(ColorPicker.COLOR.BLUE, value);
            this.colorSliderB.updatePos(value/255.0);
        });
        this.colorSliderB = (ColorScrollbarWidget) addWidget(
                new ColorScrollbarWidget(
                        x + leftMargin + 40,
                        posY,
                        48,
                        10,
                        Text.of(""),
                        ColorPicker.COLOR.BLUE
                )
        );
        this.colorSliderB.setChangedListener((pos)->{
            this.colorTextB.setText(""+((int)(Math.round(pos * 255.0))));
            this.updateColorOutputs();
        });
        this.colorTextB.setText("" + ColorPicker.getColor(ColorPicker.COLOR.BLUE));

        posY += 16;
        this.colorHex = (TextFieldWidget) addWidget(
                new OutputTextFieldWidget(
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
                new OutputTextFieldWidget(
                        textRenderer,
                        x + leftMargin + textRenderer.getWidth("R:"),
                        posY,
                        60,
                        10,
                        Text.of("")
                )
        );
        this.colorInt.setEditable(false);
        updateColorOutputs();

        posY += 20;
        this.configButton = (ButtonWidget) addWidget(ButtonWidget
                .builder(Text.translatable("bcbui.config"), button -> ((AbstractBetterCommandBlockScreen)screen).openConfig())
                .dimensions(x+leftMargin, posY, width - leftMargin*2, 20).build());

        this.height = (posY - y) + 20 + topMargin;
    }

    public void updateColorOutputs(){
        colorHex.setText(""+ColorPicker.getInteger());
        String hexText = ColorPicker.getHexString().toUpperCase();
        colorInt.setText("#"+"0".repeat(6-hexText.length())+hexText);
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
        this.piOutput.render(context, mouseX, mouseY, delta);
        this.piRotationIndicator.render(context, mouseX, mouseY, delta);
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
        context.fill(x + leftMargin, colorHex.getY(), x + leftMargin + 7, colorHex.getY() + 24, 0xFF000000 | ColorPicker.getInteger());
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
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if(!visible) return false;
        for(ClickableWidget w : widgets){
            w.mouseReleased(mouseX,mouseY,button);
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY){
        if(!visible) return false;
        for(ClickableWidget w : widgets){
            w.mouseDragged(mouseX,mouseY,button,deltaX,deltaY);
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

            if (widgets.get(focusedWidget) instanceof ColorScrollbarWidget){
                keyPressed(keyCode, scanCode, modifiers); // Skip color sliders
            } else {
                int index = 0;
                for(ClickableWidget w : widgets){
                    w.setFocused(index == focusedWidget);
                    index++;
                }
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
