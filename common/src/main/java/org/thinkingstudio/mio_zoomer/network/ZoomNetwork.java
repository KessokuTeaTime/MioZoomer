package org.thinkingstudio.mio_zoomer.network;

import org.thinkingstudio.mio_zoomer.MioZoomerClientMod;
import org.thinkingstudio.mio_zoomer.config.MioZoomerConfigManager;
import org.thinkingstudio.mio_zoomer.config.ConfigEnums.CinematicCameraOptions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

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
