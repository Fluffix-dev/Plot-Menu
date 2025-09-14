/*
 * Copyright (c) 2025 FluffixYT
 *
 * Alle Rechte vorbehalten.
 *
 * Diese Datei ist Teil des Projekts plotmenu.
 *
 * Die Nutzung, Vervielfältigung oder Verbreitung ohne vorherige schriftliche
 * Genehmigung des Rechteinhabers ist nicht gestattet.
 * 09/2025
 */

package dev.fluffix.plotmenu.plugin;

import org.bukkit.plugin.java.JavaPlugin;

public class PlotPlugin extends JavaPlugin {

    private static PlotPlugin instance;

    @Override
    public void onEnable() {
        instance = this;
    }

    @Override
    public void onDisable() {
        instance = null;
    }
}
