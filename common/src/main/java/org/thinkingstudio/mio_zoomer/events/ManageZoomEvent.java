package org.thinkingstudio.mio_zoomer.events;

import org.thinkingstudio.mio_zoomer.config.MioZoomerConfigManager;
import org.thinkingstudio.mio_zoomer.config.ConfigEnums.ZoomModes;
import org.thinkingstudio.mio_zoomer.key_binds.ZoomKeyBinds;
import org.thinkingstudio.mio_zoomer.network.ZoomNetwork;
import org.thinkingstudio.mio_zoomer.utils.ZoomUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvents;

// This event is responsible for managing the zoom signal.
public class ManageZoomEvent {
	// Used internally in order to make zoom toggling possible
	private static boolean lastZooming = false;

	// Used internally in order to make persistent zoom less buggy
	private static boolean persistentZoom = false;

	public static void startClientTick(MinecraftClient client) {
		// We need the player for spyglass shenanigans
		if (client.player == null) return;

		// If zoom is disabled, do not allow for zooming at all
		boolean disableZoom = ZoomNetwork.getDisableZoom() ||
			(switch (MioZoomerConfigManager.CONFIG.features.spyglass_dependency.value()) {
				case REQUIRE_ITEM, BOTH -> true;
				default -> false;
			} && !client.player.getInventory().contains(ZoomUtils.ZOOM_DEPENDENCIES_TAG));

		if (disableZoom) {
			ZoomUtils.ZOOMER_ZOOM.setZoom(false);
			ZoomUtils.resetZoomDivisor(false);
			lastZooming = false;
			return;
		}

		// Handle zoom mode changes.
		if (!MioZoomerConfigManager.CONFIG.features.zoom_mode.value().equals(ZoomModes.HOLD)) {
			if (!persistentZoom) {
				persistentZoom = true;
				lastZooming = true;
				ZoomUtils.resetZoomDivisor(false);
			}
		} else {
			if (persistentZoom) {
				persistentZoom = false;
				lastZooming = true;
			}
		}

		// Gathers all variables about if the press was with zoom key or with the spyglass
		boolean isUsingSpyglass = switch (MioZoomerConfigManager.CONFIG.features.spyglass_dependency.value()) {
			case REPLACE_ZOOM, BOTH -> true;
			default -> false;
		};
		boolean keyPress = ZoomKeyBinds.ZOOM_KEY.isPressed();
		boolean spyglassUse = client.player.isUsingSpyglass();
		boolean zooming = keyPress || (isUsingSpyglass && spyglassUse);

		// If the press state is the same as the previous tick's, cancel the rest
		// This makes toggling usable and the zoom divisor adjustable
		if (zooming == lastZooming) return;

		boolean doSpyglassSound = MioZoomerConfigManager.CONFIG.tweaks.use_spyglass_sounds.value();

		switch (MioZoomerConfigManager.CONFIG.features.zoom_mode.value()) {
			case HOLD -> {
				// If the zoom needs to be held, then the zoom signal is determined by if the key is pressed or not
				ZoomUtils.ZOOMER_ZOOM.setZoom(zooming);
				ZoomUtils.resetZoomDivisor(false);
			}
			case TOGGLE -> {
				// If the zoom needs to be toggled, toggle the zoom signal instead
				if (zooming) {
					ZoomUtils.ZOOMER_ZOOM.setZoom(!ZoomUtils.ZOOMER_ZOOM.getZoom());
					ZoomUtils.resetZoomDivisor(false);
				} else {
					doSpyglassSound = false;
				}
			}
			case PERSISTENT -> {
				// If persistent zoom is enabled, just keep the zoom on
				ZoomUtils.ZOOMER_ZOOM.setZoom(true);
				ZoomUtils.keepZoomStepsWithinBounds();
			}
		}

		if (doSpyglassSound && !spyglassUse) {
			boolean soundDirection = !MioZoomerConfigManager.CONFIG.features.zoom_mode.value().equals(ZoomModes.PERSISTENT)
				? ZoomUtils.ZOOMER_ZOOM.getZoom()
				: keyPress;

			client.player.playSound(soundDirection ? SoundEvents.ITEM_SPYGLASS_USE : SoundEvents.ITEM_SPYGLASS_STOP_USING, 1.0F, 1.0F);
		}

		// Set the previous zoom signal for the next tick
		lastZooming = zooming;
	}
}
