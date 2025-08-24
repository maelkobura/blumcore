package blum.api.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.TimeUnit;

@Retention(RetentionPolicy.RUNTIME)
public @interface Scheduled {

    int value();
    TimeUnit unit() default TimeUnit.SECONDS;

}
