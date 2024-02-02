package org.thinkingstudio.mio_zoomer.config;

import java.util.List;

import ho.artisan.azusa_config.shadow.quiltconfig.api.values.TrackedValue;

import ho.artisan.azusaconfig.api.config.v2.AzusaConfig;
import org.thinkingstudio.mio_zoomer.utils.ZoomUtils;
import org.thinkingstudio.mio_zoomer.zoom.LinearTransitionMode;
import org.thinkingstudio.mio_zoomer.zoom.MultipliedCinematicCameraMouseModifier;
import org.thinkingstudio.mio_zoomer.zoom.ZoomerZoomOverlay;
import org.thinkingstudio.zoomerlibrary.api.MouseModifier;
import org.thinkingstudio.zoomerlibrary.api.modifiers.CinematicCameraMouseModifier;
import org.thinkingstudio.zoomerlibrary.api.modifiers.ContainingMouseModifier;
import org.thinkingstudio.zoomerlibrary.api.modifiers.ZoomDivisorMouseModifier;
import org.thinkingstudio.zoomerlibrary.api.overlays.SpyglassZoomOverlay;
import org.thinkingstudio.zoomerlibrary.api.transitions.InstantTransitionMode;
import org.thinkingstudio.zoomerlibrary.api.transitions.SmoothTransitionMode;
import net.minecraft.util.Identifier;

@SuppressWarnings("unchecked")
public class MioZoomerConfigManager {
	public static final MioZoomerConfig CONFIG = AzusaConfig.create("mio_zoomer", "config", MioZoomerConfig.class);

	public MioZoomerConfigManager() {
		// On initialization, configure our zoom instance
		MioZoomerConfigManager.configureZoomInstance();

		CONFIG.registerCallback(config -> MioZoomerConfigManager.configureZoomInstance());
	}

	public static void configureZoomInstance() {
		// Sets zoom transition
		ZoomUtils.ZOOMER_ZOOM.setTransitionMode(
			switch (CONFIG.features.zoom_transition.value()) {
				case SMOOTH -> new SmoothTransitionMode(CONFIG.values.smooth_multiplier.value().floatValue());
				case LINEAR -> new LinearTransitionMode(CONFIG.values.minimum_linear_step.value(), CONFIG.values.maximum_linear_step.value());
				default -> new InstantTransitionMode();
			}
		);

		// Sets zoom divisor
		ZoomUtils.ZOOMER_ZOOM.setDefaultZoomDivisor(CONFIG.values.zoom_divisor.value());

		// Sets mouse modifier
		configureZoomModifier();

		// Sets zoom overlay
		Identifier overlayTextureId = new Identifier(
			CONFIG.tweaks.use_spyglass_texture.value()
			? "textures/misc/spyglass_scope.png"
			: "mio_zoomer:textures/misc/zoom_overlay.png");

		ZoomUtils.ZOOMER_ZOOM.setZoomOverlay(
			switch (CONFIG.features.zoom_overlay.value()) {
				case VIGNETTE -> new ZoomerZoomOverlay(overlayTextureId);
				case SPYGLASS -> new SpyglassZoomOverlay(overlayTextureId);
				default -> null;
			}
		);
	}

	public static void configureZoomModifier() {
		ConfigEnums.CinematicCameraOptions cinematicCamera = CONFIG.features.cinematic_camera.value();
		boolean reduceSensitivity = CONFIG.features.reduce_sensitivity.value();
		if (cinematicCamera != ConfigEnums.CinematicCameraOptions.OFF) {
			MouseModifier cinematicModifier = switch (cinematicCamera) {
				case VANILLA -> new CinematicCameraMouseModifier();
				case MULTIPLIED -> new MultipliedCinematicCameraMouseModifier(CONFIG.values.cinematic_multiplier.value());
				default -> null;
			};
			ZoomUtils.ZOOMER_ZOOM.setMouseModifier(reduceSensitivity
				? new ContainingMouseModifier(cinematicModifier, new ZoomDivisorMouseModifier())
				: cinematicModifier
			);
		} else {
			ZoomUtils.ZOOMER_ZOOM.setMouseModifier(reduceSensitivity ? new ZoomDivisorMouseModifier() : null);
		}
	}
}
