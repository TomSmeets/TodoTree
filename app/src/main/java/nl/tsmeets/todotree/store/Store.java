package nl.tsmeets.todotree.store;

import java.util.ArrayList;
import java.util.List;

import nl.tsmeets.todotree.model.Node;
import nl.tsmeets.todotree.model.Tree;

public class Store {
    public String store(Tree t) {
        StringBuilder sb = new StringBuilder();
        store(sb, t);
        return sb.toString();
    }


    private int id_counter = 0;

    public void store(StringBuilder s, Tree t) {
        id_counter = 0;
        s.append("id;");
        s.append("child;");
        s.append("next;");
        s.append("text;");
        s.append("state;");
        s.append('\n');
        t.root.next = t.yank;
        store(s, t.root);
    }

    public int store(StringBuilder s, Node n) {
        int child_id = n.child == null ? 0 : store(s, n.child);
        int next_id  = n.next  == null ? 0 : store(s, n.next);

        int node_id = ++id_counter;
        s.append(node_id);
        s.append(';');
        s.append(child_id);
        s.append(';');
        s.append(next_id);
        s.append(';');
        Util.write_csv_string(s, n.text);
        s.append(';');
        s.append(n.state);
        s.append(';');
        s.append('\n');
        return node_id;
    }

    public Tree load(String s) {
        List<Node> nodes = new ArrayList<>();
        nodes.add(null);

        String[] lines = s.split("\n");
        for(int i = 1; i < lines.length; ++i) {
            String[] args = lines[i].split(";");
            if(args.length < 5) continue;

            int j = 0;
            int id    = Integer.parseInt(args[j++]);
            int child = Integer.parseInt(args[j++]);
            int next  = Integer.parseInt(args[j++]);

            assert child < id;
            assert next  < id;
            assert(nodes.size() == id);

            Node n = new Node();
            n.child = nodes.get(child);
            n.next  = nodes.get(next);
            n.text  = Util.read_csv_string(args[j++]);
            n.state = Integer.parseInt(args[j++]);

            for(Node c = n.child; c != null; c = c.next)
                c.parent = n;
            nodes.add(n);
            assert j == args.length;
        }

        Tree t = new Tree();
        t.root = nodes.get(nodes.size()-1);
        if(t.root.next != null)
            t.yank = t.root.next;
        return t;
    }
}
