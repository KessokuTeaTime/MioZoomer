package org.thinkingstudio.mio_zoomer.events;

import org.thinkingstudio.mio_zoomer.commands.MioZoomerCommandScreen;
import org.thinkingstudio.mio_zoomer.utils.ZoomUtils;
import net.minecraft.client.MinecraftClient;

public class OpenScreenEvent {

	public static void endClientTick(MinecraftClient client) {
		if (ZoomUtils.shouldOpenCommandScreen()) {
			client.setScreen(new MioZoomerCommandScreen());
			ZoomUtils.setOpenCommandScreen(false);
		}
	}
}
