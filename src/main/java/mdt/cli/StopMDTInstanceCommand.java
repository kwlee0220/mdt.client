package mdt.cli;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mdt.client.MDTClientConfig;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.instance.MDTInstanceStatus;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(name = "stop", description = "Stop a running MDT instance.")
public class StopMDTInstanceCommand extends MDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(StopMDTInstanceCommand.class);
	
	@Parameters(index="0..*", paramLabel="ids", description="MDTInstance id list to stop")
	private List<String> m_instanceIds;
	
	@Option(names={"--all", "-a"}, description="start all stopped MDTInstances")
	private boolean m_stopAll;

	public StopMDTInstanceCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTClientConfig configs) throws Exception {
		MDTInstanceManager mgr = this.createMDTInstanceManager(configs);

		List<String> targetIdList;
		if ( m_stopAll ) {
			targetIdList = mgr.getAllInstancesOfStatus(MDTInstanceStatus.RUNNING).stream()
								.map(MDTInstance::getId)
								.collect(Collectors.toList());
		}
		else {
			targetIdList = m_instanceIds;
		}
		for ( String instId: targetIdList ) {
			try {
				MDTInstance instance = mgr.getInstance(instId);
				instance.stop();
				System.out.printf("stopped instance: %s%n", instId);
			}
			catch ( Exception e ) {
				System.out.printf("failed to stop instance: %s, cause=%s%n", instId, e);
			}
		}
	}

	public static final void main(String... args) throws Exception {
		StopMDTInstanceCommand cmd = new StopMDTInstanceCommand();

		CommandLine commandLine = new CommandLine(cmd).setUsageHelpWidth(100);
		try {
			commandLine.parse(args);

			if ( commandLine.isUsageHelpRequested() ) {
				commandLine.usage(System.out, Ansi.OFF);
			}
			else {
				cmd.run();
			}
		}
		catch ( Throwable e ) {
			System.err.println(e);
			commandLine.usage(System.out, Ansi.OFF);
		}
	}
}
