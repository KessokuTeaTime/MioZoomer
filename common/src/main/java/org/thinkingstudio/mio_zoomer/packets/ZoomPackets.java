package org.thinkingstudio.mio_zoomer.packets;

import org.thinkingstudio.mio_zoomer.config.MioZoomerConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.thinkingstudio.mio_zoomer.config.ConfigEnums;

/* 	Manages the zoom packets and their signals.
	These packets are intended to be used by the future "Zoomer Boomer" server-side mod,
	although developers are welcome to independently transmit them for other loaders */
public class ZoomPackets {
	// The IDs for packets that allows the server to have some control on the zoom.
	public static final Identifier DISABLE_ZOOM_PACKET_ID = new Identifier("mio_zoomer", "disable_zoom");
	public static final Identifier DISABLE_ZOOM_SCROLLING_PACKET_ID = new Identifier("mio_zoomer", "disable_zoom_scrolling");
	public static final Identifier FORCE_CLASSIC_MODE_PACKET_ID = new Identifier("mio_zoomer", "force_classic_mode");
	public static final Identifier FORCE_ZOOM_DIVISOR_PACKET_ID = new Identifier("mio_zoomer", "force_zoom_divisor");
	public static final Identifier ACKNOWLEDGE_MOD_PACKET_ID = new Identifier("mio_zoomer", "acknowledge_mod");
	public static final Identifier FORCE_SPYGLASS_PACKET_ID = new Identifier("mio_zoomer", "force_spyglass");
	public static final Identifier FORCE_SPYGLASS_OVERLAY_PACKET_ID = new Identifier("mio_zoomer", "force_spyglass_overlay");

	public enum Acknowledgement {
		NONE,
		HAS_RESTRICTIONS,
		HAS_NO_RESTRICTIONS
	}

	// The signals used by other parts of the zoom in order to enforce the packets
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

	private static Text toastTitle = Text.translatable("toast.mio_zoomer.title");

	public static void sendToast(MinecraftClient client, Text description) {
		if (MioZoomerConfigManager.SHOW_RESTRICTION_TOASTS.value()) {
			client.getToastManager().add(SystemToast.create(client, SystemToast.Type.TUTORIAL_HINT, toastTitle, description));
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

		ZoomPackets.hasRestrictions = hasRestrictions;
		if (hasRestrictions) {
			ZoomPackets.acknowledgement = Acknowledgement.HAS_RESTRICTIONS;
		} else {
			ZoomPackets.acknowledgement = Acknowledgement.HAS_NO_RESTRICTIONS;
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
		MioZoomerConfigManager.ZOOM_SCROLLING.setOverride(false);
		MioZoomerConfigManager.EXTRA_KEY_BINDS.setOverride(false);
	}

	public static void applyClassicMode() {
		MioZoomerConfigManager.CINEMATIC_CAMERA.setOverride(ConfigEnums.CinematicCameraOptions.VANILLA);
		MioZoomerConfigManager.REDUCE_SENSITIVITY.setOverride(false);
		MioZoomerConfigManager.ZOOM_DIVISOR.setOverride(4.0D);
	}

	//The method used to reset the signals once left the server.
	public static void resetPacketSignals() {
		ZoomPackets.hasRestrictions = false;
		ZoomPackets.disableZoom = false;
		ZoomPackets.disableZoomScrolling = false;
		MioZoomerConfigManager.ZOOM_SCROLLING.removeOverride();
		MioZoomerConfigManager.EXTRA_KEY_BINDS.removeOverride();
		ZoomPackets.forceClassicMode = false;
		MioZoomerConfigManager.CINEMATIC_CAMERA.removeOverride();
		MioZoomerConfigManager.REDUCE_SENSITIVITY.removeOverride();
		MioZoomerConfigManager.ZOOM_DIVISOR.removeOverride();
		ZoomPackets.forceZoomDivisors = false;
		ZoomPackets.maximumZoomDivisor = 0.0D;
		ZoomPackets.minimumZoomDivisor = 0.0D;
		ZoomPackets.acknowledgement = Acknowledgement.NONE;
		ZoomPackets.spyglassDependency = false;
		MioZoomerConfigManager.SPYGLASS_DEPENDENCY.removeOverride();
		ZoomPackets.spyglassOverlay = false;
		MioZoomerConfigManager.ZOOM_OVERLAY.removeOverride();
	}
}
