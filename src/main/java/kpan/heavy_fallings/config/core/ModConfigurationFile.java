package kpan.heavy_fallings.config.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import kpan.heavy_fallings.ModMain;
import kpan.heavy_fallings.ModTagsGenerated;
import kpan.heavy_fallings.config.core.properties.AbstractConfigProperty;
import kpan.heavy_fallings.util.StringReader;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Configuration.UnicodeInputStreamReader;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

public class ModConfigurationFile {
    private static final String CONFIG_VERSION = "CONFIG_VERSION";

    private final ModConfigCategory rootCategory = new ModConfigCategory("root", true, this);
    private final File file;
    private final String configVersion;
    private String loadedConfigVersion = "";

    public ModConfigurationFile(File file, String configVersion) {
        this.file = file;
        this.configVersion = configVersion;
    }

    public String getLoadedConfigVersion() {
        return loadedConfigVersion;
    }

    public ModConfigCategory getRootCategory() {
        return rootCategory;
    }

    public void load(Consumer<ConfigVersionUpdateContext> updater) {
        loadedConfigVersion = "";
        List<String> lines = new ArrayList<>();
        try {
            UnicodeInputStreamReader input = new UnicodeInputStreamReader(new FileInputStream(file), "UTF-8");
            BufferedReader buffer = new BufferedReader(input);
            while (true) {
                String line = buffer.readLine();
                if (line == null)
                    break;
                lines.add(line);
            }
        } catch (IOException e) {
            ModMain.LOGGER.error("Cannot open a config file:" + file.getPath());
            return;
        }

        List<String> category_path = new ArrayList<>();
        int lineIndex = -1;
        String listType = null;
        String listName = null;
        List<String> listItems = null;
        for (String line : lines) {
            lineIndex++;
            int commentIdx = line.indexOf('#');
            if (commentIdx >= 0)
                line = line.substring(0, commentIdx);
            line = line.trim();
            if (line.isEmpty())
                continue;
            if (listType != null) {
                if (line.startsWith(">")) {
                    ConfigVersionUpdateContext updateContext = new ConfigVersionUpdateContext(this, StringUtils.join(category_path, '.'), listType, listName, StringUtils.join(listItems, '\n'));
                    updater.accept(updateContext);
                    listType = null;
                    listName = null;
                    listItems = null;

                    if (updateContext.cancelled)
                        continue;

                    ModConfigCategory category = tryGetCategory(updateContext.categoryPath);
                    if (category == null) {
                        ModMain.LOGGER.error("category path \"{}\" is not found!", updateContext.categoryPath);
                        continue;
                    }
                    AbstractConfigProperty property = category.get(updateContext.name);
                    if (property == null) {
                        ModMain.LOGGER.error("Unknown property:" + updateContext.name);
                    } else if (!property.getTypeString().equals(updateContext.type)) {
                        ModMain.LOGGER.error("Unmatched type {} (expected {})", updateContext.type, property.getTypeString());
                    } else {
                        if (!property.readValue(updateContext.value))
                            ModMain.LOGGER.error("Invalid value \"{}\" for {}.{}", updateContext.value, updateContext.categoryPath, updateContext.name);
                    }
                } else {
                    listItems.add(line);
                }
                continue;
            }
            StringReader reader = new StringReader(line);
            outerSwitch:
            switch (reader.peek()) {
                case '}' -> // category end
                {
                    if (category_path.isEmpty()) {
                        ModMain.LOGGER.error("Invalid category end at line " + lineIndex);
                        ModMain.LOGGER.error(line);
                    } else {
                        category_path.remove(category_path.size() - 1);
                    }
                }
                case '~' ->// config option
                {
                    reader.skip();
                    if (!category_path.isEmpty()) {
                        ModMain.LOGGER.error("Cannot use option not on root at line " + lineIndex);
                        ModMain.LOGGER.error(line);
                        break;
                    }
                    if (!reader.canRead()) {
                        ModMain.LOGGER.error("Invalid line at line " + lineIndex);
                        ModMain.LOGGER.error(line);
                        break;
                    }
                    if (!isValidChar(reader.peek())) {
                        ModMain.LOGGER.error("Invalid char:" + reader.peek() + " at line " + lineIndex);
                        ModMain.LOGGER.error(line);
                        break;
                    }
                    String name = reader.readStr(ModConfigurationFile::isValidChar);
                    if (reader.canRead() && reader.peek() == ':') {
                        reader.skip();
                        readOption(name, reader.readToChar('#').trim());
                    } else {
                        readOption(name, "");
                    }
                }
                case '"' -> {
                    // category
                    String name = reader.readQuotedString();
                    reader.skipWhitespace();
                    if (reader.canRead()) {
                        ModMain.LOGGER.error("Invalid line at line " + lineIndex);
                        ModMain.LOGGER.error(line);
                        break;
                    }
                    if (reader.peek() != '{') {
                        ModMain.LOGGER.error("Invalid char:" + reader.peek() + " at line " + lineIndex);
                        ModMain.LOGGER.error(line);
                        break;
                    }
                    reader.skip();
                    category_path.add(name);
                }
                default -> {
                    String name_or_type;
                    if (!isValidChar(reader.peek())) {
                        ModMain.LOGGER.error("Invalid char:" + reader.peek() + " at line " + lineIndex);
                        ModMain.LOGGER.error(line);
                        break;
                    }
                    name_or_type = reader.readStr(ModConfigurationFile::isValidCharForType);
                    reader.skipWhitespace();
                    if (!reader.canRead()) {
                        ModMain.LOGGER.error("Invalid line at line " + lineIndex);
                        ModMain.LOGGER.error(line);
                        break;
                    }
                    switch (reader.peek()) {
                        case '{' -> {
                            // category
                            // 名前に()を含んでしまっている可能性があるので
                            boolean isValid = true;
                            for (char c : name_or_type.toCharArray()) {
                                if (!isValidChar(c)) {
                                    isValid = false;
                                    ModMain.LOGGER.error("Invalid char:" + c + " at line " + lineIndex);
                                    ModMain.LOGGER.error(line);
                                    break;
                                }
                            }
                            if (!isValid)
                                break;
                            reader.skip();
                            category_path.add(name_or_type);
                        }
                        case ':' -> {
                            // property
                            reader.skip();
                            if (!reader.canRead()) {
                                ModMain.LOGGER.error("Invalid line at line " + lineIndex);
                                ModMain.LOGGER.error(line);
                                break outerSwitch;
                            }
                            String name;
                            if (reader.peek() == '"') {
                                name = reader.readQuotedString();
                            } else {
                                name = reader.readStr(ModConfigurationFile::isValidChar);
                            }
                            reader.skipWhitespace();
                            if (!reader.canRead()) {
                                ModMain.LOGGER.error("Invalid line at line " + lineIndex);
                                ModMain.LOGGER.error(line);
                                break outerSwitch;
                            }
                            if (reader.peek() == '<') {
                                // リスト
                                listType = name_or_type;
                                listName = name;
                                listItems = new ArrayList<>();
                            } else if (reader.peek() != '=') {
                                ModMain.LOGGER.error("Invalid char:" + reader.peek() + " at line " + lineIndex);
                                ModMain.LOGGER.error(line);
                                break outerSwitch;
                            }
                            // 普通の値
                            reader.skip();
                            String value = reader.getRemaining();

                            ConfigVersionUpdateContext updateContext = new ConfigVersionUpdateContext(this, StringUtils.join(category_path, '.'), name_or_type, name, value);
                            updater.accept(updateContext);

                            if (updateContext.cancelled)
                                break outerSwitch;

                            ModConfigCategory category = tryGetCategory(updateContext.categoryPath);
                            if (category == null) {
                                ModMain.LOGGER.error("category path \"{}\" is not found!", updateContext.categoryPath);
                                break outerSwitch;
                            }
                            AbstractConfigProperty property = category.get(updateContext.name);
                            if (property == null) {
                                ModMain.LOGGER.error("Unknown property:" + updateContext.name);
                            } else if (!property.getTypeString().equals(updateContext.type)) {
                                ModMain.LOGGER.error("Unmatched type {} (expected {})", updateContext.type, property.getTypeString());
                            } else {
                                if (!property.readValue(updateContext.value))
                                    ModMain.LOGGER.error("Invalid value \"{}\" for {}.{}", updateContext.value, updateContext.categoryPath, updateContext.name);
                            }
                        }
                        default -> ModMain.LOGGER.error("Invalid char:" + reader.peek() + " at line " + lineIndex);
                    }
                }
            }
        }

        if (loadedConfigVersion.isEmpty()) {
            ModMain.LOGGER.error("Config version not detected!");
        }
    }

