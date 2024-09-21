package kpan.heavy_fallings.config.core.properties;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import kpan.heavy_fallings.config.core.gui.ModGuiConfig;
import kpan.heavy_fallings.config.core.gui.ModGuiConfigEntries;
import kpan.heavy_fallings.config.core.gui.ModGuiConfigEntries.IGuiConfigEntry;
import kpan.heavy_fallings.config.core.gui.ModGuiEditArrayEntries;
import kpan.heavy_fallings.config.core.gui.ModGuiEditArrayEntries.BlockPredicateEntry;
import kpan.heavy_fallings.config.core.gui.ModGuiEditArrayEntries.BooleanEntry;
import kpan.heavy_fallings.config.core.gui.ModGuiEditArrayEntries.CharEntry;
import kpan.heavy_fallings.config.core.gui.ModGuiEditArrayEntries.EnumEntry;
import kpan.heavy_fallings.config.core.gui.ModGuiEditArrayEntries.IArrayEntry;
import kpan.heavy_fallings.config.core.gui.ModGuiEditArrayEntries.NumericalEntry;
import kpan.heavy_fallings.config.core.gui.ModGuiEditArrayEntries.StringEntry;
import kpan.heavy_fallings.util.IBlockPredicate;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;

public abstract class PropertyValueType<T> {

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static PropertyValueType<?> byType(Type type) {
        if (type == Boolean.class) {
            return new TypeBool();
        } else if (type == Character.class) {
            return new TypeChar();
        } else if (type == Double.class) {
            return new TypeDouble();
        } else if (type == Enum.class) {
            return new TypeEnum((Class<? extends Enum<?>>) type);
        } else if (type == Float.class) {
            return new TypeFloat();
        } else if (type == Integer.class) {
            return new TypeInt();
        } else if (type == Long.class) {
            return new TypeLong();
        } else if (type == String.class) {
            return new TypeString();
        } else if (type instanceof Class<?> clazz && IBlockPredicate.class.isAssignableFrom(clazz) || type instanceof ParameterizedType pt && IBlockPredicate.class.isAssignableFrom((Class<?>) pt.getRawType())) {

            return new TypeBlockPredicate();
        } else
            throw new IllegalArgumentException("Invalid class:" + type.getTypeName());
    }

    public final EnumType enumType;

    protected PropertyValueType(EnumType enumType) { this.enumType = enumType; }

    public String getTypeString() {
        return enumType.getTypeString();
    }

    public abstract boolean hasValidation();
    public abstract boolean isValidValue(String str);
    @Nullable
    public abstract T readValue(String str);
    public String toString(T value) {
        return value.toString();
    }
    public boolean equals(T a, T b) {
        return a.equals(b);
    }

    @SideOnly(Side.CLIENT)
    public abstract IGuiConfigEntry toEntry(ModGuiConfig screen, ModGuiConfigEntries entryList, ConfigPropertySingle<T> property);

    @SideOnly(Side.CLIENT)
    public abstract IArrayEntry createEntry(ModGuiEditArrayEntries owningEntryList, ConfigPropertyList propertyList, Object value);

    @SideOnly(Side.CLIENT)
    public abstract IArrayEntry createNewEntry(ModGuiEditArrayEntries owningEntryList, ConfigPropertyList propertyList);

    public static class TypeBool extends PropertyValueType<Boolean> {

        public TypeBool() {
            super(EnumType.BOOL);
        }
        @Override
        public boolean hasValidation() {
            return false;
        }
        @Override
        public boolean isValidValue(String str) {
            return "true".equalsIgnoreCase(str) || "false".equalsIgnoreCase(str);
        }
        @Override
        public @Nullable Boolean readValue(String str) {
            if ("true".equalsIgnoreCase(str)) {
                return true;
            } else if ("false".equalsIgnoreCase(str)) {
                return false;
            } else {
                return null;
            }
        }

        @Override
        public IGuiConfigEntry toEntry(ModGuiConfig screen, ModGuiConfigEntries entryList, ConfigPropertySingle<Boolean> property) {
            return new ModGuiConfigEntries.BooleanEntry(screen, entryList, (ConfigPropertyBool) property);
        }
        @Override
        public IArrayEntry createEntry(ModGuiEditArrayEntries owningEntryList, ConfigPropertyList propertyList, Object value) {
            return new BooleanEntry(owningEntryList, propertyList, Boolean.parseBoolean(value.toString()));
        }
        @Override
        public IArrayEntry createNewEntry(ModGuiEditArrayEntries owningEntryList, ConfigPropertyList propertyList) {
            return new BooleanEntry(owningEntryList, propertyList, true);
        }

    }

