package bettercommandblockui.main.ui;

import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class TextFieldScrollbarWidget extends ScrollbarWidget{

    private MultiLineTextFieldWidget textField;
    public TextFieldScrollbarWidget(int x, int y, int width, int height, Component message, MultiLineTextFieldWidget textField, boolean horizontal) {
        super(x, y, width, height, message, horizontal);
        this.textField = textField;
    }

    @Override
    public void onDrag(MouseButtonEvent click, double distX, double distY){
        super.onDrag(click, distX, distY);
        if (horizontal) {
            textField.setHorizontalOffset(pos);
        } else {
            textField.setScroll(pos);
        }
    }
}
