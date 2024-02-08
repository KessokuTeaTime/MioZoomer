package org.thinkingstudio.mio_zoomer.utils;

import com.mojang.blaze3d.platform.InputUtil;

import org.thinkingstudio.mio_zoomer.config.MioZoomerConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.thinkingstudio.mio_zoomer.key_binds.ZoomKeyBinds;
import org.thinkingstudio.mio_zoomer.network.ZoomNetwork;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBind;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.thinkingstudio.zoomerlibrary.api.ZoomInstance;
import org.thinkingstudio.zoomerlibrary.api.modifiers.ZoomDivisorMouseModifier;
import org.thinkingstudio.zoomerlibrary.api.transitions.SmoothTransitionMode;

// The class that contains most of the logic behind the zoom itself
public class ZoomUtils {
	// The logger, used everywhere to print messages to the console
	public static final Logger LOGGER = LoggerFactory.getLogger("Ok Zoomer");

	public static final ZoomInstance ZOOMER_ZOOM = new ZoomInstance(
		new Identifier("mio_zoomer:zoom"),
		4.0F,
		new SmoothTransitionMode(0.75f),
		new ZoomDivisorMouseModifier(),
		null
	);

	public static final TagKey<Item> ZOOM_DEPENDENCIES_TAG = TagKey.of(RegistryKeys.ITEM, new Identifier("mio_zoomer", "zoom_dependencies"));

	public static int zoomStep = 0;

	private static boolean openCommandScreen = false;

	// The method used for changing the zoom divisor, used by zoom scrolling and the key binds
	public static void changeZoomDivisor(boolean increase) {
		//If the zoom is disabled, don't allow for zoom scrolling
		if (ZoomNetwork.getDisableZoom()) return;

		double zoomDivisor = MioZoomerConfigManager.CONFIG.values.zoom_divisor.value();
		double minimumZoomDivisor = MioZoomerConfigManager.CONFIG.values.minimum_zoom_divisor.value();
		double maximumZoomDivisor = MioZoomerConfigManager.CONFIG.values.maximum_zoom_divisor.value();
		int upperScrollStep = MioZoomerConfigManager.CONFIG.values.upper_scroll_steps.value();
		int lowerScrollStep = MioZoomerConfigManager.CONFIG.values.lower_scroll_steps.value();

		if (ZoomNetwork.getForceZoomDivisors()) {
			minimumZoomDivisor = Math.max(minimumZoomDivisor, ZoomNetwork.getMinimumZoomDivisor());
			maximumZoomDivisor = Math.min(maximumZoomDivisor, ZoomNetwork.getMaximumZoomDivisor());
		}

		zoomStep = increase ? Math.min(zoomStep + 1, upperScrollStep) :  Math.max(zoomStep - 1, -lowerScrollStep);

		if (zoomStep > 0) {
			ZOOMER_ZOOM.setZoomDivisor(zoomDivisor + ((maximumZoomDivisor - zoomDivisor) / upperScrollStep * zoomStep));
		} else if (zoomStep == 0) {
			ZOOMER_ZOOM.setZoomDivisor(zoomDivisor);
		} else {
			ZOOMER_ZOOM.setZoomDivisor(zoomDivisor + ((minimumZoomDivisor - zoomDivisor) / lowerScrollStep * -zoomStep));
		}
	}

	// The method used by both the "Reset Zoom" keybind and the "Reset Zoom With Mouse" tweak
	public static void resetZoomDivisor(boolean userPrompted) {
		if (userPrompted && ZoomNetwork.getDisableZoom()) return;
		if (!userPrompted && !MioZoomerConfigManager.CONFIG.tweaks.forget_zoom_divisor.value()) return;

		ZOOMER_ZOOM.resetZoomDivisor();
		zoomStep = 0;
	}

	public static void keepZoomStepsWithinBounds() {
		int upperScrollStep = MioZoomerConfigManager.CONFIG.values.upper_scroll_steps.value();
		int lowerScrollStep = MioZoomerConfigManager.CONFIG.values.lower_scroll_steps.value();

		zoomStep = MathHelper.clamp(zoomStep, -lowerScrollStep, upperScrollStep);
	}

	// The method used for unbinding the "Save Toolbar Activator"
	public static void unbindConflictingKey(MinecraftClient client, boolean userPrompted) {
		if (ZoomKeyBinds.ZOOM_KEY.isDefault()) {
			if (client.options.saveToolbarActivatorKey.isDefault()) {
				if (userPrompted) {
					ZoomUtils.LOGGER.info("[Ok Zoomer] The \"Save Toolbar Activator\" keybind was occupying C! Unbinding...");
					client.getToastManager().add(SystemToast.create(
						client, SystemToast.Type.TUTORIAL_HINT, Text.translatable("toast.mio_zoomer.title"),
						Text.translatable("toast.mio_zoomer.unbind_conflicting_key.success")));
				} else {
					ZoomUtils.LOGGER.info("[Ok Zoomer] The \"Save Toolbar Activator\" keybind was occupying C! Unbinding... This process won't be repeated until specified in the config.");
				}
				client.options.saveToolbarActivatorKey.setBoundKey(InputUtil.UNKNOWN_KEY);
				client.options.write();
				KeyBind.updateBoundKeys();
			} else {
				ZoomUtils.LOGGER.info("[Ok Zoomer] No conflicts with the \"Save Toolbar Activator\" keybind were found!");
				if (userPrompted) {
					client.getToastManager().add(SystemToast.create(
						client, SystemToast.Type.TUTORIAL_HINT, Text.translatable("toast.mio_zoomer.title"),
						Text.translatable("toast.mio_zoomer.unbind_conflicting_key.no_conflict")));
				}
			}
		}
	}

	public static boolean shouldOpenCommandScreen() {
		return openCommandScreen;
	}

	public static void setOpenCommandScreen(boolean openCommandScreen) {
		ZoomUtils.openCommandScreen = openCommandScreen;
	}
}
