package nl.tsmeets.todotree.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Node {
    public Node child, next;
    public String text;
    public int state;

    public long ctime;
    public long mtime;

    // not stored
    public Node parent;

    public Node() {
        ctime = System.currentTimeMillis();
        mtime = ctime;
    }

    public void detach() {
        assert this.parent != null;

        if(parent.child == this) {
            parent.child = this.next;
        }

        for(Node prev = parent.child; prev != null; prev = prev.next) {
            if(prev.next == this) {
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
        if(n == null) return null;

        for(;;) {
            if (n.next == null) return n;
            n = n.next;
        }
    }

    public void append_node(Node node) {
        assert node.parent == null;

        node.parent = this;
        if(this.child == null) {
            this.child = node;
            return;
        }

        Node last = this.last_child();
        last.next = node;
    }

    public int child_count() {
        int count = 0;
        for(Node child = this.child; child != null; child = child.next) {
            count++;
        }
        return count;
    }

    public int total_child_count() {
        int count = 0;
        for(Node child = this.child; child != null; child = child.next) {
            count++;
            count += child.total_child_count();
        }
        return count;
    }

    public List<Node> parents() {
        List<Node> l = new ArrayList<>();
        for(Node p = this.parent; p != null; p = p.parent)
            l.add(p);

        Collections.reverse(l);
        return l;
    }

    public List<Node> children() {
        List<Node> l = new ArrayList<>();
        for(Node child = this.child; child != null; child = child.next) {
            l.add(child);
        }
        return l;
    }
}