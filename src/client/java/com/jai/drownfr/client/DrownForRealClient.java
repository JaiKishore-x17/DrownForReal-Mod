package com.jai.drownfr.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
// Import your animation handler class if it's in a different package
// import com.jai.drownfr.DrowningAnimationHandler; 

public class DrownForRealClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Registering the end of the client tick loop
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) {
                // Replace this line with your actual method call that manages player-animation-lib states
                DrowningAnimationHandler.handleDrowningAnimation(client.player);
            }
        });
    }
}