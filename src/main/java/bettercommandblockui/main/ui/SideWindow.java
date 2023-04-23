package bettercommandblockui.main.ui;

import bettercommandblockui.main.BetterCommandBlockUI;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.LinkedList;
import java.util.List;

import static bettercommandblockui.main.BetterCommandBlockUI.BUTTON_COPY;

public class SideWindow extends DrawableHelper implements Drawable, Element {
    int x, y, width, height;
    int topMargin = 5;
    int leftMargin = 5;
    boolean visible = false;

    MultiLineTextFieldWidget commandField;
    Screen screen;

    String piFractionInputText = "2π / ";
    String indentationInputText = "Indentation: ";
    String scrollXInputText = "Scroll speed X: ";
    String scrollYInputText = "Scroll speed Y: ";
    String wraparoundInputText = "Wraparound width: ";
    String formatStringsText = "Format strings: ";
    TextFieldWidget piFractionInput, indentationInput, scrollXInput, scrollYInput, wraparoundInput;
    CyclingTexturedButtonWidget<Boolean> formatStrings;
    NotchedSlider piSlider;
    TexturedButtonWidget piCopyButton;
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
                0,
                20,
                20,
                BUTTON_COPY,
                20,
                60,
                (button)->{
                    MinecraftClient.getInstance().keyboard.setClipboard(String.valueOf(piSlider.getValue() * 2 * Math.PI));
                }
        ));

        posY += 30;
        this.indentationInput = (TextFieldWidget) addWidget(new TextFieldWidget(textRenderer, x + leftMargin, posY, 60, 10, Text.of("")));
        this.indentationInput.setChangedListener((input)->{
            try {
                int inputInt = Math.min(Math.max(Integer.parseInt(input),1),16);
                BetterCommandBlockUI.setConfig(BetterCommandBlockUI.VAR_INDENTATION, String.valueOf(inputInt));
                this.commandField.refreshFormatting();
            } catch (NumberFormatException e){
                BetterCommandBlockUI.INDENTATION_FACTOR = 2;
            }
        });
        this.indentationInput.setText(String.valueOf(BetterCommandBlockUI.INDENTATION_FACTOR));

        posY += 30;
        this.wraparoundInput = (TextFieldWidget) addWidget(new TextFieldWidget(textRenderer, x + leftMargin, posY, 80, 10, Text.of("")));
        this.wraparoundInput.setChangedListener((input)->{
            try {
                int inputInt = Math.min(Math.max(Integer.parseInt(input),10),1600);
                BetterCommandBlockUI.setConfig(BetterCommandBlockUI.VAR_WRAPAROUND, String.valueOf(inputInt));
                this.commandField.refreshFormatting();
            } catch (NumberFormatException e){
                BetterCommandBlockUI.WRAPAROUND_WIDTH = 200;
            }
        });
        this.wraparoundInput.setText(String.valueOf(BetterCommandBlockUI.WRAPAROUND_WIDTH));

        posY += 30;
        this.scrollXInput = (TextFieldWidget) addWidget(new TextFieldWidget(textRenderer, x + leftMargin, posY, 60, 10, Text.of("")));
        this.scrollXInput.setChangedListener((input)->{
            try {
                int inputInt = Math.min(Math.max(Integer.parseInt(input),1),64);
                BetterCommandBlockUI.setConfig(BetterCommandBlockUI.VAR_SCROLL_X, String.valueOf(inputInt));
            } catch (NumberFormatException e){
                BetterCommandBlockUI.SCROLL_STEP_X = 4;
            }
        });
        this.scrollXInput.setText(String.valueOf(BetterCommandBlockUI.SCROLL_STEP_X));

        posY += 30;
        this.scrollYInput = (TextFieldWidget) addWidget(new TextFieldWidget(textRenderer, x + leftMargin, posY, 60, 10, Text.of("")));
        this.scrollYInput.setChangedListener((input)->{
            try {
                int inputInt = Math.min(Math.max(Integer.parseInt(input),1),64);
                BetterCommandBlockUI.setConfig(BetterCommandBlockUI.VAR_SCROLL_Y, String.valueOf(inputInt));
            } catch (NumberFormatException e){
                BetterCommandBlockUI.SCROLL_STEP_Y = 2;
            }
        });
        this.scrollYInput.setText(String.valueOf(BetterCommandBlockUI.SCROLL_STEP_Y));

        posY += 30;
        Text[] formatStringsTooltips = {
            Text.of("Useful for nested Commands"),
            Text.of("Useful for nested Commands")
        };
        this.formatStrings = (CyclingTexturedButtonWidget<Boolean>) addWidget(
            new CyclingTexturedButtonWidget<>(
                x + leftMargin,
                posY,
                20,
                20,
                Text.of(""),
                (button)->{
                    BetterCommandBlockUI.setConfig(
                            BetterCommandBlockUI.VAR_FORMAT_STRINGS,
                            String.valueOf(button.getValue())
                    );
                    this.commandField.refreshFormatting();
                },
                screen,
                BetterCommandBlockUI.BUTTON_CHECKBOX,
                BetterCommandBlockUI.FORMAT_STRINGS ? 1 : 0,
                new Boolean[]{false, true},
                formatStringsTooltips
            )
        );
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if(!visible) return;
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        fill(matrices, x, y, x+width, y+height, 0xB0000000);

        drawBorder(matrices, x-1, y-1, width+4, height+2, 0xFFFFFFFF);

        this.textRenderer.drawWithShadow(matrices, piFractionInputText, x + leftMargin, piFractionInput.getY(), 0xFFFFFFFF);
        this.piFractionInput.render(matrices, mouseX, mouseY, delta);
        this.piSlider.render(matrices, mouseX, mouseY, delta);
        this.piCopyButton.render(matrices, mouseX, mouseY, delta);

        this.textRenderer.drawWithShadow(matrices, indentationInputText, x + leftMargin, indentationInput.getY() - 12, 0xFFFFFFFF);
        this.textRenderer.drawWithShadow(matrices, wraparoundInputText, x + leftMargin, wraparoundInput.getY() - 12, 0xFFFFFFFF);
        this.textRenderer.drawWithShadow(matrices, scrollXInputText, x + leftMargin, scrollXInput.getY() - 12, 0xFFFFFFFF);
        this.textRenderer.drawWithShadow(matrices, scrollYInputText, x + leftMargin, scrollYInput.getY() - 12, 0xFFFFFFFF);
        this.textRenderer.drawWithShadow(matrices, formatStringsText, x + leftMargin, formatStrings.getY() - 12, 0xFFFFFFFF);

        this.indentationInput.render(matrices, mouseX, mouseY, delta);
        this.wraparoundInput.render(matrices, mouseX, mouseY, delta);
        this.scrollXInput.render(matrices, mouseX, mouseY, delta);
        this.scrollYInput.render(matrices, mouseX, mouseY, delta);
        this.formatStrings.render(matrices, mouseX, mouseY, delta);
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

    }

    @Override
    public boolean isFocused() {
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(!visible) return false;
        for(ClickableWidget w : widgets){
            if(w.mouseClicked(mouseX, mouseY, button)) return true;
        }
        if(mouseX > x && mouseY > y && mouseY < y + height) return true;
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
