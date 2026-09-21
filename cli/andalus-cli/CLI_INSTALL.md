Andalus CLI packaging and install

Build

  mvn -pl cli/andalus-gollek-cli -am -DskipTests package

Local launcher

The repository contains a lightweight launcher script at:

  cli/andalus-gollek-cli/bin/andalus

Install system-wide (example):

  cp cli/andalus-gollek-cli/target/andalus-gollek-cli-1.0.0-SNAPSHOT.jar /opt/andalus/andalus.jar
  sudo ln -s /opt/andalus/andalus.jar /usr/local/lib/andalus.jar
  sudo tee /usr/local/bin/andalus >/dev/null <<'SH'
#!/usr/bin/env sh
exec java -jar /usr/local/lib/andalus.jar "$@"
SH
  sudo chmod +x /usr/local/bin/andalus

Using the launcher (development):

  cli/andalus-gollek-cli/bin/andalus serve --rest

Notes

- The shaded jar is produced during packaging; the launcher points to the module artifact under target/.
- For system packaging, create a proper package (deb/rpm) or install the jar under /opt and symlink.
- The REST server listens on 8080 by default; gRPC listens on 31013.
