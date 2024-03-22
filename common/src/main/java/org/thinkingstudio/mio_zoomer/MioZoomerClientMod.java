package org.thinkingstudio.mio_zoomer;

import dev.architectury.platform.Platform;

import org.thinkingstudio.mio_zoomer.config.MioZoomerConfigManager;
import org.thinkingstudio.mio_zoomer.config.screen.MioZoomerConfigScreen;
import org.thinkingstudio.mio_zoomer.network.ZoomNetwork;

public class MioZoomerClientMod {
	public static final String MODID = "mio_zoomer";

	public static void onInitClient() {
		// Initialize the config
		MioZoomerConfigManager.getInstance();

		// Register the zoom-controlling network
		ZoomNetwork.registerPackets();

		Platform.getMod(MODID).registerConfigurationScreen(MioZoomerConfigScreen::new);
	}
}
