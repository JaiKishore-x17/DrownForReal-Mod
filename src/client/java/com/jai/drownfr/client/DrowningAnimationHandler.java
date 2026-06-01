package com.jai.drownfr.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import java.util.WeakHashMap;

public class DrowningAnimationHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    
    private static final ResourceLocation ANIMATION_ID = ResourceLocation.fromNamespaceAndPath("drown_for_real",
            "drown_panic");

    private static final WeakHashMap<LocalPlayer, ModifierLayer<IAnimation>> animationLayers = new WeakHashMap<>();

    public static void handleDrowningAnimation(LocalPlayer player) {
        if (player == null)
            return;

        if (!player.isAlive()) {
            animationLayers.remove(player);
            return;
        }

        ModifierLayer<IAnimation> animationLayer = animationLayers.computeIfAbsent(player, p -> {
            var animationStack = PlayerAnimationAccess.getPlayerAnimLayer(p);
            ModifierLayer<IAnimation> layer = new ModifierLayer<>();
            animationStack.addAnimLayer(1000, layer);
            return layer;
        });

        // 2. CHECK CONDITION: Submerged in water and out of oxygen bubbles
        if (player.isUnderWater() && player.getAirSupply() <= 0) {
            LOGGER.info("Drowning condition met for player {}: underWater={}, airSupply={}",
                    player.getName().getString(), player.isUnderWater(), player.getAirSupply());

            // If our track isn't playing yet, load it from the asset cache and fire it
            if (!animationLayer.isActive()) {
                LOGGER.info("Animation layer inactive, loading animation for {}", ANIMATION_ID);
                // Pulls the compiled raw json animation data registered under our asset
                // namespace
                KeyframeAnimation animData = (KeyframeAnimation) PlayerAnimationRegistry.getAnimation(ANIMATION_ID);
                if (animData != null) {
                    LOGGER.info("Animation data loaded successfully, starting animation.");
                    animationLayer.setAnimation(new KeyframeAnimationPlayer(animData));
                } else {
                    LOGGER.warn("Failed to load animation data for {}", ANIMATION_ID);
                }
            }
        } else {
            // 3. CLEANUP: If they reach safety or surface for air, stop the loop smoothly
            if (animationLayer.isActive()) {
                LOGGER.info("Player no longer drowning, stopping animation.");
                animationLayer.setAnimation(null);
            }
        }
    }
}