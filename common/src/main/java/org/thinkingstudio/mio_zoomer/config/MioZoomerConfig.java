package org.thinkingstudio.mio_zoomer.config;

import ho.artisan.azusa_config.shadow.quiltconfig.api.WrappedConfig;
import ho.artisan.azusa_config.shadow.quiltconfig.api.annotations.Comment;
import ho.artisan.azusa_config.shadow.quiltconfig.api.annotations.FloatRange;
import ho.artisan.azusa_config.shadow.quiltconfig.api.annotations.IntegerRange;

import org.thinkingstudio.mio_zoomer.config.metadata.WidgetSize;

public class MioZoomerConfig extends WrappedConfig {
	@Comment("Contains the main zoom features.")
	public final FeaturesConfig features = new FeaturesConfig();

	@Comment("Contains precise configuration of the zoom.")
	public final ValuesConfig values = new ValuesConfig();

	@Comment("Contains options that are unlikely to be changed from the defaults.")
	public final TweaksConfig tweaks = new TweaksConfig();

	public class FeaturesConfig implements Section {
		@WidgetSize(WidgetSize.Size.HALF)
		@Comment("""
			Defines the cinematic camera while zooming.
			"OFF" disables the cinematic camera.
			"VANILLA" uses Vanilla's cinematic camera.
			"MULTIPLIED" is a multiplied variant of "VANILLA".
			""")
		public final ConfigEnums.CinematicCameraOptions cinematic_camera = ConfigEnums.CinematicCameraOptions.OFF;

		@WidgetSize(WidgetSize.Size.HALF)
		@Comment("Reduces the mouse sensitivity when zooming.")
		public final boolean reduce_sensitivity = true;

		@WidgetSize(WidgetSize.Size.FULL)
		@Comment("""
			Adds transitions between zooms.
			"OFF" disables transitions.
			"SMOOTH" replicates Vanilla's dynamic FOV.
			"LINEAR" removes the smoothiness.
			""")
		public final ConfigEnums.ZoomTransitionOptions zoom_transition = ConfigEnums.ZoomTransitionOptions.SMOOTH;

		@WidgetSize(WidgetSize.Size.HALF)
		@Comment("""
			The behavior of the zoom key.
			"HOLD" needs the zoom key to be hold.
			"TOGGLE" has the zoom key toggle the zoom.
			"PERSISTENT" makes the zoom permanent.
			""")
		public final ConfigEnums.ZoomModes zoom_mode = ConfigEnums.ZoomModes.HOLD;

		@WidgetSize(WidgetSize.Size.HALF)
		@Comment("Allows to increase or decrease zoom by scrolling.")
		public final boolean zoom_scrolling = true;

		@WidgetSize(WidgetSize.Size.HALF)
		@Comment("Adds zoom manipulation keys along with the zoom key.")
		public final boolean extra_key_binds = true;

		@WidgetSize(WidgetSize.Size.HALF)
		@Comment("""
			Adds an overlay in the screen during zoom.
			"VIGNETTE" uses a vignette as the overlay.
			"SPYGLASS" uses the spyglass overlay with the vignette texture.
			The vignette texture can be found at: assets/mio_zoomer/textures/misc/zoom_overlay.png
			""")
		public final ConfigEnums.ZoomOverlays zoom_overlay = ConfigEnums.ZoomOverlays.OFF;

		@WidgetSize(WidgetSize.Size.FULL)
		@Comment("""
			Determines how the zoom will depend on the spyglass.
			"REQUIRE_ITEM" will make zooming require a spyglass.
			"REPLACE_ZOOM" will replace spyglass's zoom with Mio Zoomer's zoom.
			"BOTH" will apply both options at the same time.
			The "REQUIRE_ITEM" option is configurable through the mio_zoomer:zoom_dependencies item tag.
			""")
		public final ConfigEnums.SpyglassDependency spyglass_dependency = ConfigEnums.SpyglassDependency.OFF;
	}

