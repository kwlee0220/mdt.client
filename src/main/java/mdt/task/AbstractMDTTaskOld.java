package mdt.task;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.UnitUtils;
import utils.func.Funcs;
import utils.func.KeyValue;
import utils.func.Tuple;
import utils.stream.FStream;

import mdt.client.instance.HttpMDTInstanceManagerClient;
import picocli.CommandLine.Option;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public abstract class AbstractMDTTaskOld extends MDTTask2 {
	private static final Logger s_logger = LoggerFactory.getLogger(AbstractMDTTaskOld.class);
	
	protected HttpMDTInstanceManagerClient m_manager;

	@Option(names={"--value", "-v"}, description="copy value only")
	protected boolean m_valueOnly;

	protected Duration m_timeout = null;
	@Option(names={"--timeout"}, paramLabel="duration", description="Invocation timeout (e.g. \"30s\", \"1m\"")
	public void setTimeout(String toStr) {
		m_timeout = UnitUtils.parseDuration(toStr);
	}
	
	abstract protected void run(Map<String,String> inputValues, Map<String,Port> inputVariables,
								Map<String,Port> outputVariables) throws Exception;
	
	public AbstractMDTTaskOld() {
		setLogger(s_logger);
	}

	@Override
	protected void run(HttpMDTInstanceManagerClient manager) throws Exception {
		m_manager = manager;
		
		List<Port> varDescList = FStream.from(getUnmatchedOptions())
												.filter(kv -> Port.isPortOptionName(kv.key()))
												.map(kv -> Port.from(m_manager, kv.key(), kv.value()))
												.toList();
		
		Tuple<List<Port>,List<Port>> tup
											= Funcs.partition(varDescList, desc -> desc.isInputPort());
		Map<String,Port> inputVars = FStream.from(tup._1).toMap(Port::getName);
		Map<String,Port> outputVars = FStream.from(tup._2).toMap(Port::getName);
		
		Map<String,String> inputValues = FStream.from(getUnmatchedOptions())
												.filter(kv -> !Port.isPortOptionName(kv.key()))
												.toMap(KeyValue::key, KeyValue::value);
		run(inputValues, inputVars, outputVars);
	}
}
