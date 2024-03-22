package org.thinkingstudio.mio_zoomer.network;

import dev.architectury.networking.NetworkManager;
import org.thinkingstudio.mio_zoomer.MioZoomerClientMod;
import org.thinkingstudio.mio_zoomer.config.ConfigEnums;
import org.thinkingstudio.mio_zoomer.config.MioZoomerConfigManager;
import org.thinkingstudio.mio_zoomer.config.ConfigEnums.CinematicCameraOptions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.thinkingstudio.mio_zoomer.utils.ZoomUtils;

/* 	Manages the zoom network and their signals.
	These network are intended to be used by the future "Zoomer Boomer" server-side mod,
	although developers are welcome to independently transmit them for other loaders */
public class ZoomNetwork {
	// The IDs for network that allows the server to have some control on the zoom.
	public static final Identifier DISABLE_ZOOM_PACKET_ID = new Identifier(MioZoomerClientMod.MODID, "disable_zoom");
	public static final Identifier DISABLE_ZOOM_SCROLLING_PACKET_ID = new Identifier(MioZoomerClientMod.MODID, "disable_zoom_scrolling");
	public static final Identifier FORCE_CLASSIC_MODE_PACKET_ID = new Identifier(MioZoomerClientMod.MODID, "force_classic_mode");
	public static final Identifier FORCE_ZOOM_DIVISOR_PACKET_ID = new Identifier(MioZoomerClientMod.MODID, "force_zoom_divisor");
	public static final Identifier ACKNOWLEDGE_MOD_PACKET_ID = new Identifier(MioZoomerClientMod.MODID, "acknowledge_mod");
	public static final Identifier FORCE_SPYGLASS_PACKET_ID = new Identifier(MioZoomerClientMod.MODID, "force_spyglass");
	public static final Identifier FORCE_SPYGLASS_OVERLAY_PACKET_ID = new Identifier(MioZoomerClientMod.MODID, "force_spyglass_overlay");

	public enum Acknowledgement {
		NONE,
		HAS_RESTRICTIONS,
		HAS_NO_RESTRICTIONS
	}

	// The signals used by other parts of the zoom in order to enforce the network
	public static boolean hasRestrictions = false;
	public static boolean disableZoom = false;
	public static boolean disableZoomScrolling = false;
	public static boolean forceClassicMode = false;
	public static boolean forceZoomDivisors = false;
	private static Acknowledgement acknowledgement = Acknowledgement.NONE;
	public static double maximumZoomDivisor = 0.0D;
	public static double minimumZoomDivisor = 0.0D;
	public static boolean spyglassDependency = false;
	public static boolean spyglassOverlay = false;

	private static final Text TOAST_TITLE = Text.translatable("toast.mio_zoomer.title");

	public static void sendToast(MinecraftClient client, Text description) {
		if (MioZoomerConfigManager.CONFIG.tweaks.show_restriction_toasts.value()) {
			client.getToastManager().add(SystemToast.create(client, SystemToast.Type.TUTORIAL_HINT, TOAST_TITLE, description));
		}
	}

	public static boolean getHasRestrictions() {
		return hasRestrictions;
	}

	public static void checkRestrictions() {
		boolean hasRestrictions = disableZoom
			|| disableZoomScrolling
			|| forceClassicMode
			|| forceZoomDivisors
			|| spyglassDependency
			|| spyglassOverlay;

		ZoomNetwork.hasRestrictions = hasRestrictions;
		if (hasRestrictions) {
			ZoomNetwork.acknowledgement = Acknowledgement.HAS_RESTRICTIONS;
		} else {
			ZoomNetwork.acknowledgement = Acknowledgement.HAS_NO_RESTRICTIONS;
		}
	}

