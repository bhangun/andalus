import os

base_dir = "/Users/bhangun/Workspace/workkayys/Products/Andalus/andalus-platform/Families/andalus/runtime/andalus-agent-runtime/src/main/java/tech/kayys/andalus/agent/impl"

# Fix DefaultAndalusAgent
agent_file = os.path.join(base_dir, "DefaultAndalusAgent.java")
with open(agent_file, "r") as f:
    content = f.read()

content = content.replace("package tech.kayys.andalus.agent;", "package tech.kayys.andalus.agent.impl;")
content = content.replace("import tech.kayys.andalus.json.JsonValue;", "import tech.kayys.andalus.json.JsonValue;\nimport tech.kayys.andalus.agent.Agent;\nimport tech.kayys.andalus.agent.AndalusAgentListener;\nimport tech.kayys.andalus.agent.PermissionDecision;")
content = content.replace("public final class AndalusAgent {", "public final class DefaultAndalusAgent implements Agent {")
content = content.replace("public AndalusAgent(", "public DefaultAndalusAgent(")
content = content.replace("AndalusAgent.send", "DefaultAndalusAgent.send")

with open(agent_file, "w") as f:
    f.write(content)

# Fix DefaultAndalusAgentBuilder
builder_file = os.path.join(base_dir, "DefaultAndalusAgentBuilder.java")
with open(builder_file, "r") as f:
    content = f.read()

content = content.replace("package tech.kayys.andalus.agent;", "package tech.kayys.andalus.agent.impl;\n\nimport tech.kayys.andalus.agent.Agent;")
content = content.replace("public class AndalusAgentBuilder", "public class DefaultAndalusAgentBuilder")
content = content.replace("public AndalusAgent build()", "public Agent build()")
content = content.replace("return new AndalusAgent(", "return new DefaultAndalusAgent(")
content = content.replace("AndalusAgentBuilder", "DefaultAndalusAgentBuilder")

with open(builder_file, "w") as f:
    f.write(content)

# Fix AndalusSessionPersistence
persistence_file = os.path.join(base_dir, "AndalusSessionPersistence.java")
with open(persistence_file, "r") as f:
    content = f.read()

content = content.replace("package tech.kayys.andalus.agent;", "package tech.kayys.andalus.agent.impl;")
with open(persistence_file, "w") as f:
    f.write(content)

print("Agent implementations updated.")
