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
        children.add(Objects.requireNonNull(child, "Child is required"));
    }

    public List<FileSystemNode> children() {
        return List.copyOf(children);
    }

    @Override
    public void accept(Visitor visitor) {
        Objects.requireNonNull(visitor, "Visitor is required").visit(this);
    }
}
