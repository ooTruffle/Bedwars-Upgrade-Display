package com.ootruffle.bedwarsupgradesdisplay;

import com.ootruffle.bedwarsupgradesdisplay.client.BwUpgradesConfig;
import com.ootruffle.bedwarsupgradesdisplay.client.BwUpgradesHud;
import net.ornithemc.osl.entrypoints.api.ModInitializer;
import org.polyfrost.oneconfig.api.commands.v1.CommandManager;
import org.polyfrost.oneconfig.api.hud.v1.HudManager;
import org.polyfrost.oneconfig.utils.v1.dsl.ScreensKt;

/**
 * OSL's (Ornithe Standard Library) own ModInitializer, not Fabric's
 * ClientModInitializer — pre-1.14 Ornithe targets use OSL's entrypoint
 * system instead, matching github.com/TomekoYT/HyBedWars's HyBedWars.kt for
 * its own "ornithe" build variant.
 */
public class BedwarsUpgradesDisplayEntrypoint implements ModInitializer {

    @Override
    public void init() {
        BwUpgradesConfig.INSTANCE.preload();

        HudManager.register(new BwUpgradesHud(), BwConstants.ID);

        // Chat is picked up via NetHandlerPlayClientMixin, so nothing to
        // register here for it.

        HypixelPackets.register();

        // "/bwupgradesdisplay" opens this mod's settings page directly.
        // The HUD is also editable/repositionable from OneConfig's own HUD
        // Design Studio regardless of this command.
        CommandManager.INSTANCE.register(
                ScreensKt.addDefaultCommand(BwUpgradesConfig.INSTANCE, BwConstants.ID)
        );

        System.out.println("[" + BwConstants.NAME + "] Initialized. Run /" + BwConstants.ID + " to open settings.");
    }
}
