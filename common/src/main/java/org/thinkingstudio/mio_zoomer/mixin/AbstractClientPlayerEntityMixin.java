package org.thinkingstudio.mio_zoomer.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import org.thinkingstudio.mio_zoomer.config.MioZoomerConfigManager;
import net.minecraft.client.network.AbstractClientPlayerEntity;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class AbstractClientPlayerEntityMixin {
	@ModifyExpressionValue(
		method = "getSpeed",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;isUsingSpyglass()Z")
	)
	private boolean replaceSpyglassPlayerMovement(boolean isUsingSpyglass) {
		if (switch (MioZoomerConfigManager.CONFIG.features.spyglass_dependency.value()) {
			case REPLACE_ZOOM, BOTH -> true;
			default -> false;
		}) {
			return false;
		}

		return isUsingSpyglass;
	}
}
