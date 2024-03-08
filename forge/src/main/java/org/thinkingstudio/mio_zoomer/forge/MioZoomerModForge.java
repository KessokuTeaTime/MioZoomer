package org.thinkingstudio.mio_zoomer.forge;

import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.network.NetworkConstants;
import org.thinkingstudio.mio_zoomer.MioZoomerClientMod;
import org.thinkingstudio.mio_zoomer.MioZoomerPreLaunchMod;
import org.thinkingstudio.mio_zoomer.forge.events.ZoomEventsForge;
import org.thinkingstudio.mio_zoomer.forge.network.ZoomNetworkForge;

@Mod(MioZoomerClientMod.MODID)
public class MioZoomerModForge {
    public MioZoomerModForge() {
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(MioZoomerClientMod.MODID, FMLJavaModLoadingContext.get().getModEventBus());
		ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a, b) -> true));
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

		if (FMLLoader.getDist() == Dist.CLIENT) {
			modEventBus.addListener(this::onPreLaunch);
			modEventBus.addListener(this::onInitializeClient);
		}
    }

	public void onInitializeClient(FMLClientSetupEvent event) {
		event.enqueueWork(() -> {
			MioZoomerClientMod.onInitClient();
			ZoomEventsForge.registerClient();

			// Register the zoom-controlling network
			// TODO: use UniNetworking system
			ZoomNetworkForge.registerPackets();
		});
	}

	public void onPreLaunch(InterModProcessEvent event) {
		event.enqueueWork(() -> {
			MixinExtrasBootstrap.init();
			MioZoomerPreLaunchMod.onPreLaunch();
		});
	}
}
