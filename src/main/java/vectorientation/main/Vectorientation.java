package vectorientation.main;

import net.fabricmc.api.ModInitializer;
import vectorientation.simpleLibs.simpleConfig.SimpleConfig;

public class Vectorientation implements ModInitializer {
	
	public static boolean squetch;
	
	@Override
	public void onInitialize() {
		SimpleConfig CONFIG = SimpleConfig.of( "vectorientation" ).provider( this::provider ).request();
		this.squetch = CONFIG.getOrDefault("squetch", true);
		System.out.println("[Tec] Ready to rotate some falling blocks! Squishy blocks enabled: " + squetch);
	}
	
	private String provider( String filename ) {
		return "#Enable Squash & Stretch:\n"
				+ "squetch=true";
    }
}
