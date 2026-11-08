package kz.asylstyle.filesystem;

public interface Visitor {
    default void begin() {
    }

    default void enter(VisitContext context) {
    }

    void visit(FileNode file);

    void visit(DirectoryNode directory);
}
