package org.thinkingstudio.mio_zoomer.events;

import org.thinkingstudio.mio_zoomer.config.MioZoomerConfigManager;
import org.thinkingstudio.mio_zoomer.utils.OwoUtils;
import org.thinkingstudio.mio_zoomer.utils.ZoomUtils;
import net.minecraft.client.MinecraftClient;

// The event that makes sure to load the config and puts any load-once options in effect if enabled through the config file
public class ApplyLoadOnceOptionsEvent {
	public static void readyClient(MinecraftClient client) {
		// uwu
		if (MioZoomerConfigManager.CONFIG.tweaks.print_owo_on_start.value()) {
			OwoUtils.printOwo();
		}

		// This handles the unbinding of the "Save Toolbar Activator" key
		if (MioZoomerConfigManager.CONFIG.tweaks.unbind_conflicting_key.value()) {
			ZoomUtils.unbindConflictingKey(client, false);
			MioZoomerConfigManager.CONFIG.tweaks.unbind_conflicting_key.setValue(false, true);
		}
	}
}
