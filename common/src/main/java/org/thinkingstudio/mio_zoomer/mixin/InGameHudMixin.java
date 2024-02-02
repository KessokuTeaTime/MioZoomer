package org.thinkingstudio.mio_zoomer.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import org.thinkingstudio.mio_zoomer.config.MioZoomerConfigManager;
import net.minecraft.client.gui.hud.InGameHud;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
	@ModifyExpressionValue(
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingSpyglass()Z"),
		method = "render"
	)
	private boolean mio_zoomer$activateSpyglassOverlay(boolean isUsingSpyglass) {
		if (switch (MioZoomerConfigManager.SPYGLASS_DEPENDENCY.value()) {
			case REPLACE_ZOOM, BOTH -> true;
			default -> false;
		}) {
			return false;
		}

		return isUsingSpyglass;
	}
}
