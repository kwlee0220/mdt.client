package mdt.model.instance;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeoutException;

import utils.InternalException;

import mdt.model.registry.ResourceNotFoundException;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface MDTInstanceManager<T extends MDTInstance> {
	public static final String CANONICAL_FA3ST_JAR_FILE = "faaast.starter-all.jar";
	public static final String CANONICAL_MODEL_FILE = "model.json";
	public static final String CANONICAL_CONF_FILE = "conf.json";
    
	/**
	 * 주어진 식별자에 해당하는 {@link MDTInstance} 객체를 반환한다.
	 * 
	 * @param id	검색 대상 식별자.
	 * @return		식별자에 해당하는 {@link MDTInstance} 객체.
	 * 				만일 식별자에 해당하는 MDT instance가 없는 경우는
	 * 				{@link ResourceNotFoundException} 예외가 발생된다.
	 * @throws MDTInstanceManagerException
	 */
	public T getInstance(String id) throws MDTInstanceManagerException;
	
	public default T getInstanceByAasId(String aasId) throws ResourceNotFoundException,
																		MDTInstanceManagerException {
		String filter = String.format("instance.aasId = '%s'", aasId);
		List<T> instList = getAllInstancesByFilter(filter);
		if ( instList.size() == 1 ) {
			return instList.get(0);
		}
		else if ( instList.size() == 0 ) {
			throw new ResourceNotFoundException("MDTInstance", "aasId=" + aasId);
		}
		else {
			throw new InternalException("multiple MDTInstances for aasId: " + aasId);
		}
	}
	
	public default List<T> getAllInstancesByAasIdShort(String idShort) throws MDTInstanceManagerException {
		String filter = String.format("instance.aasIdShort = '%s'", idShort);
		return getAllInstancesByFilter(filter);
	}
	
	public default List<T> getAllInstancesByAssetId(String assetId) throws MDTInstanceManagerException {
		String filter = String.format("instance.globalAssetId = '%s'", assetId);
		return getAllInstancesByFilter(filter);
	}
	
	public default T getAllInstancesBySubmodelId(String submodelId) throws ResourceNotFoundException,
																		MDTInstanceManagerException {
		String filter = String.format("submodel.id = '%s'", submodelId);
		List<T> instList = getAllInstancesByFilter(filter);
		if ( instList.size() == 1 ) {
			return instList.get(0);
		}
		else if ( instList.size() == 0 ) {
			throw new ResourceNotFoundException("Submodel", "id=" + submodelId);
		}
		else {
			throw new InternalException("multiple MDTInstances for submodel-id: " + submodelId);
		}
	}
	
	public default List<T> getAllInstancesBySubmodelIdShort(String idShort) throws MDTInstanceManagerException {
		String filter = String.format("submodel.idShort = '%s'", idShort);
		return getAllInstancesByFilter(filter);
	}
	
	/**
	 * MDT 시스템에 등록된 모든 {@link MDTInstance}를 반환한다.
	 * 
	 * @return	등록된 {@link MDTInstance}들의 리스트.
	 * @throws MDTInstanceManagerException
	 */
	public List<T> getAllInstances() throws MDTInstanceManagerException;

	public List<T> getAllInstancesByFilter(String filterExpr) throws MDTInstanceManagerException;

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
	public T addInstance(String id, File aasFile, String arguments)
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
