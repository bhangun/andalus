import os

base_dir = "/Users/bhangun/Workspace/workkayys/Products/Andalus/andalus-platform/Families/andalus/runtime/andalus-agent-runtime/src/main/java/tech/kayys/andalus/agent/impl"

for file in ["DefaultAndalusAgent.java", "DefaultAndalusAgentBuilder.java"]:
    path = os.path.join(base_dir, file)
    if os.path.exists(path):
        with open(path, "r") as f:
            content = f.read()
        content = content.replace("tech.kayys.andalus.tools.spi.Tool", "tech.kayys.andalus.tool.Tool")
        content = content.replace("tech.kayys.andalus.tools.spi.ToolContext", "tech.kayys.andalus.tool.ToolContext")
        content = content.replace("tech.kayys.andalus.tools.spi.ToolResult", "tech.kayys.andalus.tool.ToolResult")
        with open(path, "w") as f:
            f.write(content)

print("Agent Runtime fixed.")
