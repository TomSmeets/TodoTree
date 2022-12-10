package nl.tsmeets.todotree.model;

import java.util.ArrayList;
import java.util.Collections;

public class Tree {
    public Node root;
    public Node yank;
    public Node focus;

    public Tree() {
        root = new Node();
        root.text = "root";

        yank = new Node();
        yank.text = "yank";

        root.next = yank;
        focus = root;
    }

    public void yank(Node n) {
        n.detach();
        yank.prepend_node(n);
    }

    public void paste() { paste(this.focus); }

    public void paste(Node parent) {
        Node n = yank.child;
        if(n == null) return;
        n.detach();
        parent.append_node(n);
    }
}
