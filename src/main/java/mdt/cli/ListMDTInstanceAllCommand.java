package mdt.cli;

import java.util.List;

import org.nocrala.tools.texttablefmt.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.stream.FStream;

import mdt.client.MDTClientConfig;
import mdt.model.IdPair;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceManager;
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
	
	@Option(names={"-l"}, description="display details about instances.")
	private boolean m_long = false;
	
	@Option(names={"--table", "-t"}, description="display instances in a table format.")
	private boolean m_tableFormat = false;
	
	public ListMDTInstanceAllCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTClientConfig configs) throws Exception {
		MDTInstanceManager mgr = this.createMDTInstanceManager(configs);
		List<MDTInstance> instances = FStream.from(mgr.getInstanceAll()).toList();
		
		if ( m_long ) {
			if ( m_tableFormat ) {
				displayLongAsTable(instances);
			}
			else {
				displayLongWithoutTableFormat(instances);
			}
		}
		else {
			if ( m_tableFormat ) {
				displayAsTable(instances);
			}
			else {
				displayWithoutTableFormat(instances);
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
	
	private void displayWithoutTableFormat(List<MDTInstance> instances) {
		for ( MDTInstance inst : instances ) {
			System.out.println(FStream.of(toShortColumns(inst)).map(Object::toString).join('|'));
		}
	}
	
	private void displayLongWithoutTableFormat(List<MDTInstance> instances) {
		for ( MDTInstance inst : instances ) {
			System.out.println(FStream.of(toLongColumns(inst)).map(Object::toString).join('|'));
		}
	}
	
	private void displayAsTable(List<MDTInstance> instances) {
		Table table = new Table(3);
		table.setColumnWidth(1, 20, 70);
		
		table.addCell(" INSTANCE ");
		table.addCell(" AAS_ID ");
		table.addCell(" ENDPOINT ");
		
		for ( MDTInstance inst : instances ) {
			FStream.of(toShortColumns(inst))
					.map(Object::toString)
					.forEach(table::addCell);
		}
		System.out.println(table.render());
	}
	
	private void displayLongAsTable(List<MDTInstance> instances) {
		Table table = new Table(5);
		table.setColumnWidth(1, 20, 70);
		table.setColumnWidth(2, 20, 45);
		
		table.addCell(" INSTANCE ");
		table.addCell(" AAS_IDs ");
		table.addCell(" SUB_MODELS ");
		table.addCell(" STATUS ");
		table.addCell(" ENDPOINT ");
		
		for ( MDTInstance inst : instances ) {
			FStream.of(toLongColumns(inst))
					.map(Object::toString)
					.forEach(table::addCell);
		}
		System.out.println(table.render());
	}
	
	private Object[] toShortColumns(MDTInstance instance) {
		return new Object[] {
			instance.getId(),
			instance.getAASId(),
			getOrEmpty(instance.getServiceEndpoint())
		};
	}
	
	private Object[] toLongColumns(MDTInstance instance) {
		String submodelIdCsv = FStream.from(instance.getAllSubmodelDescriptors())
										.map(smd -> smd.getIdShort())
										.join(",");
		return new Object[] {
			instance.getId(),
			IdPair.of(instance.getAASId(), instance.getAASIdShort()),
			submodelIdCsv,
			instance.getStatus(),
			getOrEmpty(instance.getServiceEndpoint())
		};
	}
	
	private String getOrEmpty(Object obj) {
		return (obj != null) ? obj.toString() : "";
	}
}
