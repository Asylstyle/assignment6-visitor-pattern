package kz.asylstyle.filesystem;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TotalSizeVisitorTest {
    private final FileSystemWalker walker = new FileSystemWalker();
    private final TotalSizeVisitor visitor = new TotalSizeVisitor();

    @Test
    void sumsFilesAcrossThreeLevels() {
        DirectoryNode root = new DirectoryNode("root");
        DirectoryNode first = new DirectoryNode("first");
        DirectoryNode second = new DirectoryNode("second");
        root.add(new FileNode("a", 13));
        first.add(new FileNode("b", 29));
        second.add(new FileNode("c", 47));
        first.add(second);
        root.add(first);
        walker.walk(root, visitor);
        assertEquals(89, visitor.totalSize());
    }

    @Test
    void supportsStandaloneFile() {
        walker.walk(new FileNode("single", 61), visitor);
        assertEquals(61, visitor.totalSize());
    }

    @Test
    void emptyDirectoryAndZeroByteFileHaveZeroSize() {
        DirectoryNode root = new DirectoryNode("empty");
        root.add(new DirectoryNode("nested"));
        root.add(new FileNode("zero", 0));
        walker.walk(root, visitor);
        assertEquals(0, visitor.totalSize());
    }

    @Test
    void rejectsOverflowInsteadOfReturningNegativeTotal() {
        DirectoryNode root = new DirectoryNode("large");
        root.add(new FileNode("a", Long.MAX_VALUE));
        root.add(new FileNode("b", 1));
        assertThrows(ArithmeticException.class, () -> walker.walk(root, visitor));
        walker.walk(new FileNode("small", 7), visitor);
        assertEquals(7, visitor.totalSize());
    }
}
