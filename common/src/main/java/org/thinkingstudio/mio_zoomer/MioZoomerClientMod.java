package org.thinkingstudio.mio_zoomer;

import dev.architectury.platform.Platform;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;

import org.thinkingstudio.mio_zoomer.config.MioZoomerConfigManager;
import org.thinkingstudio.mio_zoomer.config.screen.MioZoomerConfigScreen;
import org.thinkingstudio.mio_zoomer.key_binds.ZoomKeyBinds;

// This class is responsible for registering the commands and network
public class MioZoomerClientMod {
	public static final String MODID = "mio_zoomer";

	public static void onInitClient() {
		// Initialize the config
		new MioZoomerConfigManager();

		// Register all the key binds
		KeyMappingRegistry.register(ZoomKeyBinds.ZOOM_KEY);
		if (ZoomKeyBinds.areExtraKeyBindsEnabled()) {
			KeyMappingRegistry.register(ZoomKeyBinds.DECREASE_ZOOM_KEY);
			KeyMappingRegistry.register(ZoomKeyBinds.INCREASE_ZOOM_KEY);
			KeyMappingRegistry.register(ZoomKeyBinds.RESET_ZOOM_KEY);
		}

		Platform.getMod(MODID).registerConfigurationScreen(MioZoomerConfigScreen::new);
	}
}
