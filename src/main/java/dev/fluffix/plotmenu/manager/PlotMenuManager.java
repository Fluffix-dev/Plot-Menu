package dev.fluffix.plotmenu.manager;

import com.fasterxml.jackson.databind.JsonNode;
import dev.fluffix.plotmenu.configuration.JsonFileBuilder;
import dev.fluffix.plotmenu.plugin.PlotPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class PlotMenuManager {

    public static String colorize(String s) {
        if (s == null) return null;
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    private final File configFile;
    private final JsonFileBuilder jsonBuilder;

    private final Map<String, MenuDefinition> menus = new LinkedHashMap<>();

    public PlotMenuManager() {

        this.configFile = new File(PlotPlugin.getInstance().getDataFolder(), "menus.json");
        this.jsonBuilder = new JsonFileBuilder();
        reload();
    }

    public void reload() {
        try {
            if (!configFile.exists()) {
                PlotPlugin.getInstance().getDataFolder().mkdirs();
                PlotPlugin.getInstance().saveResource("menus.json", false);
            }
            jsonBuilder.loadFromFile(configFile);

            menus.clear();
            JsonNode menusNode = jsonBuilder.getRootNode().get("menus");
            if (menusNode != null && menusNode.isObject()) {
                Iterator<Map.Entry<String, JsonNode>> it = menusNode.fields();
                while (it.hasNext()) {
                    Map.Entry<String, JsonNode> entry = it.next();
                    String name = entry.getKey().toLowerCase(Locale.ROOT);
                    menus.put(name, parseMenu(name, entry.getValue()));
                }
            }
        } catch (Exception e) {
            PlotPlugin.getInstance().getLogger().severe("Fehler beim Laden von menus.json: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Set<String> getMenuNames() {
        return new LinkedHashSet<>(menus.keySet());
    }

    private MenuDefinition parseMenu(String name, JsonNode root) {
        String title = colorize(root.path("title").asText("&aPlot-Flags"));
        int rows = Math.max(1, Math.min(6, root.path("rows").asInt(3)));

        ItemStack filler = null;
        JsonNode fillSec = root.get("fill");
        if (fillSec != null && fillSec.path("enabled").asBoolean(true)) {
            filler = buildItem(
                    fillSec.path("material").asText("GRAY_STAINED_GLASS_PANE"),
                    fillSec.path("name").asText(" "),
                    toList(fillSec.get("lore")),
                    fillSec.path("hide_attributes").asBoolean(true)
            );
        }

        Map<Integer, MenuItem> items = new HashMap<>();
        JsonNode itemsSec = root.get("items");
        if (itemsSec != null && itemsSec.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> it = itemsSec.fields();
            while (it.hasNext()) {
                Map.Entry<String, JsonNode> en = it.next();
                try {
                    int slot = Integer.parseInt(en.getKey());
                    if (slot < 0 || slot >= rows * 9) continue;

                    JsonNode sec = en.getValue();
                    ItemStack stack = buildItem(
                            sec.path("material").asText("STONE"),
                            sec.path("name").asText("&7Item"),
                            toList(sec.get("lore")),
                            sec.path("hide_attributes").asBoolean(true)
                    );
                    List<String> actions = toList(sec.get("actions"));
                    items.put(slot, new MenuItem(stack, actions));
                } catch (NumberFormatException ignored) { }
            }
        }

        return new MenuDefinition(name, title, rows, filler, items);
    }

    private List<String> toList(JsonNode node) {
        List<String> list = new ArrayList<>();
        if (node != null && node.isArray()) {
            node.forEach(n -> list.add(n.asText()));
        }
        return list;
    }

    private ItemStack buildItem(String materialName, String display, List<String> lore, boolean hide) {
        Material mat = Material.matchMaterial(materialName.toUpperCase(Locale.ROOT));
        if (mat == null) mat = Material.STONE;

        ItemStack it = new ItemStack(mat);
        ItemMeta meta = it.getItemMeta();
        if (meta != null) {
            if (display != null) meta.setDisplayName(colorize(display));
            if (lore != null && !lore.isEmpty()) {
                meta.setLore(lore.stream().map(PlotMenuManager::colorize).collect(Collectors.toList()));
            }
            if (hide) {
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_DYE);
            }
            it.setItemMeta(meta);
        }
        return it;
    }

    public boolean openMenu(Player player, String name) {
        MenuDefinition def = menus.getOrDefault(name.toLowerCase(Locale.ROOT),
                menus.get("default"));
        if (def == null) return false;

        Inventory inv = Bukkit.createInventory(new MenuHolder(def.name()), def.rows() * 9, def.title());
        if (def.filler() != null) {
            for (int i = 0; i < def.rows() * 9; i++) inv.setItem(i, def.filler());
        }
        for (Map.Entry<Integer, MenuItem> e : def.items().entrySet()) {
            inv.setItem(e.getKey(), e.getValue().renderedFor(player));
        }
        player.openInventory(inv);
        return true;
    }

    public Optional<MenuItem> getItem(String menuName, int slot) {
        MenuDefinition def = menus.get(menuName.toLowerCase(Locale.ROOT));
        if (def == null) return Optional.empty();
        return Optional.ofNullable(def.items().get(slot));
    }

    public void runAction(Player p, String raw) {
        String line = replacePlaceholders(raw, p);
        if (line == null || line.isBlank()) return;

        if (line.equalsIgnoreCase("close")) {
            p.closeInventory();
            return;
        }
        if (line.toLowerCase(Locale.ROOT).startsWith("message:")) {
            String msg = line.substring("message:".length()).trim();
            p.sendMessage(colorize(msg));
            return;
        }
        if (line.toLowerCase(Locale.ROOT).startsWith("sound:")) {
            String snd = line.substring("sound:".length()).trim().toUpperCase(Locale.ROOT);
            try {
                Sound sound = null;
                p.playSound(p.getLocation(), sound, 1f, 1f);
            } catch (IllegalArgumentException ignored) {
                p.sendMessage("§cUnbekannter Sound: " + snd);
            }
            return;
        }
        if (line.toLowerCase(Locale.ROOT).startsWith("console:")) {
            String cmd = line.substring("console:".length()).trim();
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), replacePlaceholders(cmd, p));
            return;
        }
        if (line.toLowerCase(Locale.ROOT).startsWith("cmd:")) {
            String cmd = line.substring("cmd:".length()).trim();
            p.performCommand(replacePlaceholders(cmd, p));
            return;
        }
        p.performCommand(line);
    }

    String replacePlaceholders(String s, Player p) {
        if (s == null) return null;
        return s.replace("%player%", p.getName())
                .replace("%world%", p.getWorld().getName());
    }

    // Datenklassen / Holder
    public record MenuDefinition(String name, String title, int rows, ItemStack filler, Map<Integer, MenuItem> items) { }

    public static class MenuItem {
        private final ItemStack base;
        private final List<String> actions;

        public MenuItem(ItemStack base, List<String> actions) {
            this.base = base;
            this.actions = actions == null ? Collections.emptyList() : new ArrayList<>(actions);
        }
        public ItemStack renderedFor(Player p) { return base.clone(); }
        public List<String> actions() { return actions; }
    }

    public static class MenuHolder implements InventoryHolder {
        private final String menuName;
        public MenuHolder(String menuName) { this.menuName = menuName; }
        public String menuName() { return menuName; }
        @Override public org.bukkit.inventory.Inventory getInventory() { return null; }
    }
}
