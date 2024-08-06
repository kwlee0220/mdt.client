package mdt.task;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import utils.func.Funcs;
import utils.io.IOUtils;
import utils.stream.FStream;

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
		List<String> commandLine = Lists.newArrayList(m_command);
		
		// option 정보를 command line의 option으로 추가시킨다.
		FStream.from(options)
				.forEach(kv -> {
					commandLine.add("--" + kv.key());
					commandLine.add(kv.value());
				});
		
		FStream.from(Iterables.concat(inputPorts.values(), inoutPorts.values(), outputPorts.values()))
				.forEachOrThrow(port -> {
					// port를 읽어 JSON 형식으로 변환한 후 지정된 file에 저장한다.
					File portFile = downloadToFile(port);
					commandLine.add(portFile.getPath());
				});

		ProcessBuilder builder = new ProcessBuilder(commandLine);
		if ( m_workingDir != null ) {
			builder.directory(new File(m_workingDir));
		}
		
		Process process = builder.start();
		if ( m_timeout != null ) {
			if ( process.waitFor(m_timeout.toMillis(), TimeUnit.MILLISECONDS) ) {
				uploadFromOutputFiles(inoutPorts, outputPorts);
				cleanArgFiles(inputPorts, inoutPorts, outputPorts);
			}
			else {
				process.destroyForcibly();
				throw new TimeoutException(m_timeout.toString());
			}
		}
		else {
			int retCode = process.waitFor();
			if ( retCode == 0 ) {
				uploadFromOutputFiles(inoutPorts, outputPorts);
				cleanArgFiles(inputPorts, inoutPorts, outputPorts);
			}
			else {
				throw new Exception("Process failed");
			}
		}
	}
	
	private File toFile(Port port) {
		String name = port.getName();
		return (m_workingDir != null) ? new File(m_workingDir, name) : new File(name);
	}
	
	private File downloadToFile(Port port) throws IOException {
		File file = toFile(port);
		file.getParentFile().mkdirs();
		
		String valueString = Funcs.toNonNull("" + port.getRawValue(), "");
		Files.writeString(file.toPath(), valueString, StandardCharsets.UTF_8);
		
		return file;
	}
	
	private void uploadFile(Port port) throws IOException {
		File file = toFile(port);
		try {
			if ( port.isOutputPort() && !file.canRead() ) {
				return;
			}
			
			String jsonStr = IOUtils.toString(file);
			port.set(jsonStr);
		}
		catch ( IOException e ) {
			throw new IOException("Failed to read Port file: " + file + ", cause=" + e.getMessage());
		}
	}
	
	private void uploadFromOutputFiles(Map<String,Port> inoutPorts,
										Map<String,Port> outputPorts) throws IOException {
		FStream.from(inoutPorts.values()).forEachOrThrow(this::uploadFile);
		FStream.from(outputPorts.values()).forEachOrThrow(this::uploadFile);
	}
	
	private void cleanArgFiles(Map<String,Port> inputPorts, Map<String,Port> inoutPorts,
								Map<String,Port> outputPorts) {
		FStream.from(inputPorts.values()).mapOrIgnore(this::toFile).forEach(File::delete);
		FStream.from(inoutPorts.values()).mapOrIgnore(this::toFile).forEach(File::delete);
		FStream.from(outputPorts.values()).mapOrIgnore(this::toFile).forEach(File::delete);
	}

	public static final void main(String... args) throws Exception {
		main(new LegacyExecutableTask(), args);
	}
}
