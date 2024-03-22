package org.thinkingstudio.mio_zoomer.fabric.events;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import org.thinkingstudio.mio_zoomer.events.ZoomEvents;
import org.thinkingstudio.mio_zoomer.key_binds.ZoomKeyBinds;
import org.thinkingstudio.mio_zoomer.network.ZoomNetwork;

public class ZoomEventsFabric {
	public static void registerClient() {
		ZoomEvents.registerClient();

		registerAllKeyBinds();
		registerClientPlayerNetwork();
	}

	// Register all the key binds
	public static void registerAllKeyBinds() {
		KeyBindingHelper.registerKeyBinding(ZoomKeyBinds.ZOOM_KEY);
		if (ZoomKeyBinds.areExtraKeyBindsEnabled()) {
			KeyBindingHelper.registerKeyBinding(ZoomKeyBinds.DECREASE_ZOOM_KEY);
			KeyBindingHelper.registerKeyBinding(ZoomKeyBinds.INCREASE_ZOOM_KEY);
			KeyBindingHelper.registerKeyBinding(ZoomKeyBinds.RESET_ZOOM_KEY);
		}
	}

	public static void registerClientPlayerNetwork() {
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			if (ZoomNetwork.hasRestrictions) {
				ZoomNetwork.resetPacketSignals();
			}
		});
	}
}
