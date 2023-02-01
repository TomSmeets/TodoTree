package nl.tsmeets.todotree.model;

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

    public void paste(Settings settings) { paste(this.focus, settings); }

    public void paste(Node parent, Settings settings) {
        Node n = yank.child;
        if(n == null) return;
        n.detach();
        if(settings.insert_top) {
            parent.prepend_node(n);
        } else {
            parent.append_node(n);
        }
    }
}
