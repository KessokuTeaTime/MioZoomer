package org.thinkingstudio.mio_zoomer;

import dev.architectury.platform.Platform;

import org.thinkingstudio.mio_zoomer.config.MioZoomerConfigManager;
import org.thinkingstudio.mio_zoomer.config.screen.MioZoomerConfigScreen;

public class MioZoomerClientMod {
	public static final String MODID = "mio_zoomer";

	public static void onInitClient() {
		// Initialize the config
		new MioZoomerConfigManager();

		Platform.getMod(MODID).registerConfigurationScreen(MioZoomerConfigScreen::new);
	}
}
