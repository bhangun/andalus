# Andalus Service Installation

Release service bundles are laid out as a portable `.andalus` home. Extract the archive, then run the installer for your platform:

- Linux: `scripts/service/linux/install-systemd-user.sh`
- macOS: `scripts/service/macos/install-launchd-agent.sh`
- Windows: `scripts/service/windows/install-service.ps1`

The installers copy bundled files into `~/.andalus/*` before registering the service.

Runtime defaults:

- `ANDALUS_HOME`: `~/.andalus`
- `ANDALUS_CONFIG_DIR`: `~/.andalus/config`
- `ANDALUS_LOG_DIR`: `~/.andalus/logs`
- `ANDALUS_SERVER_LOG_DIR`: `~/.andalus/logs/server`
- `ANDALUS_LOG_FILE_PATH`: `~/.andalus/logs/server/server.log`
- `ANDALUS_PLUGINS_DIR`: `~/.andalus/plugins`
- `ANDALUS_SECRETS_DIR`: `~/.andalus/secrets`
- `ANDALUS_MODELS_DIR`: `~/.andalus/models`
- `ANDALUS_MCP_DIR`: `~/.andalus/mcp`
- `ANDALUS_RUN_DIR`: `~/.andalus/run`
- `ANDALUS_VECTOR_DIR`: `~/.andalus/vector`
- `ANDALUS_GOLLEK_HOME`: `~/.andalus/gollek`
- `ANDALUS_GOLLEK_MODELS_DIR`: `~/.andalus/gollek/models`
- `ANDALUS_GOLLEK_STORAGE_DIR`: `~/.andalus/gollek/storage`

Gollek compatibility defaults to `~/.andalus/gollek`. Set `GOLLEK_HOME=~/.gollek` only when a legacy deployment must keep using the old Gollek directory.
