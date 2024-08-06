package mdt.task;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import utils.stream.FStream;

import mdt.task.MDTTaskModule.Outputs;
import picocli.CommandLine.Option;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class JavaBasedTask extends AbstractMDTTask {
	@Option(names={"--module"}, paramLabel="FQCN", description="Fully qualified class name for MDTTaskModule")
	private String m_taskModuleClassName;

	@Override
	protected void run(Map<String,Port> inputPorts, Map<String,Port> inoutPorts,
						Map<String,Port> outputPorts, Map<String,String> options) throws Exception {
		Class<?> moduleCls = Class.forName(m_taskModuleClassName);
		MDTTaskModule module = (MDTTaskModule)moduleCls.getConstructor().newInstance();
		
		Map<String,Object> inputValues = FStream.from(inputPorts)
												.mapValue(Port::getRawValue)
												.toMap();
		Map<String,Object> inoutValues = FStream.from(inoutPorts)
												.mapValue(Port::getRawValue)
												.toMap();
		List<String> outputPortNames = FStream.from(outputPorts).toKeyStream().toList();
		Outputs outputs = module.run(m_manager, inputValues, inoutValues, outputPortNames, options, m_timeout);
		
		Map<String,Object> merged = Maps.newHashMap(outputs.getInoutPortValues());
		merged.putAll(outputs.getOutputPortValues());
		merged.forEach((k,v) -> {
			Port port = outputPorts.get(k);
			if ( port != null ) {
				port.set(v);
			}
		});
	}

	public static final void main(String... args) throws Exception {
		main(new JavaBasedTask(), args);
	}
}
