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

package dev.fluffix.plotmenu.listener;

import dev.fluffix.plotmenu.manager.PlotMenuManager;
import dev.fluffix.plotmenu.plugin.PlotPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class MenuListener implements Listener {

    public MenuListener() {
        Bukkit.getPluginManager().registerEvents(this, PlotPlugin.getInstance());
    }

    private PlotMenuManager menu = PlotPlugin.getInstance().getPlotMenuManager();
    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        if (!(e.getView().getTopInventory().getHolder() instanceof PlotMenuManager.MenuHolder holder)) return;
        e.setCancelled(true);
        int slot = e.getRawSlot();
        if (slot < 0 || slot >= e.getView().getTopInventory().getSize()) return;

        Player p = (Player) e.getWhoClicked();
        menu.getItem(holder.menuName(), slot).ifPresent(item -> item.actions().forEach(a -> menu.runAction(p, a)));
    }
}
