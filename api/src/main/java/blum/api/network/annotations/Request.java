package blum.api.network.annotations;

import java.lang.annotation.*;
import java.lang.reflect.Method;

public class Request {

    @TypedRequest(Type.GET)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Get {
        String value() default "";
    }

    @TypedRequest(Type.POST)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Post {
        String value() default "";
    }

    @TypedRequest(Type.PUT)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Put {
        String value() default "";
    }

    @TypedRequest(Type.DELETE)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Delete {
        String value() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.ANNOTATION_TYPE) // correction : ça doit cibler d'autres annotations
    private @interface TypedRequest {
        Type value();
    }

    public enum Type {
        GET, POST, PUT, DELETE
    }

    /** Vérifie si une annotation est un @Request.* */
    public static boolean isRequestAnnotation(Annotation anno) {
        return anno.annotationType().isAnnotationPresent(TypedRequest.class);
    }

    /** Récupère le type d'une annotation @Request.* */
    public static Type parseType(Annotation anno) {
        if (!isRequestAnnotation(anno)) {
            throw new IllegalArgumentException("Not a Request annotation: " + anno);
        }
        return anno.annotationType().getAnnotation(TypedRequest.class).value();
    }

    /** Récupère la valeur (path) d'une annotation @Request.* */
    public static String parseValue(Annotation anno) {
        try {
            return (String) anno.annotationType().getMethod("value").invoke(anno);
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract value() from annotation: " + anno, e);
        }
    }

    /** Vérifie si une méthode a une @Request.* */
    public static boolean hasRequestAnnotation(Method mtd) {
        for (Annotation anno : mtd.getAnnotations()) {
            if (isRequestAnnotation(anno)) {
                return true;
            }
        }
        return false;
    }

    /** Récupère le (type, value) de la première @Request.* trouvée */
    public static RequestInfo extract(Method mtd) {
        for (Annotation anno : mtd.getAnnotations()) {
            if (isRequestAnnotation(anno)) {
                return new RequestInfo(parseType(anno), parseValue(anno));
            }
        }
        return null;
    }

    /** Petit POJO pour contenir l'info */
    public static record RequestInfo(Type type, String path) {}
}
