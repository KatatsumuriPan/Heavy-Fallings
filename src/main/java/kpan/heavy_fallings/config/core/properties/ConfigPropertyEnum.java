package kpan.heavy_fallings.config.core.properties;

import java.lang.reflect.Field;
import kpan.heavy_fallings.config.core.ConfigSide;
import kpan.heavy_fallings.config.core.properties.PropertyValueType.TypeEnum;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

public class ConfigPropertyEnum<E extends Enum<E>> extends ConfigPropertySingle<E> {


    @SuppressWarnings("unchecked")
    public ConfigPropertyEnum(Field field, @Nullable Object fieldOwnerInstance, String id, String commentForFile, int order, ConfigSide side) throws IllegalAccessException {
        super(field, fieldOwnerInstance, id, commentForFile, order, side, new TypeEnum<E>((Class<E>) field.get(fieldOwnerInstance).getClass()));
    }

    @Override
    public String getAdditionalComment() {
        return "Possible values: [" + StringUtils.join(((TypeEnum<E>) valueType).getEnumClass().getEnumConstants(), ", ") + "]\nDefault: " + defaultValue;
    }

}
