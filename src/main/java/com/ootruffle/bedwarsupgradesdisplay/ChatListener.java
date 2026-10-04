package com.ootruffle.bedwarsupgradesdisplay;

public class ChatListener {

    public static void onChatReceived(final String message) {
        if (message.contains("1st Killer")) {
            System.out.println("[" + BwConstants.NAME + "] Game ended. Resetting upgrades.");
            UpgradeTracker.getUpgrades().clear();
            TrapTracker.getTraps().clear();
        }
        processUpgrade(message);
    }

    private static void processUpgrade(final String message) {
        if (message.contains("Sharpened Swords")) {
            UpgradeTracker.addOrUpdateUpgrade("Sharpened Swords", 0);
        }
        if (message.contains("Sharpened Swords") && message.endsWith("II")) {
            UpgradeTracker.addOrUpdateUpgrade("Sharpened Swords", 2);
        }
        if (message.contains("Reinforced Armor") && message.endsWith("I")) {
            UpgradeTracker.addOrUpdateUpgrade("Reinforced Armor", 1);
        }
        if (message.contains("Reinforced Armor") && message.endsWith("II")) {
            UpgradeTracker.addOrUpdateUpgrade("Reinforced Armor", 2);
        }
        if (message.contains("Reinforced Armor") && message.endsWith("III")) {
            UpgradeTracker.addOrUpdateUpgrade("Reinforced Armor", 3);
        }
        if (message.contains("Cushioned Boots") && message.endsWith("I")) {
            UpgradeTracker.addOrUpdateUpgrade("Feather Falling", 1);
        }
        if (message.contains("Cushioned Boots") && message.endsWith("II")) {
            UpgradeTracker.addOrUpdateUpgrade("Feather Falling", 2);
        }
        if (message.contains("Reinforced Armor") && message.endsWith("IV")) {
            UpgradeTracker.addOrUpdateUpgrade("Reinforced Armor", 4);
        }
        if (message.contains("Maniac Miner") && message.endsWith("I")) {
            UpgradeTracker.addOrUpdateUpgrade("Maniac Miner", 1);
        }
        if (message.contains("Maniac Miner") && message.endsWith("II")) {
            UpgradeTracker.addOrUpdateUpgrade("Maniac Miner", 2);
        }
        if (message.contains("Iron Forge")) {
            UpgradeTracker.addOrUpdateUpgrade("Iron Forge", 0);
        }
        if (message.contains("Golden Forge")) {
            UpgradeTracker.addOrUpdateUpgrade("Golden Forge", 0);
        }
        if (message.contains("Emerald Forge")) {
            UpgradeTracker.addOrUpdateUpgrade("Emerald Forge", 0);
        }
        if (message.contains("Molten Forge")) {
            UpgradeTracker.addOrUpdateUpgrade("Molten Forge", 0);
        }
        if (message.contains("Heal Pool")) {
            UpgradeTracker.addOrUpdateUpgrade("Heal Pool", 0);
        }
        if (message.contains("purchased It's a trap!")) {
            TrapTracker.addOrUpdateTrap("It's a trap!");
        }
        if (message.contains("purchased Alarm Trap")) {
            TrapTracker.addOrUpdateTrap("Reveal Trap");
        }
        if (message.contains("purchased Counter-Offensive Trap")) {
            TrapTracker.addOrUpdateTrap("Counter-Offensive Trap");
        }
        if (message.contains("purchased Miner Fatigue Trap")) {
            TrapTracker.addOrUpdateTrap("Miner Fatigue Trap");
        }
        if (message.contains("Miner Fatigue Trap was set off!")) {
            TrapTracker.removeTrap("Miner Fatigue Trap");
        }
        if (message.contains("It's a trap! was set off!")) {
            TrapTracker.removeTrap("It's a trap!");
        }
        if (message.contains("Reveal Trap was set off!")) {
            TrapTracker.removeTrap("Reveal Trap");
        }
        if (message.contains("Counter-Offensive Trap was set off!")) {
            TrapTracker.removeTrap("Counter-Offensive Trap");
        }
    }
}
