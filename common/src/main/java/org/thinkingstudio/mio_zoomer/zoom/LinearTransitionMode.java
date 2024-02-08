package org.thinkingstudio.mio_zoomer.zoom;

import org.thinkingstudio.mio_zoomer.MioZoomerClientMod;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.thinkingstudio.zoomerlibrary.api.TransitionMode;

// The implementation of the linear transition
public class LinearTransitionMode implements TransitionMode {
    private static final Identifier TRANSITION_ID = new Identifier(MioZoomerClientMod.MODID + ":linear_transition");
    private boolean active;
    private final double minimumLinearStep;
    private final double maximumLinearStep;
    private double fovMultiplier;
    private float internalMultiplier;
    private float lastInternalMultiplier;

    public LinearTransitionMode(double minimumLinearStep, double maximumLinearStep) {
        this.active = false;
        this.minimumLinearStep = minimumLinearStep;
        this.maximumLinearStep = maximumLinearStep;
        this.internalMultiplier = 1.0F;
        this.lastInternalMultiplier = 1.0F;
    }

    @Override
    public Identifier getIdentifier() {
        return TRANSITION_ID;
    }

    @Override
    public boolean getActive() {
        return this.active;
    }

    @Override
    public double applyZoom(double fov, float tickDelta) {
        fovMultiplier = MathHelper.lerp(tickDelta, this.lastInternalMultiplier, this.internalMultiplier);
        return fov * fovMultiplier;
    }

    @Override
    public void tick(boolean active, double divisor) {
        double zoomMultiplier = 1.0D / divisor;

        this.lastInternalMultiplier = this.internalMultiplier;

        double linearStep = MathHelper.clamp(zoomMultiplier, this.minimumLinearStep, this.maximumLinearStep);
        this.internalMultiplier = MathHelper.stepTowards(this.internalMultiplier, (float)zoomMultiplier, (float)linearStep);

        if (active || fovMultiplier == this.internalMultiplier) {
            this.active = active;
        }
    }

    @Override
    public double getInternalMultiplier() {
        return this.internalMultiplier;
    }
}
