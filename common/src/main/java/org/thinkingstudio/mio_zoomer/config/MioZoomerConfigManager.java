package org.thinkingstudio.mio_zoomer.config;

import java.util.List;

import ho.artisan.azusaconfig.api.config.AzusaConfig;
import ho.artisan.azusa_config.shadow.quiltconfig.api.values.TrackedValue;

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

	// Features
	public static final TrackedValue<ConfigEnums.CinematicCameraOptions> CINEMATIC_CAMERA = (TrackedValue<ConfigEnums.CinematicCameraOptions>) CONFIG.getValue(List.of("features", "cinematic_camera"));
	public static final TrackedValue<Boolean> REDUCE_SENSITIVITY = (TrackedValue<Boolean>) CONFIG.getValue(List.of("features", "reduce_sensitivity"));
	public static final TrackedValue<ConfigEnums.ZoomTransitionOptions> ZOOM_TRANSITION = (TrackedValue<ConfigEnums.ZoomTransitionOptions>) CONFIG.getValue(List.of("features", "zoom_transition"));
	public static final TrackedValue<ConfigEnums.ZoomModes> ZOOM_MODE = (TrackedValue<ConfigEnums.ZoomModes>) CONFIG.getValue(List.of("features", "zoom_mode"));
	public static final TrackedValue<Boolean> ZOOM_SCROLLING = (TrackedValue<Boolean>) CONFIG.getValue(List.of("features", "zoom_scrolling"));
	public static final TrackedValue<Boolean> EXTRA_KEY_BINDS = (TrackedValue<Boolean>) CONFIG.getValue(List.of("features", "extra_key_binds"));
	public static final TrackedValue<ConfigEnums.ZoomOverlays> ZOOM_OVERLAY = (TrackedValue<ConfigEnums.ZoomOverlays>) CONFIG.getValue(List.of("features", "zoom_overlay"));
	public static final TrackedValue<ConfigEnums.SpyglassDependency> SPYGLASS_DEPENDENCY = (TrackedValue<ConfigEnums.SpyglassDependency>) CONFIG.getValue(List.of("features", "spyglass_dependency"));

	// Values
	public static final TrackedValue<Double> ZOOM_DIVISOR = (TrackedValue<Double>) CONFIG.getValue(List.of("values", "zoom_divisor"));
	public static final TrackedValue<Double> MINIMUM_ZOOM_DIVISOR = (TrackedValue<Double>) CONFIG.getValue(List.of("values", "minimum_zoom_divisor"));
	public static final TrackedValue<Double> MAXIMUM_ZOOM_DIVISOR = (TrackedValue<Double>) CONFIG.getValue(List.of("values", "maximum_zoom_divisor"));
	public static final TrackedValue<Integer> UPPER_SCROLL_STEPS = (TrackedValue<Integer>) CONFIG.getValue(List.of("values", "upper_scroll_steps"));
	public static final TrackedValue<Integer> LOWER_SCROLL_STEPS = (TrackedValue<Integer>) CONFIG.getValue(List.of("values", "lower_scroll_steps"));
	public static final TrackedValue<Double> SMOOTH_MULTIPLIER = (TrackedValue<Double>) CONFIG.getValue(List.of("values", "smooth_multiplier"));
	public static final TrackedValue<Double> CINEMATIC_MULTIPLIER = (TrackedValue<Double>) CONFIG.getValue(List.of("values", "cinematic_multiplier"));
	public static final TrackedValue<Double> MINIMUM_LINEAR_STEP = (TrackedValue<Double>) CONFIG.getValue(List.of("values", "minimum_linear_step"));
	public static final TrackedValue<Double> MAXIMUM_LINEAR_STEP = (TrackedValue<Double>) CONFIG.getValue(List.of("values", "maximum_linear_step"));

	// Tweaks
	public static final TrackedValue<Boolean> RESET_ZOOM_WITH_MOUSE = (TrackedValue<Boolean>) CONFIG.getValue(List.of("tweaks", "reset_zoom_with_mouse"));
	public static final TrackedValue<Boolean> FORGET_ZOOM_DIVISOR = (TrackedValue<Boolean>) CONFIG.getValue(List.of("tweaks", "forget_zoom_divisor"));
	public static final TrackedValue<Boolean> UNBIND_CONFLICTING_KEY = (TrackedValue<Boolean>) CONFIG.getValue(List.of("tweaks", "unbind_conflicting_key"));
	public static final TrackedValue<Boolean> USE_SPYGLASS_TEXTURE = (TrackedValue<Boolean>) CONFIG.getValue(List.of("tweaks", "use_spyglass_texture"));
	public static final TrackedValue<Boolean> USE_SPYGLASS_SOUNDS = (TrackedValue<Boolean>) CONFIG.getValue(List.of("tweaks", "use_spyglass_sounds"));
	public static final TrackedValue<Boolean> SHOW_RESTRICTION_TOASTS = (TrackedValue<Boolean>) CONFIG.getValue(List.of("tweaks", "show_restriction_toasts"));
	public static final TrackedValue<Boolean> PRINT_OWO_ON_START = (TrackedValue<Boolean>) CONFIG.getValue(List.of("tweaks", "print_owo_on_start"));


	public MioZoomerConfigManager() {
		// On initialization, configure our zoom instance
		MioZoomerConfigManager.configureZoomInstance();

		CONFIG.registerCallback(config -> {
			MioZoomerConfigManager.configureZoomInstance();
		});
	}

	public static void configureZoomInstance() {
		// Sets zoom transition
		ZoomUtils.ZOOMER_ZOOM.setTransitionMode(
			switch (ZOOM_TRANSITION.value()) {
				case SMOOTH -> new SmoothTransitionMode(SMOOTH_MULTIPLIER.value().floatValue());
				case LINEAR -> new LinearTransitionMode(MINIMUM_LINEAR_STEP.value(), MAXIMUM_LINEAR_STEP.value());
				default -> new InstantTransitionMode();
			}
		);

		// Sets zoom divisor
		ZoomUtils.ZOOMER_ZOOM.setDefaultZoomDivisor(ZOOM_DIVISOR.value());

		// Sets mouse modifier
		configureZoomModifier();

		// Sets zoom overlay
		Identifier overlayTextureId = new Identifier(
			USE_SPYGLASS_TEXTURE.value()
			? "textures/misc/spyglass_scope.png"
			: "mio_zoomer:textures/misc/zoom_overlay.png");

		ZoomUtils.ZOOMER_ZOOM.setZoomOverlay(
			switch (ZOOM_OVERLAY.value()) {
				case VIGNETTE -> new ZoomerZoomOverlay(overlayTextureId);
				case SPYGLASS -> new SpyglassZoomOverlay(overlayTextureId);
				default -> null;
			}
		);
	}

	public static void configureZoomModifier() {
		ConfigEnums.CinematicCameraOptions cinematicCamera = CINEMATIC_CAMERA.value();
		boolean reduceSensitivity = REDUCE_SENSITIVITY.value();
		if (cinematicCamera != ConfigEnums.CinematicCameraOptions.OFF) {
			MouseModifier cinematicModifier = switch (cinematicCamera) {
				case VANILLA -> new CinematicCameraMouseModifier();
				case MULTIPLIED -> new MultipliedCinematicCameraMouseModifier(CINEMATIC_MULTIPLIER.value());
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
