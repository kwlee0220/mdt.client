package mdt.cli;

import java.nio.file.Path;
import java.nio.file.Paths;

import utils.func.FOption;

import mdt.client.MDTClientConfig;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.instance.MDTInstanceManagerException;
import picocli.CommandLine.Option;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public abstract class MDTCommand extends HomeDirPicocliCommand {
	private static final String ENVVAR_HOME = "MDT_HOME";
	
	@Option(names={"--conf"}, paramLabel="path", description={"MDT management configuration file path"})
	protected String m_configPath = "mdt_client_config.yaml";
	
	abstract protected void run(MDTClientConfig configs) throws Exception;
	
	public MDTCommand() {
		super(FOption.of(ENVVAR_HOME));
	}
	
	protected MDTInstanceManager createMDTInstanceManager(MDTClientConfig config) {
		try {
			return config.createMDTInstanceManager();
		}
		catch ( Exception e ) {
			throw new MDTInstanceManagerException("" + e);
		}
	}
	
	@Override
	protected final void run(Path homeDir) throws Exception {
		Path configPath = Paths.get(m_configPath);
		if ( !configPath.isAbsolute() ) {
			configPath = homeDir.resolve(configPath);
		}
		
		run(MDTClientConfig.load(configPath));
	}
}
