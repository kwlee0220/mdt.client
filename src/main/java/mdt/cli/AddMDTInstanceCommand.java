package mdt.cli;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import mdt.client.MDTClientConfig;
import mdt.client.instance.HttpMDTInstanceManagerClient;
import mdt.model.instance.MDTInstanceManagerException;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(name = "add", description = "Add an MDT instance.")
public class AddMDTInstanceCommand extends MDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(AddMDTInstanceCommand.class);

	@Parameters(index="0", paramLabel="id", description="MDTInstance implementation jar file path")
	private String m_id;
	
	@Option(names={"--jar"}, paramLabel="path",
			description="Path to the MDTInstance implementation jar")
	private File m_jarFile;
	
	@Option(names={"--image"}, paramLabel="id",
			description="Docker image id for the MDTInstance")
	private String m_imageId;
	
	@Option(names={"--aas"}, paramLabel="path", required=true, description="AAS Environment JSON file path")
	private File m_aasFile;
	
	@Option(names={"--conf"}, paramLabel="path", description="MDTInstance configuration path")
	private File m_confFile;

	public AddMDTInstanceCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTClientConfig configs) throws Exception {
		HttpMDTInstanceManagerClient mgr = (HttpMDTInstanceManagerClient)createMDTInstanceManager(configs);
		
		if ( m_jarFile != null ) {
			Preconditions.checkArgument(m_confFile != null, "Fa3st configuration file was missing");
			
			mgr.addJarInstance(m_id, m_jarFile, m_aasFile, m_confFile);
		}
		else if ( m_imageId != null ) {
//			args = DockerExecutionArguments.builder()
//											.imageId(m_imageId)
//											.modelFile(m_aasFile.getAbsolutePath())
//											.build();
			mgr.addDockerInstance(m_id, m_imageId, m_aasFile, m_confFile);
		}
		else {
			throw new MDTInstanceManagerException("invalid MDTInstance addition command");
		}
	}

	public static final void main(String... args) throws Exception {
		runCommand(new AddMDTInstanceCommand(), args);
	}
}
