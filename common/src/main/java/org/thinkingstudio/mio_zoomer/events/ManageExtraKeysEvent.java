package org.thinkingstudio.mio_zoomer.events;

import org.thinkingstudio.mio_zoomer.config.MioZoomerConfigManager;
import org.thinkingstudio.mio_zoomer.key_binds.ZoomKeyBinds;
import org.thinkingstudio.mio_zoomer.utils.ZoomUtils;
import net.minecraft.client.MinecraftClient;

// This event manages the extra key binds' behavior
public class ManageExtraKeysEvent {
	public static void startClientTick(MinecraftClient client) {
		if (!ZoomKeyBinds.areExtraKeyBindsEnabled()) return;
		if (!MioZoomerConfigManager.EXTRA_KEY_BINDS.value()) return;
		if (MioZoomerConfigManager.ZOOM_SCROLLING.isBeingOverridden()) return;

		if (ZoomKeyBinds.DECREASE_ZOOM_KEY.isPressed() && !ZoomKeyBinds.INCREASE_ZOOM_KEY.isPressed()) {
			ZoomUtils.changeZoomDivisor(false);
		}

		if (ZoomKeyBinds.INCREASE_ZOOM_KEY.isPressed() && !ZoomKeyBinds.DECREASE_ZOOM_KEY.isPressed()) {
			ZoomUtils.changeZoomDivisor(true);
		}

		if (ZoomKeyBinds.RESET_ZOOM_KEY.isPressed()) {
			ZoomUtils.resetZoomDivisor(true);
		}
	}
}
