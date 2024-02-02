package org.thinkingstudio.mio_zoomer.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import org.thinkingstudio.mio_zoomer.config.MioZoomerConfigManager;
import org.thinkingstudio.mio_zoomer.key_binds.ZoomKeyBinds;
import org.thinkingstudio.mio_zoomer.utils.ZoomUtils;
import net.minecraft.client.Mouse;
import org.thinkingstudio.mio_zoomer.config.ConfigEnums;

// This mixin is responsible for the mouse-behavior-changing part of the zoom
@Mixin(Mouse.class)
public abstract class MouseMixin {
	@Shadow
	private double scrollDelta;

	// Handles zoom scrolling
	@Inject(
		method = "onMouseScroll",
		at = @At(value = "FIELD", target = "Lnet/minecraft/client/Mouse;scrollDelta:D", ordinal = 7),
		cancellable = true
	)
	private void mio_zoomer$zoomerOnMouseScroll(CallbackInfo ci) {
		if (this.scrollDelta != 0.0) {
			if (MioZoomerConfigManager.ZOOM_SCROLLING.value()) {
				if (MioZoomerConfigManager.ZOOM_MODE.value().equals(ConfigEnums.ZoomModes.PERSISTENT)) {
					if (!ZoomKeyBinds.ZOOM_KEY.isPressed()) return;
				}

				if (ZoomUtils.ZOOMER_ZOOM.getZoom()) {
					ZoomUtils.changeZoomDivisor(this.scrollDelta > 0.0);
					ci.cancel();
				}
			}
		}
	}

	// Handles the zoom scrolling reset through the middle button
	@Inject(
		method = "onMouseButton",
		at = @At(value = "INVOKE", target = "net/minecraft/client/option/KeyBind.setKeyPressed(Lcom/mojang/blaze3d/platform/InputUtil$Key;Z)V"),
		cancellable = true,
		locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void mio_zoomer$zoomerOnMouseButton(long window, int button, int action, int modifiers, CallbackInfo ci, boolean bl, int i) {
		if (MioZoomerConfigManager.ZOOM_SCROLLING.value()) {
			if (MioZoomerConfigManager.ZOOM_MODE.value().equals(ConfigEnums.ZoomModes.PERSISTENT)) {
				if (!ZoomKeyBinds.ZOOM_KEY.isPressed()) return;
			}

			if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE && bl) {
				if (ZoomKeyBinds.ZOOM_KEY.isPressed()) {
					if (MioZoomerConfigManager.RESET_ZOOM_WITH_MOUSE.value()) {
						ZoomUtils.resetZoomDivisor(true);
						ci.cancel();
					}
				}
			}
		}
	}

	// Prevents the spyglass from working if zooming replaces its zoom
	@ModifyExpressionValue(
		method = "updateLookDirection",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingSpyglass()Z")
	)
	private boolean mio_zoomer$replaceSpyglassMouseMovement(boolean isUsingSpyglass) {
		if (switch (MioZoomerConfigManager.SPYGLASS_DEPENDENCY.value()) {
			case REPLACE_ZOOM, BOTH -> true;
			default -> false;
		}) {
			return false;
		}

		return isUsingSpyglass;
	}
}
