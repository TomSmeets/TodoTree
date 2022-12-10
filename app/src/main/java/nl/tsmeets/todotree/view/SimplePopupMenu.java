package nl.tsmeets.todotree.view;

import android.content.Context;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import java.util.ArrayList;

public class SimplePopupMenu implements PopupMenu.OnMenuItemClickListener {
    PopupMenu inner;
    ArrayList<Runnable> actions = new ArrayList<>();

    public SimplePopupMenu(Context ctx, View view) {
        inner = new PopupMenu(ctx, view);
        inner.setOnMenuItemClickListener(this);
    }

    public void add(String title, Runnable f) {
        int index = actions.size();
        actions.add(f);
        inner.getMenu().add(0, index, 0, title);
    }

    public void show() {
        inner.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        actions.get(item.getItemId()).run();
        return true;
    }
}
