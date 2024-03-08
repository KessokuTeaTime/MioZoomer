package org.thinkingstudio.mio_zoomer.fabric.events;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import org.thinkingstudio.mio_zoomer.events.ZoomEvents;
import org.thinkingstudio.mio_zoomer.key_binds.ZoomKeyBinds;
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

		registerAllKeyBinds();
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
}
