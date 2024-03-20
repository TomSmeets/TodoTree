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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import nl.tsmeets.todotree.model.Node;
import nl.tsmeets.todotree.model.Settings;
import nl.tsmeets.todotree.model.Tree;
import nl.tsmeets.todotree.view.NodeView;
import nl.tsmeets.todotree.view.SimplePopupMenu;

public class MainActivity extends Activity {
    private static final int INTENT_CODE_EXPORT_CSV = 1;
    private static final int INTENT_CODE_IMPORT_CSV = 2;

    // The current state
    public Tree tree;

    // Application Settings
    public Settings settings = new Settings();

    public void insert_node_with_text(String text) {
        tree.focus.insert(new Node(text), settings.insert_top);
        view_node();
        saveData();
    }

    // Show the 'add node' dialog for the currently focused node
    public void show_insert_dialog() {
        final EditText text = new EditText(this);
        text.setHint(R.string.insert_hint);
        AlertDialog d = new AlertDialog.Builder(this)
                .setTitle(R.string.insert_title)
                .setView(text)
                // "ok" inserts a single item
                .setPositiveButton(R.string.dialog_ok, (dialog, which) -> {
                    insert_node_with_text(text.getText().toString());
                })

                // "next" inserts the item and continues to the next item
                .setNeutralButton(R.string.dialog_next, (dialog, which) -> {
                    insert_node_with_text(text.getText().toString());
                    show_insert_dialog();
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .create();

        // prevent pressing outside the dialog from dismissing
        // only the cancel button should do this
        d.setCancelable(false);

        // try to get they keyboard to show, with focus
        text.requestFocus();
        d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        d.show();
    }


    public void remove_node_with_prompt(Node node) {
        if (node.parent == null)
            return;

        int child_count = node.child_count_total();

        Runnable action = () -> {
            Node parent = node.parent;
            node.detach();
            view_node(parent);
            saveData();
        };

        if (child_count > 0) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_confirmation)
                    .setMessage(getString(R.string.button_remove_confirm, child_count))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(R.string.dialog_yes, (dialog, which) -> action.run())
                    .setNegativeButton(R.string.dialog_no, null)
                    .show();
        } else {
            action.run();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.main_list_button_add).setOnClickListener(l -> show_insert_dialog());


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
            tree.paste(settings);
            view_node();
            saveData();
        });

        findViewById(R.id.main_list_button_more).setOnClickListener(v -> {
            SimplePopupMenu menu = new SimplePopupMenu(this, v);

            menu.add(getString(R.string.setting_scale), () -> {
                SimplePopupMenu m = new SimplePopupMenu(this, v);
                int[] scales = { 40, 60, 80, 100, 120, 140, 160, 200};
                for (int scale : scales) {
                    m.add(String.format("%d%%", scale), () -> {
                        settings.ui_scale = (float) scale / 100;
                        view_node();
                    });
                }
                m.show();
            });

            if (settings.insert_top) {
                menu.add(getString(R.string.setting_insert_bottom), () -> {
                    settings.insert_top = false;
                });
            } else {
                menu.add(getString(R.string.setting_insert_top), () -> {
                    settings.insert_top = true;
                });
            }

            menu.add(getString(R.string.menu_export), () -> {
                SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("text/comma-separated-values");
                intent.putExtra(Intent.EXTRA_TITLE, "TodoTree-data-" + f.format(new Date()) + ".csv");
                startActivityForResult(intent, INTENT_CODE_EXPORT_CSV);
            });

            menu.add(getString(R.string.menu_import), () -> {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.dialog_confirmation)
                        .setMessage(R.string.menu_import_warning)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(R.string.dialog_ok, (dialog, which) -> {
                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                            intent.setType("text/comma-separated-values");
                            startActivityForResult(intent, INTENT_CODE_IMPORT_CSV);
                        })
                        .setNegativeButton(R.string.dialog_cancel, null)
                        .show();
            });

            menu.add(getString(R.string.menu_remove_done), () -> {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.dialog_confirmation)
                        .setMessage(R.string.menu_remove_done_confirm)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(R.string.dialog_yes, (dialog, which) -> {
                            for (Node n : tree.focus.child_list()) {
                                if (n.state == 1) n.detach();
                            }
                            view_node();
                        })
                        .setNegativeButton(R.string.dialog_no, null)
                        .show();
            });

            menu.add(getString(R.string.menu_remove_tree), () -> {
                remove_node_with_prompt(tree.focus);
            });
            menu.show();
        });

        tree = new Tree();
        loadData();
        view_node(tree.root);
    }

    public File get_save_file(String name) {
        return new File(getFilesDir(), name);
    }

    public void saveData() {
        tree.save(get_save_file("data.csv"));
        settings.save(get_save_file("data_settings.csv"));
    }

    public void loadData() {
        tree.load(get_save_file("data.csv"));
        settings.load(get_save_file("data_settings.csv"));
    }

    @Override
    protected void onPause() {
        Log.d(getString(R.string.app_name), "PAUSE");
        saveData();
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

        LinearLayout list = findViewById(R.id.main_list);

        // clear view
        list.removeAllViews();

        // calculate the display size based on the dpi
        float dpi = getResources().getDisplayMetrics().scaledDensity;
        int size = (int) (115.0f / 3.5 * dpi * settings.ui_scale);

        int i = 0;
        for (Node n : node.parents())
            add_node(list, n, size, false, true, i++);

        add_node(list, node, size, true, true, i++);

        for (Node n : node.child_list())
            add_node(list, n, size, false, false, i++);

        int yank_count = tree.yank.child_count();
        Button paste_button = findViewById(R.id.main_list_button_paste);
        if (yank_count > 0) {
            paste_button.setText(getString(R.string.button_place_quantity, tree.yank.child_count()));
        } else {
            paste_button.setText(R.string.button_place);
        }
    }

    public void add_node(LinearLayout layout, Node n, int size, boolean editable, boolean is_parent, int index) {
        new NodeView(this, layout, n, size, editable, is_parent, index);
    }

    private static void stream_copy(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[16 * 1024]; // 16 KB
        for(;;) {
            int len = input.read(buffer);
            if(len <= 0) break;
            output.write(buffer, 0, len);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == INTENT_CODE_IMPORT_CSV && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            try {
                File temp_file = get_save_file("data.csv.tmp");
                InputStream input  = getContentResolver().openInputStream(uri);
                OutputStream output = new FileOutputStream(temp_file);
                stream_copy(input, output);
                input.close();
                output.close();
                tree.load(temp_file);
                temp_file.delete();
                view_node();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (requestCode == INTENT_CODE_EXPORT_CSV && resultCode == RESULT_OK) {
            saveData();
            Uri uri = data.getData();
            try {
                File f = get_save_file("data.csv");
                byte[] buffer = new byte[(int) f.length()];

                FileInputStream input = new FileInputStream(f);
                input.read(buffer);
                input.close();

                OutputStream output = getContentResolver().openOutputStream(uri);
                output.write(buffer);
                output.close();
            } catch (IOException e) {
                Toast.makeText(this, getString(R.string.dialog_error), Toast.LENGTH_SHORT).show();
            }
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
