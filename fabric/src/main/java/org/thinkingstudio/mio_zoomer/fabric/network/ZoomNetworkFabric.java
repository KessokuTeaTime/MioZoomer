package org.thinkingstudio.mio_zoomer.fabric.network;

import org.thinkingstudio.mio_zoomer.config.ConfigEnums;
import org.thinkingstudio.mio_zoomer.config.MioZoomerConfigManager;
import org.thinkingstudio.mio_zoomer.network.ZoomNetwork;
import org.thinkingstudio.mio_zoomer.utils.ZoomUtils;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.text.Text;

public class ZoomNetworkFabric {
	//Registers all the network
	public static void registerPackets() {
		/*  The "Disable Zoom" packet,
			If this packet is received, Mio Zoomer's zoom will be disabled completely while in the server
			Supported since Mio Zoomer 4.0.0 (1.16)
			Arguments: None */
		ClientPlayNetworking.registerGlobalReceiver(ZoomNetwork.DISABLE_ZOOM_PACKET_ID, (client, handler, buf, sender) -> {
			client.execute(() -> {
				ZoomUtils.LOGGER.info("[Mio Zoomer] This server has disabled zooming");
				ZoomNetwork.disableZoom = true;
				ZoomNetwork.checkRestrictions();
			});
		});

		/*  The "Disable Zoom Scrolling" packet,
			If this packet is received, zoom scrolling will be disabled while in the server
			Supported since Mio Zoomer 4.0.0 (1.16)
			Arguments: None */
		ClientPlayNetworking.registerGlobalReceiver(ZoomNetwork.DISABLE_ZOOM_SCROLLING_PACKET_ID, (client, handler, buf, sender) -> {
			client.execute(() -> {
				ZoomUtils.LOGGER.info("[Mio Zoomer] This server has disabled zoom scrolling");
				ZoomNetwork.applyDisableZoomScrolling();
				ZoomNetwork.disableZoomScrolling = true;
				ZoomNetwork.checkRestrictions();
			});
		});

		/*  The "Force Classic Mode" packet,
			If this packet is received, the Classic Mode will be activated while connected to the server,
			under the Classic mode, the Classic preset will be forced on all non-cosmetic options
			Supported since Mio Zoomer 5.0.0-beta.1 (1.17)
			Arguments: None */
		ClientPlayNetworking.registerGlobalReceiver(ZoomNetwork.FORCE_CLASSIC_MODE_PACKET_ID, (client, handler, buf, sender) -> {
			client.execute(() -> {
				ZoomUtils.LOGGER.info("[Mio Zoomer] This server has imposed classic mode");
				ZoomNetwork.disableZoomScrolling = true;
				ZoomNetwork.forceClassicMode = true;
				ZoomNetwork.applyDisableZoomScrolling();
				ZoomNetwork.applyClassicMode();
				MioZoomerConfigManager.configureZoomInstance();
				ZoomNetwork.checkRestrictions();
			});
		});

		/*  The "Force Zoom Divisor" packet,
			If this packet is received, the minimum and maximum zoom divisor values will be overriden
			with the provided arguments
			Supported since Mio Zoomer 5.0.0-beta.2 (1.17)
			Arguments: One double (max & min) or two doubles (first is max, second is min) */
		ClientPlayNetworking.registerGlobalReceiver(ZoomNetwork.FORCE_ZOOM_DIVISOR_PACKET_ID, (client, handler, buf, sender) -> {
			int readableBytes = buf.readableBytes();
			if (readableBytes == 8 || readableBytes == 16) {
				double maxDouble = buf.readDouble();
				double minDouble = (readableBytes == 16) ? buf.readDouble() : maxDouble;
				client.execute(() -> {
					if ((minDouble <= 0.0 || maxDouble <= 0.0) || minDouble > maxDouble) {
						ZoomUtils.LOGGER.info(String.format("[Mio Zoomer] This server has attempted to set invalid divisor values! (min %s, max %s)", minDouble, maxDouble));
					} else {
						ZoomUtils.LOGGER.info(String.format("[Mio Zoomer] This server has set the zoom divisors to minimum %s and maximum %s", minDouble, maxDouble));
						ZoomNetwork.maximumZoomDivisor = maxDouble;
						ZoomNetwork.minimumZoomDivisor = minDouble;
						ZoomNetwork.forceZoomDivisors = true;
						MioZoomerConfigManager.configureZoomInstance();
						ZoomNetwork.checkRestrictions();
					}
				});
			}
		});

		/*  The "Acknowledge Mod" packet,
			If received, a toast will appear, the toast will either state that
			the server won't restrict the mod or say that the server controls will be activated
			Supported since Mio Zoomer 5.0.0-beta.2 (1.17)
			Arguments: one boolean, false for restricting, true for restrictionless */
		ClientPlayNetworking.registerGlobalReceiver(ZoomNetwork.ACKNOWLEDGE_MOD_PACKET_ID, (client, handler, buf, sender) -> {
			boolean restricting = !buf.readBoolean();
			client.execute(() -> {
				ZoomNetwork.checkRestrictions();
				if (restricting) {
					if (ZoomNetwork.getAcknowledgement().equals(ZoomNetwork.Acknowledgement.HAS_RESTRICTIONS)) {
						ZoomUtils.LOGGER.info("[Mio Zoomer] This server acknowledges the mod and has established some restrictions");
						ZoomNetwork.sendToast(client, Text.translatable("toast.mio_zoomer.acknowledge_mod_restrictions"));
					}
				} else {
					if (ZoomNetwork.getAcknowledgement().equals(ZoomNetwork.Acknowledgement.HAS_NO_RESTRICTIONS)) {
						ZoomUtils.LOGGER.info("[Mio Zoomer] This server acknowledges the mod and establishes no restrictions");
						ZoomNetwork.sendToast(client, Text.translatable("toast.mio_zoomer.acknowledge_mod"));
					}
				}
			});
		});

		/*  The "Force Spyglass" packet,
			This packet lets the server to impose a spyglass restriction
			Supported since Mio Zoomer 5.0.0-beta.4 (1.18.2) */
		ClientPlayNetworking.registerGlobalReceiver(ZoomNetwork.FORCE_SPYGLASS_PACKET_ID, (client, handler, buf, sender) -> {
			boolean requireItem = buf.readBoolean();
			boolean replaceZoom = buf.readBoolean();
			client.execute(() -> {
				ZoomUtils.LOGGER.info(String.format("[Mio Zoomer] This server has the following spyglass restrictions: Require Item: %s, Replace Zoom: %s", requireItem, replaceZoom));

				MioZoomerConfigManager.CONFIG.features.spyglass_dependency.setOverride(requireItem
					? (replaceZoom ? ConfigEnums.SpyglassDependency.BOTH : ConfigEnums.SpyglassDependency.REQUIRE_ITEM)
					: (replaceZoom ? ConfigEnums.SpyglassDependency.REPLACE_ZOOM : null));
				ZoomNetwork.spyglassDependency = true;

				ZoomNetwork.checkRestrictions();
			});
		});

		/*  The "Force Spyglass Overlay" packet,
			This packet will let the server restrict the mod to spyglass-only usage
			Not supported yet!
			Arguments: probably some, we'll see */
		ClientPlayNetworking.registerGlobalReceiver(ZoomNetwork.FORCE_SPYGLASS_OVERLAY_PACKET_ID, (client, handler, buf, sender) -> {
			client.execute(() -> {
				ZoomUtils.LOGGER.info(String.format("[Mio Zoomer] This server has imposed a spyglass overlay on the zoom"));
				MioZoomerConfigManager.CONFIG.features.zoom_overlay.setOverride(ConfigEnums.ZoomOverlays.SPYGLASS);
				ZoomNetwork.spyglassOverlay = true;
				ZoomNetwork.checkRestrictions();
			});
		});

		/*
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			PacketByteBuf emptyBuf = PacketByteBufs.empty();
			//sender.sendPacket(DISABLE_ZOOM_PACKET_ID, emptyBuf);
			//sender.sendPacket(DISABLE_ZOOM_SCROLLING_PACKET_ID, emptyBuf);
			//sender.sendPacket(FORCE_CLASSIC_MODE_PACKET_ID, emptyBuf);
			PacketByteBuf buf = PacketByteBufs.create();
			buf.writeDouble(25.0D);
			buf.writeDouble(1.0D);
			sender.sendPacket(FORCE_ZOOM_DIVISOR_PACKET_ID, buf);
			PacketByteBuf buffy = PacketByteBufs.create();
			buffy.writeBoolean(true);
			buffy.writeBoolean(true);
			sender.sendPacket(FORCE_SPYGLASS_PACKET_ID, buffy);
			sender.sendPacket(FORCE_SPYGLASS_OVERLAY_PACKET_ID, emptyBuf);
			PacketByteBuf boolBuf = PacketByteBufs.create();
			boolBuf.writeBoolean(false);
			sender.sendPacket(ACKNOWLEDGE_MOD_PACKET_ID, boolBuf);
		});
		*/

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			if (ZoomNetwork.hasRestrictions) {
				ZoomNetwork.resetPacketSignals();
			}
		});
	}
}
