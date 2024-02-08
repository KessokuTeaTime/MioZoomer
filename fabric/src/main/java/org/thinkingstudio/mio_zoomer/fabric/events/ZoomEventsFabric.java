package org.thinkingstudio.mio_zoomer.fabric.events;

import org.thinkingstudio.mio_zoomer.events.ZoomEvents;
import org.thinkingstudio.mio_zoomer.utils.ZoomUtils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

public class ZoomEventsFabric {
	public static void registerClient() {
		ZoomEvents.registerClient();

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(
				ClientCommandManager.literal("mio_zoomer").executes(ctx -> {
						ZoomUtils.setOpenCommandScreen(true);
						return 0;
					}
				)
			);
		});
	}
}
