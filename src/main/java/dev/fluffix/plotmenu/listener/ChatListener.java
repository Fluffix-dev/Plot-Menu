package dev.fluffix.plotmenu.listener;

import dev.fluffix.plotmenu.handler.MessageHandler;
import dev.fluffix.plotmenu.service.MenuService;
import dev.fluffix.plotmenu.session.ConfirmSession;
import dev.fluffix.plotmenu.session.PromptSession;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ChatListener implements Listener {
    private final MenuService service;
    private final MessageHandler msg;

    public ChatListener(MenuService service, MessageHandler msg) {
        this.service = service;
        this.msg = msg;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        UUID id = p.getUniqueId();

        if (service.hasConfirm(id)) {
            e.setCancelled(true);
            String in = e.getMessage().trim().toLowerCase();
            ConfirmSession conf = service.getConfirm(id);
            if (in.equals("ja")) {
                Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("PlotMenu"), () -> {
                    service.runActions(p, conf.actionsWithout("confirm"), Map.of());
                    service.clearConfirm(id);
                });
            } else {
                p.sendMessage(msg.getComponent("input_cancelled"));
                service.clearConfirm(id);
            }
            return;
        }

        if (service.hasPrompt(id)) {
            e.setCancelled(true);
            PromptSession ps = service.getPrompt(id);

            String currentKey = ps.nextKey();
            ps.values.put(currentKey, e.getMessage().trim());

            if (ps.hasMore()) {
                String next = ps.peekKey();
                p.sendMessage(msg.getComponent("input_required", Map.of("key", next)));
            } else {
                Map<String, String> vals = new HashMap<>(ps.values);
                List<String> actions = ps.actionsWithoutPrompts();
                Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("PlotMenu"), () -> {
                    service.runActions(p, actions, vals);
                    service.clearPrompt(id);
                });
            }
        }
    }
}
