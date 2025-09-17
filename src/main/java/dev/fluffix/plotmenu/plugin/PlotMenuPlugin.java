package dev.fluffix.plotmenu.plugin;

import dev.fluffix.plotmenu.configuration.JsonFileBuilder;
import dev.fluffix.plotmenu.handler.MessageHandler;
import dev.fluffix.plotmenu.service.MenuService;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class PlotMenuPlugin extends JavaPlugin {


    public static PlotMenuPlugin instance;

    private MenuService menuService;
    private MessageHandler messages;

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

            getLogger().info("PlotMenu geladen.");
        } catch (Exception ex) {
            getLogger().severe("Fehler beim Laden: " + ex.getMessage());
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        instance = null;
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
}
