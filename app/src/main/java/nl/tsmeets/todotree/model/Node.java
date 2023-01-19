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

    public void insert_after_me(Node node) {
        assert node.parent == null;
        assert node.next   == null;
        node.next = this.next;
        node.parent = this.parent;
        this.next = node;
    }

    public int child_count() {
        int count = 0;
        for(Node child = this.child; child != null; child = child.next) {
            count++;
        }
        return count;
    }

    public int child_todo_count() {
        int count = 0;
        for(Node child = this.child; child != null; child = child.next) {
            if(child.state == 0)
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

    public void update_children_states() {
        Node current;

        for (current = this; current != null; current = current.next) {
            for (Node c : current.children()) {
                c.state = this.state;
                c.update_children_states();
            }
        }
    }

    public void update_parents_states(){
        Node current;

        for (current = this; current.parent != null; current = current.parent) {
            boolean subtree_done = true;
            for (Node c : current.parent.children()) {
                if (c.state == 0) {
                    subtree_done = false;
                    break;
                }
            }
            if (subtree_done)
                current.parent.state = 1;
            else
                current.parent.state = 0;
        }
    }
}