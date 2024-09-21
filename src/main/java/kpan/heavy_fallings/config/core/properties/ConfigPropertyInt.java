package kpan.heavy_fallings.config.core.properties;

import java.lang.reflect.Field;
import kpan.heavy_fallings.config.core.ConfigAnnotations.RangeInt;
import kpan.heavy_fallings.config.core.ConfigSide;
import kpan.heavy_fallings.config.core.properties.PropertyValueType.TypeInt;
import org.jetbrains.annotations.Nullable;

public class ConfigPropertyInt extends ConfigPropertySingle<Integer> {


    private boolean hasSlidingControl = false;

    public ConfigPropertyInt(Field field, @Nullable Object fieldOwnerInstance, String id, String commentForFile, int order, ConfigSide side) throws IllegalAccessException {
        super(field, fieldOwnerInstance, id, commentForFile, order, side, createTypeInt(field.getAnnotation(RangeInt.class)));
    }

    private static TypeInt createTypeInt(@Nullable RangeInt range) {
        return range != null ? new TypeInt(range.minValue(), range.maxValue()) : new TypeInt();
    }

    @Override
    public String getAdditionalComment() {
        int minValue = ((TypeInt) valueType).minValue;
        int maxValue = ((TypeInt) valueType).maxValue;
        if (minValue == Integer.MIN_VALUE) {
            if (maxValue == Integer.MAX_VALUE)
                return "Default: " + defaultValue;
            else
                return "Range: ~ " + maxValue + "\nDefault: " + defaultValue;
        } else {
            if (maxValue == Integer.MAX_VALUE)
                return "Range: " + minValue + " ~" + "\nDefault: " + defaultValue;
            else
                return "Range: " + minValue + " ~ " + maxValue + "\ndefault: " + defaultValue;
        }
    }

    public boolean hasSlidingControl() {
        return hasSlidingControl;
    }

    public void setHasSlidingControl(boolean hasSlidingControl) {
        this.hasSlidingControl = hasSlidingControl;
    }
}
