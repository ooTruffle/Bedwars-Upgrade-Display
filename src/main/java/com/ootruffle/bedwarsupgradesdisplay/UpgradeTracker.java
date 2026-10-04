package com.ootruffle.bedwarsupgradesdisplay;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class UpgradeTracker
{
    private static final Map<String, Integer> upgrades;

    public static void addOrUpdateUpgrade(final String upgradeName, final int tier) {
        if (upgradeName.contains("Forge")) {
            UpgradeTracker.upgrades.keySet().removeIf(UpgradeTracker::isForgeUpgrade);
        }
        if (UpgradeTracker.upgrades.containsKey(upgradeName)) {
            final int currentTier = UpgradeTracker.upgrades.get(upgradeName);
            if (tier > currentTier) {
                UpgradeTracker.upgrades.put(upgradeName, tier);
            }
        }
        else {
            UpgradeTracker.upgrades.put(upgradeName, tier);
        }
    }

    private static boolean isForgeUpgrade(final String upgradeName) {
        return upgradeName.contains("Forge");
    }

    public static Map<String, Integer> getUpgrades() {
        return UpgradeTracker.upgrades;
    }

    static {
        upgrades = new ConcurrentHashMap<String, Integer>();
    }
}