    public static class TypeChar extends PropertyValueType<Character> {

        public TypeChar() {
            super(EnumType.CHAR);
        }
        @Override
        public boolean hasValidation() {
            return true;
        }
        @Override
        public boolean isValidValue(String str) {
            return str.length() == 1;
        }
        @Override
        public @Nullable Character readValue(String str) {
            if (str.length() != 1)
                return null;
            return str.charAt(0);
        }

        @Override
        public IGuiConfigEntry toEntry(ModGuiConfig screen, ModGuiConfigEntries entryList, ConfigPropertySingle<Character> property) {
            return new ModGuiConfigEntries.CharEntry(screen, entryList, property);
        }
        @Override
        public IArrayEntry createEntry(ModGuiEditArrayEntries owningEntryList, ConfigPropertyList propertyList, Object value) {
            return new CharEntry(owningEntryList, propertyList, value);
        }
        @Override
        public IArrayEntry createNewEntry(ModGuiEditArrayEntries owningEntryList, ConfigPropertyList propertyList) {
            return new CharEntry(owningEntryList, propertyList, "");
        }

    }

    public static class TypeDouble extends PropertyValueType<Double> {

        public final double minValue;
        public final double maxValue;

        public TypeDouble() {
            this(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        }
        public TypeDouble(double minValue, double maxValue) {
            super(EnumType.DOUBLE);
            this.minValue = minValue;
            this.maxValue = maxValue;
        }
        @Override
        public boolean hasValidation() {
            return minValue != Double.NEGATIVE_INFINITY || maxValue != Double.POSITIVE_INFINITY;
        }
        @Override
        public boolean isValidValue(String str) {
            try {
                double value = Double.parseDouble(str);
                return value >= minValue && value <= maxValue;
            } catch (NumberFormatException ignore) {
                return false;
            }
        }
        @Override
        public @Nullable Double readValue(String str) {
            try {
                double val = Double.parseDouble(str);
                if (val < minValue || val > maxValue)
                    return null;
                return val;
            } catch (NumberFormatException ignore) {
                return null;
            }
        }

        @Override
        public IGuiConfigEntry toEntry(ModGuiConfig screen, ModGuiConfigEntries entryList, ConfigPropertySingle<Double> property) {
            return new ModGuiConfigEntries.DoubleEntry(screen, entryList, (ConfigPropertyDouble) property);
        }
        @Override
        public IArrayEntry createEntry(ModGuiEditArrayEntries owningEntryList, ConfigPropertyList propertyList, Object value) {
            return new NumericalEntry(owningEntryList, propertyList, value, true);
        }
        @Override
        public IArrayEntry createNewEntry(ModGuiEditArrayEntries owningEntryList, ConfigPropertyList propertyList) {
            return new NumericalEntry(owningEntryList, propertyList, 0.0D, true);
        }

    }

    public static class TypeEnum<E extends Enum<E>> extends PropertyValueType<E> {

        private final Class<E> enumClass;

        public TypeEnum(Class<E> enumClass) {
            super(EnumType.ENUM);
            this.enumClass = enumClass;
        }

        public Class<E> getEnumClass() {
            return enumClass;
        }
        @Override
        public boolean hasValidation() {
            return true;
        }
        @Override
        public boolean isValidValue(String str) {
            for (E e : enumClass.getEnumConstants()) {
                if (e.toString().equalsIgnoreCase(str)) {
                    return true;
                }
            }
            return false;
        }
        @Override
        public @Nullable E readValue(String str) {
            for (E e : enumClass.getEnumConstants()) {
                if (e.toString().equalsIgnoreCase(str)) {
                    return e;
                }
            }
            return null;
        }

