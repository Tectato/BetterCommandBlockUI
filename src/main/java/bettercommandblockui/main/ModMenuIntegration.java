package bettercommandblockui.main;

import bettercommandblockui.main.config.ConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.MinecraftClient;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory(){
        return parent -> new ConfigScreen(parent, MinecraftClient.getInstance(), parent.width, parent.height);
    }
}
