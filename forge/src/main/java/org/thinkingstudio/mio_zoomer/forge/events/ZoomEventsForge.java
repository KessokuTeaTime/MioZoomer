package org.thinkingstudio.mio_zoomer.forge.events;

import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import org.thinkingstudio.mio_zoomer.events.ZoomEvents;
import org.thinkingstudio.mio_zoomer.key_binds.ZoomKeyBinds;
import org.thinkingstudio.mio_zoomer.network.ZoomNetwork;

public class ZoomEventsForge {
	public static void registerClient() {
		ZoomEvents.registerClient();

		MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, ZoomEventsForge::registerAllKeybinds);
		MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, ZoomEventsForge::registerClientPlayerNetwork);
	}

	// Register all the key binds
	public static void registerAllKeybinds(RegisterKeyMappingsEvent event) {
		event.register(ZoomKeyBinds.ZOOM_KEY);
		if (ZoomKeyBinds.areExtraKeyBindsEnabled()) {
			event.register(ZoomKeyBinds.DECREASE_ZOOM_KEY);
			event.register(ZoomKeyBinds.INCREASE_ZOOM_KEY);
			event.register(ZoomKeyBinds.RESET_ZOOM_KEY);
		}
	}

	public static void registerClientPlayerNetwork(ClientPlayerNetworkEvent.LoggingOut event) {
		if (ZoomNetwork.hasRestrictions) {
			ZoomNetwork.resetPacketSignals();
		}
	}
}
