package org.thinkingstudio.mio_zoomer.key_binds;

import com.mojang.blaze3d.platform.InputUtil;

import org.lwjgl.glfw.GLFW;

import org.thinkingstudio.mio_zoomer.config.MioZoomerConfigManager;
import net.minecraft.client.option.KeyBind;

// Manages the zoom key binds themselves
public class ZoomKeyBinds {
	// TODO - Bleh, immutability; I have a plan
	private static final boolean ENABLE_EXTRA_KEY_BINDS = MioZoomerConfigManager.EXTRA_KEY_BINDS.getRealValue();

	// The "Zoom" category
	public static final String ZOOM_CATEGORY = "key.mio_zoomer.category";

	// The zoom key bind, which will be registered
	public static final KeyBind ZOOM_KEY = new KeyBind("key.mio_zoomer.zoom", GLFW.GLFW_KEY_C, ZOOM_CATEGORY);

	// The "Decrease Zoom" key bind
	public static final KeyBind DECREASE_ZOOM_KEY = getExtraKeyBind("key.mio_zoomer.decrease_zoom");

	// The "Increase Zoom" key bind
	public static final KeyBind INCREASE_ZOOM_KEY = getExtraKeyBind("key.mio_zoomer.increase_zoom");

	// The "Reset Zoom" key bind
	public static final KeyBind RESET_ZOOM_KEY = getExtraKeyBind("key.mio_zoomer.reset_zoom");

	// The method used to check if the zoom manipulation key binds should be disabled, can be used by other mods.
	public static boolean areExtraKeyBindsEnabled() {
		return ZoomKeyBinds.ENABLE_EXTRA_KEY_BINDS;
	}

	// The method used to get the extra keybinds, if disabled, return null.
	public static KeyBind getExtraKeyBind(String translationKey) {
		if (ZoomKeyBinds.areExtraKeyBindsEnabled()) {
			return new KeyBind(translationKey, InputUtil.UNKNOWN_KEY.getKeyCode(), ZOOM_CATEGORY);
		}

		return null;
	}
}
