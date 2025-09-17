package dev.fluffix.plotmenu.listener;

import com.fasterxml.jackson.databind.JsonNode;
import dev.fluffix.plotmenu.handler.MessageHandler;
import dev.fluffix.plotmenu.service.MenuHolder;
import dev.fluffix.plotmenu.service.MenuService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class MenuListener implements Listener {
    private final MenuService service;
    private final MessageHandler msg;

    public MenuListener(MenuService service, MessageHandler msg) {
        this.service = service;
        this.msg = msg;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (!(e.getInventory().getHolder() instanceof MenuHolder holder)) return;

        e.setCancelled(true);

        JsonNode menuNode = service.findMenuByTitle(holder.getTitle());
        if (menuNode == null) return;

        int slot = e.getRawSlot();
        if (slot < 0 || slot >= e.getInventory().getSize()) return;

        service.handleClick(p, menuNode, slot);
    }
}
