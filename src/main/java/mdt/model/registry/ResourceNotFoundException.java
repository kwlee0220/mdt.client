package mdt.model.registry;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class ResourceNotFoundException extends RegistryException {
    private static final long serialVersionUID = 1L;

    public ResourceNotFoundException(final String message) {
        super(message);
    }
}
