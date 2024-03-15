package nl.tsmeets.todotree.view;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import nl.tsmeets.todotree.MainActivity;
import nl.tsmeets.todotree.R;
import nl.tsmeets.todotree.model.Node;

public class NodeView implements TextWatcher, View.OnClickListener, View.OnDragListener, View.OnLongClickListener {
    private final Node node;

    private final MainActivity ctx;
    private final LinearLayout row;
    private final TextView text;
    private ImageView checkbox;
    private TextView count;

    public NodeView(MainActivity ctx, LinearLayout layout, Node node, int size, boolean editable, boolean is_parent, int index) {
        this.node = node;
        this.row = new LinearLayout(ctx);
        this.ctx = ctx;
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setMinimumHeight(size);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackground(null);

        if (editable) {
            text = new EditText(ctx);
            text.addTextChangedListener(this);
        } else {
            text = new TextView(ctx);
            text.setOnClickListener(this);
        }

        text.setOnDragListener(this);
        if (!editable && !is_parent) {
            text.setOnLongClickListener(this);
        }

        int pad_size = (int) (size*0.4f);
        text.setPadding(20, pad_size, 0, pad_size);
        text.setBackground(null);
        text.setTextSize(TypedValue.COMPLEX_UNIT_PX, size * 0.50f);
        text.setGravity(Gravity.CENTER_VERTICAL);
        text.setText(node.text);
        text.setMinimumHeight(size);
        row.addView(text, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));

        int child_count = node.child_count();
        if(child_count > 0) {
            this.count = new TextView(ctx);
            count.setPadding(0, 0, 10, 0);
            count.setText(Integer.toString(child_count));
            count.setTextSize(TypedValue.COMPLEX_UNIT_PX, size*0.5f);
            count.setOnClickListener(this);
            count.setHeight(size);
            count.setGravity(Gravity.CENTER_VERTICAL);
            row.addView(count);
        }

        if (child_count == 0) {
            this.checkbox = new ImageView(ctx);
            checkbox.setBackground(null);
            checkbox.setScaleType(ImageView.ScaleType.FIT_CENTER);
            checkbox.setAdjustViewBounds(true);
            checkbox.setOnClickListener(this);
            checkbox.setScaleX(0.8f);
            checkbox.setScaleY(0.8f * ((float) size / (float) (size + pad_size*2)));
            checkbox.setAlpha(0.5f);
            row.addView(checkbox, size, size + pad_size*2);
        }

        ImageView pad = new ImageView(ctx);
        row.addView(pad, 20, size);

        if (is_parent) {
            row.setBackgroundResource(R.color.color1);
        } else {
            row.setBackgroundResource(index % 2 == 0 ? R.color.black : R.color.black2);
        }

        update(ctx);
        layout.addView(row);
    }

    public void update(Context ctx) {
        if (this.checkbox != null) {
            if (node.state == 0) checkbox.setBackgroundResource(R.drawable.box_empty_dark);
            if (node.state == 1) checkbox.setBackgroundResource(R.drawable.box_check_dark);
        }

        if (node.state == 0) text.setAlpha(1.0f);
        if (node.state == 1) text.setAlpha(0.4f);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        node.text = s.toString();
    }

    @Override
    public void onClick(View v) {
        if (v == checkbox) {
            node.state = (node.state + 1) % 2;
            update(ctx);
            ctx.saveData();
        }

        if (v == text || v == count) {
            ctx.view_node(node);
        }
    }

    @Override
    public boolean onDrag(View v, DragEvent event) {
        if (event.getAction() == DragEvent.ACTION_DROP) {
            Node drag = (Node) event.getLocalState();
            if (drag == null) return false;
            if (drag == node) {

            } else if (drag.parent == node.parent) {
                drag.detach();
                node.insert_after_me(drag);
            } else if (drag.parent == node) {
                drag.detach();
                node.prepend_node(drag);
            }
            ctx.view_node();
            return true;
        }

        return true;
    }

    @Override
    public boolean onLongClick(View v) {
        Log.d("TodoTree", "LONG " + node.text);
        String tag = node.text;

        // NOTE: there is still a bug, if we drag outside the window
        // the node stays green
        row.setBackgroundColor(0xff006000);
        View.DragShadowBuilder shadow = new View.DragShadowBuilder(row);
        ClipData.Item item = new ClipData.Item(tag);
        ClipData clip = new ClipData(tag, new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN}, item);
        v.startDragAndDrop(clip, shadow, node, View.DRAG_FLAG_OPAQUE);
        return true;
    }
}
