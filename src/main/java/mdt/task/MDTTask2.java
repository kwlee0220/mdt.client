package mdt.task;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import utils.stream.FStream;

import mdt.cli.MDTCommand;
import mdt.client.MDTClientConfig;
import mdt.client.instance.HttpMDTInstanceManagerClient;
import picocli.CommandLine;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Unmatched;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public abstract class MDTTask2 extends MDTCommand {
	@Unmatched()
	private List<String> m_unmatcheds = Lists.newArrayList();
	
	private Map<String,String> m_unmatchedOptions = Maps.newHashMap();
	
	abstract protected void run(HttpMDTInstanceManagerClient manager) throws Exception;
	
	protected Map<String,String> getUnmatchedOptions() {
		return m_unmatchedOptions;
	}

	@Override
	public void run(MDTClientConfig configs) throws Exception {
		HttpMDTInstanceManagerClient manager = (HttpMDTInstanceManagerClient)createMDTInstanceManager(configs);
		
		m_unmatchedOptions = FStream.from(m_unmatcheds)
									.buffer(2, 2)
									.toMap(buf -> trimHeadingDashes(buf.get(0)), buf -> buf.get(1));
				
		run(manager);
	}
	
	private String trimHeadingDashes(String optName) {
		if ( optName.startsWith("--") ) {
			return optName.substring(2);
		}
		else if ( optName.startsWith("-") ) {
			return optName.substring(1);
		}
		else {
			throw new IllegalArgumentException("Invalid option name: " + optName);
		}
	}

	protected static final void main(MDTTask2 task, String... args) throws Exception {
		CommandLine commandLine = new CommandLine(task).setUsageHelpWidth(100);
		commandLine = commandLine.setStopAtUnmatched(true)
								.setUnmatchedArgumentsAllowed(true)
								.setUnmatchedOptionsArePositionalParams(true);
		try {
			commandLine.parse(args);

			if ( commandLine.isUsageHelpRequested() ) {
				commandLine.usage(System.out, Ansi.OFF);
			}
			else {
				task.run();
			}
		}
		catch ( Throwable e ) {
			System.err.println(e);
			commandLine.usage(System.out, Ansi.OFF);
		}
	}
}
