package bettercommandblockui.main.ui;

import net.minecraft.text.Text;

public class TextFieldScrollbarWidget extends ScrollbarWidget{

    private MultiLineTextFieldWidget textField;
    public TextFieldScrollbarWidget(int x, int y, int width, int height, Text message, MultiLineTextFieldWidget textField, boolean horizontal) {
        super(x, y, width, height, message, horizontal);
        this.textField = textField;
    }

    @Override
    public void onDrag(double mouseX, double mouseY, double distX, double distY){
        super.onDrag(mouseX, mouseY, distX, distY);
        if (horizontal) {
            textField.setHorizontalOffset(pos);
        } else {
            textField.setScroll(pos);
        }
    }
}