	public class ValuesConfig implements Section  {
		@WidgetSize(WidgetSize.Size.FULL)
		@Comment("The divisor applied to the FOV when zooming.")
		@FloatRange(min = Double.MIN_NORMAL, max = Double.MAX_VALUE)
		public final double zoom_divisor = 4.0;

		@WidgetSize(WidgetSize.Size.HALF)
		@Comment("The minimum value that you can scroll down.")
		@FloatRange(min = Double.MIN_NORMAL, max = Double.MAX_VALUE)
		public final double minimum_zoom_divisor = 1.0;

		@WidgetSize(WidgetSize.Size.HALF)
		@Comment("The maximum value that you can scroll down.")
		@FloatRange(min = Double.MIN_NORMAL, max = Double.MAX_VALUE)
		public final double maximum_zoom_divisor = 50.0;

		@WidgetSize(WidgetSize.Size.HALF)
		@Comment("""
			The number of steps between the zoom divisor and the maximum zoom divisor.
			Used by zoom scrolling.
			""")
		@IntegerRange(min = 0, max = Integer.MAX_VALUE)
		public final int upper_scroll_steps = 10;

		@WidgetSize(WidgetSize.Size.HALF)
		@Comment("""
			The number of steps between the zoom divisor and the minimum zoom divisor.
			Used by zoom scrolling.
			""")
		@IntegerRange(min = 0, max = Integer.MAX_VALUE)
		public final int lower_scroll_steps = 5;

		@WidgetSize(WidgetSize.Size.HALF)
		@Comment("The multiplier used for smooth transitions.")
		@FloatRange(min = Double.MIN_NORMAL, max = 1.0)
		public final double smooth_multiplier = 0.75;

		@WidgetSize(WidgetSize.Size.HALF)
		@Comment("The multiplier used for the multiplied cinematic camera.")
		@FloatRange(min = Double.MIN_NORMAL, max = 32.0)
		public final double cinematic_multiplier = 4.0;

		@WidgetSize(WidgetSize.Size.HALF)
		@Comment("The minimum value which the linear transition step can reach.")
		@FloatRange(min = 0.0, max = Double.MAX_VALUE)
		public final double minimum_linear_step = 0.125;

		@WidgetSize(WidgetSize.Size.HALF)
		@Comment("The maximum value which the linear transition step can reach.")
		@FloatRange(min = 0.0, max = Double.MAX_VALUE)
		public final double maximum_linear_step = 0.25;
	}

	public class TweaksConfig implements Section  {
		@WidgetSize(WidgetSize.Size.HALF)
		@Comment("Allows for resetting the zoom with the middle mouse button.")
		public final boolean reset_zoom_with_mouse = true;

		@WidgetSize(WidgetSize.Size.HALF)
		@Comment("If enabled, the current zoom divisor is forgotten once zooming is done.")
		public final boolean forget_zoom_divisor = true;

		@WidgetSize(WidgetSize.Size.FULL)
		@Comment("If pressed, the \"Save Toolbar Activator\" keybind will be unbound if there's a conflict with the zoom key.")
		public final boolean unbind_conflicting_key = true;

		@WidgetSize(WidgetSize.Size.HALF)
		@Comment("If enabled, the spyglass overlay texture is used instead of Mio Zoomer's overlay texture.")
		public final boolean use_spyglass_texture = false;

		@WidgetSize(WidgetSize.Size.HALF)
		@Comment("If enabled, the zoom will use spyglass sounds on zooming in and out.")
		public final boolean use_spyglass_sounds = false;

		@WidgetSize(WidgetSize.Size.HALF)
		@Comment("Shows toasts when the server imposes a restriction.")
		public final boolean show_restriction_toasts = true;

		// TODO - Enable it again during eternal betas!
		@WidgetSize(WidgetSize.Size.HALF)
		@Comment("Prints a random owo in the console when the game starts.")
		public final boolean print_owo_on_start = false;
	}

	// TODO - What if we had a secret Debug section?
}
