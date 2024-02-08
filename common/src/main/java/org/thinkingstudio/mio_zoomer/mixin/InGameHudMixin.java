package org.thinkingstudio.mio_zoomer.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import org.thinkingstudio.mio_zoomer.config.MioZoomerConfigManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.gui.hud.InGameHud;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
	@ModifyExpressionValue(
		method = "render",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingSpyglass()Z")
	)
	private boolean activateSpyglassOverlay(boolean isUsingSpyglass) {
		if (switch (MioZoomerConfigManager.CONFIG.features.spyglass_dependency.value()) {
			case REPLACE_ZOOM, BOTH -> true;
			default -> false;
		}) {
			return false;
		}

		return isUsingSpyglass;
	}
}
