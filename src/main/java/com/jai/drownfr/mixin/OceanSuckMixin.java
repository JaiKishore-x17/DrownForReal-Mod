package com.jai.drownfr.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class OceanSuckMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void injectOceanSuckForce(CallbackInfo ci) {
        Player player = (Player) (Object) this;

        // Check if the player is fully submerged and out of oxygen
        if (player.isUnderWater() && player.getAirSupply() <= 0) {
            
            Vec3 currentVelocity = player.getDeltaMovement();

            // Modifies the movement vector: slowly dragging them down along the Y axis
            // and dampening X/Z speed to simulate an inability to swim forward efficiently
            player.setDeltaMovement(
                currentVelocity.x * 0.82, 
                currentVelocity.y - 0.038, 
                currentVelocity.z * 0.82
            );
        }
    }
}