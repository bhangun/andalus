package tech.kayys.andalus.cli;

// import tech.kayys.andalus.tui.Component; // removed: class no longer exists
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import tech.kayys.andalus.client.AndalusGollekSdk;
import tech.kayys.andalus.client.AndalusWorkbenchModel;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled("AndalusGollekTuiView and Component have been removed; test needs migration")
class AndalusGollekTuiViewTest {

    @Test
    void buildsElementTreeFromWorkbenchModel() {
        var sdk = AndalusGollekSdk.local();
        AndalusWorkbenchModel model = sdk.workbench();
        var workspace = sdk.inspectWorkspace(new tech.kayys.andalus.client.WorkspaceInspectionRequest(".", 80, false));
        // AndalusGollekTuiView view = new AndalusGollekTuiView();
        // Component element = view.render(model, workspace);
        // assertThat(element).isNotNull();
    }
}
