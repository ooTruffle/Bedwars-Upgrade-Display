package com.ootruffle.bedwarsupgradesdisplay.client;

import com.ootruffle.bedwarsupgradesdisplay.HypixelPackets;
import com.ootruffle.bedwarsupgradesdisplay.TrapTracker;
import com.ootruffle.bedwarsupgradesdisplay.UpgradeTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.polyfrost.compose.render.PolyColor;
import org.polyfrost.oneconfig.api.config.v1.annotations.Color;
import org.polyfrost.oneconfig.api.config.v1.annotations.Slider;
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch;
import org.polyfrost.oneconfig.api.hud.v1.HudManager;
import org.polyfrost.oneconfig.api.hud.v1.LegacyHud;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * On the Ornithe (1.8.9) target, OneConfig V1's HUD rendering is NOT the
 * Compose-based Hud/TextHud used on modern Fabric targets — it's the older
 * LegacyHud bridge, using plain immediate-mode drawing.
 *
 * Text color, shadow, and background are NOT inherited free from the base
 * Hud class here (an earlier version of this file assumed they were, via
 * getTextColor() etc.) — github.com/TomekoYT/HyBedWars's own
 * BedwarsResourceDisplay.kt (a near-identical resource-display LegacyHud)
 * shows these need to be declared as this HUD's own @Color/@Switch/@Slider
 * option fields and drawn manually, same as the mod-level config options
 * (BwUpgradesConfig). This file now follows that exact pattern.
 */
public class BwUpgradesHud extends LegacyHud {

    // Both predefined Hud.Category constants tried previously (INFO, then
    // COMBAT) were inaccessible from Java in the actually published 1.2.0
    // artifact — constructing our own Category directly sidesteps that.
    private static final Category CATEGORY = new Category("Bedwars Upgrades Display", (byte) 50);

    private static final String CATEGORY_GENERAL = "General";
    private static final String SUBCATEGORY_GENERAL = "General";
    private static final String CATEGORY_BACKGROUND = "Background";

    @Switch(
            title = "Show Titles",
            category = CATEGORY_GENERAL,
            subcategory = SUBCATEGORY_GENERAL
    )
    public boolean showTitles = true;

    @Color(
            title = "Text Color",
            category = CATEGORY_GENERAL,
            subcategory = SUBCATEGORY_GENERAL
    )
    public PolyColor textColorOption = new PolyColor(0xFFFFFFFF);

    @Switch(
            title = "Text Shadow",
            category = CATEGORY_GENERAL,
            subcategory = SUBCATEGORY_GENERAL
    )
    public boolean textShadow = true;

    @Color(
            title = "Text Shadow Color",
            category = CATEGORY_GENERAL,
            subcategory = SUBCATEGORY_GENERAL
    )
    public PolyColor textShadowColor = new PolyColor(0xFF000000);

    @Switch(
            title = "Show Background",
            category = CATEGORY_BACKGROUND
    )
    public boolean background = false;

    @Color(
            title = "Background Color",
            category = CATEGORY_BACKGROUND
    )
    public PolyColor backgroundColorOption = new PolyColor(0x80000000);

    @Slider(
            title = "Background Padding",
            min = 0f,
            max = 10f,
            step = 0.1f,
            category = CATEGORY_BACKGROUND
    )
    public float backgroundPadding = 4f;

    private float actualWidth = 1f;
    private float actualHeight = 1f;

    public BwUpgradesHud() {
        super("bwupgradesdisplay_hud.json", "Upgrades/Traps HUD", CATEGORY);
    }

    @Override
    public float getWidth() {
        return actualWidth;
    }

    @Override
    public float getHeight() {
        return actualHeight;
    }

    @Override
    public boolean update() {
        return true;
    }

    private List<String> computeLines() {
        List<String> lines = new ArrayList<>();

        if (HudManager.INSTANCE.isEditing()) {
            // Placeholder content so there's always something to see and
            // drag in the HUD editor, even outside of an actual game.
            if (showTitles) lines.add("\u00A7c\u00A7lUpgrades \u00A77(2)");
            lines.add("- Sharpness 2");
            lines.add("- Protection 1");
            if (showTitles) lines.add("\u00A7c\u00A7lTraps \u00A77(1)");
            lines.add("- Alarm Trap");
            return lines;
        }

        if (!BwUpgradesConfig.INSTANCE.displayEnabled || !HypixelPackets.isInBedwars()) {
            return lines;
        }

        Map<String, Integer> upgrades = UpgradeTracker.getUpgrades();
        if (!upgrades.isEmpty() && showTitles) {
            lines.add("\u00A7c\u00A7lUpgrades \u00A77(" + upgrades.size() + ")");
        }
        for (Map.Entry<String, Integer> entry : upgrades.entrySet()) {
            int tier = entry.getValue();
            lines.add(tier == 0 ? "- " + entry.getKey() : "- " + entry.getKey() + " " + tier);
        }

        Map<String, Integer> traps = TrapTracker.getTraps();
        if (!traps.isEmpty() && showTitles) {
            lines.add("\u00A7c\u00A7lTraps \u00A77(" + traps.size() + ")");
        }
        for (Map.Entry<String, Integer> entry : traps.entrySet()) {
            int count = entry.getValue();
            lines.add(count == 1 ? "- " + entry.getKey() : "- " + entry.getKey() + " " + count);
        }

        return lines;
    }

    @Override
    public void render() {
        List<String> lines = computeLines();

        Minecraft mc = Minecraft.getMinecraft();
        int lineHeight = mc.fontRendererObj.FONT_HEIGHT + 1;
        int pad = (int) backgroundPadding;

        int widestLine = 0;
        for (String line : lines) {
            widestLine = Math.max(widestLine, mc.fontRendererObj.getStringWidth(line));
        }

        if (background && !lines.isEmpty()) {
            Gui.drawRect(
                    -pad,
                    -pad,
                    widestLine + pad,
                    lines.size() * lineHeight + pad,
                    backgroundColorOption.getArgb()
            );
        }

        int y = 0;
        for (String line : lines) {
            if (textShadow) {
                mc.fontRendererObj.drawString(line, 1, y + 1, textShadowColor.getArgb(), false);
            }
            mc.fontRendererObj.drawString(line, 0, y, textColorOption.getArgb(), false);
            y += lineHeight;
        }

        actualWidth = widestLine;
        actualHeight = Math.max(lines.size() * lineHeight, lineHeight);
    }
}
