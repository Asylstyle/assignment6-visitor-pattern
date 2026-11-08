# Visitor File System

An in-memory hierarchical file system for Assignment 06, Software Design Patterns.
Java 17, Maven and JUnit 5. No runtime dependencies.

## Build and run

Install JDK 17 or newer and Maven 3.9 or newer. Run from the repository root:

```sh
mvn test
mvn package
java -jar target/visitor-file-system-1.0.0.jar
```

The optional arguments change the case-sensitive filename suffix and inclusive
minimum byte size:

```sh
java -jar target/visitor-file-system-1.0.0.jar .pdf 1000
```

## Demonstration

```text
workspace/
  readme.txt                      320
  documents/
    notes.txt                    120
    plan.pdf                    2048
    archive/
      draft.txt                   80
      summary.txt                180
  media/
    logo.png                    4096
  empty/
```

Default output:

```text
Root: workspace
Total size: 6844 bytes
Files ending with .txt (minimum 100 bytes):
  documents/archive/summary.txt
  documents/notes.txt
  readme.txt
Files: 6
Directories: 5
Maximum depth: 3
```

## Traversal and dispatch

`FileSystemWalker` owns traversal. Its explicit stack performs depth-first preorder
without recursive Java calls. Children are pushed in reverse insertion order, so
each operation receives each node once in insertion order. Elements only perform
double dispatch: `accept(Visitor)` calls the overload `visit(this)` for the concrete
type. The walker's private `Dispatch` visitor forwards that typed call to the
operation and schedules children only for directories. Operation visitors never
recurse. No type tests, reflection or behavior-selecting casts are needed.

`Visitor.begin()` runs once before each valid walk, resetting accumulated state.
`enter(VisitContext)` then supplies each node's root-relative path and depth before
its typed `visit` call. Use `walker.walk(root, visitor)` for complete operations;
calling `accept` directly dispatches one node and does not reset or traverse.

Visitor instances can be reused sequentially across trees and after mutations.
They are mutable and are not thread-safe; concurrent operations need separate
instances. Do not mutate the tree during a walk. If a predicate or arithmetic
operation throws, discard that run's partial results; the next walk resets them.

## Paths, depth and integrity

Directory-root paths omit the chosen root's name and use `/`. An attached subtree
can be a new root; paths and depth restart there. For a standalone file root,
search returns its filename. `TreeSet` sorts search paths lexicographically using
Java's natural, case-sensitive string order, independent of child insertion order.
Search combines `Predicate<String>` over the basename with `size >= minimumSize`.

Root depth is zero. Each edge adds one, including edges to empty directories.
Statistics count the directory root and all empty directories. Sizes count files
only; overflow raises `ArithmeticException` rather than wrapping the total.

Names must be non-blank path segments, excluding `.`, `..`, `/`, `\` and NUL.
Sibling names are case-sensitive and unique across both element types. File sizes
and search minimum sizes are non-negative. Invalid values or tree mutations raise
`IllegalArgumentException`; null required arguments raise `NullPointerException`.
Validation completes before ownership or child collections change.

`DirectoryNode.add` rejects a child that already has a parent, itself and its
ancestors. `remove` releases ownership, allowing a subtree to move to another
directory. `children()` and search results return immutable snapshots.

## Extension boundary

`StatisticsVisitor` was added after size, search and their tests. That commit adds
only the new visitor, its immutable result record and its tests. `FileNode`,
`DirectoryNode`, their `accept` methods and the existing `Visitor` contract stay
unchanged. New operations implement the existing two overloads and optional
lifecycle hooks, keeping counting and searching out of the elements.

A new element type is more expensive: it needs an `accept` implementation, a new
`Visitor.visit` overload, an implementation in each existing visitor, and a rule
in the walker dispatch adapter. Visitor favors adding operations over adding
element types. The extra adapter keeps traversal in one owner.

## Clean Code

| Practice | Code reference | Reason |
| --- | --- | --- |
| Meaningful naming | `minimumSize`, `relativePath`, `maximumDepth` | Names state the units or role without explanatory comments. |
| Single Responsibility | `FileSystemWalker`, `TotalSizeVisitor`, `SearchVisitor`, `StatisticsVisitor` | Traversal and independent operations change separately. |
| Small methods | `FileNode.accept`, `SearchVisitor.visit`, `StatisticsVisitor.enter` | Each method performs one short, directly readable action. |
| Encapsulation | `DirectoryNode.add/remove/children`, `FileSystemNode` | Controlled mutations preserve ownership; snapshots protect collections. |
| No type-check chains | Both `accept` methods and `FileSystemWalker.Dispatch` | Overloaded dispatch selects behavior through the type system. |

## Verification

`mvn test` runs seven JUnit 5 test classes:

| Test class | Coverage |
| --- | --- |
| `DemoTest` | Exact default output, changed search options and invalid arguments |
| `VisitorDispatchTest` | Correct overloads and single-node `accept` behavior |
| `TotalSizeVisitorTest` | Nested totals, zero-byte nodes and overflow |
| `SearchVisitorTest` | Both filters, inclusive boundary, ordering, relative paths and snapshots |
| `TreeIntegrityTest` | Invalid names and sizes, duplicates, ownership, cycles and reparenting |
| `FileSystemWalkerTest` | Exactly-once traversal, context, repeat runs and deep nesting |
| `StatisticsVisitorTest` | Counts, directory roots, depth, empty nodes and reuse |

Repository: https://github.com/Asylstyle/assignment6-visitor-pattern
