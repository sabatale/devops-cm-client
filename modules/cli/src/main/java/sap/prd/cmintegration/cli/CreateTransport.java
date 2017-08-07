package sap.prd.cmintegration.cli;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static sap.prd.cmintegration.cli.Commands.Helpers.getChangeId;
import static sap.prd.cmintegration.cli.Commands.Helpers.getCommandName;
import static sap.prd.cmintegration.cli.Commands.Helpers.getHost;
import static sap.prd.cmintegration.cli.Commands.Helpers.getPassword;
import static sap.prd.cmintegration.cli.Commands.Helpers.getUser;
import static sap.prd.cmintegration.cli.Commands.Helpers.handleHelpOption;
import static sap.prd.cmintegration.cli.Commands.Helpers.helpRequested;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataClient;
import sap.ai.st.cm.plugins.ciintegration.odataclient.CMODataTransport;

@CommandDescriptor(name = "create-transport")
class CreateTransport extends Command {

	final static private Logger logger = LoggerFactory.getLogger(CreateTransport.class);
	private final String changeId, owner, description;

    public CreateTransport(String host, String user, String password, String changeId,
            String owner, String description) {
        super(host, user, password);
        this.changeId = changeId;
        this.owner = owner;
        this.description = description;
    }

    public final static void main(String[] args) throws Exception {

        logger.debug(Commands.Helpers.getArgsLogString(args));
    	Options options = new Options();
        Commands.Helpers.addStandardParameters(options);

        Option owner = new Option("o", "owner", true, "The transport owner. If ommited the login user us used."),
               description = new Option("d", "description", true, "The description of the transport request.");

        options.addOption(owner).addOption(description);

        if(helpRequested(args)) {
            handleHelpOption(format("%s [--owner <owner>][--description <description>] <changeId>", getCommandName(CreateTransport.class)),
            "Creates a new transport entity. " +
            "Returns the ID of the transport entity. " +
            "If there is already an open transport, the ID of the already existing open transport might be returned.",
            new Options().addOption(owner).addOption(description)); return;
        }

        CommandLine commandLine = new DefaultParser().parse(options, args);

        new CreateTransport(getHost(commandLine),
                getUser(commandLine),
                getPassword(commandLine),
                getChangeId(commandLine),
                commandLine.getOptionValue(owner.getOpt()),
                commandLine.getOptionValue(description.getOpt())).execute();
    }

    @Override
    void execute() throws Exception {
        try(CMODataClient client = ClientFactory.getInstance().newClient(host, user,  password)) {
            CMODataTransport transport;
            if(owner == null && description == null) {
                transport = client.createDevelopmentTransport(changeId);
            } else {
                transport = client.createDevelopmentTransportAdvanced(
                              changeId,
                              isBlank(description) ? "" : description,
                              isBlank(owner) ? user : owner);
            }
            logger.debug(String.format("Tansport Id: '%s' Owner: '%s' isModifiable: '%s'", transport.getTransportID(), transport.getOwner(), Boolean.toString(transport.isModifiable())));
            System.out.println(transport.getTransportID());
            System.out.flush();
        }
    }
}
