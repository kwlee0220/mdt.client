package mdt.model.registry;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class ResourceAlreadyExistsException extends RegistryException {
    private static final long serialVersionUID = 1L;


    public ResourceAlreadyExistsException(final String message) {
        super(message);
    }
}
