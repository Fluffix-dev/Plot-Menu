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

import dev.fluffix.plotmenu.commands.PlotMenuCommand;
import dev.fluffix.plotmenu.listener.MenuListener;
import dev.fluffix.plotmenu.manager.PlotMenuManager;
import org.bukkit.plugin.java.JavaPlugin;

public class PlotPlugin extends JavaPlugin {

    private static PlotPlugin instance;

    private PlotMenuManager plotMenuManager;

    @Override
    public void onEnable() {
        instance = this;

        this.plotMenuManager = new PlotMenuManager(this);
        plotMenuManager.reload();

        getCommand("plotmenu").setExecutor(new PlotMenuCommand());

        new MenuListener();
    }

    @Override
    public void onDisable() {
        instance = null;
    }

    public static PlotPlugin getInstance() {
        return instance;
    }

    public PlotMenuManager getPlotMenuManager() {
        return plotMenuManager;
    }
}
