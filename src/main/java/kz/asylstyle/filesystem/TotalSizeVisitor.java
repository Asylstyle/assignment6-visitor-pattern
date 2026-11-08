package kz.asylstyle.filesystem;

public final class TotalSizeVisitor implements Visitor {
    private long totalSize;

    @Override
    public void begin() {
        totalSize = 0;
    }

    @Override
    public void visit(FileNode file) {
        totalSize = Math.addExact(totalSize, file.size());
    }

    @Override
    public void visit(DirectoryNode directory) {
    }

    public long totalSize() {
        return totalSize;
    }
}
