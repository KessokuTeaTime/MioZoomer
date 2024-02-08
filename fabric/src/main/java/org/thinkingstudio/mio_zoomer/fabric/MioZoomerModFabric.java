package org.thinkingstudio.mio_zoomer.fabric;

import net.fabricmc.api.ClientModInitializer;
import org.thinkingstudio.mio_zoomer.MioZoomerClientMod;
import org.thinkingstudio.mio_zoomer.fabric.events.ZoomEventsFabric;
import org.thinkingstudio.mio_zoomer.fabric.network.ZoomNetworkFabric;

public class MioZoomerModFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MioZoomerClientMod.onInitClient();
		ZoomEventsFabric.registerClient();

		// Register the zoom-controlling network
		ZoomNetworkFabric.registerPackets();
    }
}
