package com.jai.drownfr.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import java.util.WeakHashMap;
import dev.kosmx.playerAnim.core.util.Ease;

public class DrowningAnimationHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final ResourceLocation ANIMATION_ID = ResourceLocation.fromNamespaceAndPath("drown_for_real", "drown_panic");

    private static final WeakHashMap<LocalPlayer, ModifierLayer<IAnimation>> animationLayers = new WeakHashMap<>();
    private static final WeakHashMap<LocalPlayer, Boolean> isAnimatingState = new WeakHashMap<>();

    public static void handleDrowningAnimation(LocalPlayer player) {
        if (player == null)
            return;

        if (!player.isAlive()) {
            animationLayers.remove(player);
            isAnimatingState.remove(player);
            return;
        }

        ModifierLayer<IAnimation> animationLayer = animationLayers.computeIfAbsent(player, p -> {
            var animationStack = PlayerAnimationAccess.getPlayerAnimLayer(p);
            ModifierLayer<IAnimation> layer = new ModifierLayer<>();
            animationStack.addAnimLayer(1000, layer);
            return layer;
        });

        boolean isDrowning = player.isUnderWater() && player.getAirSupply() <= 0;
        boolean wasAnimating = isAnimatingState.getOrDefault(player, false);

        if (isDrowning && !wasAnimating) {
            LOGGER.info("Drowning condition met for player {}: underWater={}, airSupply={}",
                    player.getName().getString(), player.isUnderWater(), player.getAirSupply());

            LOGGER.info("Loading animation for {}", ANIMATION_ID);
            KeyframeAnimation animData = (KeyframeAnimation) PlayerAnimationRegistry.getAnimation(ANIMATION_ID);
            if (animData != null) {
                LOGGER.info("Animation data loaded successfully, starting animation with fade-in.");
                
                // FIX: Use replaceAnimationWithFade to handle the smooth transition over 10 ticks
                animationLayer.replaceAnimationWithFade(AbstractFadeModifier.standardFadeIn(10, Ease.LINEAR), new KeyframeAnimationPlayer(animData));
                isAnimatingState.put(player, true);
            } else {
                LOGGER.warn("Failed to load animation data for {}", ANIMATION_ID);
            }
        } else if (!isDrowning && wasAnimating) {
            LOGGER.info("Player no longer drowning, stopping animation with fade-out.");
            
            // FIX: Smoothly fade out to null (nothing) over 8 ticks instead of popping off
            animationLayer.replaceAnimationWithFade(AbstractFadeModifier.standardFadeIn(8, Ease.LINEAR), null);
            isAnimatingState.put(player, false);
        }
    }
}