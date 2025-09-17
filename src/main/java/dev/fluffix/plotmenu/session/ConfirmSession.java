package dev.fluffix.plotmenu.session;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ConfirmSession {
    public final UUID uuid;
    public final List<String> actions;

    public ConfirmSession(Player p, List<String> actions) {
        this.uuid = p.getUniqueId();
        this.actions = new ArrayList<>(actions);
    }

    public List<String> actionsWithout(String token) {
        List<String> out = new ArrayList<>();
        for (String a : actions) if (!a.equalsIgnoreCase(token)) out.add(a);
        return out;
    }
}
