package dev.fluffix.plotmenu.handler;

import dev.fluffix.plotmenu.configuration.JsonFileBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class MessageHandler {

    private final JsonFileBuilder builder;
    private final File file;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public MessageHandler(File pluginFolder) {
        this.file = new File(pluginFolder, "messages.json");
        this.builder = new JsonFileBuilder();

        if (!file.exists()) {
            try {
                builder
                        .add("prefix", "<green>Plot </green><dark_gray>»</dark_gray>")
                        .add("menu_opened", "%prefix% <gray>Menü geöffnet.</gray>")
                        .add("no_permission", "%prefix% <red>Dazu hast du keine Rechte.</red>")
                        .add("error", "%prefix% <red>Ein Fehler ist aufgetreten.</red>");
                builder.build(file.getPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            builder.loadFromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Component getComponent(String key) {
        return getComponent(key, Map.of());
    }

    public Component getComponent(String key, Map<String, String> placeholders) {
        String raw = builder.getString(key);
        if (raw == null) return Component.text("[SYSTEM] Nachricht '" + key + "' nicht gefunden.");

        placeholders = new java.util.HashMap<>(placeholders);
        placeholders.put("prefix", builder.getString("prefix"));

        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            raw = raw.replace("%" + entry.getKey() + "%", entry.getValue());
        }

        return mm.deserialize(raw);
    }
}
