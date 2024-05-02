package mdt.model.repository;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;

import mdt.model.service.SubmodelService;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface SubmodelRepository {
	public List<SubmodelService> getAllSubmodels();
	public SubmodelService getSubmodelById(String id);
	public List<SubmodelService> getAllSubmodelBySemanticId(String semanticId);
	public List<SubmodelService> getAllSubmodelsByIdShort(String idShort);
	
	public SubmodelService addSubmodel(Submodel submodel);
	public SubmodelService updateSubmodelById(Submodel submodel);
	public void removeSubmodelById(String id);
	
}
