package org.thinkingstudio.mio_zoomer.forge.events;

import net.minecraft.server.command.CommandManager;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import org.thinkingstudio.mio_zoomer.events.ZoomEvents;
import org.thinkingstudio.mio_zoomer.key_binds.ZoomKeyBinds;
import org.thinkingstudio.mio_zoomer.utils.ZoomUtils;

public class ZoomEventsForge {
	public static void registerClient() {
		ZoomEvents.registerClient();

		MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, ZoomEventsForge::registerClientCommand);
		MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, ZoomEventsForge::registerAllKeybinds);
	}

	// Register all the commands
	public static void registerClientCommand(RegisterClientCommandsEvent event) {
		event.getDispatcher().register(
			CommandManager.literal("mio_zoomer").executes(ctx -> {
					ZoomUtils.setOpenCommandScreen(true);
					return 0;
				}
			)
		);
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
}
