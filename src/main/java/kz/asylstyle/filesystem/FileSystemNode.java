package kz.asylstyle.filesystem;

import java.util.Objects;
import java.util.Optional;

public abstract class FileSystemNode implements Element {
    private final String name;
    private DirectoryNode parent;

    protected FileSystemNode(String name) {
        Objects.requireNonNull(name, "Name is required");
        if (name.isBlank() || name.equals(".") || name.equals("..")
                || name.indexOf('/') >= 0 || name.indexOf('\\') >= 0
                || name.indexOf('\0') >= 0) {
            throw new IllegalArgumentException("Name must be a single non-blank path segment");
        }
        this.name = name;
    }

    public final Optional<DirectoryNode> parent() {
        return Optional.ofNullable(parent);
    }

    final void attachTo(DirectoryNode directory) {
        parent = directory;
    }

    @Override
    public final String name() {
        return name;
    }
}
