package scene;

import java.io.Serial;

/**
 * Thrown when a scene or model file cannot be read or is malformed.
 *
 * @author itzik123
 */
public class SceneLoadException extends RuntimeException {

    /** Serialization version. */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Creates an exception caused by another one.
     *
     * @param message the description of the problem
     * @param cause   the underlying exception
     */
    public SceneLoadException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates an exception.
     *
     * @param message the description of the problem
     */
    public SceneLoadException(String message) {
        super(message);
    }
}
