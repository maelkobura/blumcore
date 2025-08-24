package blum.api.services.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceDescriptor {

    String name();
    String displayName() default "Default Name";
    String description() default "No description";
    String version();
    String[] dependencies() default {};

}
