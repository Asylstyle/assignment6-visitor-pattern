package kz.asylstyle.filesystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class DirectoryNode extends FileSystemNode {
    private final List<FileSystemNode> children = new ArrayList<>();

    public DirectoryNode(String name) {
        super(name);
    }

    public void add(FileSystemNode child) {
        Objects.requireNonNull(child, "Child is required");
        if (children.stream().anyMatch(existing -> existing.name().equals(child.name()))) {
            throw new IllegalArgumentException("Duplicate child name: " + child.name());
        }
        children.add(child);
    }

    public List<FileSystemNode> children() {
        return List.copyOf(children);
    }

    @Override
    public void accept(Visitor visitor) {
        Objects.requireNonNull(visitor, "Visitor is required").visit(this);
    }
}
