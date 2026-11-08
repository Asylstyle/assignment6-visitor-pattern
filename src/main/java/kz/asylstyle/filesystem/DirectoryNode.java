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
        if (child.parent().isPresent()) {
            throw new IllegalArgumentException("Child already belongs to a directory");
        }
        for (FileSystemNode ancestor = this; ancestor != null;
                ancestor = ancestor.parent().orElse(null)) {
            if (ancestor == child) {
                throw new IllegalArgumentException("A directory cannot contain itself or an ancestor");
            }
        }
        children.add(child);
        child.attachTo(this);
    }

    public boolean remove(FileSystemNode child) {
        Objects.requireNonNull(child, "Child is required");
        if (!children.remove(child)) {
            return false;
        }
        child.attachTo(null);
        return true;
    }

    public List<FileSystemNode> children() {
        return List.copyOf(children);
    }

    @Override
    public void accept(Visitor visitor) {
        Objects.requireNonNull(visitor, "Visitor is required").visit(this);
    }
}
