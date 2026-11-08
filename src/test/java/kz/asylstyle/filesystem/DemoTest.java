package kz.asylstyle.filesystem;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DemoTest {
    @Test
    void printsTheCompleteDeterministicDefaultDemonstration() {
        assertEquals("""
                Root: workspace
                Total size: 6844 bytes
                Files ending with .txt (minimum 100 bytes):
                  documents/archive/summary.txt
                  documents/notes.txt
                  readme.txt
                Files: 6
                Directories: 5
                Maximum depth: 3
                """, output());
    }

    @Test
    void supportsChangingSuffixAndThreshold() {
        assertEquals("""
                Root: workspace
                Total size: 6844 bytes
                Files ending with .pdf (minimum 1000 bytes):
                  documents/plan.pdf
                Files: 6
                Directories: 5
                Maximum depth: 3
                """, output(".pdf", "1000"));
    }

    @Test
    void supportsNoMatchDemonstrationsWithoutChangingStatistics() {
        assertEquals("""
                Root: workspace
                Total size: 6844 bytes
                Files ending with .zip (minimum 0 bytes):
                Files: 6
                Directories: 5
                Maximum depth: 3
                """, output(".zip", "0"));
    }

    @Test
    void rejectsInvalidCommandLineArguments() {
        assertThrows(IllegalArgumentException.class, () -> Demo.main(new String[]{"a", "0", "extra"}));
        assertThrows(NumberFormatException.class, () -> Demo.main(new String[]{"a", "abc"}));
        assertThrows(IllegalArgumentException.class, () -> Demo.main(new String[]{"a", "-1"}));
    }

    private String output(String... arguments) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream original = System.out;
        try (PrintStream capture = new PrintStream(buffer, true, StandardCharsets.UTF_8)) {
            System.setOut(capture);
            Demo.main(arguments);
        } finally {
            System.setOut(original);
        }
        return buffer.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
    }
}
