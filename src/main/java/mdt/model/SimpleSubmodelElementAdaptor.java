package mdt.model;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import utils.func.Funcs;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SimpleSubmodelElementAdaptor implements MDTSubmodelElement {
	private SubmodelElement m_sme;
	
	public SimpleSubmodelElementAdaptor() { }
	public SimpleSubmodelElementAdaptor(SubmodelElement sme) {
		m_sme = sme;
	}
	
	public String getIdShort() {
		return Funcs.applyIfNonNull(m_sme, SubmodelElement::getIdShort);
	}

	@Override
	public SubmodelElement toAasModel() {
		return m_sme;
	}

	@Override
	public void fromAasModel(SubmodelElement model) {
		m_sme = model;
	}

}
