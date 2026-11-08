package kz.asylstyle.filesystem;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VisitorDispatchTest {
    @Test
    void fileSelectsFileOverloadThroughElementReference() {
        Element element = new FileNode("log.txt", 4);
        CountingVisitor visitor = new CountingVisitor();
        element.accept(visitor);
        assertEquals(1, visitor.files);
        assertEquals(0, visitor.directories);
    }

    @Test
    void directorySelectsDirectoryOverloadThroughElementReference() {
        Element element = new DirectoryNode("logs");
        CountingVisitor visitor = new CountingVisitor();
        element.accept(visitor);
        assertEquals(0, visitor.files);
        assertEquals(1, visitor.directories);
    }

    @Test
    void acceptDoesNotTraverseChildren() {
        DirectoryNode directory = new DirectoryNode("logs");
        directory.add(new FileNode("log.txt", 4));
        CountingVisitor visitor = new CountingVisitor();
        directory.accept(visitor);
        assertEquals(0, visitor.files);
        assertEquals(1, visitor.directories);
    }

    @Test
    void bothElementsRejectNullVisitor() {
        assertThrows(NullPointerException.class, () -> new FileNode("a", 0).accept(null));
        assertThrows(NullPointerException.class, () -> new DirectoryNode("a").accept(null));
    }

    private static final class CountingVisitor implements Visitor {
        private int files;
        private int directories;

        @Override
        public void visit(FileNode file) {
            files++;
        }

        @Override
        public void visit(DirectoryNode directory) {
            directories++;
        }
    }
}
