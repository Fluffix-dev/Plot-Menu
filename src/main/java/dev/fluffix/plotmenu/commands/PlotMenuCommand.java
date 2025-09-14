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

package dev.fluffix.plotmenu.commands;

import dev.fluffix.plotmenu.logger.PluginLogger;
import dev.fluffix.plotmenu.plugin.PlotPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlotMenuCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if(!(sender instanceof Player)) {
            PluginLogger.printWithLabel("PlotMenuCommand","Du kannst dieses Kommando nicht in der Konsole ausführen", "RED");
            return true;
        }

        Player player = (Player) sender;
        String menuName = args.length >= 1 ? args[0] : "default";

        PlotPlugin.getInstance().getPlotMenuManager().openMenu(player,menuName);
        return false;
    }
}
