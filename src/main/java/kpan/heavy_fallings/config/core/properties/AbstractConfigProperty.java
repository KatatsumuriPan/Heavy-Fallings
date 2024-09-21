package kpan.heavy_fallings.config.core.properties;

import com.google.common.base.Splitter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import kpan.heavy_fallings.ModTagsGenerated;
import kpan.heavy_fallings.config.core.CommentLocalizer;
import kpan.heavy_fallings.config.core.ConfigSide;
import kpan.heavy_fallings.config.core.IConfigElement;
import kpan.heavy_fallings.config.core.ModConfigCategory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractConfigProperty implements IConfigElement {

    protected final Field field;
    protected final @Nullable Object fieldOwnerInstance;
    private final String id;
    private final String commentForFile;
    private final int order;
    private final ConfigSide side;

    private boolean isReadValue = false;
    protected boolean requiresWorldRestart = false;
    protected boolean requiresMcRestart = false;

    @SideOnly(Side.CLIENT)
    protected boolean showInGui = true;

    protected AbstractConfigProperty(Field field, @Nullable Object fieldOwnerInstance, String id, String commentForFile, int order, ConfigSide side) {
        this.field = field;
        this.fieldOwnerInstance = fieldOwnerInstance;
        this.id = id;
        this.commentForFile = commentForFile;
        this.order = order;
        this.side = side;
    }

    @Override
    public int getOrder() {
        return order;
    }

    public String getId() {
        return id;
    }

    @Override
    public ConfigSide getSide() {
        return side;
    }

    public String getCommentForFile() {
        return commentForFile;
    }

    @Override
    public String getNameTranslationKey(String path) {
        return ModTagsGenerated.MODID + ".config." + path + "." + getId();
    }

    @Override
    public String getCommentTranslationKey(String path) {
        return ModTagsGenerated.MODID + ".config." + path + "." + getId() + ".tooltip";
    }

    public abstract boolean readValue(String value);

    public abstract String getAdditionalComment();

    public abstract String getTypeString();

    public abstract String getValueString();

    public abstract String getDefaultValueString();

    public abstract boolean isDefault();

    public abstract void setToDefault();

    public abstract void storeToField() throws IllegalAccessException;

    public abstract void loadFromField() throws IllegalAccessException;

    @Override
    public boolean requiresWorldRestart() {
        return requiresWorldRestart;
    }

    @Override
    public boolean requiresMcRestart() {
        return requiresMcRestart;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean showInGui() {
        return showInGui;
    }

    public abstract boolean isValidValue(String str);

    @Override
    public void write(BufferedWriter out, int indent, String path) throws IOException {
        String pad0 = ModConfigCategory.getIndent(indent);

        String comment = CommentLocalizer.tryLocalize(getCommentTranslationKey(path), getCommentForFile());
        if (!comment.isEmpty() || !getAdditionalComment().isEmpty()) {

            Splitter splitter = Splitter.onPattern("\r?\n");
            if (!comment.isEmpty()) {
                for (String commentLine : splitter.split(comment)) {
                    ModConfigCategory.writeLine(out, pad0, "# ", commentLine);
                }
            }
            if (!getAdditionalComment().isEmpty()) {
                for (String commentLine : splitter.split(getAdditionalComment())) {
                    ModConfigCategory.writeLine(out, pad0, "# ", commentLine);
                }
            }
        }

        String id = getId();
        if (!ModConfigCategory.allowedProperties.matchesAllOf(id)) {
            id = '"' + id + '"';
        }

        if (this instanceof ConfigPropertyList list) {
            String pad1 = ModConfigCategory.getIndent(indent + 1);
            ModConfigCategory.writeLine(out, pad0, getTypeString(), ":", id, " <");

            for (String line : list.getValueStrings()) {
                ModConfigCategory.writeLine(out, pad1, line);
            }

            ModConfigCategory.writeLine(out, pad0, " >");
        } else {
            ModConfigCategory.writeLine(out, pad0, getTypeString(), ":", id, "=", getValueString());
        }
    }


}
