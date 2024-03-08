package org.thinkingstudio.mio_zoomer.events;

import dev.architectury.event.events.client.ClientCommandRegistrationEvent;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import org.thinkingstudio.mio_zoomer.utils.ZoomUtils;

public class ZoomEvents {
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

		ClientCommandRegistrationEvent.EVENT.register((dispatcher, context) -> {
			dispatcher.register(
				ClientCommandRegistrationEvent.literal("mio_zoomer").executes(ctx -> {
						ZoomUtils.setOpenCommandScreen(true);
						return 0;
					}
				)
			);
		});
	}
}
