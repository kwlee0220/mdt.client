package mdt.cli;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.ObjectUtils;
import org.nocrala.tools.texttablefmt.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.StopWatch;
import utils.UnitUtils;
import utils.stream.FStream;

import mdt.client.MDTClientConfig;
import mdt.client.instance.HttpMDTInstanceClient;
import mdt.client.instance.HttpMDTInstanceManagerClient;
import mdt.model.IdPair;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Option;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(name = "list", description = "list all MDTInstances.")
public class ListMDTInstanceAllCommand extends MDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(ListMDTInstanceAllCommand.class);
	private static final String CLEAR_CONSOLE_CONTROL = "\033[2J\033[1;1H";
	
	@Option(names={"--filter", "-f"}, description="instance filter.")
	private String m_filter = null;
	
	@Option(names={"--long", "-l"}, description="display details about instances.")
	private boolean m_long = false;
	
	@Option(names={"--table", "-t"}, description="display instances in a table format.")
	private boolean m_tableFormat = false;

	@Option(names={"--repeat", "-r"}, paramLabel="interval",
			description="repeat interval (e.g. \"1s\", \"500ms\"")
	private String m_repeat = null;
	
	public ListMDTInstanceAllCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTClientConfig configs) throws Exception {
		HttpMDTInstanceManagerClient mgr = this.createMDTInstanceManager(configs);
		
		Duration repeatInterval = (m_repeat != null) ? UnitUtils.parseDuration(m_repeat) : null;
		while ( true ) {
			StopWatch watch = StopWatch.start();
			
			try {
				List<HttpMDTInstanceClient> instances;
				if ( m_filter == null ) {
					instances = mgr.getAllInstances();
				}
				else {
					instances = mgr.getAllInstancesByFilter(m_filter);
				}
				
				String outputString = buildOutputString(instances);
				if ( repeatInterval != null ) {
					System.out.print(CLEAR_CONSOLE_CONTROL);
				}
				System.out.println(outputString);
			}
			catch ( Exception e ) {
				if ( repeatInterval != null ) {
					System.out.print(CLEAR_CONSOLE_CONTROL);
				}
				System.out.println("" + e);
			}
			System.out.println("elapsed: " + watch.stopAndGetElpasedTimeString());
			
			if ( repeatInterval == null ) {
				break;
			}
			
			Duration remains = repeatInterval.minus(watch.getElapsed());
			if ( !(remains.isNegative() || remains.isZero()) ) {
				TimeUnit.MILLISECONDS.sleep(remains.toMillis());
			}
		}
	}

	public static final void main(String... args) throws Exception {
		ListMDTInstanceAllCommand cmd = new ListMDTInstanceAllCommand();

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
	
	private String buildOutputString(List<HttpMDTInstanceClient> instances) {
		if ( m_long ) {
			if ( m_tableFormat ) {
				return buildLongTableString(instances);
			}
			else {
				return buildLongListString(instances);
			}
		}
		else {
			if ( m_tableFormat ) {
				return buildTableString(instances);
			}
			else {
				return buildListString(instances);
			}
		}
	}
	
	private String buildListString(List<HttpMDTInstanceClient> instances) {
		try ( ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintStream out = new PrintStream(baos) ) {
			for ( HttpMDTInstanceClient inst : instances ) {
				System.out.println(FStream.of(toShortColumns(inst)).map(Object::toString).join('|'));
			}
			out.close();
			return baos.toString();
		}
		catch ( IOException e ) {
			throw new RuntimeException(e);
		}
	}
	
	private String buildLongListString(List<HttpMDTInstanceClient> instances) {
		try ( ByteArrayOutputStream baos = new ByteArrayOutputStream();
				PrintStream out = new PrintStream(baos) ) {
			int seqNo = 0;
			for ( HttpMDTInstanceClient inst : instances ) {
				System.out.println(FStream.of(toLongColumns(seqNo, inst)).map(Object::toString).join('|'));
				++seqNo;
			}
			out.close();
			return baos.toString();
		}
		catch ( IOException e ) {
			throw new RuntimeException(e);
		}
	}
	
	private String buildTableString(List<HttpMDTInstanceClient> instances) {
		Table table = new Table(3);
		table.setColumnWidth(1, 20, 70);
		
		table.addCell(" INSTANCE ");
		table.addCell(" AAS_ID ");
		table.addCell(" ENDPOINT ");
		
		for ( HttpMDTInstanceClient inst : instances ) {
			FStream.of(toShortColumns(inst))
					.map(Object::toString)
					.forEach(table::addCell);
		}
		return table.render();
	}
	
	private String buildLongTableString(List<HttpMDTInstanceClient> instances) {
		Table table = new Table(6);
		table.setColumnWidth(2, 20, 70);
		table.setColumnWidth(3, 20, 45);

		table.addCell(" # ");
		table.addCell(" INSTANCE ");
		table.addCell(" AAS_IDs ");
		table.addCell(" SUB_MODELS ");
		table.addCell(" STATUS ");
		table.addCell(" ENDPOINT ");
		
		int seqNo = 1;
		for ( HttpMDTInstanceClient inst : instances ) {
			FStream.of(toLongColumns(seqNo, inst))
					.map(Object::toString)
					.forEach(table::addCell);
			++seqNo;
		}
		return table.render();
	}
	
	private Object[] toShortColumns(HttpMDTInstanceClient instance) {
		return new Object[] {
			instance.getId(),
			instance.getAasId(),
			getOrEmpty(instance.getEndpoint())
		};
	}
	
	private Object[] toLongColumns(int seqNo, HttpMDTInstanceClient instance) {
		String submodelIdCsv = FStream.from(instance.getSubmodelIdShorts()).join(",");
		
		String serviceEndpoint = ObjectUtils.defaultIfNull(instance.getEndpoint(), "");
		return new Object[] {
			String.format("%3d", seqNo),
			instance.getId(),
			IdPair.of(instance.getAasId(), instance.getAasIdShort()),
			submodelIdCsv,
			instance.getStatus(), serviceEndpoint
		};
	}
	
	private String getOrEmpty(Object obj) {
		return (obj != null) ? obj.toString() : "";
	}
}
