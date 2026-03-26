package bettercommandblockui.main.ui;

import bettercommandblockui.main.BetterCommandBlockUI;
import bettercommandblockui.main.ui.screen.AbstractBetterCommandBlockScreen;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.joml.AxisAngle4d;
import org.joml.Quaternionf;

import java.util.LinkedList;
import java.util.List;

public class SideWindow implements Renderable, GuiEventListener {
    protected static final WidgetSprites COPY_BUTTON_TEXTURES = new WidgetSprites(
            Identifier.parse("bettercommandblockui:button_copy_enabled"),
            Identifier.parse("bettercommandblockui:button_copy_disabled"),
            Identifier.parse("bettercommandblockui:button_copy_focused")
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
    EditBox piFractionInput, piOutput, colorTextR, colorTextG, colorTextB, colorHex, colorInt;
    ColorScrollbarWidget colorSliderR, colorSliderG, colorSliderB;
    NotchedSlider piSlider;
    RotationIndicator piRotationIndicator;
    Button configButton;
    Font textRenderer;

    List<AbstractWidget> widgets;
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
        Minecraft minecraftClient = Minecraft.getInstance();
        this.textRenderer = minecraftClient.font;
        this.piFractionInput = (EditBox) addWidget(
                new EditBox(
                        textRenderer,
                        x + leftMargin + textRenderer.width(piFractionInputText),
                        posY,
                        60,
                        10,
                        Component.nullToEmpty("")
                )
        );
        this.piFractionInput.setResponder((input)->{
            try {
                piFraction = Math.min(Math.max(Integer.parseInt(input),1),16);
                this.piSlider.setSubdivisions(piFraction);
            } catch (NumberFormatException e){
                this.piSlider.setSubdivisions(4);
            }
        });

        posY += 12;
        this.piSlider = (NotchedSlider) addWidget(new NotchedSlider(x + leftMargin, posY, width - 30, 16, Component.nullToEmpty("")));
        this.piSlider.setSubdivisions(piFraction);
        this.piSlider.setPos(piSetting);
        this.piFractionInput.setValue(String.valueOf(piFraction));
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
        this.piOutput = (EditBox) addWidget(
                new OutputTextFieldWidget(
                        textRenderer,
                        x + leftMargin,
                        posY,
                        60,
                        10,
                        Component.nullToEmpty("")
                )
        );
        this.piOutput.setEditable(false);
        String piOutputText = Double.toString(piSetting * 2*Math.PI);
        this.piOutput.setValue(piOutputText.substring(0, Math.min(8,piOutputText.length())));
        this.piSlider.setChangedListener((value)->{
            piSetting = value;
            this.piRotationIndicator.setAngle(value);
            String text = Double.toString(piSetting * 2*Math.PI);
            this.piOutput.setValue(text.substring(0, Math.min(8,text.length())));
        });

        this.piRotationIndicator = (RotationIndicator) addWidget(
                new RotationIndicator(
                        piOutput.getX() + piOutput.getWidth() + 8,
                        piOutput.getY() - 2,
                        Component.nullToEmpty("")
                )
        );
        this.piRotationIndicator.setChangedListener((value)->{
            this.piSlider.setPos(value);
            piSetting = value;
            String text = Double.toString(piSetting * 2*Math.PI);
            this.piOutput.setValue(text.substring(0, Math.min(8,text.length())));
        });
        this.piRotationIndicator.setAngle(piSetting);

        posY += 32;
        this.colorTextR = (EditBox) addWidget(
                new EditBox(
                        textRenderer,
                        x + leftMargin + textRenderer.width("R:"),
                        posY,
                        30,
                        10,
                        Component.nullToEmpty("")
                )
        );
        this.colorTextR.setMaxLength(3);
        this.colorTextR.setResponder((input)->{
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
                        Component.nullToEmpty(""),
                        ColorPicker.COLOR.RED
                )
        );
        this.colorSliderR.setChangedListener((pos)->{
            this.colorTextR.setValue(""+((int)(Math.round(pos * 255.0))));
            this.updateColorOutputs();
        });
        this.colorTextR.setValue("" + ColorPicker.getColor(ColorPicker.COLOR.RED));

        posY += 14;
        this.colorTextG = (EditBox) addWidget(
                new EditBox(
                        textRenderer,
                        x + leftMargin + textRenderer.width("R:"),
                        posY,
                        30,
                        10,
                        Component.nullToEmpty("")
                )
        );
        this.colorTextG.setMaxLength(3);
        this.colorTextG.setResponder((input)->{
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
                        Component.nullToEmpty(""),
                        ColorPicker.COLOR.GREEN
                )
        );
        this.colorSliderG.setChangedListener((pos)->{
            this.colorTextG.setValue(""+((int)(Math.round(pos * 255.0))));
            this.updateColorOutputs();
        });
        this.colorTextG.setValue("" + ColorPicker.getColor(ColorPicker.COLOR.GREEN));

        posY += 14;
        this.colorTextB = (EditBox) addWidget(
                new EditBox(
                        textRenderer,
                        x + leftMargin + textRenderer.width("R:"),
                        posY,
                        30,
                        10,
                        Component.nullToEmpty("")
                )
        );
        this.colorTextB.setMaxLength(3);
        this.colorTextB.setResponder((input)->{
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
                        Component.nullToEmpty(""),
                        ColorPicker.COLOR.BLUE
                )
        );
        this.colorSliderB.setChangedListener((pos)->{
            this.colorTextB.setValue(""+((int)(Math.round(pos * 255.0))));
            this.updateColorOutputs();
        });
        this.colorTextB.setValue("" + ColorPicker.getColor(ColorPicker.COLOR.BLUE));

        posY += 16;
        this.colorHex = (EditBox) addWidget(
                new OutputTextFieldWidget(
                        textRenderer,
                        x + leftMargin + textRenderer.width("R:"),
                        posY,
                        60,
                        10,
                        Component.nullToEmpty("")
                )
        );
        this.colorHex.setEditable(false);
        posY += 14;
        this.colorInt = (EditBox) addWidget(
                new OutputTextFieldWidget(
                        textRenderer,
                        x + leftMargin + textRenderer.width("R:"),
                        posY,
                        60,
                        10,
                        Component.nullToEmpty("")
                )
        );
        this.colorInt.setEditable(false);
        updateColorOutputs();

        posY += 20;
        this.configButton = (Button) addWidget(Button
                .builder(Component.translatable("bcbui.config"), button -> ((AbstractBetterCommandBlockScreen)screen).openConfig())
                .bounds(x+leftMargin, posY, width - leftMargin*2, 20).build());

        this.height = (posY - y) + 20 + topMargin;
    }

