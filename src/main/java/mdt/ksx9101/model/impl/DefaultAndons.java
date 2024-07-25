package mdt.ksx9101.model.impl;

import org.eclipse.digitaltwin.aas4j.v3.model.AasSubmodelElements;

import lombok.Getter;
import lombok.Setter;
import mdt.ksx9101.model.Andon;
import mdt.ksx9101.model.Andons;
import mdt.model.SubmodelElementListHandle;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultAndons extends SubmodelElementListHandle<Andon,DefaultAndon> implements Andons {
	public DefaultAndons() {
		super("Andons", null, false, AasSubmodelElements.SUBMODEL_ELEMENT_COLLECTION);
	}

	@Override
	public DefaultAndon newElementHandle() {
		return new DefaultAndon();
	}
}