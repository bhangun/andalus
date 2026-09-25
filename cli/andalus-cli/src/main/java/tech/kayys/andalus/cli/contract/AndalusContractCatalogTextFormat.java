package tech.kayys.andalus.cli.contract;

import tech.kayys.andalus.contract.AndalusContractDescriptor;
import tech.kayys.andalus.contract.AndalusContractDiscovery;
import tech.kayys.andalus.contract.AndalusContractEnvelopes;
import tech.kayys.andalus.contract.AndalusContractQuery;

/**
 * Text renderer for contract catalog and index responses shown by the Andalus CLI.
 */
final class AndalusContractCatalogTextFormat {

    private static final String NL = System.lineSeparator();

    private AndalusContractCatalogTextFormat() {
    }

    static String text(String productName, AndalusContractDiscovery discovery) {
        AndalusContractDiscovery model = AndalusContractEnvelopes.normalize(discovery);
        StringBuilder output = new StringBuilder();
        output.append(productName).append(" contracts").append(NL);
        appendMatchLine(output, model);
        appendFilteredQuery(output, model.query());
        output.append("schemas: ").append(model.schemas()).append(NL);
        output.append("domains: ").append(model.domains()).append(NL);
        output.append("jsonSchemaIds: ").append(model.jsonSchemaIds()).append(NL);
        output.append("commandIds: ").append(model.commandIds()).append(NL);
        for (AndalusContractDescriptor contract : model.contracts()) {
            output.append("- ")
                    .append(contract.schema())
                    .append(" ")
                    .append(contract.envelope())
                    .append(" v")
                    .append(contract.version())
                    .append(" [")
                    .append(contract.domain())
                    .append("]")
                    .append(NL);
            output.append("  ").append(contract.description()).append(NL);
            output.append("  jsonSchemaId: ").append(contract.jsonSchemaId()).append(NL);
            output.append("  commandIds: ").append(contract.commandIds()).append(NL);
            output.append("  commands: ").append(contract.commands()).append(NL);
        }
        return output.toString();
    }

    static String indexText(String productName, AndalusContractDiscovery discovery) {
        AndalusContractDiscovery model = AndalusContractEnvelopes.normalize(discovery);
        StringBuilder output = new StringBuilder("Andalus contract index\n");
        output.append("product: ").append(productName).append(NL);
        appendMatchLine(output, model);
        appendFilteredQuery(output, model.query());
        output.append("schemas: ").append(model.schemas()).append(NL);
        output.append("schemaCounts: ").append(model.schemaCounts()).append(NL);
        output.append("domains: ").append(model.domains()).append(NL);
        output.append("domainCounts: ").append(model.domainCounts()).append(NL);
        output.append("envelopes: ").append(model.envelopes()).append(NL);
        output.append("jsonSchemaIds: ").append(model.jsonSchemaIds()).append(NL);
        output.append("commandIds: ").append(model.commandIds()).append(NL);
        output.append("commandIdCounts: ").append(model.commandIdCounts()).append(NL);
        return output.toString();
    }

    private static void appendMatchLine(StringBuilder output, AndalusContractDiscovery model) {
        output.append("matching: ").append(model.matchingContracts())
                .append("/")
                .append(model.totalContracts())
                .append(NL);
    }

    private static void appendFilteredQuery(StringBuilder output, AndalusContractQuery query) {
        if (!query.filtered()) {
            return;
        }
        output.append("schema: ").append(query.schema() == null ? "all" : query.schema()).append(NL);
        output.append("envelope: ").append(query.envelope() == null ? "all" : query.envelope()).append(NL);
        output.append("commandId: ").append(query.commandId() == null ? "all" : query.commandId()).append(NL);
        output.append("domain: ").append(query.domain() == null ? "all" : query.domain()).append(NL);
        output.append("jsonSchemaId: ")
                .append(query.jsonSchemaId() == null ? "all" : query.jsonSchemaId())
                .append(NL);
    }
}
