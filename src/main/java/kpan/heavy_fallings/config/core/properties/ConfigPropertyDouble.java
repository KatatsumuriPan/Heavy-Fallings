package kpan.heavy_fallings.config.core.properties;

import java.lang.reflect.Field;
import kpan.heavy_fallings.config.core.ConfigAnnotations.RangeDouble;
import kpan.heavy_fallings.config.core.ConfigSide;
import kpan.heavy_fallings.config.core.properties.PropertyValueType.TypeDouble;
import org.jetbrains.annotations.Nullable;

public class ConfigPropertyDouble extends ConfigPropertySingle<Double> {

    private boolean hasSlidingControl = false;

    public ConfigPropertyDouble(Field field, @Nullable Object fieldOwnerInstance, String id, String commentForFile, int order, ConfigSide side) throws IllegalAccessException {
        super(field, fieldOwnerInstance, id, commentForFile, order, side, createTypeDouble(field.getAnnotation(RangeDouble.class)));
    }

    private static TypeDouble createTypeDouble(@Nullable RangeDouble range) {
        return range != null ? new TypeDouble(range.minValue(), range.maxValue()) : new TypeDouble();
    }

    @Override
    public String getAdditionalComment() {
        double minValue = ((TypeDouble) valueType).minValue;
        double maxValue = ((TypeDouble) valueType).maxValue;
        if (minValue == Double.NEGATIVE_INFINITY) {
            if (maxValue == Double.POSITIVE_INFINITY)
                return "Default: " + defaultValue;
            else
                return "Range: ~ " + maxValue + "\nDefault: " + defaultValue;
        } else {
            if (maxValue == Double.POSITIVE_INFINITY)
                return "Range: " + minValue + " ~" + "\nDefault: " + defaultValue;
            else
                return "Range: " + minValue + " ~ " + maxValue + "\nDefault: " + defaultValue;
        }
    }

    public boolean hasSlidingControl() {
        return hasSlidingControl;
    }

    public void setHasSlidingControl(boolean hasSlidingControl) {
        this.hasSlidingControl = hasSlidingControl;
    }
}
