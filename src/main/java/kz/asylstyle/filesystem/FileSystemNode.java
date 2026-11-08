package kz.asylstyle.filesystem;

import java.util.Objects;

public abstract class FileSystemNode implements Element {
    private final String name;

    protected FileSystemNode(String name) {
        Objects.requireNonNull(name, "Name is required");
        if (name.isBlank() || name.equals(".") || name.equals("..")
                || name.indexOf('/') >= 0 || name.indexOf('\\') >= 0
                || name.indexOf('\0') >= 0) {
            throw new IllegalArgumentException("Name must be a single non-blank path segment");
        }
        this.name = name;
    }

    @Override
    public final String name() {
        return name;
    }
}
