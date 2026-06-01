package com.jai.drownfr.client;

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

    // Unique reference identifier for your custom Blockbench json file
    private static final ResourceLocation ANIMATION_ID = ResourceLocation.fromNamespaceAndPath("drown_for_real",
            "drown_panic");

    private static final WeakHashMap<LocalPlayer, ModifierLayer<IAnimation>> animationLayers = new WeakHashMap<>();

    public static void handleDrowningAnimation(LocalPlayer player) {
        if (player == null)
            return;

        ModifierLayer<IAnimation> animationLayer = animationLayers.computeIfAbsent(player, p -> {
            var animationStack = PlayerAnimationAccess.getPlayerAnimLayer(p);
            ModifierLayer<IAnimation> layer = new ModifierLayer<>();
            animationStack.addAnimLayer(1000, layer);
            return layer;
        });

        // 2. CHECK CONDITION: Submerged in water and out of oxygen bubbles
        if (player.isUnderWater() && player.getAirSupply() <= 0) {

            // If our track isn't playing yet, load it from the asset cache and fire it
            if (!animationLayer.isActive()) {
                // Pulls the compiled raw json animation data registered under our asset
                // namespace
                KeyframeAnimation animData = (KeyframeAnimation) PlayerAnimationRegistry.getAnimation(ANIMATION_ID);
                if (animData != null) {
                    animationLayer.setAnimation(new KeyframeAnimationPlayer(animData));
                }
            }
        } else {
            // 3. CLEANUP: If they reach safety or surface for air, stop the loop smoothly
            if (animationLayer.isActive()) {
                animationLayer.setAnimation(null);
            }
        }
    }
}