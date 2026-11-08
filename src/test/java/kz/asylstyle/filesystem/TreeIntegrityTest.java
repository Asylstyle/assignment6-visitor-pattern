package kz.asylstyle.filesystem;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TreeIntegrityTest {
    @Test
    void rejectsNegativeFileSize() {
        assertThrows(IllegalArgumentException.class, () -> new FileNode("bad", -1));
        assertEquals(0, new FileNode("empty", 0).size());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", ".", "..", "a/b", "a\\b", "a\0b"})
    void rejectsInvalidPathSegments(String name) {
        assertThrows(IllegalArgumentException.class, () -> new FileNode(name, 0));
        assertThrows(IllegalArgumentException.class, () -> new DirectoryNode(name));
    }

    @Test
    void rejectsNullNamesAndChildren() {
        assertThrows(NullPointerException.class, () -> new FileNode(null, 0));
        assertThrows(NullPointerException.class, () -> new DirectoryNode(null));
        DirectoryNode root = new DirectoryNode("root");
        assertThrows(NullPointerException.class, () -> root.add(null));
        assertThrows(NullPointerException.class, () -> root.remove(null));
        assertTrue(root.children().isEmpty());
    }

    @Test
    void rejectsDuplicateNamesAcrossElementTypesWithoutChangingOwnership() {
        DirectoryNode root = new DirectoryNode("root");
        FileNode existing = new FileNode("same", 1);
        DirectoryNode rejected = new DirectoryNode("same");
        root.add(existing);
        assertThrows(IllegalArgumentException.class, () -> root.add(rejected));
        assertEquals(List.of(existing), root.children());
        assertTrue(rejected.parent().isEmpty());
    }

    @Test
    void preventsMultipleParents() {
        DirectoryNode first = new DirectoryNode("first");
        DirectoryNode second = new DirectoryNode("second");
        FileNode child = new FileNode("child", 9);
        first.add(child);
        assertThrows(IllegalArgumentException.class, () -> second.add(child));
        assertEquals(first, child.parent().orElseThrow());
        assertEquals(List.of(child), first.children());
        assertTrue(second.children().isEmpty());
    }

    @Test
    void preventsSharingDirectorySubtrees() {
        DirectoryNode first = new DirectoryNode("first");
        DirectoryNode second = new DirectoryNode("second");
        DirectoryNode subtree = new DirectoryNode("subtree");
        first.add(subtree);
        assertThrows(IllegalArgumentException.class, () -> second.add(subtree));
        assertEquals(first, subtree.parent().orElseThrow());
    }

    @Test
    void rejectsSelfAndAncestorCycles() {
        DirectoryNode root = new DirectoryNode("root");
        DirectoryNode nested = new DirectoryNode("nested");
        DirectoryNode leaf = new DirectoryNode("leaf");
        root.add(nested);
        nested.add(leaf);
        assertThrows(IllegalArgumentException.class, () -> root.add(root));
        assertThrows(IllegalArgumentException.class, () -> leaf.add(root));
        assertTrue(root.parent().isEmpty());
        assertTrue(leaf.children().isEmpty());
    }

    @Test
    void detachingAllowsReparentingAnEntireSubtree() {
        DirectoryNode first = new DirectoryNode("first");
        DirectoryNode second = new DirectoryNode("second");
        DirectoryNode child = new DirectoryNode("child");
        FileNode file = new FileNode("file", 3);
        child.add(file);
        first.add(child);
        assertFalse(second.remove(child));
        assertEquals(first, child.parent().orElseThrow());
        assertTrue(first.remove(child));
        assertTrue(child.parent().isEmpty());
        second.add(child);
        assertEquals(second, child.parent().orElseThrow());
        assertEquals(child, file.parent().orElseThrow());
        assertEquals(List.of(child), second.children());
    }

    @Test
    void childSnapshotsCannotMutateTheTree() {
        DirectoryNode root = new DirectoryNode("root");
        List<FileSystemNode> snapshot = root.children();
        assertThrows(UnsupportedOperationException.class, () -> snapshot.add(new FileNode("a", 0)));
        root.add(new FileNode("b", 0));
        assertTrue(snapshot.isEmpty());
        assertEquals(1, root.children().size());
    }

    @Test
    void separateDirectoriesMayContainTheSameFilename() {
        DirectoryNode first = new DirectoryNode("first");
        DirectoryNode second = new DirectoryNode("second");
        first.add(new FileNode("same", 1));
        second.add(new FileNode("same", 2));
        DirectoryNode root = new DirectoryNode("root");
        root.add(first);
        root.add(second);
        TotalSizeVisitor total = new TotalSizeVisitor();
        new FileSystemWalker().walk(root, total);
        assertEquals(3, total.totalSize());
    }
}
