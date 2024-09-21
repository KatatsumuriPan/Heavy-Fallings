package kpan.heavy_fallings.config.core.properties;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import kpan.heavy_fallings.config.core.ConfigSide;
import kpan.heavy_fallings.config.core.gui.ModGuiConfig;
import kpan.heavy_fallings.config.core.gui.ModGuiConfigEntries;
import kpan.heavy_fallings.config.core.gui.ModGuiConfigEntries.IGuiConfigEntry;
import kpan.heavy_fallings.config.core.gui.ModGuiConfigEntries.ListEntry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

public abstract class ConfigPropertyList extends AbstractConfigProperty {

    public static ConfigPropertyList create(Field field, @Nullable Object fieldOwnerInstance, String id, String commentForFile, int order, ConfigSide side) throws IllegalAccessException {
        Object fieldValue = field.get(fieldOwnerInstance);
        if (fieldValue.getClass().isArray()) {
            return new ArrayImpl(field, fieldOwnerInstance, id, commentForFile, order, side);
        } else if (fieldValue instanceof Collection<?> collection) {
            return new CollectionImpl(field, fieldOwnerInstance, id, commentForFile, order, side, collection);
        } else {
            throw new IllegalArgumentException(field.getName() + " is not array or collection");
        }

    }

    protected final PropertyValueType<?> elementType;
    protected final Object[] defaultValue;
    protected Object[] value;

    public ConfigPropertyList(Field field, @Nullable Object fieldOwnerInstance, String id, String commentForFile, int order, ConfigSide side, PropertyValueType<?> elementType, Object[] defaultValue) {
        super(field, fieldOwnerInstance, id, commentForFile, order, side);
        this.elementType = elementType;
        this.defaultValue = defaultValue;
        value = defaultValue;
    }

    public abstract boolean isLengthFixed();
    public abstract int getMaxListLength();

    public PropertyValueType<?> getElementType() {
        return elementType;
    }
    public Object[] getValues() {
        return value;
    }

    public Object[] getDefaultValues() {
        return defaultValue;
    }

    public String[] getValueStrings() {
        String[] res = new String[value.length];
        for (int i = 0; i < value.length; i++) {
            res[i] = elementType.toString(cast(value[i]));
        }
        return res;
    }

    public void setValues(Object[] currentValues) {
        value = currentValues;
    }

    @Override
    public boolean readValue(String value) {
        String[] values = value.split("\n");
        Object[] newValue = new Object[values.length];
        for (int i = 0; i < values.length; i++) {
            if (!elementType.isValidValue(values[i]))
                return false;
            newValue[i] = elementType.readValue(values[i]);
        }
        this.value = newValue;
        return true;
    }

    @Override
    public String getAdditionalComment() {
        return "Default: <" + StringUtils.join(defaultValue, ' ') + ">";
    }

    @Override
    public String getTypeString() {
        return "List(" + elementType.getTypeString() + ")";
    }

    @Override
    public String getValueString() {
        return StringUtils.join(value, ' ');
    }

    @Override
    public String getDefaultValueString() {
        return StringUtils.join(defaultValue, ' ');
    }

    @Override
    public boolean isDefault() {
        return value == defaultValue;
    }

    @Override
    public void setToDefault() { value = defaultValue; }

    @Override
    public boolean isValidValue(String str) {
        String[] values = str.split("\n");
        for (String s : values) {
            if (!elementType.isValidValue(s))
                return false;
        }
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IGuiConfigEntry toEntry(ModGuiConfig screen, ModGuiConfigEntries entryList) {
        return new ListEntry(screen, entryList, this);
    }

    @SuppressWarnings("unchecked")
    public <T> T cast(Object o) {
        return (T) o;
    }


    private static class ArrayImpl extends ConfigPropertyList {

        public ArrayImpl(Field field, @Nullable Object fieldOwnerInstance, String id, String commentForFile, int order, ConfigSide side) throws IllegalAccessException {
            super(field, fieldOwnerInstance, id, commentForFile, order, side, createPropertyValueType(field, fieldOwnerInstance), getValues(field, fieldOwnerInstance));
        }

        private static PropertyValueType<?> createPropertyValueType(Field field, @Nullable Object fieldOwnerInstance) throws IllegalAccessException {
            return PropertyValueType.byType(field.get(fieldOwnerInstance).getClass().getComponentType());
        }

        @Override
        public void storeToField() throws IllegalAccessException {
            Object array = field.get(fieldOwnerInstance);
            for (int i = 0; i < value.length; i++) {
                Array.set(array, i, value[i]);
            }
        }

        @Override
        public void loadFromField() throws IllegalAccessException {
            Object array = field.get(fieldOwnerInstance);
            for (int i = 0; i < value.length; i++) {
                value[i] = Array.get(array, i);
            }
        }

        private static Object[] getValues(Field field, @Nullable Object fieldOwnerInstance) throws IllegalAccessException {
            Object array = field.get(fieldOwnerInstance);
            Object[] res = new Object[Array.getLength(array)];
            for (int i = 0; i < res.length; i++) {
                res[i] = Array.get(array, i);
            }
            return res;
        }
        @Override
        public boolean isLengthFixed() {
            return true;
        }
        @Override
        public int getMaxListLength() {
            return value.length;
        }
    }

    private static class CollectionImpl extends ConfigPropertyList {

        private final Collection<?> collection;

        public CollectionImpl(Field field, @Nullable Object fieldOwnerInstance, String id, String commentForFile, int order, ConfigSide side, Collection<?> collection) {
            super(field, fieldOwnerInstance, id, commentForFile, order, side, createPropertyValueType(field), collection.toArray());
            this.collection = collection;
        }

        private static PropertyValueType<?> createPropertyValueType(Field field) {
            Type elementType = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
            return PropertyValueType.byType(elementType);
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        @Override
        public void storeToField() {
            collection.clear();
            Collections.addAll((Collection) collection, value);
        }

        @Override
        public void loadFromField() {
            value = collection.toArray();
        }

        public boolean isLengthFixed() {
            return false;
        }

        public int getMaxListLength() {
            return Integer.MAX_VALUE - 10;
        }

    }
}
