package kz.asylstyle.filesystem;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SearchVisitorTest {
    private final FileSystemWalker walker = new FileSystemWalker();

    @Test
    void combinesFilenameAndInclusiveSizeConstraints() {
        DirectoryNode root = new DirectoryNode("root");
        root.add(new FileNode("small.txt", 49));
        root.add(new FileNode("exact.txt", 50));
        root.add(new FileNode("large.txt", 90));
        root.add(new FileNode("image.png", 500));
        SearchVisitor visitor = new SearchVisitor(name -> name.endsWith(".txt"), 50);
        walker.walk(root, visitor);
        assertEquals(List.of("exact.txt", "large.txt"), visitor.matches());
    }

    @Test
    void sortsPathsRatherThanRelyingOnInsertionOrder() {
        DirectoryNode root = new DirectoryNode("root");
        DirectoryNode nested = new DirectoryNode("alpha");
        root.add(new FileNode("z.txt", 5));
        nested.add(new FileNode("same.txt", 5));
        root.add(nested);
        root.add(new FileNode("same.txt", 5));
        SearchVisitor visitor = new SearchVisitor(name -> true, 0);
        walker.walk(root, visitor);
        assertEquals(List.of("alpha/same.txt", "same.txt", "z.txt"), visitor.matches());
    }

    @Test
    void pathsAreRelativeToChosenSubtree() {
        DirectoryNode root = Demo.sampleTree();
        DirectoryNode subtree = new DirectoryNode("chosen");
        subtree.add(new FileNode("inside.txt", 11));
        root.add(subtree);
        SearchVisitor visitor = new SearchVisitor(name -> true, 0);
        walker.walk(subtree, visitor);
        assertEquals(List.of("inside.txt"), visitor.matches());
    }

    @Test
    void standaloneFileUsesItsOwnName() {
        SearchVisitor visitor = new SearchVisitor(name -> name.startsWith("memo"), 0);
        walker.walk(new FileNode("memo.txt", 0), visitor);
        assertEquals(List.of("memo.txt"), visitor.matches());
    }

    @Test
    void emptyDirectoriesAndNonMatchingFilesProduceNoResults() {
        DirectoryNode root = new DirectoryNode("root");
        root.add(new DirectoryNode("wanted.txt"));
        root.add(new FileNode("other.bin", 10));
        SearchVisitor visitor = new SearchVisitor(name -> name.endsWith(".txt"), 0);
        walker.walk(root, visitor);
        assertEquals(List.of(), visitor.matches());
        walker.walk(new DirectoryNode("empty"), visitor);
        assertEquals(List.of(), visitor.matches());
    }

    @Test
    void returnsImmutableSnapshots() {
        SearchVisitor visitor = new SearchVisitor(name -> true, 0);
        walker.walk(new FileNode("first", 1), visitor);
        List<String> snapshot = visitor.matches();
        assertThrows(UnsupportedOperationException.class, () -> snapshot.add("other"));
        walker.walk(new FileNode("second", 1), visitor);
        assertEquals(List.of("first"), snapshot);
        assertEquals(List.of("second"), visitor.matches());
    }

    @Test
    void validatesSearchConfiguration() {
        assertThrows(NullPointerException.class, () -> new SearchVisitor(null, 0));
        assertThrows(IllegalArgumentException.class, () -> new SearchVisitor(name -> true, -1));
    }
}
