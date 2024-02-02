package org.thinkingstudio.mio_zoomer.events;

import org.thinkingstudio.mio_zoomer.config.MioZoomerConfigManager;
import org.thinkingstudio.mio_zoomer.utils.OwoUtils;
import org.thinkingstudio.mio_zoomer.utils.ZoomUtils;
import net.minecraft.client.MinecraftClient;

// The event that makes sure to load the config and puts any load-once options in effect if enabled through the config file
public class ApplyLoadOnceOptionsEvent {
	public static void readyClient(MinecraftClient client) {
		// uwu
		if (MioZoomerConfigManager.PRINT_OWO_ON_START.value()) {
			OwoUtils.printOwo();
		}

		// This handles the unbinding of the "Save Toolbar Activator" key
		if (MioZoomerConfigManager.UNBIND_CONFLICTING_KEY.value()) {
			ZoomUtils.unbindConflictingKey(client, false);
			MioZoomerConfigManager.UNBIND_CONFLICTING_KEY.setValue(false, true);
		}
	}
}
