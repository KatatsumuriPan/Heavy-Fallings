package kpan.heavy_fallings.config.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class ConfigAnnotations {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Id {
        String value();
    }

    // .cfgファイルに書き込まれるコメント
    // GUIで表示されるものはこれではなく、Idから翻訳キーを通したもの
    // シングルの場合のみ、Idからの翻訳を適用するようにしている
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface FileComment {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface ConfigOrder {
        int value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Side {
        ConfigSide value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface ServerOnly {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface RangeInt {
        int minValue() default Integer.MIN_VALUE;

        int maxValue() default Integer.MAX_VALUE;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface RangeLong {
        long minValue() default Long.MIN_VALUE;

        long maxValue() default Long.MAX_VALUE;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface RangeFloat {
        float minValue() default Float.NEGATIVE_INFINITY;

        float maxValue() default Float.POSITIVE_INFINITY;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface RangeDouble {
        double minValue() default Double.NEGATIVE_INFINITY;

        double maxValue() default Double.POSITIVE_INFINITY;
    }

}
