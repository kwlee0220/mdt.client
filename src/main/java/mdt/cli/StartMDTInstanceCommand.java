package mdt.cli;

import java.time.Duration;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.UnitUtils;

import mdt.client.MDTClientConfig;
import mdt.client.instance.StatusChangeMonitor;
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
@Command(name = "start", description = "Start an MDT instance.")
public class StartMDTInstanceCommand extends MDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(StartMDTInstanceCommand.class);
	
	@Parameters(index="0", paramLabel="ids", description="MDTInstance id list to start")
	private String m_instanceId;

	@Option(names={"--sampling", "-s"}, paramLabel="duration",
			description="Status sampling interval (e.g. \"1s\", \"500ms\"")
	private String m_sampleInterval = "3s";

	@Option(names={"--timeout", "-t"}, paramLabel="duration",
			description="Status sampling timeout (e.g. \"30s\", \"1m\"")
	private String m_timeout = "1m";

	@Option(names={"--nowait"}, paramLabel="duration",
			description="Do not wait until the instance gets to running")
	private boolean m_nowait = false;
	
	@Option(names={"-v"}, description="verbose")
	private boolean m_verbose = false;
	
	@Option(names={"-vv"}, description="verbose")
	private boolean m_vverbose = false;

	public StartMDTInstanceCommand() {
		setLogger(s_logger);
	}

	public static final void main(String... args) throws Exception {
		StartMDTInstanceCommand cmd = new StartMDTInstanceCommand();

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
	
	@Override
	public void run(MDTClientConfig configs) throws Exception {
		MDTInstanceManager mgr = this.createMDTInstanceManager(configs);
		
		MDTInstance instance = mgr.getInstance(m_instanceId);
		if ( m_verbose  ) {
			System.out.printf("starting instance: %s ", instance.getId());
		}
		else if (m_vverbose ) {
			System.out.printf("starting instance: %s%n", instance.getId());
		}
		
		instance.start();
		if ( !m_nowait ) {
			Function<MDTInstanceStatus,Boolean> pred = new Function<>() {
				@Override
				public Boolean apply(MDTInstanceStatus status) {
					if ( m_vverbose ) {
						System.out.printf("checking status: instance=%s status=%s%n",
											instance.getId(), status);
					}
					else if ( m_verbose ) {
						System.out.print(".");
					}
					return status != MDTInstanceStatus.STARTING;
				}
			};

			Duration sampleInterval = UnitUtils.parseDuration(m_sampleInterval);
			Duration timeout = UnitUtils.parseDuration(m_timeout);
			StatusChangeMonitor monitor = new StatusChangeMonitor(instance, pred);
			monitor.setPollingInterval(sampleInterval);
			monitor.setTimeout(timeout);
			monitor.run();
		}
		
		if ( m_verbose || m_vverbose ) {
			MDTInstanceStatus status = instance.getStatus();
			String svcEp = instance.getServiceEndpoint();
			
			if ( m_verbose ) {
				System.out.println();
			}
			System.out.printf("instance: id=%s, status=%s, endpoint=%s%n", instance.getId(), status, svcEp);
		}
		
		System.exit(0);
	}
}
