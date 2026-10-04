package com.ootruffle.bedwarsupgradesdisplay;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class TrapTracker
{
    private static final Map<String, Integer> traps;

    public static void addOrUpdateTrap(final String trapName) {
        TrapTracker.traps.put(trapName, TrapTracker.traps.getOrDefault(trapName, 0) + 1);
    }

    public static void removeTrap(final String trapName) {
        if (TrapTracker.traps.containsKey(trapName)) {
            final int currentCount = TrapTracker.traps.get(trapName);
            if (currentCount > 1) {
                TrapTracker.traps.put(trapName, currentCount - 1);
            }
            else {
                TrapTracker.traps.remove(trapName);
            }
        }
    }

    public static Map<String, Integer> getTraps() {
        return TrapTracker.traps;
    }

    static {
        traps = new ConcurrentHashMap<String, Integer>();
    }
}