	//Registers all the network
	public static void registerPackets() {
		MinecraftClient client = MinecraftClient.getInstance();

		/*  The "Disable Zoom" packet,
			If this packet is received, Mio Zoomer's zoom will be disabled completely while in the server
			Supported since Mio Zoomer 4.0.0 (1.16)
			Arguments: None */
		NetworkManager.registerReceiver(NetworkManager.Side.C2S, ZoomNetwork.DISABLE_ZOOM_PACKET_ID, (buf, context) -> {
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
		NetworkManager.registerReceiver(NetworkManager.Side.C2S, ZoomNetwork.DISABLE_ZOOM_SCROLLING_PACKET_ID, (buf, context) -> {
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
		NetworkManager.registerReceiver(NetworkManager.Side.C2S, ZoomNetwork.FORCE_CLASSIC_MODE_PACKET_ID, (buf, context) -> {
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
		NetworkManager.registerReceiver(NetworkManager.Side.C2S, ZoomNetwork.FORCE_ZOOM_DIVISOR_PACKET_ID, (buf, context) -> {
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
		NetworkManager.registerReceiver(NetworkManager.Side.C2S, ZoomNetwork.ACKNOWLEDGE_MOD_PACKET_ID, (buf, context) -> {
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
		NetworkManager.registerReceiver(NetworkManager.Side.C2S, ZoomNetwork.FORCE_SPYGLASS_PACKET_ID, (buf, context) -> {
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
		NetworkManager.registerReceiver(NetworkManager.Side.C2S, ZoomNetwork.FORCE_SPYGLASS_OVERLAY_PACKET_ID, (buf, context) -> {
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

		//ClientPlayConnectionEvent.DISCONNECT.register((handler, client) -> {
		//	if (ZoomNetwork.hasRestrictions) {
		//		ZoomNetwork.resetPacketSignals();
		//	}
		//});
	}

	public static boolean getDisableZoom() {
		return disableZoom;
	}

	public static boolean getDisableZoomScrolling() {
		return disableZoomScrolling;
	}

	public static boolean getForceClassicMode() {
		return forceClassicMode;
	}

	public static boolean getForceZoomDivisors() {
		return forceZoomDivisors;
	}

	public static Acknowledgement getAcknowledgement() {
		return acknowledgement;
	}

	public static double getMaximumZoomDivisor() {
		return maximumZoomDivisor;
	}

	public static double getMinimumZoomDivisor() {
		return minimumZoomDivisor;
	}

	public static boolean getSpyglassDependency() {
		return spyglassDependency;
	}

	public static boolean getSpyglassOverlay() {
		return spyglassOverlay;
	}

	public static void applyDisableZoomScrolling() {
		MioZoomerConfigManager.CONFIG.features.zoom_scrolling.setOverride(false);
		MioZoomerConfigManager.CONFIG.features.extra_key_binds.setOverride(false);
	}

	public static void applyClassicMode() {
		MioZoomerConfigManager.CONFIG.features.cinematic_camera.setOverride(CinematicCameraOptions.VANILLA);
		MioZoomerConfigManager.CONFIG.features.reduce_sensitivity.setOverride(false);
		MioZoomerConfigManager.CONFIG.values.zoom_divisor.setOverride(4.0D);
	}

	//The method used to reset the signals once left the server.
	public static void resetPacketSignals() {
		ZoomNetwork.hasRestrictions = false;
		ZoomNetwork.disableZoom = false;
		ZoomNetwork.disableZoomScrolling = false;
		MioZoomerConfigManager.CONFIG.features.zoom_scrolling.removeOverride();
		MioZoomerConfigManager.CONFIG.features.extra_key_binds.removeOverride();
		ZoomNetwork.forceClassicMode = false;
		MioZoomerConfigManager.CONFIG.features.cinematic_camera.removeOverride();
		MioZoomerConfigManager.CONFIG.features.reduce_sensitivity.removeOverride();
		MioZoomerConfigManager.CONFIG.values.zoom_divisor.removeOverride();
		ZoomNetwork.forceZoomDivisors = false;
		ZoomNetwork.maximumZoomDivisor = 0.0D;
		ZoomNetwork.minimumZoomDivisor = 0.0D;
		ZoomNetwork.acknowledgement = Acknowledgement.NONE;
		ZoomNetwork.spyglassDependency = false;
		MioZoomerConfigManager.CONFIG.features.spyglass_dependency.removeOverride();
		ZoomNetwork.spyglassOverlay = false;
		MioZoomerConfigManager.CONFIG.features.zoom_overlay.removeOverride();
	}
}
