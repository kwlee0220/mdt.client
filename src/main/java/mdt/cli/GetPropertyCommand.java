package mdt.cli;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.barfuin.texttree.api.Node;
import org.barfuin.texttree.api.TextTree;
import org.barfuin.texttree.api.TreeOptions;
import org.barfuin.texttree.api.style.TreeStyles;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import utils.StopWatch;
import utils.UnitUtils;
import utils.stream.FStream;

import mdt.cli.tree.SubmodelElementNodeFactory;
import mdt.client.MDTClientConfig;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.service.SubmodelService;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(name = "get", description = "get Property information.")
public class GetPropertyCommand extends MDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(GetPropertyCommand.class);

	@Parameters(index="0", paramLabel="id", description="Submodel id")
	private String m_submodelId = null;

	@Parameters(index="1..*", paramLabel="path", description="Property path to show")
	private List<String> m_idPathList = Lists.newArrayList();

	@Option(names={"--repeat", "-r"}, paramLabel="interval",
			description="repeat interval (e.g. \"1s\", \"500ms\"")
	private String m_repeat = null;
	
	public GetPropertyCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTClientConfig configs) throws Exception {
		MDTInstanceManager mgr = this.createMDTInstanceManager(configs);
		
		TreeOptions opts = new TreeOptions();
		opts.setStyle(TreeStyles.UNICODE_ROUNDED);
		opts.setMaxDepth(5);
		
		Duration repeatInterval = (m_repeat != null) ? UnitUtils.parseDuration(m_repeat) : null;
		while ( true ) {
			StopWatch watch = StopWatch.start();
			
			try {
				SubmodelService svc = mgr.getSubmodelService(m_submodelId);
				List<SubmodelElement> smeList = FStream.from(m_idPathList)
														.mapOrIgnore(svc::getSubmodelElementByPath)
														.toList();
				SubmodelElementListNode root = new SubmodelElementListNode(smeList);
				System.out.print("\033[2J\033[1;1H");
				System.out.print(TextTree.newInstance(opts).render(root));
			}
			catch ( Exception e ) {
				System.out.print("\033[2J\033[1;1H");
				System.out.println("" + e);
			}
			System.out.println("elapsed: " + watch.stopAndGetElpasedTimeString());
			
			if ( repeatInterval == null ) {
				break;
			}
			TimeUnit.MILLISECONDS.sleep(repeatInterval.toMillis());
		}
	}

	public static final void main(String... args) throws Exception {
		GetPropertyCommand cmd = new GetPropertyCommand();

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

	public final class SubmodelElementListNode implements Node {
		private final List<SubmodelElement> m_smeList;
		
		public SubmodelElementListNode(List<SubmodelElement> smeList) {
			m_smeList = smeList;
		}
	
		@Override
		public String getText() {
			return "Properties";
		}
	
		@Override
		public Iterable<? extends Node> getChildren() {
			return FStream.from(m_smeList)
							.map(sme -> SubmodelElementNodeFactory.toNode(sme, null))
							.filter(n -> n != null);
		}
	}
}
