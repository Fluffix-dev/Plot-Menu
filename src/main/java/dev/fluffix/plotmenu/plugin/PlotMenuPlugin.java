package dev.fluffix.plotmenu.plugin;

import dev.fluffix.plotmenu.configuration.JsonFileBuilder;
import dev.fluffix.plotmenu.handler.MessageHandler;
import dev.fluffix.plotmenu.logger.PluginLogger;
import dev.fluffix.plotmenu.service.MenuService;
import dev.fluffix.plotmenu.updater.UpdateChecker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class PlotMenuPlugin extends JavaPlugin implements Listener {

    public static PlotMenuPlugin instance;

    private MenuService menuService;
    private MessageHandler messages;

    private UpdateChecker updater;
    private volatile boolean updateAvailable = false;
    private volatile String latestVersion = null;
    private volatile String downloadUrl = null;

    @Override
    public void onEnable() {
        instance = this;
        try {
            saveResource("menu.json", false);
            saveResource("messages.json", false);

            messages = new MessageHandler(getDataFolder());

            JsonFileBuilder json = new JsonFileBuilder();
            json.loadFromFile(new File(getDataFolder(), "menu.json"));

            menuService = new MenuService(this, json.getRootNode(), messages);
            menuService.register();

            getCommand("plotmenu").setExecutor((sender, cmd, label, args) -> {
                if (sender instanceof org.bukkit.entity.Player p) {
                    menuService.open(p, "main");
                }
                return true;
            });

            String currentVersion = getDescription().getVersion();
            this.updater = new UpdateChecker(this, currentVersion);

            Bukkit.getPluginManager().registerEvents(this, this);

            Bukkit.getScheduler().runTaskAsynchronously(this, this::refreshUpdateInfo);

            long intervalTicks = 20L * 60L * 60L * 6;
            Bukkit.getScheduler().runTaskTimerAsynchronously(this, this::refreshUpdateInfo, intervalTicks, intervalTicks);

            PluginLogger.printWithLabel("PLOT-MENU","Das Plugin wurde erfolgreich geladen (" + getInstance().getPluginMeta().getVersion() + ")","GREEN");
        } catch (Exception ex) {
            getLogger().severe("Fehler beim Laden: " + ex.getMessage());
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        instance = null;
    }


    private void refreshUpdateInfo() {
        UpdateChecker.UpdateResult res = updater.checkNow();
        if (res.error() != null) {
            PluginLogger.print("[Updater] Fehler: " + res.error(), "RED");
            return;
        }
        if (res.isNewerAvailable()) {
            this.updateAvailable = true;
            this.latestVersion = res.latestTag();
            this.downloadUrl = res.htmlUrl();
            PluginLogger.print("[Updater] Neue Version gefunden: " + latestVersion + " → " + downloadUrl, "GREEN");
        } else {
            this.updateAvailable = false;
            this.latestVersion = null;
            this.downloadUrl = null;
        }
    }

    public static PlotMenuPlugin getInstance() {
        return instance;
    }

    public MenuService getMenuService() {
        return menuService;
    }

    public MessageHandler getMessages() {
        return messages;
    }

    @EventHandler
    public void handleJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (updateAvailable && (player.hasPermission("homesystem.setup") || player.hasPermission("*"))) {

            String current = getDescription().getVersion();
            String latest  = (latestVersion != null ? latestVersion : "unbekannt");
            String url     = (downloadUrl != null ? downloadUrl : "—");

            player.sendMessage(MiniMessage.miniMessage().deserialize(
                    "<b><dark_gray>[</dark_gray><yellow>PLOTMENU UPDATE</yellow><dark_gray>]</dark_gray></b> <green>" + latest));

            player.sendMessage(MiniMessage.miniMessage().deserialize(
                    "<gray>Deine Version: <green>" + current));

            player.sendMessage(MiniMessage.miniMessage().deserialize(
                    "<gray>Neue Version: <dark_green><b>" + latest + "</b></dark_green>"));

            player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>Download Url: ")
                            .append(Component.text(url).color(NamedTextColor.BLUE).clickEvent(ClickEvent.openUrl(url))));
        }
    }
}
