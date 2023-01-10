package nl.tsmeets.todotree;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import nl.tsmeets.todotree.model.Node;
import nl.tsmeets.todotree.model.Tree;
import nl.tsmeets.todotree.store.Store;
import nl.tsmeets.todotree.store.Util;
import nl.tsmeets.todotree.view.NodeView;
import nl.tsmeets.todotree.view.SimplePopupMenu;

public class MainActivity extends Activity {
    public Tree tree;
    public Store store;

    private static final int INTENT_CODE_EXPORT_CSV = 1;

    private boolean node_is_below(Node child, Node parent) {
        if (child == null) return false;
        if (child == parent) return true;
        return node_is_below(child.parent, parent);
    }

    private void validate() {
        assert tree.focus != null;
        assert node_is_below(tree.focus, tree.root);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.main_list_button_add).setOnClickListener(l -> {
            final EditText text = new EditText(this);
            text.setHint("text");
            AlertDialog d = new AlertDialog.Builder(this)
                    .setTitle("Node Text")
                    .setView(text)
                    .setPositiveButton("Ok", (dialog, which) -> {
                        Node n = new Node();
                        n.text = text.getText().toString();
                        tree.focus.prepend_node(n);
                        view_node();
                        saveData();
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                    })
                    .create();
            text.requestFocus();
            d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            d.show();
        });

        findViewById(R.id.main_list_button_del).setOnClickListener(l -> {
            Node node = tree.focus;
            if (node.parent != null) {
                int child_count = node.child_count();

                Runnable action = () -> {
                    Node parent = node.parent;
                    node.detach();
                    view_node(parent);
                    saveData();
                };

                if (child_count > 0) {
                    new AlertDialog.Builder(this)
                            .setTitle("Confirm")
                            .setMessage("Are you sure you want to remove this node with " + child_count + " children ?")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton("Yes", (dialog, which) -> action.run())
                            .setNegativeButton("No", null)
                            .show();
                } else {
                    action.run();
                }
            }
        });

        findViewById(R.id.main_list_button_yank).setOnClickListener(l -> {
            if (tree.focus.parent != null) {
                Node n = tree.focus;
                tree.focus = n.parent;
                tree.yank(n);
                view_node();
                saveData();
            }
        });


        findViewById(R.id.main_list_button_paste).setOnClickListener(l -> {
            tree.paste();
            view_node();
            saveData();
        });

        findViewById(R.id.main_list_button_more).setOnClickListener(v -> {
            SimplePopupMenu menu = new SimplePopupMenu(this, v);

            menu.add("Export Data", () -> {
                SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("text/csv");
                intent.putExtra(Intent.EXTRA_TITLE, "TodoTree-data-" + f.format(new Date()) + ".csv");
                startActivityForResult(intent, INTENT_CODE_EXPORT_CSV);
            });

            menu.add("Remove Done", () -> {
                new AlertDialog.Builder(this)
                        .setTitle("Confirm")
                        .setMessage("Do you want to remove all DONE nodes?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton("Yes", (dialog, which) -> {
                            for (Node n : tree.focus.children()) {
                                if (n.state == 1) n.detach();
                            }
                            view_node();
                        })
                        .setNegativeButton("No", null)
                        .show();
            });
            menu.show();
        });

        tree = new Tree();
        store = new Store();

        String data = Util.read_file_to_string(get_save_file());
        if (data != null && !data.isEmpty()) tree = store.load(data);

        view_node(tree.root);
    }

    public File get_save_file() {
        return new File(getFilesDir(), "data.csv");
    }

    public void saveData() {
        File temp_save_file = new File(getFilesDir(), "data-new.csv");
        Util.write_string_to_file(temp_save_file, new Store().store(tree));
        temp_save_file.renameTo(get_save_file());
    }

    @Override
    protected void onPause() {
        Log.d("TodoTree", "PAUSE");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d("TodoTree", "STOP");
        saveData();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d("TodoTree", "DESTROY");
        saveData();
        super.onDestroy();
    }

    public void view_node() {
        view_node(tree.focus);
    }

    public void view_node(Node node) {
        tree.focus = node;
        validate();

        LinearLayout list = findViewById(R.id.main_list);

        // clear view
        list.removeAllViews();

        // calculate the display size based on the dpi
        float dpi = getResources().getDisplayMetrics().scaledDensity;
        int size = (int) (130.0f / 3.5 * dpi);

        for (Node n : node.parents())
            add_node(list, n, size, false, true);
        add_node(list, node, size, true, true);

        for (Node n : node.children()) {
            add_node(list, n, size, false, false);
        }

        int yank_count = tree.yank.child_count();
        Button paste_button = findViewById(R.id.main_list_button_paste);
        if (yank_count > 0) {
            paste_button.setText("Place (" + tree.yank.child_count() + ")");
        } else {
            paste_button.setText("Place");
        }
    }

    public void add_node(LinearLayout layout, Node n, int size, boolean editable, boolean is_parent) {
        new NodeView(this, layout, n, size, editable, is_parent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == INTENT_CODE_EXPORT_CSV && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            try {
                OutputStream output = getContentResolver().openOutputStream(uri);
                output.write(store.store(tree).getBytes());
                output.flush();
                output.close();
            } catch (IOException e) {
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
