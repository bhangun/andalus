package tech.kayys.andalus.cli;

import tech.kayys.andalus.gollek.sdk.AndalusWorkbenchModel;
import tech.kayys.andalus.gollek.sdk.WorkspaceSnapshot;

interface AndalusWorkbenchRenderer<T> {

    T render(AndalusWorkbenchModel model, WorkspaceSnapshot workspace);
}
