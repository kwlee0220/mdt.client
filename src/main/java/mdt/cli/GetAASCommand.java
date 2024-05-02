package mdt.cli;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.SerializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonSerializer;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.Key;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringNameType;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.nocrala.tools.texttablefmt.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.stream.FStream;

import mdt.client.MDTClientConfig;
import mdt.client.registry.RegistryModelConverter;
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
@Command(name = "get", description = "get AssetAdministrationShell information.")
public class GetAASCommand extends MDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(GetAASCommand.class);

	@Parameters(index="0..*", arity="0..1", paramLabel="id", description="AssetAdministrationShell id to show")
	private String m_aasId = null;
	
	@Option(names={"--instance"}, paramLabel="id", description="MDTInstance id to show")
	private String m_instanceId = null;
	
	@Option(names={"--id_short"}, paramLabel="id", description="Submodel id-short")
	private String m_aasIdShort = null;
	
	@Option(names={"--output", "-o"}, paramLabel="type", required=false,
			description="output type (candidnates: table or json)")
	private String m_output = "table";
	
	public GetAASCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTClientConfig configs) throws Exception {
		MDTInstanceManager mgr = this.createMDTInstanceManager(configs);
		
		MDTInstance instance = null;
		if ( m_aasId != null ) {
			instance = mgr.getInstanceByAasId(m_aasId);
		}
		else if ( m_instanceId != null ) {
			instance = mgr.getInstance(m_instanceId);
		}
		else if ( m_aasIdShort != null ) {
			List<MDTInstance> instList = mgr.getInstanceAllByIdShort(m_aasIdShort)
											.stream()
											.filter(inst -> inst.getStatus() == MDTInstanceStatus.RUNNING)
											.collect(Collectors.toList());
			if ( instList.size() > 1 ) {
				System.err.println("Multiple AssetAdministrationShells of idShort: " + m_aasIdShort
									+ ", count=" + instList.size());
				System.exit(-1);
			}
			else if ( instList.size() == 0 ) {
				System.err.println("There is no AssetAdministration of idShort: " + m_aasIdShort);
				System.exit(-1);
			}
			else {
				instance = instList.get(0);
			}
		}
		else {
			System.out.printf("Target AssetAdministrationShell was not specified");
			m_spec.commandLine().usage(System.out, Ansi.OFF);
			return;
		}
		
		AssetAdministrationShell aas = instance.getAssetAdministrationShellService()
												.getAssetAdministrationShell();
			
		m_output = m_output.toLowerCase();
		if ( m_output == null || m_output.equalsIgnoreCase("table") ) {
			displayAsSimple(aas, instance);
		}
		else if ( m_output.equalsIgnoreCase("json") ) {
			displayAsJson(aas);
		}
		else {
			System.err.println("Unknown output: " + m_output);
			System.exit(-1);
		}
	}

	public static final void main(String... args) throws Exception {
		GetAASCommand cmd = new GetAASCommand();

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
	
	private void displayAsJson(AssetAdministrationShell aas) throws SerializationException {
		JsonSerializer ser = new JsonSerializer();
		System.out.println(ser.write(aas));
	}
	
	private void displayAsSimple(AssetAdministrationShell aas, MDTInstance instance) {
		Table table = new Table(2);

		table.addCell(" FIELD "); table.addCell(" VALUE");
		table.addCell(" ID "); table.addCell(" " + aas.getId());
		table.addCell(" ID_SHORT "); table.addCell(" " + getOrEmpty(aas.getIdShort()) + " ");
		table.addCell(" GLOBAL_ASSET_ID ");
			table.addCell(" " + getOrEmpty(aas.getAssetInformation().getGlobalAssetId()) + " ");
		table.addCell(" INSTANCE "); table.addCell(" " + instance.getId());
		
		List<LangStringNameType> names = aas.getDisplayName();
		if ( names != null ) {
			String displayName = FStream.from(names)
										.map(name -> name.getText())
										.join(". ");
			table.addCell(" DISPLAY_NAME "); table.addCell(" " + displayName);
		}
		else {
			table.addCell(" DISPLAY_NAME "); table.addCell("");
		}
		
		FStream.from(aas.getSubmodels())
				.flatMapIterable(Reference::getKeys)
				.map(Key::getValue)
				.zipWithIndex()
				.forEach(tup -> {
					table.addCell(String.format(" SUB_MODEL_REF_[%02d] ", tup._2));
					table.addCell(" " + tup._1 + " ");
				});
		
		String url = RegistryModelConverter.getEndpointString(instance.getAssetAdministrationShellDescriptor().getEndpoints());
		table.addCell(" ENDPOINT "); table.addCell(" " + getOrEmpty(url));
		
		System.out.println(table.render());
	}
	
	private String getOrEmpty(Object obj) {
		return (obj != null) ? obj.toString() : "";
	}
}
