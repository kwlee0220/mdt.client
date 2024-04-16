package mdt.model.repository;

import java.util.List;
import java.util.Optional;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;

import mdt.model.resource.SubmodelService;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface SubmodelRepository {
	public List<SubmodelService> getAllSubmodels();
	public Optional<SubmodelService> getSubmodelById(String id);
	public List<SubmodelService> getAllSubmodelBySemanticId(String semanticId);
	public List<SubmodelService> getAllSubmodelByIdShort(String idShort);
	
	public SubmodelService addSubmodel(Submodel submodel);
	public SubmodelService updateSubmodelById(Submodel submodel);
	public void removeSubmodelById(String id);
	
}
