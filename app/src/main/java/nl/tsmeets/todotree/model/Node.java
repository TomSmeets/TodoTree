package nl.tsmeets.todotree.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base class for the tree Node
 * The tree is built entirely out of these nodes.
 * The nodes are linked together with their 'child' and 'next' pointers.
 */
public class Node {
    // The very first child of this node
    // > child.parent == this
    public Node child;

    // The next sibling of this node
    // > next.parent == this.parent
    public Node next;

    // The text should be displayed for this node
    public String text;

    // Node state
    // 0 = not checked
    // 1 = checked
    public int state;

    // Pointer to the parent node.
    // This pointer is not stored and is here only for performance reasons
    public Node parent;

    public Node() { }

    public void detach() {
        assert this.parent != null;

        if (parent.child == this) {
            parent.child = this.next;
        }

        for (Node prev = parent.child; prev != null; prev = prev.next) {
            if (prev.next == this) {
                prev.next = this.next;
                break;
            }
        }

        this.next = null;
        this.parent = null;
    }

    public void prepend_node(Node node) {
        assert node.parent == null;

        node.next = this.child;
        this.child = node;

        node.parent = this;
    }

    public Node last_child() {
        Node n = this.child;
        if (n == null) return null;

        for (; ; ) {
            if (n.next == null) return n;
            n = n.next;
        }
    }

    public void append_node(Node node) {
        assert node.parent == null;

        node.parent = this;
        if (this.child == null) {
            this.child = node;
            return;
        }

        Node last = this.last_child();
        last.next = node;
    }

    public void insert_after_me(Node node) {
        assert node.parent == null;
        assert node.next == null;
        node.next = this.next;
        node.parent = this.parent;
        this.next = node;
    }

    public int child_count() {
        int count = 0;
        for (Node child = this.child; child != null; child = child.next) {
            count++;
        }
        return count;
    }

    public int child_todo_count() {
        int count = 0;
        for (Node child = this.child; child != null; child = child.next) {
            if (child.state == 0) count++;
        }
        return count;
    }

    public int total_child_count() {
        int count = 0;
        for (Node child = this.child; child != null; child = child.next) {
            count++;
            count += child.total_child_count();
        }
        return count;
    }

    public List<Node> parents() {
        List<Node> l = new ArrayList<>();
        for (Node p = this.parent; p != null; p = p.parent)
            l.add(p);

        Collections.reverse(l);
        return l;
    }

    public List<Node> children() {
        List<Node> l = new ArrayList<>();
        for (Node child = this.child; child != null; child = child.next) {
            l.add(child);
        }
        return l;
    }
}