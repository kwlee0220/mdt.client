package mdt.cli;

import java.util.List;
import java.util.Map;

import org.eclipse.digitaltwin.aas4j.v3.model.Endpoint;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.nocrala.tools.texttablefmt.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.func.FOption;
import utils.stream.FStream;

import mdt.client.MDTClientConfig;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.repository.SubmodelRepository;
import mdt.model.resource.SubmodelService;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Option;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(name = "list", description = "list all Submodels.")
public class ListSubmodelAllCommand extends MDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(ListSubmodelAllCommand.class);
	
	@Option(names={"-l"}, description="display details about Submodels.")
	private boolean m_long = false;
	
	@Option(names={"--table", "-t"}, description="display instances in a table format.")
	private boolean m_tableFormat = false;
	
	public ListSubmodelAllCommand() {
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
		ListSubmodelAllCommand cmd = new ListSubmodelAllCommand();

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
			for ( SubmodelDescriptor smDesc: inst.getAllSubmodelDescriptors() ) {
				System.out.println(FStream.of(toShortColumns(smDesc)).join('|'));
			}
		}
	}
	
	private void displayShortTable(MDTInstanceManager instanceMgr) {
		Table table = new Table(3);
		table.setColumnWidth(1, 20, 70);
		
		table.addCell(" ID ");
		table.addCell(" ID_SHORT ");
		table.addCell(" ENDPOINT ");
		for ( MDTInstance inst : instanceMgr.getInstanceAll() ) {
			for ( SubmodelDescriptor smDesc: inst.getAllSubmodelDescriptors() ) {
				FStream.of(toShortColumns(smDesc))
						.forEach(table::addCell);
			}
		}
		System.out.println(table.render());
	}
	
	private void displayLongNoTable(MDTInstanceManager instanceMgr) {
		Map<String, SubmodelDescriptor> descMap
									= FStream.from(instanceMgr.getInstanceAll())
											.flatMapIterable(MDTInstance::getAllSubmodelDescriptors)
											.toMap(desc -> getEndpointHref(desc.getEndpoints()));
	
		for ( MDTInstance inst : instanceMgr.getInstanceAll() ) {
			SubmodelRepository submodelRepo = inst.getSubmodelRepository();
			for ( SubmodelService submodelSvc: submodelRepo.getAllSubmodels() ) {
				Submodel submodel = submodelSvc.getSubmodel();
				System.out.println(FStream.of(toLongColumns(submodel, descMap)).join('|'));
			}
		}
	}
	
	private void displayLongAsTable(MDTInstanceManager instanceMgr) {
		Table table = new Table(5);
		table.setColumnWidth(1, 20, 70);
		table.setColumnWidth(3, 10, 50);
		
		table.addCell(" ID ");
		table.addCell(" ID_SHORT ");
		table.addCell(" DISPLAY_NAMES ");
		table.addCell(" ELEMENTS ");
		table.addCell(" ENDPOINT ");
		
		Map<String, SubmodelDescriptor> descMap
									= FStream.from(instanceMgr.getInstanceAll())
											.flatMapIterable(MDTInstance::getAllSubmodelDescriptors)
											.toMap(desc -> getEndpointHref(desc.getEndpoints()));
		
		for ( MDTInstance inst : instanceMgr.getInstanceAll() ) {
			SubmodelRepository submodelRepo = inst.getSubmodelRepository();
			for ( SubmodelService submodelSvc: submodelRepo.getAllSubmodels() ) {
				Submodel submodel = submodelSvc.getSubmodel();
				FStream.of(toLongColumns(submodel, descMap)).forEach(table::addCell);
			}
		}
		System.out.println(table.render());
	}
	
	private String[] toShortColumns(SubmodelDescriptor smDesc) {
		return new String[] {
			smDesc.getId(),
			smDesc.getIdShort(),
			toNullabelString(getEndpointHref(smDesc.getEndpoints()))
		};
	}
	
	private String[] toLongColumns(Submodel submodel, Map<String, SubmodelDescriptor> descMap) {
		String displayNames = FOption.ofNullable(submodel.getDisplayName())
									.flatMapFStream(names -> FStream.from(names))
									.join(", ");
		String elmIds = FStream.from(submodel.getSubmodelElements())
								.map(SubmodelElement::getIdShort)
								.join(", ");
		String href = "";
		SubmodelDescriptor desc = descMap.get(submodel.getId());
		if ( desc != null ) {
			href = toNullabelString(getEndpointHref(desc.getEndpoints()));
		}
		
		return new String[] {
			submodel.getId(),
			submodel.getIdShort(),
			displayNames,
			elmIds,
			href
		};
	}
	
	private String getEndpointHref(List<Endpoint> epList) {
		return epList.get(0).getProtocolInformation().getHref();
	}
	
	private String toNullabelString(Object str) {
		return (str != null) ? str.toString() : "";
	}
}
