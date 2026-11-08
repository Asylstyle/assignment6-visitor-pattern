package kz.asylstyle.filesystem;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

public final class FileSystemWalker {
    public void walk(Element root, Visitor visitor) {
        Objects.requireNonNull(root, "Root is required");
        Objects.requireNonNull(visitor, "Visitor is required");
        visitor.begin();
        Deque<PendingVisit> pending = new ArrayDeque<>();
        pending.push(new PendingVisit(root, new VisitContext("", 0)));
        while (!pending.isEmpty()) {
            PendingVisit current = pending.pop();
            visitor.enter(current.context());
            current.element().accept(new Dispatch(visitor, current.context(), pending));
        }
    }

    private record PendingVisit(Element element, VisitContext context) {
    }

    private record Dispatch(Visitor visitor, VisitContext context,
                            Deque<PendingVisit> pending) implements Visitor {
        @Override
        public void visit(FileNode file) {
            visitor.visit(file);
        }

        @Override
        public void visit(DirectoryNode directory) {
            visitor.visit(directory);
            List<FileSystemNode> children = directory.children();
            for (int index = children.size() - 1; index >= 0; index--) {
                FileSystemNode child = children.get(index);
                String path = context.relativePath().isEmpty()
                        ? child.name() : context.relativePath() + "/" + child.name();
                VisitContext childContext = new VisitContext(path, context.depth() + 1);
                pending.push(new PendingVisit(child, childContext));
            }
        }
    }
}
