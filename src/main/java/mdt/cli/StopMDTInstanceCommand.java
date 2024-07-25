package mdt.cli;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.StateChangePoller;
import utils.UnitUtils;
import utils.func.CheckedSupplier;
import utils.stream.FStream;

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
@Command(name = "stop", description = "Stop a running MDT instance.")
public class StopMDTInstanceCommand extends MDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(StopMDTInstanceCommand.class);
	
	@Parameters(index="0..*", paramLabel="ids", description="MDTInstance id list to stop")
	private List<String> m_instanceIds;
	
	@Option(names={"--all", "-a"}, description="start all stopped MDTInstances")
	private boolean m_stopAll;

	@Option(names={"--nowait"}, paramLabel="duration",
			description="Do not wait until the instance gets to running")
	private boolean m_nowait = false;
	
	@Option(names={"-v"}, description="verbose")
	private boolean m_verbose = false;

	public StopMDTInstanceCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTClientConfig configs) throws Exception {
		HttpMDTInstanceManagerClient mgr = this.createMDTInstanceManager(configs);

		List<HttpMDTInstanceClient> targetInstList;
		if ( m_stopAll ) {
			targetInstList = mgr.getAllInstancesByFilter("instance.status = 'RUNNING'");
		}
		else {
			targetInstList = FStream.from(m_instanceIds)
									.map(mgr::getInstance)
									.toList();
		}
		for ( HttpMDTInstanceClient instance: targetInstList ) {
			try {
				System.out.println("stopping instance: " + instance.getId());
				stopInstance(instance);
			}
			catch ( Exception e ) {
				System.out.printf("failed to stop instance: %s, cause=%s%n", instance.getId(), e);
			}
		}
	}
	
	private void stopInstance(HttpMDTInstanceClient instance) throws TimeoutException, InterruptedException,
																		ExecutionException {
		instance.stopAsync();
		if ( !m_nowait ) {
			// wait하는 경우에는 MDTInstance의 상태를 계속적으로 polling하여
			// 'STOPPING' 상태에서 벗어날 때까지 대기한다.
			CheckedSupplier<Boolean> whilePredicate = new CheckedSupplier<>() {
				@Override
				public Boolean get() {
					MDTInstanceStatus status = instance.reload().getStatus();
					if ( m_verbose ) {
						System.out.print(".");
					}
					return status == MDTInstanceStatus.STOPPING;
				}
			};
			
			StateChangePoller.pollWhile(whilePredicate)
							.interval(UnitUtils.parseDuration("1s"))
							.build()
							.run();
			if ( m_verbose ) {
				System.out.println();
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
