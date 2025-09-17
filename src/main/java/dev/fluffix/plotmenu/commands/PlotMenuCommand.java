package dev.fluffix.plotmenu.commands;

import dev.fluffix.plotmenu.logger.PluginLogger;
import dev.fluffix.plotmenu.plugin.PlotMenuPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlotMenuCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if(!(commandSender instanceof Player)) {
            PluginLogger.printWithLabel("PLOTMENU","Das Kommando kann nur aufm Server vom Spieler ausgeführt werden.","RED");
            return true;
        }

        Player player = (Player) commandSender;

        PlotMenuPlugin.getInstance().getMenuService().open(player,"main");
        return false;
    }
}
