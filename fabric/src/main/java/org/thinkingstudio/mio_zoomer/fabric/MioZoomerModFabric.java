package org.thinkingstudio.mio_zoomer.fabric;

import net.fabricmc.api.ClientModInitializer;
import org.thinkingstudio.mio_zoomer.MioZoomerClientMod;
import org.thinkingstudio.mio_zoomer.fabric.events.ZoomEventHandlerFabric;
import org.thinkingstudio.mio_zoomer.fabric.packets.ZoomPacketsFabric;

public class MioZoomerModFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MioZoomerClientMod.onInitClient();
		ZoomEventHandlerFabric.registerClient();

		// Register the zoom-controlling packets
		ZoomPacketsFabric.registerPackets();
    }
}
