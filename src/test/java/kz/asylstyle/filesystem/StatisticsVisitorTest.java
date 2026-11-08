package kz.asylstyle.filesystem;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StatisticsVisitorTest {
    private final FileSystemWalker walker = new FileSystemWalker();
    private final StatisticsVisitor visitor = new StatisticsVisitor();

    @Test
    void countsEveryElementAndUsesRootDepthZero() {
        DirectoryNode root = new DirectoryNode("root");
        DirectoryNode branch = new DirectoryNode("branch");
        DirectoryNode empty = new DirectoryNode("empty");
        branch.add(new FileNode("a", 9));
        branch.add(empty);
        root.add(branch);
        root.add(new FileNode("b", 0));
        walker.walk(root, visitor);
        assertEquals(new Statistics(2, 3, 2), visitor.statistics());
    }

    @Test
    void emptyDirectoryStillCountsTheRoot() {
        walker.walk(new DirectoryNode("empty"), visitor);
        assertEquals(new Statistics(0, 1, 0), visitor.statistics());
    }

    @Test
    void standaloneFileHasDepthZeroAndNoDirectories() {
        walker.walk(new FileNode("file", 10), visitor);
        assertEquals(new Statistics(1, 0, 0), visitor.statistics());
    }

    @Test
    void deepestEmptyDirectoryContributesToDepth() {
        DirectoryNode root = new DirectoryNode("root");
        DirectoryNode middle = new DirectoryNode("middle");
        DirectoryNode leaf = new DirectoryNode("leaf");
        root.add(middle);
        middle.add(leaf);
        walker.walk(root, visitor);
        assertEquals(new Statistics(0, 3, 2), visitor.statistics());
    }

    @Test
    void repeatRunsResetCountsAndDepthWhilePreservingOldSnapshot() {
        walker.walk(Demo.sampleTree(), visitor);
        Statistics first = visitor.statistics();
        assertEquals(new Statistics(6, 5, 3), first);
        walker.walk(Demo.sampleTree(), visitor);
        assertEquals(first, visitor.statistics());
        walker.walk(new DirectoryNode("empty"), visitor);
        assertEquals(new Statistics(0, 1, 0), visitor.statistics());
        assertEquals(new Statistics(6, 5, 3), first);
    }

    @Test
    void selectedSubtreeStartsAtDepthZeroEvenWhenItHasAParent() {
        DirectoryNode root = new DirectoryNode("root");
        DirectoryNode subtree = new DirectoryNode("subtree");
        subtree.add(new FileNode("file", 3));
        root.add(subtree);
        walker.walk(subtree, visitor);
        assertEquals(new Statistics(1, 1, 1), visitor.statistics());
    }
}
