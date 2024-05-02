package mdt.model.registry;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class ResourceNotFoundException extends RegistryException {
    private static final long serialVersionUID = 1L;

    public ResourceNotFoundException(String resourceType, String resourceId) {
        super(String.format("Resource(type=%s, id=%s)", resourceType, resourceId));
    }
}
