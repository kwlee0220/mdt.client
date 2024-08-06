package mdt.task;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class CopySubmodelElement extends AbstractMDTTask {
	private static final Logger s_logger = LoggerFactory.getLogger(CopySubmodelElement.class);
	
	public CopySubmodelElement() {
		setLogger(s_logger);
	}

	@Override
	protected void run(Map<String,Port> inputPorts, Map<String,Port> inoutPorts,
						Map<String,Port> outputPorts, Map<String,String> options) throws Exception {
		Port srcPort = inputPorts.get("src");
		Port tarPort = outputPorts.get("tar");
		
		if ( srcPort == null ) {
			throw new IllegalArgumentException("source port is missing");
		}
		if ( tarPort == null ) {
			throw new IllegalArgumentException("target port is missing");
		}
		
		tarPort.set(srcPort.get());
	}

	public static final void main(String... args) throws Exception {
		main(new CopySubmodelElement(), args);
	}
}
