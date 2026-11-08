package kz.asylstyle.filesystem;

public interface Element {
    String name();

    void accept(Visitor visitor);
}
