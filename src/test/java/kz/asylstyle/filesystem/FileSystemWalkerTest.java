package kz.asylstyle.filesystem;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileSystemWalkerTest {
    private final FileSystemWalker walker = new FileSystemWalker();

    @Test
    void visitsEveryNodeExactlyOnceWithCorrectContext() {
        DirectoryNode root = new DirectoryNode("root");
        DirectoryNode nested = new DirectoryNode("nested");
        nested.add(new FileNode("file", 2));
        root.add(nested);
        root.add(new FileNode("other", 3));
        TraceVisitor visitor = new TraceVisitor();
        walker.walk(root, visitor);
        assertEquals(List.of("D::0", "D:nested:1", "F:nested/file:2", "F:other:1"), visitor.trace);
        walker.walk(root, visitor);
        assertEquals(4, visitor.trace.size());
    }

    @Test
    void reusedVisitorsResetAcrossRootsAndTreeChanges() {
        DirectoryNode root = new DirectoryNode("root");
        root.add(new FileNode("first.txt", 17));
        TotalSizeVisitor size = new TotalSizeVisitor();
        SearchVisitor search = new SearchVisitor(name -> true, 0);
        walker.walk(root, size);
        walker.walk(root, search);
        root.add(new FileNode("second.txt", 23));
        walker.walk(root, search);
        walker.walk(root, size);
        assertEquals(40, size.totalSize());
        assertEquals(List.of("first.txt", "second.txt"), search.matches());
        walker.walk(new DirectoryNode("empty"), size);
        walker.walk(new DirectoryNode("empty"), search);
        assertEquals(0, size.totalSize());
        assertEquals(List.of(), search.matches());
    }

    @Test
    void traversesDeepTreesWithoutRecursiveCalls() {
        DirectoryNode root = new DirectoryNode("root");
        DirectoryNode current = root;
        for (int depth = 0; depth < 1500; depth++) {
            DirectoryNode child = new DirectoryNode("d");
            current.add(child);
            current = child;
        }
        current.add(new FileNode("end", 31));
        TotalSizeVisitor size = new TotalSizeVisitor();
        walker.walk(root, size);
        assertEquals(31, size.totalSize());
    }

    @Test
    void validatesArgumentsBeforeResettingVisitor() {
        TotalSizeVisitor size = new TotalSizeVisitor();
        walker.walk(new FileNode("file", 8), size);
        assertThrows(NullPointerException.class, () -> walker.walk(null, size));
        assertEquals(8, size.totalSize());
        assertThrows(NullPointerException.class, () -> walker.walk(new DirectoryNode("root"), null));
    }

    private static final class TraceVisitor implements Visitor {
        private final List<String> trace = new ArrayList<>();
        private VisitContext context;

        @Override
        public void begin() {
            trace.clear();
        }

        @Override
        public void enter(VisitContext context) {
            this.context = context;
        }

        @Override
        public void visit(FileNode file) {
            trace.add("F:" + context.relativePath() + ":" + context.depth());
        }

        @Override
        public void visit(DirectoryNode directory) {
            trace.add("D:" + context.relativePath() + ":" + context.depth());
        }
    }
}
