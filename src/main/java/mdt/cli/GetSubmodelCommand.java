package mdt.cli;

import java.util.List;

import org.barfuin.texttree.api.TextTree;
import org.barfuin.texttree.api.TreeOptions;
import org.barfuin.texttree.api.style.TreeStyles;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.SerializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonSerializer;
import org.eclipse.digitaltwin.aas4j.v3.model.Endpoint;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringNameType;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;
import org.nocrala.tools.texttablefmt.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.stream.FStream;

import mdt.cli.tree.SubmodelNode;
import mdt.client.MDTClientConfig;
import mdt.client.SSLUtils;
import mdt.client.registry.RegistryModelConverter;
import mdt.client.resource.HttpSubmodelServiceClient;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.resource.SubmodelService;
import okhttp3.OkHttpClient;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(name = "get", description = "get Submodel information.")
public class GetSubmodelCommand extends MDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(GetSubmodelCommand.class);

	@Parameters(index="0..*", arity="0..1", paramLabel="id", description="Submodel id to show")
	private String m_submodelId = null;
	
	@Option(names={"--instance"}, paramLabel="id", description="MDTInstance id to show")
	private String m_instanceId = null;
	
	@Option(names={"--id_short"}, paramLabel="id", description="Submodel id-short")
	private String m_submodelIdShort = null;
	
	@Option(names={"--output", "-o"}, paramLabel="type", required=false,
			description="output type (candidnates: table or json)")
	private String m_output = "table";
	
	public GetSubmodelCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTClientConfig configs) throws Exception {
		MDTInstanceManager mgr = this.createMDTInstanceManager(configs);
		
		String submodelUrl = null;
		if ( m_submodelId != null ) {
			List<Endpoint> eps = mgr.getSubmodelRegistry()
									.getSubmodelDescriptorById(m_submodelId)
									.getEndpoints();
			submodelUrl = RegistryModelConverter.getEndpointString(eps);
		}
		else if ( m_submodelIdShort != null ) {
			if ( m_instanceId != null ) {
				MDTInstance inst = mgr.getInstance(m_instanceId);
				submodelUrl = mgr.getAssetAdministrationShellRegistry()
									.getAssetAdministrationShellDescriptorById(inst.getAASId())
									.getSubmodelDescriptors()
									.stream()
									.filter(d -> m_submodelIdShort.equals(d.getIdShort()))
									.findAny()
									.map(d -> RegistryModelConverter.getEndpointString(d.getEndpoints()))
									.orElse(null);
			}
			else {
				List<SubmodelDescriptor> smDescList = mgr.getSubmodelRegistry()
															.getAllSubmodelDescriptorsByIdShort(m_submodelIdShort);
				if ( smDescList.size() > 1 ) {
					System.err.println("Multiple Submodels of idShort: " + m_submodelIdShort
										+ ", count=" + smDescList.size());
					System.exit(-1);
				}
				else if ( smDescList.size() == 0 ) {
					System.err.println("There is no Submodels of idShort: " + m_submodelIdShort);
					System.exit(-1);
				}
				else {
					submodelUrl = RegistryModelConverter.getEndpointString(smDescList.get(0).getEndpoints());
				}
			}
		}
		else {
			System.err.println("Submodel id is not specified");
			System.exit(-1);
		}
		
		if ( submodelUrl == null ) {
			return;
		}
		
		OkHttpClient client = SSLUtils.newTrustAllOkHttpClientBuilder().build();
		SubmodelService submodel = new HttpSubmodelServiceClient(client, submodelUrl);
			
		m_output = m_output.toLowerCase();
		if ( m_output == null || m_output.equalsIgnoreCase("table") ) {
			displayAsSimple(submodel);
		}
		else if ( m_output.equalsIgnoreCase("json") ) {
			displayAsJson(submodel);
		}
		else if ( m_output.equalsIgnoreCase("tree") ) {
			displayAsTree(submodel);
		}
		else {
			System.err.println("Unknown output: " + m_output);
			System.exit(-1);
		}
	}

	public static final void main(String... args) throws Exception {
		GetSubmodelCommand cmd = new GetSubmodelCommand();

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
	
	private void displayAsJson(SubmodelService submodelSvc)
		throws SerializationException {
		Submodel submodel = submodelSvc.getSubmodel();
		
		JsonSerializer ser = new JsonSerializer();
		String jsonStr = ser.write(submodel);
		System.out.println(jsonStr);
	}
	
	private void displayAsSimple(SubmodelService submodelSvc) {
		Table table = new Table(2);
		Submodel submodel = submodelSvc.getSubmodel();

		table.addCell(" FIELD "); table.addCell(" VALUE");
		table.addCell(" ID "); table.addCell(" " + submodel.getId());
		table.addCell(" ID_SHORT "); table.addCell(" " + getOrEmpty(submodel.getIdShort()) + " ");

		FStream.from(submodel.getSubmodelElements())
				.map(sme -> sme.getIdShort())
				.zipWithIndex()
				.forEach(tup -> {
					table.addCell(String.format(" SUB_MODEL_ELEMENT[%02d] ", tup._2));
					table.addCell(" " + tup._1 + " ");
				});
		
		List<LangStringNameType> names = submodel.getDisplayName();
		if ( names != null ) {
			String displayName = FStream.from(names)
										.map(name -> name.getText())
										.join(". ");
			table.addCell(" DISPLAY_NAME "); table.addCell(" " + displayName);
		}
		else {
			table.addCell(" DISPLAY_NAME "); table.addCell("");
		}
		
		List<LangStringTextType> descs = submodel.getDescription();
		if ( names != null ) {
			String description = FStream.from(descs)
										.map(desc -> desc.getText())
										.join(". ");
			table.addCell(" DESCRIPTION "); table.addCell(" " + description);
		}
		else {
			table.addCell(" DESCRIPTION "); table.addCell("");
		}
		
		String epStr = RegistryModelConverter.getEndpointString(submodelSvc.getEndpoint()); 
		table.addCell(" ENDPOINT "); table.addCell(" " + epStr);
		
		System.out.println(table.render());
	}
	
	private void displayAsTree(SubmodelService submodelSvc)
		throws SerializationException {
		Submodel submodel = submodelSvc.getSubmodel();

		TreeOptions opts = new TreeOptions();
		opts.setStyle(TreeStyles.UNICODE_ROUNDED);
		opts.setMaxDepth(5);
		SubmodelNode root = new SubmodelNode(submodel);
		System.out.println(TextTree.newInstance(opts).render(root));
	}
	
	private String getOrEmpty(Object obj) {
		return (obj != null) ? obj.toString() : "";
	}
}