        @Override
        public IGuiConfigEntry toEntry(ModGuiConfig screen, ModGuiConfigEntries entryList, ConfigPropertySingle<E> property) {
            return new ModGuiConfigEntries.EnumEntry<>(screen, entryList, (ConfigPropertyEnum<E>) property, property.value, property.defaultValue);
        }
        @Override
        public IArrayEntry createEntry(ModGuiEditArrayEntries owningEntryList, ConfigPropertyList propertyList, Object value) {
            return new EnumEntry(owningEntryList, propertyList, (Enum<?>) value);
        }
        @Override
        public IArrayEntry createNewEntry(ModGuiEditArrayEntries owningEntryList, ConfigPropertyList propertyList) {
            return new EnumEntry(owningEntryList, propertyList, enumClass.getEnumConstants()[0]);
        }

    }

    public static class TypeFloat extends PropertyValueType<Float> {

        public final float minValue;
        public final float maxValue;

        public TypeFloat() {
            this(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);
        }
        public TypeFloat(float minValue, float maxValue) {
            super(EnumType.FLOAT);
            this.minValue = minValue;
            this.maxValue = maxValue;
        }
        @Override
        public boolean hasValidation() {
            return minValue != Float.NEGATIVE_INFINITY || maxValue != Float.POSITIVE_INFINITY;
        }
        @Override
        public boolean isValidValue(String str) {
            try {
                float value = Float.parseFloat(str);
                return value >= minValue && value <= maxValue;
            } catch (NumberFormatException ignore) {
                return false;
            }
        }
        @Override
        public @Nullable Float readValue(String str) {
            try {
                float i = Float.parseFloat(str);
                if (i < minValue || i > maxValue)
                    return null;
                return i;
            } catch (NumberFormatException ignore) {
                return null;
            }
        }

        @Override
        public IGuiConfigEntry toEntry(ModGuiConfig screen, ModGuiConfigEntries entryList, ConfigPropertySingle<Float> property) {
            return new ModGuiConfigEntries.FloatEntry(screen, entryList, (ConfigPropertyFloat) property);
        }
        @Override
        public IArrayEntry createEntry(ModGuiEditArrayEntries owningEntryList, ConfigPropertyList propertyList, Object value) {
            return new NumericalEntry(owningEntryList, propertyList, value, true);
        }
        @Override
        public IArrayEntry createNewEntry(ModGuiEditArrayEntries owningEntryList, ConfigPropertyList propertyList) {
            return new NumericalEntry(owningEntryList, propertyList, 0.0F, true);
        }

    }

    public static class TypeInt extends PropertyValueType<Integer> {

        public final int minValue;
        public final int maxValue;

        public TypeInt() {
            this(Integer.MIN_VALUE, Integer.MAX_VALUE);
        }
        public TypeInt(int minValue, int maxValue) {
            super(EnumType.INT);
            this.minValue = minValue;
            this.maxValue = maxValue;
        }
        @Override
        public boolean hasValidation() {
            return minValue != Integer.MIN_VALUE || maxValue != Integer.MAX_VALUE;
        }
        @Override
        public boolean isValidValue(String str) {
            try {
                int value = Integer.parseInt(str);
                return value >= minValue && value <= maxValue;
            } catch (NumberFormatException ignore) {
                return false;
            }
        }
        @Override
        public @Nullable Integer readValue(String str) {
            try {
                int i = Integer.parseInt(str);
                if (i < minValue || i > maxValue)
                    return null;
                return i;
            } catch (NumberFormatException ignore) {
                return null;
            }
        }

        @Override
        public IGuiConfigEntry toEntry(ModGuiConfig screen, ModGuiConfigEntries entryList, ConfigPropertySingle<Integer> property) {
            return new ModGuiConfigEntries.IntegerEntry(screen, entryList, (ConfigPropertyInt) property);
        }
        @Override
        public IArrayEntry createEntry(ModGuiEditArrayEntries owningEntryList, ConfigPropertyList propertyList, Object value) {
            return new NumericalEntry(owningEntryList, propertyList, value, false);
        }
        @Override
        public IArrayEntry createNewEntry(ModGuiEditArrayEntries owningEntryList, ConfigPropertyList propertyList) {
            return new NumericalEntry(owningEntryList, propertyList, 0, false);
        }

    }

    public static class TypeLong extends PropertyValueType<Long> {

        public final long minValue;
        public final long maxValue;

