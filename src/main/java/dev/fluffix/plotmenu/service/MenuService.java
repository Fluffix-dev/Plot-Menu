package dev.fluffix.plotmenu.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import dev.fluffix.plotmenu.handler.MessageHandler;
import dev.fluffix.plotmenu.listener.ChatListener;
import dev.fluffix.plotmenu.listener.MenuListener;
import dev.fluffix.plotmenu.plugin.PlotMenuPlugin;
import dev.fluffix.plotmenu.session.ConfirmSession;
import dev.fluffix.plotmenu.session.PromptSession;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Hinweis: Für Component-basierte Inventar-Titel ist Paper nötig
 * (Bukkit#createInventory(InventoryHolder, int, Component)).
 */
public class MenuService implements Listener {
    private final PlotMenuPlugin plugin;
    private final ObjectNode root;
    private final MessageHandler msg;
    private final MiniMessage mm = MiniMessage.miniMessage();

    private final Map<UUID, PromptSession> prompts = new HashMap<>();
    private final Map<UUID, ConfirmSession> confirms = new HashMap<>();

    public MenuService(PlotMenuPlugin plugin, ObjectNode menuRoot, MessageHandler msg) {
        this.plugin = plugin;
        this.root = menuRoot;
        this.msg = msg;
    }

    public void register() {
        Bukkit.getPluginManager().registerEvents(new MenuListener(this, msg), plugin);
        Bukkit.getPluginManager().registerEvents(new ChatListener(this, msg), plugin);
    }

    public void unregister() { HandlerList.unregisterAll(plugin); }

    public void open(Player p, String menuKey) {
        JsonNode menus = root.get("menus");
        if (menus == null || menus.get(menuKey) == null) {
            p.sendMessage(msg.getComponent("error", Map.of("detail", "Menu '" + menuKey + "' nicht gefunden")));
            return;
        }
        Inventory inv = buildInventory(p, menus.get(menuKey));
        p.openInventory(inv);
        p.sendMessage(msg.getComponent("menu_opened"));
    }

    public void handleClick(Player p, JsonNode menuNode, int slot) {
        for (JsonNode item : menuNode.withArray("items")) {
            if (item.has("slot") && item.get("slot").asInt() == slot) {
                processItemClick(p, item);
                return;
            }
        }
        JsonNode nav = menuNode.get("navigation");
        if (nav != null) {
            if (nav.has("close") && nav.get("close").get("slot").asInt() == slot) {
                p.closeInventory();
                return;
            }
            if (nav.has("back") && nav.get("back").get("slot").asInt() == slot) {
                open(p, "main");
                return;
            }
        }
    }

    /** Suche Menü anhand des *Component*-Titels (MiniMessage). */
    public JsonNode findMenuByTitle(Component title) {
        JsonNode menus = root.get("menus");
        if (menus == null) return null;
        Iterator<String> it = menus.fieldNames();
        while (it.hasNext()) {
            String key = it.next();
            JsonNode m = menus.get(key);
            if (m.has("title")) {
                Component candidate = mm.deserialize(m.get("title").asText());
                if (candidate.equals(title)) return m;
            }
        }
        return null;
    }

    /** Bequeme Überladung, falls irgendwo noch ein String-Titel reinkommt. */
    public JsonNode findMenuByTitle(String miniMessageTitle) {
        return findMenuByTitle(mm.deserialize(miniMessageTitle));
    }

    private Inventory buildInventory(Player p, JsonNode menuNode) {
        String titleRaw = menuNode.has("title") ? menuNode.get("title").asText() : "<gray>Menu</gray>";
        int size = menuNode.has("size") ? menuNode.get("size").asInt() : 54;

        Component title = mm.deserialize(titleRaw);

        // WICHTIG: Paper-Overload mit Component verwenden
        Inventory inv = Bukkit.createInventory(new MenuHolder(title), size, title);

        if (menuNode.has("fill")) {
            JsonNode fill = menuNode.get("fill");
            ItemStack filler = buildIcon(
                    fill,
                    fill.has("name") ? mm.deserialize(fill.get("name").asText()) : Component.text(" "),
                    List.of()
            );
            for (int i = 0; i < size; i++) inv.setItem(i, filler);
        }

        for (JsonNode item : menuNode.withArray("items")) {
            int slot = item.get("slot").asInt();
            JsonNode icon = item.get("icon");
            Component name = item.has("name") ? mm.deserialize(item.get("name").asText()) : Component.empty();
            List<Component> lore = readLoreComponents(item.get("lore"));
            ItemStack stack = buildIcon(icon, name, lore);
            inv.setItem(slot, stack);
        }

        JsonNode nav = menuNode.get("navigation");
        if (nav != null) {
            if (nav.has("close")) {
                JsonNode n = nav.get("close");
                inv.setItem(n.get("slot").asInt(),
                        buildIcon(n, mm.deserialize(n.get("name").asText()),
                                List.of(mm.deserialize("<gray>Schließen</gray>"))));
            }
            if (nav.has("back")) {
                JsonNode n = nav.get("back");
                inv.setItem(n.get("slot").asInt(),
                        buildIcon(n, mm.deserialize(n.get("name").asText()),
                                List.of(mm.deserialize("<gray>Zurück</gray>"))));
            }
        }
        return inv;
    }

    private ItemStack buildIcon(JsonNode node, Component display, List<Component> lore) {
        String mat = node.has("material") ? node.get("material").asText() : "STONE";
        Material material = Material.matchMaterial(mat);
        if (material == null) material = Material.STONE;

        ItemStack is = new ItemStack(material);
        ItemMeta meta = is.getItemMeta();
        if (meta != null) {
            meta.displayName(display);
            if (lore != null && !lore.isEmpty()) meta.lore(lore);
            boolean glow = node.has("glow") && node.get("glow").asBoolean();
            if (glow) {
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                is.setItemMeta(meta);
                is.addUnsafeEnchantment(Enchantment.PROTECTION, 1);
                return is;
            }
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            is.setItemMeta(meta);
        }
        return is;
    }

    private List<Component> readLoreComponents(JsonNode loreNode) {
        if (loreNode == null || !loreNode.isArray()) return List.of();
        List<Component> lore = new ArrayList<>();
        loreNode.forEach(n -> lore.add(mm.deserialize(n.asText())));
        return lore;
    }

    private void processItemClick(Player p, JsonNode item) {
        if (item.has("permission")) {
            String perm = item.get("permission").asText();
            if (!p.hasPermission(perm)) {
                p.sendMessage(msg.getComponent("no_permission"));
                return;
            }
        }
        if (item.has("open_menu")) {
            open(p, item.get("open_menu").asText());
            return;
        }

        List<String> actions = new ArrayList<>();
        item.withArray("actions").forEach(n -> actions.add(n.asText()));

        if (actions.contains("confirm")) {
            confirms.put(p.getUniqueId(), new ConfirmSession(p, actions));
            p.closeInventory();
            p.sendMessage(msg.getComponent("confirm_title"));
            return;
        }

        List<String> promptsNeeded = actions.stream()
                .filter(a -> a.startsWith("prompt:"))
                .map(a -> a.substring("prompt:".length()))
                .collect(Collectors.toList());

        if (!promptsNeeded.isEmpty()) {
            prompts.put(p.getUniqueId(), new PromptSession(p, actions, promptsNeeded));
            p.closeInventory();
            String key = promptsNeeded.get(0);
            p.sendMessage(msg.getComponent("input_required", Map.of("key", key)));
            return;
        }

        runActions(p, actions, Map.of());
    }

    public void runActions(Player p, List<String> actions, Map<String, String> values) {
        for (String action : actions) {
            if (action.startsWith("command:")) {
                String cmd = action.substring("command:".length()).trim();
                for (Map.Entry<String, String> e : values.entrySet()) {
                    cmd = cmd.replace("{" + e.getKey() + "}", e.getValue());
                }
                p.performCommand(cmd.startsWith("/") ? cmd.substring(1) : cmd);
            }
        }
    }

    public boolean hasPrompt(UUID uuid) { return prompts.containsKey(uuid); }
    public PromptSession getPrompt(UUID uuid) { return prompts.get(uuid); }
    public void clearPrompt(UUID uuid) { prompts.remove(uuid); }

    public boolean hasConfirm(UUID uuid) { return confirms.containsKey(uuid); }
    public ConfirmSession getConfirm(UUID uuid) { return confirms.get(uuid); }
    public void clearConfirm(UUID uuid) { confirms.remove(uuid); }
}
