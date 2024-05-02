package mdt.cli;

import java.util.List;

import mdt.cli.MDTCommandsMain.AASCommand;
import mdt.cli.MDTCommandsMain.MDTInstanceCommand;
import mdt.cli.MDTCommandsMain.PropertyCommand;
import mdt.cli.MDTCommandsMain.SubmodelCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.DefaultExceptionHandler;
import picocli.CommandLine.Help;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.RunLast;
import picocli.CommandLine.Spec;
import utils.UsageHelp;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(name="mdt",
		parameterListHeading = "Parameters:%n",
		optionListHeading = "Options:%n",
		description="Manufactoring DigitalTwin (MDT) related commands",
		subcommands = {
			MDTInstanceCommand.class,
			AASCommand.class,
			SubmodelCommand.class,
			PropertyCommand.class,
		})
public class MDTCommandsMain implements Runnable {
	@Spec private CommandSpec m_spec;
	@Mixin private UsageHelp m_help;
	
	@Override
	public void run() {
		m_spec.commandLine().usage(System.out, Ansi.OFF);
	}

	public static final void main(String... args) throws Exception {
		MDTCommandsMain main = new MDTCommandsMain();
		CommandLine cmd = new CommandLine(main).setCaseInsensitiveEnumValuesAllowed(true);
        cmd.parseWithHandlers(new RunLast().useOut(System.out).useAnsi(Help.Ansi.OFF),
        						new DefaultExceptionHandler<List<Object>>().useErr(System.err)
        																	.useAnsi(Help.Ansi.OFF),
        						args);
		
//		CommandLine.run(main, System.out, System.err, Help.Ansi.OFF, args);
	}

	@Command(name="instance",
			description="MDT Instance related commands",
			subcommands= {
				ListMDTInstanceAllCommand.class,
				GetMDTInstanceCommand.class,
				AddMDTInstanceCommand.class,
				RemoveMDTInstanceCommand.class,
				StartMDTInstanceCommand.class,
				StopMDTInstanceCommand.class,
			})
	public static class MDTInstanceCommand implements Runnable {
		@Spec private CommandSpec m_spec;
		@Mixin private UsageHelp m_help;
		
		@Override
		public void run() {
			m_spec.commandLine().usage(System.out, Ansi.OFF);
		}
	}

	@Command(name="aas",
			description="AssetAdministrationShell related commands",
			subcommands= {
				ListAASAllCommand.class,
				GetAASCommand.class,
			})
	public static class AASCommand implements Runnable {
		@Spec private CommandSpec m_spec;
		@Mixin private UsageHelp m_help;
		
		@Override
		public void run() {
			m_spec.commandLine().usage(System.out, Ansi.OFF);
		}
	}

	@Command(name="submodel",
			description="Submodel related commands",
			subcommands= {
				ListSubmodelAllCommand.class,
				GetSubmodelCommand.class,
			})
	public static class SubmodelCommand implements Runnable {
		@Spec private CommandSpec m_spec;
		@Mixin private UsageHelp m_help;
		
		@Override
		public void run() {
			m_spec.commandLine().usage(System.out, Ansi.OFF);
		}
	}

	@Command(name="property",
			description="Property related commands",
			subcommands= {
				GetPropertyCommand.class,
			})
	public static class PropertyCommand implements Runnable {
		@Spec private CommandSpec m_spec;
		@Mixin private UsageHelp m_help;
		
		@Override
		public void run() {
			m_spec.commandLine().usage(System.out, Ansi.OFF);
		}
	}
}
