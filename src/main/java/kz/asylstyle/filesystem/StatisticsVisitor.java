package kz.asylstyle.filesystem;

public final class StatisticsVisitor implements Visitor {
    private long fileCount;
    private long directoryCount;
    private int maximumDepth;

    @Override
    public void begin() {
        fileCount = 0;
        directoryCount = 0;
        maximumDepth = 0;
    }

    @Override
    public void enter(VisitContext context) {
        maximumDepth = Math.max(maximumDepth, context.depth());
    }

    @Override
    public void visit(FileNode file) {
        fileCount++;
    }

    @Override
    public void visit(DirectoryNode directory) {
        directoryCount++;
    }

    public Statistics statistics() {
        return new Statistics(fileCount, directoryCount, maximumDepth);
    }
}
