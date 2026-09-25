package tech.kayys.andalus.cli;

import org.junit.jupiter.api.Test;

import tech.kayys.andalus.cli.PlainWorkbenchRenderer;
import tech.kayys.andalus.client.AndalusGollekSdk;

import static org.assertj.core.api.Assertions.assertThat;

class PlainWorkbenchRendererTest {

    @Test
    void rendersSameWorkbenchModelWithoutTamboui() {
        var sdk = AndalusGollekSdk.local();
        var workspace = sdk.inspectWorkspace(new tech.kayys.andalus.client.WorkspaceInspectionRequest(".", 80, false));
        String text = new PlainWorkbenchRenderer().render(AndalusGollekSdk.local().workbench(), workspace);

        assertThat(text)
                .contains("Andalus Workbench")
                .contains("Gollek")
                .contains("Gamelan")
                .contains("Command Palette")
                .contains("Product Surfaces")
                .contains("coding-agent");
    }
}
