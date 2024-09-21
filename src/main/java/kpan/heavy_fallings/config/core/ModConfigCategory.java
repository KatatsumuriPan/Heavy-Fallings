package kpan.heavy_fallings.config.core;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import kpan.heavy_fallings.ModTagsGenerated;
import kpan.heavy_fallings.config.core.ConfigAnnotations.ConfigOrder;
import kpan.heavy_fallings.config.core.ConfigAnnotations.FileComment;
import kpan.heavy_fallings.config.core.gui.ModGuiConfig;
import kpan.heavy_fallings.config.core.gui.ModGuiConfigEntries;
import kpan.heavy_fallings.config.core.gui.ModGuiConfigEntries.CategoryEntry;
import kpan.heavy_fallings.config.core.gui.ModGuiConfigEntries.IGuiConfigEntry;
import kpan.heavy_fallings.config.core.properties.AbstractConfigProperty;
import kpan.heavy_fallings.config.core.properties.ConfigPropertyBlockPredicate;
import kpan.heavy_fallings.config.core.properties.ConfigPropertyBool;
import kpan.heavy_fallings.config.core.properties.ConfigPropertyChar;
import kpan.heavy_fallings.config.core.properties.ConfigPropertyDouble;
import kpan.heavy_fallings.config.core.properties.ConfigPropertyEnum;
import kpan.heavy_fallings.config.core.properties.ConfigPropertyFloat;
import kpan.heavy_fallings.config.core.properties.ConfigPropertyInt;
import kpan.heavy_fallings.config.core.properties.ConfigPropertyList;
import kpan.heavy_fallings.config.core.properties.ConfigPropertyLong;
import kpan.heavy_fallings.config.core.properties.ConfigPropertyString;
import kpan.heavy_fallings.util.IBlockPredicate;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

public class ModConfigCategory implements IConfigElement {
    private static final String INDENT = "    ";
    public static final CharMatcher allowedProperties = CharMatcher.forPredicate(ModConfigCategory::isValidChar);
    private final String id;
    public final boolean isRoot;
    private final ModConfigurationFile configuration;
    private String commentForFile = "";
    private int order = 0;
    private ConfigSide side;
    private final Map<String, ModConfigCategory> children = new TreeMap<>();
    private final Map<String, AbstractConfigProperty> id2PropertyMap = new TreeMap<>();

    @SideOnly(Side.CLIENT)
    private boolean showInGUI = true;

    public ModConfigCategory(String id, boolean isRoot, ModConfigurationFile configuration) {
        this.id = id;
        this.isRoot = isRoot;
        this.configuration = configuration;
    }

    public String getId() {
        return id;
    }

    public void setCommentForFile(String commentForFile) {
        this.commentForFile = commentForFile;
    }
    public String getCommentForFile() {
        return commentForFile;
    }

    @Override
    public String getNameTranslationKey(String path) {
        if (path.isEmpty())
            return ModTagsGenerated.MODID + ".config." + getId();
        else
            return ModTagsGenerated.MODID + ".config." + path + "." + getId();
    }

    @Override
    public String getCommentTranslationKey(String path) {
        if (path.isEmpty())
            return ModTagsGenerated.MODID + ".config." + getId() + ".tooltip";
        else
            return ModTagsGenerated.MODID + ".config." + path + "." + getId() + ".tooltip";
    }

    public void clear() {
        children.clear();
        id2PropertyMap.clear();
    }

    public void put(String id, AbstractConfigProperty property) {
        id2PropertyMap.put(id, property);
    }

    @Nullable
    public AbstractConfigProperty get(String id) {
        AbstractConfigProperty property = id2PropertyMap.get(id);
        if (property == null)
            throw new IllegalArgumentException("Property \"" + id + "\" is not found!");
        return property;
    }

    public List<IConfigElement> getOrderedElements() {
        List<IConfigElement> list = new ArrayList<>(children.size() + id2PropertyMap.size());
        list.addAll(children.values());
        list.addAll(id2PropertyMap.values());
        list.sort(Comparator.comparingInt(IConfigElement::getOrder));
        return list;
    }