    private void readOption(String name, String value) {
        switch (name) {
            case CONFIG_VERSION -> loadedConfigVersion = value;
            default -> ModMain.LOGGER.error("Unknown config option:" + name);
        }
    }

    public void save() {
        try {
            if (file.getParentFile() != null)
                file.getParentFile().mkdirs();

            if (!file.exists() && !file.createNewFile())
                return;

            if (file.canWrite()) {
                FileOutputStream fos = new FileOutputStream(file);
                BufferedWriter buffer = new BufferedWriter(new OutputStreamWriter(fos, StandardCharsets.UTF_8));

                buffer.write("# This is a configuration file of " + ModTagsGenerated.MODNAME + Configuration.NEW_LINE);
                buffer.write("~" + CONFIG_VERSION + ": " + configVersion + Configuration.NEW_LINE + Configuration.NEW_LINE);

                rootCategory.write(buffer, -1, "");

                buffer.close();
                fos.close();
            }
        } catch (IOException e) {
            ModMain.LOGGER.error("Error while saving config {}.", file.getName(), e);
        }
    }

    public void create(Field field, @Nullable Object fieldOwnerInstance) throws IllegalAccessException {
        rootCategory.create(field, fieldOwnerInstance);
    }

    public void storeToField() throws IllegalAccessException {
        rootCategory.storeToField();
    }

    public void loadFromField() throws IllegalAccessException {
        rootCategory.loadFromField();
    }

    public ModConfigCategory getOrCreateCategory(String categoryPath) {
        if (categoryPath.isEmpty())
            return rootCategory;
        ModConfigCategory category = rootCategory;
        for (String c : categoryPath.split("\\.")) {
            category = category.getOrCreateCategory(c);
        }
        return category;
    }

    public ModConfigCategory getCategory(String categoryPath) {
        if (categoryPath.isEmpty())
            return rootCategory;
        ModConfigCategory category = rootCategory;
        for (String c : categoryPath.split("\\.")) {
            category = category.tryGetCategory(c);
            if (category == null)
                throw new IllegalStateException("category path \"" + categoryPath + "\" is not found!");
        }
        return category;
    }

    @Nullable
    public ModConfigCategory tryGetCategory(String categoryPath) {
        if (categoryPath.isEmpty())
            return rootCategory;
        ModConfigCategory category = rootCategory;
        for (String c : categoryPath.split("\\.")) {
            category = category.tryGetCategory(c);
            if (category == null)
                return null;
        }
        return category;
    }


    public static boolean isValidChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }
    public static boolean isValidCharForType(char c) {
        return Character.isLetterOrDigit(c) || c == '_' || c == '(' || c == ')';
    }
}
