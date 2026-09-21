package tech.kayys.andalus.cli;

import tech.kayys.andalus.gollek.sdk.AndalusStandardCatalog;
import tech.kayys.andalus.gollek.sdk.AndalusStandardCatalogEnvelopes;
import tech.kayys.andalus.gollek.sdk.AndalusStandardDefinition;

/**
 * Text renderer for standards catalog responses shown by the Andalus CLI.
 */
final class AndalusStandardCatalogTextFormat {

    private static final String NL = System.lineSeparator();

    private AndalusStandardCatalogTextFormat() {
    }

    static String text(String productName, AndalusStandardCatalog catalog) {
        AndalusStandardCatalog model = AndalusStandardCatalogEnvelopes.normalize(catalog);
        StringBuilder output = new StringBuilder("Andalus standards catalog").append(NL);
        output.append("product: ").append(productName).append(NL);
        output.append("standards: ").append(model.totalStandards()).append(NL);
        output.append("standardIds: ").append(model.standardIds()).append(NL);
        output.append("versions: ").append(model.versions()).append(NL);
        output.append("bindings: ").append(model.bindings()).append(NL);
        output.append("bindingCounts: ").append(model.bindingCounts()).append(NL);
        for (AndalusStandardDefinition standard : model.standards()) {
            appendStandard(output, standard);
        }
        return output.toString();
    }

    private static void appendStandard(StringBuilder output, AndalusStandardDefinition standard) {
        output.append("- ")
                .append(standard.standardId())
                .append(" ")
                .append(standard.name())
                .append(" ")
                .append(standard.version())
                .append(" [")
                .append(standard.binding())
                .append("]")
                .append(NL);
        output.append("  specUrl: ").append(standard.specUrl()).append(NL);
        output.append("  aliases: ").append(standard.aliases()).append(NL);
        if (!standard.attributes().isEmpty()) {
            output.append("  attributes: ").append(standard.attributes()).append(NL);
        }
    }

}
