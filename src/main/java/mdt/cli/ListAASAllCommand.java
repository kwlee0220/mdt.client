package mdt.cli;

import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;
import org.nocrala.tools.texttablefmt.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.func.FOption;
import utils.stream.FStream;

import mdt.client.MDTClientConfig;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.repository.AssetAdministrationShellRepository;
import mdt.model.resource.AssetAdministrationShellService;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Option;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(name = "list", description = "list all AssetAdministrationShells.")
public class ListAASAllCommand extends MDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(ListAASAllCommand.class);
	
	@Option(names={"-l"}, description="display details about AssetAdministrationShells.")
	private boolean m_long = false;
	
	@Option(names={"--table", "-t"}, description="display AssetAdministrationShells in a table format.")
	private boolean m_tableFormat = false;
	
	public ListAASAllCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTClientConfig configs) throws Exception {
		MDTInstanceManager mgr = this.createMDTInstanceManager(configs);
		if ( m_long ) {
			if ( m_tableFormat ) {
				displayLongAsTable(mgr);
			}
			else {
				displayLongNoTable(mgr);
			}
		}
		else {
			if ( m_tableFormat ) {
				displayShortTable(mgr);
			}
			else {
				displayShortNoTable(mgr);
			}
		}
	}

	public static final void main(String... args) throws Exception {
		ListAASAllCommand cmd = new ListAASAllCommand();

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
	
	private void displayShortNoTable(MDTInstanceManager instanceMgr) {
		for ( MDTInstance inst : instanceMgr.getInstanceAll() ) {
			AssetAdministrationShellRepository repo = inst.getAssetAdministrationShellRepository();
			for ( AssetAdministrationShellService aasSvc: repo.getAllAssetAdministrationShells() ) {
				AssetAdministrationShell aas = aasSvc.getAssetAdministrationShell();
				System.out.println(FStream.of(toShortColumns(aas, inst)).join('|'));
			}
		}
	}
	
	private void displayShortTable(MDTInstanceManager instanceMgr) {
		Table table = new Table(4);
		table.setColumnWidth(1, 20, 70);
		
		table.addCell(" ID ");
		table.addCell(" ID_SHORT ");
		table.addCell(" INSTANCE ");
		table.addCell(" SUBMODELS ");

		for ( MDTInstance inst : instanceMgr.getInstanceAll() ) {
			AssetAdministrationShellRepository repo = inst.getAssetAdministrationShellRepository();
			for ( AssetAdministrationShellService aasSvc: repo.getAllAssetAdministrationShells() ) {
				AssetAdministrationShell aas = aasSvc.getAssetAdministrationShell();
				FStream.of(toShortColumns(aas, inst)).forEach(table::addCell);
			}
		}
		System.out.println(table.render());
	}
	
	private void displayLongNoTable(MDTInstanceManager instanceMgr) {
		for ( MDTInstance inst : instanceMgr.getInstanceAll() ) {
			AssetAdministrationShellRepository repo = inst.getAssetAdministrationShellRepository();
			for ( AssetAdministrationShellService aasSvc: repo.getAllAssetAdministrationShells() ) {
				AssetAdministrationShell aas = aasSvc.getAssetAdministrationShell();
				System.out.println(FStream.of(toLongColumns(aas, inst)).join('|'));
			}
		}
	}
	
	private void displayLongAsTable(MDTInstanceManager instanceMgr) {
		Table table = new Table(6);
		table.setColumnWidth(0, 20, 70);
		table.setColumnWidth(4, 10, 50);
		table.setColumnWidth(5, 10, 50);
		
		table.addCell(" ID ");
		table.addCell(" ID_SHORT ");
		table.addCell(" GLOBAL_ASSET_ID ");
		table.addCell(" INSTANCE ");
		table.addCell(" DISPLAY_NAMES ");
		table.addCell(" SUBMODELS ");
		
		for ( MDTInstance inst : instanceMgr.getInstanceAll() ) {
			AssetAdministrationShellRepository repo = inst.getAssetAdministrationShellRepository();
			for ( AssetAdministrationShellService aasSvc: repo.getAllAssetAdministrationShells() ) {
				AssetAdministrationShell aas = aasSvc.getAssetAdministrationShell();
				FStream.of(toLongColumns(aas, inst)).forEach(table::addCell);
			}
		}
		System.out.println(table.render());
	}
	
	private String[] toShortColumns(AssetAdministrationShell aas, MDTInstance inst) {
		String smIdCsv = FStream.from(inst.getAllSubmodelDescriptors())
								.map(SubmodelDescriptor::getIdShort)
								.join(", ");
		
		return new String[] {
			aas.getId(),					// ID
			aas.getIdShort(),				// ID_SHORT
			inst.getId(),					// INSTANCE
			smIdCsv,						// SUBMODELS
		};
	}
	
	private String[] toLongColumns(AssetAdministrationShell aas, MDTInstance inst) {
		String displayNames = FOption.ofNullable(aas.getDisplayName())
									.flatMapFStream(names -> FStream.from(names))
									.join(", ");
		
		String smIdCsv = FStream.from(inst.getAllSubmodelDescriptors())
								.map(SubmodelDescriptor::getIdShort)
								.join(", ");
		return new String[] {
			aas.getId(),										// ID
			aas.getIdShort(),									// ID_SHORT
			aas.getAssetInformation().getGlobalAssetId(),		// GLOBAL_ASSET_ID
			inst.getId(),										// INSTANCE
			displayNames,										// DISPLAY_NAME
			smIdCsv,											// SUBMODELS
		};
	}
	
	private String toNullabelString(Object str) {
		return (str != null) ? str.toString() : "";
	}
}
