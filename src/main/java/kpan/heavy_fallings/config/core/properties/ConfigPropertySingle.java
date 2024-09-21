package kpan.heavy_fallings.config.core.properties;

import java.lang.reflect.Field;
import kpan.heavy_fallings.config.core.ConfigSide;
import kpan.heavy_fallings.config.core.gui.ModGuiConfig;
import kpan.heavy_fallings.config.core.gui.ModGuiConfigEntries;
import kpan.heavy_fallings.config.core.gui.ModGuiConfigEntries.IGuiConfigEntry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;

public abstract class ConfigPropertySingle<T> extends AbstractConfigProperty {


    protected final PropertyValueType<T> valueType;
    protected final T defaultValue;
    protected T value;

    @SuppressWarnings("unchecked")
    public ConfigPropertySingle(Field field, @Nullable Object fieldOwnerInstance, String id, String commentForFile, int order, ConfigSide side, PropertyValueType<T> valueType) throws IllegalAccessException {
        super(field, fieldOwnerInstance, id, commentForFile, order, side);
        this.valueType = valueType;
        this.defaultValue = (T) field.get(fieldOwnerInstance);
        this.value = defaultValue;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    @Override
    public boolean readValue(String value) {
        this.value = valueType.readValue(value);
        return true;
    }

    @Override
    public String getAdditionalComment() {
        return "Default: " + defaultValue;
    }

    public PropertyValueType<T> getValueType() {
        return valueType;
    }
    @Override
    public String getTypeString() {
        return valueType.getTypeString();
    }

    @Override
    public String getValueString() {
        return valueType.toString(value);
    }

    @Override
    public String getDefaultValueString() {
        return valueType.toString(defaultValue);
    }

    @Override
    public boolean isDefault() {
        return valueType.equals(value, defaultValue);
    }

    @Override
    public void setToDefault() {
        value = defaultValue;
    }

    @Override
    public boolean isValidValue(String str) {
        return valueType.isValidValue(str);
    }

    @Override
    public void storeToField() throws IllegalAccessException {
        field.set(fieldOwnerInstance, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void loadFromField() throws IllegalAccessException {
        value = (T) field.get(fieldOwnerInstance);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IGuiConfigEntry toEntry(ModGuiConfig screen, ModGuiConfigEntries entryList) {
        return valueType.toEntry(screen, entryList, this);
    }
}
