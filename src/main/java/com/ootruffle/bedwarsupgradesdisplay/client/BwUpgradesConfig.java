package com.ootruffle.bedwarsupgradesdisplay.client;

import org.polyfrost.oneconfig.api.config.v1.Config;
import org.polyfrost.oneconfig.api.config.v1.annotations.Slider;
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch;

/**
 * OneConfig V1 config (org.polyfrost.oneconfig), used here because V1 does
 * publish a 1.8.9 build for Fabric/Ornithe (as "1.8.9-ornithe") even though
 * it has no Forge 1.8.9 build.
 *
 * preload() must be called once during client init (see
 * BedwarsUpgradesDisplayEntrypoint) — unlike V0, the constructor itself
 * doesn't load anything from disk.
 */
public class BwUpgradesConfig extends Config {

    @Switch(title = "Enable Mod")
    public boolean displayEnabled = true;

    @Slider(
            title = "Remember Upgrades After Leaving (seconds)",
            description = "If you rejoin the same Bedwars game within this time, your upgrades and traps are kept. 0 = always clear on leaving.",
            min = 0f,
            max = 600f,
            step = 1f,
            subcategory = "Display"
    )
    public float rememberSeconds = 120f;

    public static final BwUpgradesConfig INSTANCE = new BwUpgradesConfig();

    private BwUpgradesConfig() {
        super("bwupgradesdisplay.json", "Bedwars Upgrades Display", Category.HYPIXEL);
    }
}
