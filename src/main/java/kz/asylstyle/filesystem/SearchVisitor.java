package kz.asylstyle.filesystem;

import java.util.List;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Predicate;

public final class SearchVisitor implements Visitor {
    private final Predicate<String> filenameCriterion;
    private final long minimumSize;
    private final SortedSet<String> matches = new TreeSet<>();
    private String relativePath = "";

    public SearchVisitor(Predicate<String> filenameCriterion, long minimumSize) {
        this.filenameCriterion = Objects.requireNonNull(filenameCriterion, "Filename criterion is required");
        if (minimumSize < 0) {
            throw new IllegalArgumentException("Minimum size must be non-negative");
        }
        this.minimumSize = minimumSize;
    }

    @Override
    public void begin() {
        matches.clear();
        relativePath = "";
    }

    @Override
    public void enter(VisitContext context) {
        relativePath = context.relativePath();
    }

    @Override
    public void visit(FileNode file) {
        if (file.size() >= minimumSize && filenameCriterion.test(file.name())) {
            matches.add(relativePath.isEmpty() ? file.name() : relativePath);
        }
    }

    @Override
    public void visit(DirectoryNode directory) {
    }

    public List<String> matches() {
        return List.copyOf(matches);
    }
}