    public void updateColorOutputs(){
        colorHex.setValue(""+ColorPicker.getInteger());
        String hexText = ColorPicker.getHexString().toUpperCase();
        colorInt.setValue("#"+"0".repeat(6-hexText.length())+hexText);
    }

    @Override
    public void extractRenderState(final GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        if(!visible) return;

        graphics.fill(x, y, x+width, y+height, 0xB0000000);

        graphics.outline(x-1, y-1, width+4, height+2, 0xFFFFFFFF);

        graphics.text(this.textRenderer, "2π / ", x + leftMargin, piFractionInput.getY(), 0xFFFFFFFF);
        this.piFractionInput.extractRenderState(graphics, mouseX, mouseY, delta);
        this.piSlider.extractRenderState(graphics, mouseX, mouseY, delta);
        this.piOutput.extractRenderState(graphics, mouseX, mouseY, delta);
        this.piRotationIndicator.extractRenderState(graphics, mouseX, mouseY, delta);
        this.configButton.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.text(this.textRenderer, "R:", x + leftMargin, colorTextR.getY(), 0xFFFF0000);
        this.colorTextR.extractRenderState(graphics, mouseX, mouseY, delta);
        this.colorSliderR.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.text(this.textRenderer, "G:", x + leftMargin, colorTextG.getY(), 0xFF00FF00);
        this.colorTextG.extractRenderState(graphics, mouseX, mouseY, delta);
        this.colorSliderG.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.text(this.textRenderer, "B:", x + leftMargin, colorTextB.getY(), 0xFF0000FF);
        this.colorTextB.extractRenderState(graphics, mouseX, mouseY, delta);
        this.colorSliderB.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.fill(x + leftMargin, colorHex.getY(), x + leftMargin + 7, colorHex.getY() + 24, 0xFF000000 | ColorPicker.getInteger());
        this.colorHex.extractRenderState(graphics, mouseX, mouseY, delta);
        this.colorInt.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    LayoutElement addWidget(AbstractWidget widget){
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
        for(AbstractWidget w : widgets){
            if (w.isFocused()) return true;
        }
        return false;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if(!visible) return false;
        boolean widgetClicked = false;
        int index = 0;
        for(AbstractWidget w : widgets){
            if(!widgetClicked && w.mouseClicked(click, doubled)){
                w.setFocused(true);
                widgetClicked = true;
                focusedWidget = index;
            } else {
                w.setFocused(false);
            }
            index++;
        }
        boolean returnVal = widgetClicked;
        if(click.x() > x && click.y() > y && click.y() < y + height) returnVal = true;
        if (returnVal){
            ((AbstractBetterCommandBlockScreen)screen).sideWindowFocused();
            return true;
        }
        setFocused(false);
        return false;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        if(!visible) return false;
        for(AbstractWidget w : widgets){
            w.onRelease(click);
        }
        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent click, double deltaX, double deltaY){
        if(!visible) return false;
        for(AbstractWidget w : widgets){
            if(!(w instanceof EditBox)) {
                w.mouseDragged(click, deltaX, deltaY);
            }
        }
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent input){
        if(!visible) return false;
        for(AbstractWidget w : widgets){
            if(w.keyPressed(input)) return true;
        }
        if (input.input() == 258) {
            focusedWidget += Minecraft.getInstance().hasShiftDown() ? -1 : 1;
            focusedWidget %= widgets.size();
            if (focusedWidget < 0) focusedWidget = widgets.size() + focusedWidget;

            /*if (widgets.get(focusedWidget) instanceof ColorScrollbarWidget){
                keyPressed(input); // Skip color sliders
            } else {
                int index = 0;
                for(ClickableWidget w : widgets){
                    w.setFocused(index == focusedWidget);
                    index++;
                }
            }*/
        }
        return false;
    }

    @Override
    public boolean charTyped(CharacterEvent input){
        if(!visible) return false;
        for(AbstractWidget w : widgets){
            if(w.charTyped(input)) return true;
        }
        return false;
    }
}
