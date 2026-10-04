package com.ootruffle.bedwarsupgradesdisplay;

import com.ootruffle.bedwarsupgradesdisplay.client.BwUpgradesConfig;
import net.hypixel.modapi.HypixelModAPI;
import net.hypixel.modapi.packet.impl.clientbound.ClientboundHelloPacket;
import net.hypixel.modapi.packet.impl.clientbound.event.ClientboundLocationPacket;
import net.ornithemc.osl.networking.api.client.ClientConnectionEvents;

/**
 * Tells us reliably whether the player is currently in a Bedwars game, using
 * Hypixel's own official Mod API instead of matching on chat text. Ported
 * from github.com/TomekoYT/HyBedWars's HypixelPackets.kt.
 *
 * Leaving Bedwars (or disconnecting) only HIDES the HUD — it does not clear
 * the tracked upgrades/traps. When a Bedwars location packet arrives again,
 * we compare the Hypixel server name (e.g. "mini123A") with the one we left:
 * same server within the configured grace period means you rejoined the same
 * game, so the data is kept; anything else is a different/new game, so it's
 * cleared before tracking starts again.
 */
public class HypixelPackets {

    private static volatile boolean inBedwars = false;

    // Guarded by HypixelPackets.class
    private static String lastServerName = null;
    private static long leftAtMillis = 0L;

    public static boolean isInBedwars() {
        return inBedwars;
    }

    public static void register() {
        HypixelModAPI.getInstance().createHandler(ClientboundHelloPacket.class, packet -> {});
        HypixelModAPI.getInstance().createHandler(ClientboundLocationPacket.class, HypixelPackets::onLocationPacket);
        HypixelModAPI.getInstance().subscribeToEventPacket(ClientboundLocationPacket.class);

        // DISCONNECT is Event<Consumer<DisconnectContext>> in OSL's source.
        ClientConnectionEvents.DISCONNECT.register(context -> leaveBedwars());
    }

    private static void onLocationPacket(ClientboundLocationPacket packet) {
        boolean bedwars = packet.getServerType().isPresent()
                && "Bed Wars".equals(packet.getServerType().get().getName());
        if (bedwars) {
            enterBedwars(packet.getServerName());
        } else {
            leaveBedwars();
        }
    }

    private static synchronized void enterBedwars(String serverName) {
        if (inBedwars && serverName.equals(lastServerName)) {
            return; // just a mode/map update for the game we're already in
        }

        long graceMillis = (long) (BwUpgradesConfig.INSTANCE.rememberSeconds * 1000f);
        boolean sameGame = !inBedwars
                && serverName.equals(lastServerName)
                && System.currentTimeMillis() - leftAtMillis <= graceMillis;

        if (!sameGame) {
            UpgradeTracker.getUpgrades().clear();
            TrapTracker.getTraps().clear();
        }

        lastServerName = serverName;
        inBedwars = true;
    }

    private static synchronized void leaveBedwars() {
        if (!inBedwars) {
            return;
        }
        inBedwars = false; // hides the HUD; data is intentionally kept
        leftAtMillis = System.currentTimeMillis();
    }
}
