package tech.kayys.andalus.cli;

import tech.kayys.andalus.client.AndalusWorkbenchModel;
import tech.kayys.andalus.client.WorkspaceSnapshot;

interface AndalusWorkbenchRenderer<T> {

    T render(AndalusWorkbenchModel model, WorkspaceSnapshot workspace);
}
