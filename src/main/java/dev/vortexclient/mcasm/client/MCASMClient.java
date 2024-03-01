package dev.vortexclient.mcasm.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MCASMClient implements ClientModInitializer {
    private static final Logger LOGGER = LogManager.getLogger("ASM");
    @Override
    public void onInitializeClient() {
        LOGGER.info("Registering events");

        // Register events
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Run code every tick
        });
        LOGGER.info("Registered Tick Event (1/8)");
    }
}
