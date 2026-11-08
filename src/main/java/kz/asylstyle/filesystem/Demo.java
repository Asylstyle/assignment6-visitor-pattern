package kz.asylstyle.filesystem;

public final class Demo {
    private Demo() {
    }

    public static void main(String[] args) {
        DirectoryNode root = sampleTree();
        FileSystemWalker walker = new FileSystemWalker();
        TotalSizeVisitor size = new TotalSizeVisitor();
        SearchVisitor search = new SearchVisitor(name -> name.endsWith(".txt"), 100);
        walker.walk(root, size);
        walker.walk(root, search);
        System.out.println("Root: " + root.name());
        System.out.println("Total size: " + size.totalSize() + " bytes");
        System.out.println("Text files (minimum 100 bytes):");
        search.matches().forEach(path -> System.out.println("  " + path));
    }

    static DirectoryNode sampleTree() {
        DirectoryNode root = new DirectoryNode("workspace");
        DirectoryNode documents = new DirectoryNode("documents");
        DirectoryNode archive = new DirectoryNode("archive");
        DirectoryNode media = new DirectoryNode("media");
        archive.add(new FileNode("draft.txt", 80));
        archive.add(new FileNode("summary.txt", 180));
        documents.add(new FileNode("notes.txt", 120));
        documents.add(new FileNode("plan.pdf", 2048));
        documents.add(archive);
        media.add(new FileNode("logo.png", 4096));
        root.add(new FileNode("readme.txt", 320));
        root.add(documents);
        root.add(media);
        root.add(new DirectoryNode("empty"));
        return root;
    }
}
