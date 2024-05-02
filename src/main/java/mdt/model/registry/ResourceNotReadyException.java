package mdt.model.registry;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class ResourceNotReadyException extends RegistryException {
    private static final long serialVersionUID = 1L;

    public ResourceNotReadyException(String resourceType, String resourceId) {
        super(String.format("Resource(type=%s, id=%s)", resourceType, resourceId));
    }
}
