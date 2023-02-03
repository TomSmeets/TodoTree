package nl.tsmeets.todotree.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import nl.tsmeets.todotree.store.CSVFileFormat;

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

    private int save_id_counter;

    public void save(File file) {
        CSVFileFormat f = new CSVFileFormat();
        f.write_begin(file, "id", "child", "next", "text", "state");
        save_id_counter = 0;
        root.next = yank;
        yank.next = null;
        save_impl(f, root);

        f.write_end();
    }

    public int save_impl(CSVFileFormat f, Node n) {
        int child_id = n.child == null ? 0 : save_impl(f, n.child);
        int next_id  = n.next  == null ? 0 : save_impl(f, n.next);

        int node_id = ++save_id_counter;
        f.write_value(node_id);
        f.write_value(child_id);
        f.write_value(next_id);
        f.write_value(n.text);
        f.write_value(n.state);
        f.write_next();
        return node_id;
    }

    public void load(File file) {
        CSVFileFormat f = new CSVFileFormat();
        String[] hdr = f.read_begin(file);
        assert hdr.length == 5;

        List<Node> nodes = new ArrayList<>();

        // noe id '0' is invalid
        nodes.add(null);

        for(;;) {
            int id    = f.read_int();
            int child = f.read_int();
            int next  = f.read_int();

            assert child < id;
            assert next  < id;
            assert nodes.size() == id;

            Node n = new Node();
            n.child = nodes.get(child);
            n.next  = nodes.get(next);
            n.text  = f.read_string();
            n.state = f.read_int();

            // set 'parent' pointer for each child
            for(Node c = n.child; c != null; c = c.next)
                c.parent = n;

            // finally add to nodes
            nodes.add(n);
            if(!f.read_next()) break;
        }

        // last is root node
        this.root  = nodes.get(nodes.size()-1);
        this.yank  = root.next;
        this.focus = root;
    }
}
