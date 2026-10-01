package showcase;

import org.junit.jupiter.api.Tag;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks heavy showcase renders and benchmarks. They take minutes to hours, so they are excluded
 * from the default build and run with {@code mvn test -Pshowcase}.
 *
 * @author itzik123
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Tag("showcase")
public @interface Showcase {
}
