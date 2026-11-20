package dev.clientbot.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.nio.file.Files;
import java.nio.file.Path;

public final class JsonFiles {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private JsonFiles() {}

    public static <T> T load(Path file, Class<T> type, T defaults) {
        try {
            if (Files.exists(file)) {
                T value = GSON.fromJson(Files.readString(file), type);
                if (value != null) return value;
            }
            save(file, defaults);
            return defaults;
        } catch (Exception e) {
            throw new IllegalStateException("无法读取扩展配置 " + file, e);
        }
    }

    public static void save(Path file, Object value) {
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, GSON.toJson(value));
        } catch (Exception e) {
            throw new IllegalStateException("无法保存扩展配置 " + file, e);
        }
    }
}
