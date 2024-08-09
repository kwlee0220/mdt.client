package mdt.task;

import java.io.File;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;

import utils.func.FOption;
import utils.stream.FStream;

import mdt.task.ProcessBasedMDTOperation.Builder;
import picocli.CommandLine.Option;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class LegacyExecutableTask extends AbstractMDTTask {
	private static final Logger s_logger = LoggerFactory.getLogger(LegacyExecutableTask.class);

	@Option(names={"--command"}, paramLabel="string", required=true, description="command line")
	private String m_command;

	@Option(names={"--workingDir"}, paramLabel="dir", required=false, description="current working directory")
	private String m_workingDir;
	
	public LegacyExecutableTask() {
		setLogger(s_logger);
	}

	@Override
	protected void run(Map<String,Port> inputPorts, Map<String,Port> inoutPorts,
						Map<String,Port> outputPorts, Map<String,String> options) throws Exception {
		Builder opBuilder = ProcessBasedMDTOperation.builder()
													.setCommand(m_command);
		if ( m_workingDir != null ) {
			opBuilder.setWorkingDirectory(new File(m_workingDir));
		}
		
		// option 정보를 command line의 option으로 추가시킨다.
		FStream.from(options)
				.forEach(kv -> opBuilder.addOption(kv.key(), kv.value()));
		
		FStream.from(inputPorts.values())
				.forEachOrThrow(port -> {
					// port를 읽어 JSON 형식으로 변환한 후 지정된 file에 저장한다.
					String valueString = FOption.getOrElse("" + port.getRawValue(), "");
					opBuilder.addFileArgument(port.getName(), valueString, false);
				});
		FStream.from(Iterables.concat(inoutPorts.values(), outputPorts.values()))
				.forEachOrThrow(port -> {
					// port를 읽어 JSON 형식으로 변환한 후 지정된 file에 저장한다.
					String valueString = FOption.getOrElse("" + port.getRawValue(), "");
					opBuilder.addFileArgument(port.getName(), valueString, true);
				});
		if ( m_timeout != null ) {
			opBuilder.setTimeout(m_timeout);
		}
		ProcessBasedMDTOperation op = opBuilder.build();
		
		Map<String,String> outputs = op.run();
		FStream.from(inoutPorts)
				.forEach((key, port) -> FOption.accept(outputs.get(key), json -> port.setJson(json)));
		FStream.from(outputPorts)
				.forEach((key, port) -> FOption.accept(outputs.get(key), json -> port.setJson(json)));
	}

	public static final void main(String... args) throws Exception {
		main(new LegacyExecutableTask(), args);
	}
}
