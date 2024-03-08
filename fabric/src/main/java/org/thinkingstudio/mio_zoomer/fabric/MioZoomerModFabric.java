package org.thinkingstudio.mio_zoomer.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import org.thinkingstudio.mio_zoomer.MioZoomerClientMod;
import org.thinkingstudio.mio_zoomer.MioZoomerPreLaunchMod;
import org.thinkingstudio.mio_zoomer.fabric.events.ZoomEventsFabric;
import org.thinkingstudio.mio_zoomer.fabric.network.ZoomNetworkFabric;

public class MioZoomerModFabric implements ClientModInitializer, PreLaunchEntrypoint {
    @Override
    public void onInitializeClient() {
        MioZoomerClientMod.onInitClient();
		ZoomEventsFabric.registerClient();

		// Register the zoom-controlling network
		// TODO: use UniNetworking system
		ZoomNetworkFabric.registerPackets();
    }

	@Override
	public void onPreLaunch() {
		MioZoomerPreLaunchMod.onPreLaunch();
	}
}