        public TypeLong() {
            this(Long.MIN_VALUE, Long.MAX_VALUE);
        }
        public TypeLong(long minValue, long maxValue) {
            super(EnumType.LONG);
            this.minValue = minValue;
            this.maxValue = maxValue;
        }
        @Override
        public boolean hasValidation() {
            return minValue != Long.MIN_VALUE || maxValue != Long.MAX_VALUE;
        }
        @Override
        public boolean isValidValue(String str) {
            try {
                long value = Long.parseLong(str);
                return value >= minValue && value <= maxValue;
            } catch (NumberFormatException ignore) {
                return false;
            }
        }
        @Override
        public @Nullable Long readValue(String str) {
            try {
                long i = Long.parseLong(str);
                if (i < minValue || i > maxValue)
                    return null;
                return i;
            } catch (NumberFormatException ignore) {
                return null;
            }

        }

        @Override
        public IGuiConfigEntry toEntry(ModGuiConfig screen, ModGuiConfigEntries entryList, ConfigPropertySingle<Long> property) {
            return new ModGuiConfigEntries.LongEntry(screen, entryList, (ConfigPropertyLong) property);
        }
        @Override
        public IArrayEntry createEntry(ModGuiEditArrayEntries owningEntryList, ConfigPropertyList propertyList, Object value) {
            return new NumericalEntry(owningEntryList, propertyList, value, false);
        }
        @Override
        public IArrayEntry createNewEntry(ModGuiEditArrayEntries owningEntryList, ConfigPropertyList propertyList) {
            return new NumericalEntry(owningEntryList, propertyList, 0L, false);
        }

    }

    public static class TypeString extends PropertyValueType<String> {

        public TypeString() {
            super(EnumType.STRING);
        }
        @Override
        public boolean hasValidation() {
            return false;
        }
        @Override
        public boolean isValidValue(String str) {
            return true;
        }
        @Override
        public @Nullable String readValue(String value) {
            return value;
        }

        @Override
        public IGuiConfigEntry toEntry(ModGuiConfig screen, ModGuiConfigEntries entryList, ConfigPropertySingle<String> property) {
            return new ModGuiConfigEntries.StringEntry(screen, entryList, property);
        }
        @Override
        public IArrayEntry createEntry(ModGuiEditArrayEntries owningEntryList, ConfigPropertyList propertyList, Object value) {
            return new StringEntry(owningEntryList, propertyList, value);
        }
        @Override
        public IArrayEntry createNewEntry(ModGuiEditArrayEntries owningEntryList, ConfigPropertyList propertyList) {
            return new StringEntry(owningEntryList, propertyList, "");
        }

    }

    public static class TypeBlockPredicate extends PropertyValueType<IBlockPredicate> {

        public TypeBlockPredicate() {
            super(EnumType.BLOCK_PREDICATE);
        }
        @Override
        public boolean hasValidation() {
            return true;
        }
        @Override
        public boolean isValidValue(String str) {
            return IBlockPredicate.canParse(str);
        }
        @Override
        public @Nullable IBlockPredicate readValue(String str) {
            if (!isValidValue(str))
                return null;
            return IBlockPredicate.parse(str);
        }

        @Override
        public IGuiConfigEntry toEntry(ModGuiConfig screen, ModGuiConfigEntries entryList, ConfigPropertySingle<IBlockPredicate> property) {
            return new ModGuiConfigEntries.BlockPredicateEntry(screen, entryList, (ConfigPropertyBlockPredicate) property);
        }
        @Override
        public IArrayEntry createEntry(ModGuiEditArrayEntries owningEntryList, ConfigPropertyList propertyList, Object value) {
            return new BlockPredicateEntry(owningEntryList, propertyList, value);
        }
        @Override
        public IArrayEntry createNewEntry(ModGuiEditArrayEntries owningEntryList, ConfigPropertyList propertyList) {
            return new BlockPredicateEntry(owningEntryList, propertyList);
        }

    }

    public enum EnumType {
        BOOL("Bool"),
        CHAR("Char"),
        DOUBLE("Double"),
        ENUM("Enum"),
        FLOAT("Float"),
        INT("Int"),
        LONG("Long"),
        STRING("String"),
        BLOCK_PREDICATE("BlockPredicate"),
        ;

        private final String typeString;

        EnumType(String typeString) { this.typeString = typeString; }

        public final String getTypeString() {
            return typeString;
        }
    }
}
