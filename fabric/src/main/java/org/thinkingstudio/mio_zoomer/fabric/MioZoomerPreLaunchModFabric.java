package org.thinkingstudio.mio_zoomer.fabric;

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import org.thinkingstudio.mio_zoomer.MioZoomerPreLaunchMod;

public class MioZoomerPreLaunchModFabric implements PreLaunchEntrypoint {
	@Override
	public void onPreLaunch() {
		MioZoomerPreLaunchMod.onPreLaunch();
	}
}
