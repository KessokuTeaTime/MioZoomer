package org.thinkingstudio.mio_zoomer.forge.events;

import net.minecraft.server.command.CommandManager;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.thinkingstudio.mio_zoomer.events.ZoomEventHandlerCommon;
import org.thinkingstudio.mio_zoomer.utils.ZoomUtils;

public class ZoomEventHandlerForge {
	public static void registerClient() {
		ZoomEventHandlerCommon.registerClient();

		MinecraftForge.EVENT_BUS.addListener(ZoomEventHandlerForge::registerClientCommand);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void registerClientCommand(RegisterClientCommandsEvent event) {
		event.getDispatcher().register(
			CommandManager.literal("mio_zoomer").executes(ctx -> {
					ZoomUtils.setOpenCommandScreen(true);
					return 0;
				}
			)
		);
	}
}