    @Override
    public void write(BufferedWriter out, int indent, String path) throws IOException {
        String pad = getIndent(indent);

        String comment = CommentLocalizer.tryLocalize(getCommentTranslationKey(path), getCommentForFile());
        if (!comment.isEmpty()) {
            writeLine(out, pad, Configuration.COMMENT_SEPARATOR);
            writeLine(out, pad, "# ", id);
            writeLine(out, pad, "#--------------------------------------------------------------------------------------------------------#");
            Splitter splitter = Splitter.onPattern("\r?\n");

            for (String line : splitter.split(comment)) {
                writeLine(out, pad, "# ", line);
            }

            writeLine(out, pad, Configuration.COMMENT_SEPARATOR);
        }

        if (!isRoot) {
            String id = this.id;
            if (!allowedProperties.matchesAllOf(id)) {
                id = '"' + id + '"';
            }
            writeLine(out, pad, id, " {");
        }

        out.newLine();
        for (IConfigElement element : getOrderedElements()) {
            String p;
            if (isRoot)
                p = "";
            else if (path.isEmpty())
                p = getId();
            else
                p = path + "." + getId();
            element.write(out, indent + 1, p);
            out.write(Configuration.NEW_LINE);
        }

        if (!isRoot)
            writeLine(out, pad, "}");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IGuiConfigEntry toEntry(ModGuiConfig screen, ModGuiConfigEntries entryList) {
        return new CategoryEntry(screen, entryList, this);
    }

    public ModConfigCategory getOrCreateCategory(String name) {
        ModConfigCategory category = children.get(name);
        if (category == null) {
            category = new ModConfigCategory(name, false, configuration);
            children.put(category.id, category);
        }
        return category;
    }

    @Nullable
    public ModConfigCategory tryGetCategory(String name) {
        return children.get(name);
    }

    public void create(Field field, @Nullable Object fieldOwnerInstance) throws IllegalAccessException {
        String id = readId(field);
        String fileComment = readCommentForFile(field);
        int order = readOrder(field);
        ConfigSide side = readSide(field, getSide());
        AbstractConfigProperty prop;
        Class<?> type = field.getType();
        if (type == boolean.class) {
            prop = new ConfigPropertyBool(field, fieldOwnerInstance, id, fileComment, order, side);
        } else if (type == int.class) {
            prop = new ConfigPropertyInt(field, fieldOwnerInstance, id, fileComment, order, side);
        } else if (type == long.class) {
            prop = new ConfigPropertyLong(field, fieldOwnerInstance, id, fileComment, order, side);
        } else if (type == float.class) {
            prop = new ConfigPropertyFloat(field, fieldOwnerInstance, id, fileComment, order, side);
        } else if (type == double.class) {
            prop = new ConfigPropertyDouble(field, fieldOwnerInstance, id, fileComment, order, side);
        } else if (type == char.class) {
            prop = new ConfigPropertyChar(field, fieldOwnerInstance, id, fileComment, order, side);
        } else if (type.isPrimitive()) {
            throw new RuntimeException("Not Supported:" + type.getName());
        } else if (type.isEnum()) {
            prop = new ConfigPropertyEnum(field, fieldOwnerInstance, id, fileComment, order, side);
        } else if (type.isArray()) {
            prop = ConfigPropertyList.create(field, fieldOwnerInstance, id, fileComment, order, side);
        } else if (type == String.class) {
            prop = new ConfigPropertyString(field, fieldOwnerInstance, id, fileComment, order, side);
        } else if (Collection.class.isAssignableFrom(type)) {
            prop = ConfigPropertyList.create(field, fieldOwnerInstance, id, fileComment, order, side);
        } else if (IBlockPredicate.class.isAssignableFrom(type)) {
            prop = new ConfigPropertyBlockPredicate(field, fieldOwnerInstance, id, fileComment, order, side);
        } else {
            ModConfigCategory new_category = getOrCreateCategory(id);
            new_category.setCommentForFile(fileComment);
            new_category.setOrder(order);
            new_category.setSide(side);
            for (Field f : field.getType().getFields()) {
                new_category.create(f, field.get(fieldOwnerInstance));
            }
            return;
        }
        if (id2PropertyMap.containsKey(prop.getId()))
            throw new IllegalStateException("property named to \"" + id + "\" already exists!");
        put(prop.getId(), prop);
    }

    public void storeToField() throws IllegalAccessException {
        for (ModConfigCategory child : children.values()) {
            child.storeToField();
        }
        for (AbstractConfigProperty property : id2PropertyMap.values()) {
            property.storeToField();
        }
    }

    public void loadFromField() throws IllegalAccessException {
        for (ModConfigCategory child : children.values()) {
            child.loadFromField();
        }
        for (AbstractConfigProperty property : id2PropertyMap.values()) {
            property.loadFromField();
        }
    }


    // TODO:
    @Override
    public boolean requiresWorldRestart() {
        return false;
    }

    @Override
    public boolean requiresMcRestart() {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean showInGui() {
        return showInGUI;
    }


    public static String getIndent(int indent) {
        return StringUtils.repeat(INDENT, Math.max(0, indent));
    }

    public static void writeLine(BufferedWriter out, String... data) throws IOException {
        for (String datum : data) {
            out.write(datum);
        }
        out.write(Configuration.NEW_LINE);
    }

    private static boolean isValidChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    @Override
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public ConfigSide getSide() {
        return side;
    }
    public void setSide(ConfigSide side) {
        this.side = side;
    }


    private static String readId(Field field) {
        ConfigAnnotations.Id annotation = field.getAnnotation(ConfigAnnotations.Id.class);
        if (annotation != null)
            return annotation.value();
        return field.getName();
    }

    private static String readCommentForFile(Field field) {
        FileComment annotation = field.getAnnotation(FileComment.class);
        if (annotation != null)
            return annotation.value();
        return "";
    }

    private static int readOrder(Field field) {
        ConfigOrder annotation = field.getAnnotation(ConfigOrder.class);
        if (annotation == null)
            return 0;
        return annotation.value();
    }

    private static ConfigSide readSide(Field field, @Nullable ConfigSide defaultValue) {
        ConfigAnnotations.Side annotation = field.getAnnotation(ConfigAnnotations.Side.class);
        if (annotation != null)
            return annotation.value();
        if (defaultValue == null)
            throw new RuntimeException("There are no side specifications.!");
        return defaultValue;
    }

}
