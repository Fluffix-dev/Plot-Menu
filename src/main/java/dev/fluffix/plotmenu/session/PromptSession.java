package dev.fluffix.plotmenu.session;

import org.bukkit.entity.Player;

import java.util.*;

public class PromptSession {
    public final UUID uuid;
    public final List<String> actions;
    public final List<String> keys;
    public final Map<String, String> values = new LinkedHashMap<>();
    private int idx = 0;

    public PromptSession(Player p, List<String> actions, List<String> keys) {
        this.uuid = p.getUniqueId();
        this.actions = actions;
        this.keys = new ArrayList<>(keys);
    }

    public String nextKey() { return keys.get(idx++); }
    public String peekKey() { return keys.get(idx); }
    public boolean hasMore() { return idx < keys.size(); }

    public List<String> actionsWithoutPrompts() {
        List<String> out = new ArrayList<>();
        for (String a : actions) if (!a.startsWith("prompt:")) out.add(a);
        return out;
    }
}

