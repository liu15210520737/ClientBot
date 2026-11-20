package dev.clientbot.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import dev.clientbot.integration.IntegrationRegistry;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Reflection adapter for MaLiLib-compatible public configuration objects. */
public final class ReflectedConfig {
    private final Map<String, Option> options = new LinkedHashMap<>();

    public static ReflectedConfig scan(String... rootClasses) throws ClassNotFoundException {
        ReflectedConfig result = new ReflectedConfig();
        for (String root : rootClasses) result.scanClass(Class.forName(root), 0);
        return result;
    }

    private void scanClass(Class<?> type, int depth) {
        for (Field field : type.getFields()) {
            if (!Modifier.isStatic(field.getModifiers())) continue;
            try {
                Object candidate = field.get(null);
                if (candidate != null && isConfig(candidate)) {
                    Option option = new Option(candidate);
                    options.putIfAbsent(option.name().toLowerCase(Locale.ROOT), option);
                }
            } catch (ReflectiveOperationException | RuntimeException ignored) {
                // A single broken third-party option must not hide every other option.
            }
        }
        if (depth < 3) {
            for (Class<?> nested : type.getDeclaredClasses()) {
                if (Modifier.isPublic(nested.getModifiers()) || Modifier.isStatic(nested.getModifiers())) {
                    scanClass(nested, depth + 1);
                }
            }
        }
    }

    private static boolean isConfig(Object value) {
        try {
            value.getClass().getMethod("getName");
            value.getClass().getMethod("getAsJsonElement");
            value.getClass().getMethod("setValueFromJsonElement", JsonElement.class);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    public List<Option> list() {
        return options.values().stream().sorted(Comparator.comparing(Option::name)).toList();
    }

    public Option get(String name) {
        Option option = options.get(name.toLowerCase(Locale.ROOT));
        if (option == null) throw new IllegalArgumentException("未知配置: " + name + "；先用 list 查看");
        return option;
    }

    public void save(String rootClass) {
        try {
            Method save = Class.forName(rootClass).getMethod("saveToFile");
            save.invoke(null);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("该 Mod 不提供 saveToFile；可打开其配置界面触发保存", e);
        }
    }

    public void registerBooleanToggles(IntegrationRegistry registry, String owner, String prefix) {
        for (Option option : list()) {
            if (option.value() instanceof JsonPrimitive primitive && primitive.isBoolean()) {
                registry.register(prefix + option.name(), owner, option.name(),
                        () -> option.value().getAsBoolean(), value -> option.set(new JsonPrimitive(value)));
            }
        }
    }

    public static final class Option {
        private final Object target;

        private Option(Object target) {
            this.target = target;
        }

        public String name() {
            return invoke("getName", String.class);
        }

        public JsonElement value() {
            try {
                return (JsonElement) target.getClass().getMethod("getAsJsonElement").invoke(target);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        }

        public void set(JsonElement value) {
            try {
                target.getClass().getMethod("setValueFromJsonElement", JsonElement.class).invoke(target, value);
            } catch (ReflectiveOperationException e) {
                throw new IllegalArgumentException("配置拒绝该值: " + value, e);
            }
        }

        public void setFromText(String text) {
            JsonElement parsed;
            try {
                parsed = JsonParser.parseString(text);
            } catch (Exception ignored) {
                parsed = new JsonPrimitive(text);
            }
            set(parsed);
        }

        private <T> T invoke(String method, Class<T> result) {
            try {
                return result.cast(target.getClass().getMethod(method).invoke(target));
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
