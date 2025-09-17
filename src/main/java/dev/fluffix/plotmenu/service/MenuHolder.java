package dev.fluffix.plotmenu.service;

import net.kyori.adventure.text.Component;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class MenuHolder implements InventoryHolder {
    private final Component title;

    public MenuHolder(Component title) { this.title = title; }

    public Component getTitle() { return title; }

    @Override
    public Inventory getInventory() { return null; }
}
