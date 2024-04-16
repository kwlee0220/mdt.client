package mdt.model.registry;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface SubmodelRegistry {
	/**
	 * 등록된 모든 SubmodelDescriptor 들을 반환한다.
	 * 
	 * @return	SubmodelDescriptor 리스트.
	 * @throws RegistryException	Descriptor 획득 과정 중 오류가 발생한 경우.
	 */
	public List<SubmodelDescriptor> getAllSubmodelDescriptors() throws RegistryException;
	
	public List<SubmodelDescriptor> getAllSubmodelDescriptorsByIdShort(String idShort);
	
	/**
	 * 주어진 식별자에 해당하는 {@link SubmodelDescriptor}를 반환한다.
	 * 
	 * @param aasId	SubmodelDescriptor 식별자.
	 * @return	SubmodelDescriptor 객체
	 * @throws ResourceNotFoundException	식별자에 해당하는 등록 SubmodelDescriptor가 없는 경우
	 * @throws RegistryException		Descriptor 획득 과정 중 오류가 발생한 경우.
	 */
	public SubmodelDescriptor getSubmodelDescriptorById(String submodelId)
		throws ResourceNotFoundException, RegistryException;
	
	/**
	 * 주어진 SubmodelDescriptor를 등록시킨다.
	 * 
	 * @param descriptor	SubmodelDescriptor 객체.
	 * @return				등록된 SubmodelDescriptor 객체.
	 * @throws ResourceAlreadyExistsException	동일 식별자에 해당하는 SubmodelDescriptor가
	 * 									이미 존재하는 경우
	 * @throws RegistryException		Descriptor 등록 과정 중 오류가 발생한 경우.
	 */
	public SubmodelDescriptor addSubmodelDescriptor(SubmodelDescriptor submodel)
		throws ResourceAlreadyExistsException, RegistryException;
	
	/**
	 * 기존에 등록된  SubmodelDescriptor를 주어진 것으로 갱신시킨다.
	 * 
	 * @param descriptor	변경시킬 SubmodelDescriptor 객체.
	 * @return	갱신된 SubmodelDescriptor 객체.
	 * @throws ResourceNotFoundException	식별자에 해당하는 등록 SubmodelDescriptor가 없는 경우
	 * @throws RegistryException		Descriptor 갱신 과정 중 오류가 발생한 경우.
	 */
	public SubmodelDescriptor updateSubmodelDescriptorById(SubmodelDescriptor submodel)
		throws ResourceNotFoundException, RegistryException;
	
	/**
	 * 주어진 식별자에 해당하는 등록 SubmodelDescriptor를 해제시킨다.
	 * 
	 * @param aasId		해제시킬 SubmodelDescriptor의 식별자.
	 * @throws ResourceNotFoundException	식별자에 해당하는 SubmodelDescriptor가 존재하지 않는 경우.
	 * @throws RegistryException	Descriptor 삭제 과정 중 오류가 발생한 경우.
	 */
	public void removeSubmodelDescriptorById(String submodelId)
		throws ResourceNotFoundException, RegistryException;
}
