package kpan.heavy_fallings.config.core.properties;

import java.lang.reflect.Field;
import kpan.heavy_fallings.config.core.ConfigSide;
import kpan.heavy_fallings.config.core.properties.PropertyValueType.TypeBlockPredicate;
import kpan.heavy_fallings.util.IBlockPredicate;
import org.jetbrains.annotations.Nullable;

public class ConfigPropertyBlockPredicate extends ConfigPropertySingle<IBlockPredicate> {


    public ConfigPropertyBlockPredicate(Field field, @Nullable Object fieldOwnerInstance, String id, String commentForFile, int order, ConfigSide side) throws IllegalAccessException {
        super(field, fieldOwnerInstance, id, commentForFile, order, side, new TypeBlockPredicate());
    }

}
