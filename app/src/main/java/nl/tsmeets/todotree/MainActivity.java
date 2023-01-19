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

    public void insert_node_with_text(String text) {
        Node n = new Node();
        n.text = text;
        tree.focus.prepend_node(n);
        n.update_parents_states();
        view_node();
        saveData();
    }

    // Show the 'add node' dialog for the currently focused node
    public void show_insert_dialog() {
        final EditText text = new EditText(this);
        text.setHint(R.string.text_hint);
        AlertDialog d = new AlertDialog.Builder(this)
                .setTitle(R.string.add_title)
                .setView(text)
                // "ok" inserts a single item
                .setPositiveButton(R.string.ok_option, (dialog, which) -> {
                    insert_node_with_text(text.getText().toString());
                })

                // "next" inserts the item and continues to the next item
                .setNeutralButton("next", (dialog, which) -> {
                    insert_node_with_text(text.getText().toString());
                    show_insert_dialog();
                })
                .setNegativeButton(R.string.cancel_option, null)
                .create();

        // prevent pressing outside the dialog from dismissing
        // only the cancel button should do this
        d.setCancelable(false);

        // try to get they keyboard to show, with focus
        text.requestFocus();
        d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        d.show();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.main_list_button_add).setOnClickListener(l -> show_insert_dialog());

        findViewById(R.id.main_list_button_del).setOnClickListener(l -> {
            Node node = tree.focus;
            if (node.parent != null) {
                int child_count = node.child_count();

                Runnable action = () -> {
                    Node parent = node.parent;
                    node.detach();
                    parent.child.update_parents_states();
                    view_node(parent);
                    saveData();
                };

                if (child_count > 0) {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.confirm)
                            .setMessage(getString(R.string.remove_confirm, child_count))
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(R.string.yes_option, (dialog, which) -> action.run())
                            .setNegativeButton(R.string.no_option, null)
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
                tree.focus.child.update_parents_states();
                view_node();
                saveData();
            }
        });


        findViewById(R.id.main_list_button_paste).setOnClickListener(l -> {
            tree.paste();
            tree.focus.child.update_parents_states();
            view_node();
            saveData();
        });

        findViewById(R.id.main_list_button_more).setOnClickListener(v -> {
            SimplePopupMenu menu = new SimplePopupMenu(this, v);

            menu.add(getString(R.string.export_menu_item), () -> {
                SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("text/csv");
                intent.putExtra(Intent.EXTRA_TITLE, "TodoTree-data-" + f.format(new Date()) + ".csv");
                startActivityForResult(intent, INTENT_CODE_EXPORT_CSV);
            });

            menu.add(getString(R.string.remove_menu_item), () -> {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.confirm)
                        .setMessage(R.string.remove_done_confirm)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(R.string.yes_option, (dialog, which) -> {
                            for (Node n : tree.focus.children()) {
                                if (n.state == 1) n.detach();
                            }
                            // when you remove done nodes you most likely want to reuse subtree
                            tree.focus.state = 0;
                            view_node();
                        })
                        .setNegativeButton(R.string.no_option, null)
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
        Log.d(getString(R.string.app_name), "PAUSE");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(getString(R.string.app_name), "STOP");
        saveData();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(getString(R.string.app_name), "DESTROY");
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
            paste_button.setText(getString(R.string.place_quantity_button, tree.yank.child_count()));
        } else {
            paste_button.setText(R.string.place_button);
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
                Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show();
            }
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
