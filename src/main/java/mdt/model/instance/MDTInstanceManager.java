package mdt.model.instance;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import mdt.model.registry.AssetAdministrationShellRegistry;
import mdt.model.registry.ResourceNotFoundException;
import mdt.model.registry.SubmodelRegistry;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface MDTInstanceManager {
	public AssetAdministrationShellRegistry getAssetAdministrationShellRegistry();
	public SubmodelRegistry getSubmodelRegistry();
	
	/**
	 * 주어진 식별자에 해당하는 {@link MDTInstance} 객체를 반환한다.
	 * 
	 * @param id	검색 대상 식별자.
	 * @return		식별자에 해당하는 {@link MDTInstance} 객체.
	 * 				만일 식별자에 해당하는 MDT instance가 없는 경우는
	 * 				{@link ResourceNotFoundException} 예외가 발생된다.
	 * @throws ResourceNotFoundException
	 */
	public MDTInstance getInstance(String id) throws ResourceNotFoundException;
	
	public MDTInstance getInstanceByAasId(String aasId) throws ResourceNotFoundException;

	/**
	 * 주어진 식별자에 해당하는 {@link MDTInstance} 객체를 반환한다.
	 * 만일 식별자에 해당하는 MDT instance가 없는 경우에는 {@code null}을 반환한다.
	 * 
	 * @param id	검색 대상 식별자.
	 * @return		식별자에 해당하는 {@link MDTInstance} 객체.
	 * 				만일 식별자에 해당하는 MDT instance가 없는 경우는 {@code null}.
	 * @throws ResourceNotFoundException
	 */
	public default @Nullable MDTInstance getInstanceOrNull(String id) throws MDTInstanceManagerException {
		try {
			return getInstance(id);
		}
		catch ( ResourceNotFoundException expected ) {
			return null;
		}
	}
	
	/**
	 * MDT 시스템에 등록된 모든 {@link MDTInstance}를 반환한다.
	 * 
	 * @return	등록된 {@link MDTInstance}들의 리스트.
	 * @throws MDTInstanceManagerException
	 */
	public List<MDTInstance> getInstanceAll() throws MDTInstanceManagerException;
	
	public List<MDTInstance> getInstanceAllByIdShort(String aasIdShort) throws MDTInstanceManagerException;
	
	public default List<MDTInstance> getAllInstancesOfStatus(MDTInstanceStatus status)
		throws MDTInstanceManagerException {
		return getInstanceAll().stream()
								.filter(inst -> inst.getStatus() == status)
								.collect(Collectors.toList());
	}

	/**
	 * Docker에 저장된 image를 MDT instace로 등록시킨다.
	 * 
	 * @param id			저장할 MDTInstance의 식별자.
	 * @param aasFile		등록할 asset의 AAS Environment 정보 파일 경로.
	 * @param arguments 	Instance 구동에 필요한 argument 정보.
	 * 						Json으로 serializable해야 함.
	 * @param timeout		등록 소요시간 timeout.
	 * 						등록 소요시간이 주어진 시간보다 길어지는 경우에는
	 * 						{@link TimeoutException} 예외가 발생한다.
	 * 						timeout 값이 <code>null</code>인 경우는 무한 대기를 의미한다.
	 * @return 등록된 MDT Instace 객체.
	 * @throws MDTInstanceManagerException	기타 다른 이유로 MDTInstance 등록에 실패한 경우.
	 */
	public MDTInstance addInstance(String id, File aasFile, Object arguments)
		throws MDTInstanceManagerException;
	
	/**
	 * 등록된 MDT Instance을 해제시킨다.
	 * 
	 * @param id	해제시킬 MDT Instance의 식별자.
	 * @throws MDTInstanceManagerException	등록 해제가 실패한 경우.
	 */
	public void removeInstance(String id) throws MDTInstanceManagerException;

	/**
	 * MDT 시스템에 등록된 모든 {@link MDTInstance}를 등록 해제시킨다.
	 * 
	 * @throws MDTInstanceManagerException	등록 해제가 실패한 경우.
	 */
	public void removeInstanceAll() throws MDTInstanceManagerException;
}
