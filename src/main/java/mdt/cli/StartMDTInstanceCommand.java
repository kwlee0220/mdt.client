package mdt.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.StateChangePoller;
import utils.UnitUtils;
import utils.func.CheckedSupplier;

import mdt.client.MDTClientConfig;
import mdt.client.instance.HttpMDTInstanceClient;
import mdt.client.instance.HttpMDTInstanceManagerClient;
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
	
	@Parameters(index="0", paramLabel="id", description="MDTInstance id to start")
	private String m_instanceId;

	@Option(names={"--poll"}, paramLabel="duration",
			description="Status polling interval (e.g. \"1s\", \"500ms\"")
	private String m_pollingInterval = "1s";

	@Option(names={"--timeout"}, paramLabel="duration",
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
		HttpMDTInstanceManagerClient mgr = this.createMDTInstanceManager(configs);
		
		HttpMDTInstanceClient instance = mgr.getInstance(m_instanceId);
		if ( m_verbose  ) {
			System.out.printf("starting instance: %s ", instance.getId());
		}
		else if (m_vverbose ) {
			System.out.printf("starting instance: %s%n", instance.getId());
		}
		
		instance.startAsync();
		if ( !m_nowait ) {
			// wait하는 경우에는 MDTInstance의 상태를 계속적으로 polling하여
			// 'STARTING' 상태에서 벗어날 때까지 대기한다.
			CheckedSupplier<Boolean> whilePredicate = new CheckedSupplier<>() {
				@Override
				public Boolean get() {
					MDTInstanceStatus status = instance.reload().getStatus();
					if ( m_vverbose ) {
						System.out.printf("checking status: instance=%s status=%s%n",
											instance.getId(), status);
					}
					else if ( m_verbose ) {
						System.out.print(".");
					}
					return status == MDTInstanceStatus.STARTING;
				}
			};
			
			StateChangePoller.pollWhile(whilePredicate)
							.interval(UnitUtils.parseDuration(m_pollingInterval))
							.timeout(UnitUtils.parseDuration(m_timeout))
							.build()
							.run();
		}
		
		if ( m_verbose || m_vverbose ) {
			MDTInstanceStatus status = instance.getStatus();
			String svcEp = instance.getEndpoint();
			
			if ( m_verbose ) {
				System.out.println();
			}
			System.out.printf("instance: id=%s, status=%s, endpoint=%s%n", instance.getId(), status, svcEp);
		}
		
		System.exit(0);
	}
}
