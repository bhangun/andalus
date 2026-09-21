package tech.kayys.andalus.cli.operator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.PrintStream;
import java.util.*;

/**
 * Presentation abstraction for operator CLI command output.
 */
public interface CliOutput {

    void print(Object value);

    void success(String message);

    void error(String message);

    static CliOutput of(String format, PrintStream out, PrintStream err) {
        if ("json".equalsIgnoreCase(format)) {
            return new JsonCliOutput(out, err);
        }
        return new TableCliOutput(out, err);
    }

    class JsonCliOutput implements CliOutput {
        private final PrintStream out;
        private final PrintStream err;
        private final ObjectMapper mapper;

        public JsonCliOutput(PrintStream out, PrintStream err) {
            this.out = out != null ? out : System.out;
            this.err = err != null ? err : System.err;
            this.mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        }

        @Override
        public void print(Object value) {
            try {
                out.println(mapper.writeValueAsString(value));
            } catch (Exception e) {
                out.println(String.valueOf(value));
            }
        }

        @Override
        public void success(String message) {
            print(Map.of("status", "SUCCESS", "message", message));
        }

        @Override
        public void error(String message) {
            err.println("{\"status\":\"ERROR\",\"message\":\"" + message.replace("\"", "\\\"") + "\"}");
        }
    }

    class TableCliOutput implements CliOutput {
        private final PrintStream out;
        private final PrintStream err;

        public TableCliOutput(PrintStream out, PrintStream err) {
            this.out = out != null ? out : System.out;
            this.err = err != null ? err : System.err;
        }

        @Override
        public void print(Object value) {
            if (value == null) {
                out.println("<null>");
                return;
            }
            if (value instanceof Iterable<?> iter) {
                printList(iter);
            } else if (value instanceof Map<?, ?> map) {
                printMap(map);
            } else {
                out.println(value);
            }
        }

        private void printList(Iterable<?> items) {
            int count = 0;
            for (Object item : items) {
                count++;
                out.printf("[%d] %s%n", count, item);
            }
            if (count == 0) {
                out.println("(Empty list)");
            }
        }

        private void printMap(Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                out.printf("%-20s: %s%n", entry.getKey(), entry.getValue());
            }
        }

        @Override
        public void success(String message) {
            out.println("✓ " + message);
        }

        @Override
        public void error(String message) {
            err.println("✗ Error: " + message);
        }
    }
}
