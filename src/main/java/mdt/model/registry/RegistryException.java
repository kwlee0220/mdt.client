package mdt.model.registry;

import mdt.model.instance.MDTInstanceManagerException;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class RegistryException extends MDTInstanceManagerException {
    private static final long serialVersionUID = 1L;

    public RegistryException(final String message) {
        super(message);
    }
}
