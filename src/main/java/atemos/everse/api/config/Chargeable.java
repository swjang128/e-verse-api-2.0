package atemos.everse.api.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Chargeable 어노테이션은 메서드에 부과 가능한 요금이 있는지 여부를 지정하는 데 사용됩니다.
 * value 속성은 기본적으로 false로 설정되어 있으며, 필요에 따라 true로 설정할 수 있습니다.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Chargeable {
    boolean value() default false;
}