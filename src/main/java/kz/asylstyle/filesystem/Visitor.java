package kz.asylstyle.filesystem;

public interface Visitor {
    void visit(FileNode file);

    void visit(DirectoryNode directory);
}
