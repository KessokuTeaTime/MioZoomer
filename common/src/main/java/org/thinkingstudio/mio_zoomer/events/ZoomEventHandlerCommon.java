package org.thinkingstudio.mio_zoomer.events;

import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.event.events.client.ClientTickEvent;

public class ZoomEventHandlerCommon {

	public static void registerClient() {
		ClientLifecycleEvent.CLIENT_SETUP.register(instance -> {
			ApplyLoadOnceOptionsEvent.readyClient(instance);
		});

		ClientTickEvent.CLIENT_POST.register(instance -> {
			OpenScreenEvent.endClientTick(instance);
		});

		ClientTickEvent.CLIENT_PRE.register(instance -> {
			ManageExtraKeysEvent.startClientTick(instance);
			ManageZoomEvent.startClientTick(instance);
		});
	}
}
