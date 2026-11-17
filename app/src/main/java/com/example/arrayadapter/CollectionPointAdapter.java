package com.example.arrayadapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.List;

/**
 * Custom ArrayAdapter for CollectionPoint objects.
 * 
 * Data flow: source List<CollectionPoint> -> adapter -> row_collection_point.xml -> Spinner
 * 
 * The prompt row (position 0, id = -1) looks different and is disabled in the dropdown.
 */
public class CollectionPointAdapter extends ArrayAdapter<CollectionPoint> {

    private final LayoutInflater inflater;

    public CollectionPointAdapter(@NonNull Context context, @NonNull List<CollectionPoint> objects) {
        super(context, 0, objects);
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createRowView(position, convertView, parent, false);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createRowView(position, convertView, parent, true);
    }

    /**
     * Creates a row view for either the spinner selected view or dropdown view.
     * @param position Position in the adapter
     * @param convertView Recycled view
     * @param parent Parent view group
     * @param isDropDown True if this is a dropdown view, false for selected view
     * @return The configured row view
     */
    private View createRowView(int position, @Nullable View convertView, @NonNull ViewGroup parent, boolean isDropDown) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.row_collection_point, parent, false);
        }

        CollectionPoint point = getItem(position);
        if (point == null) return view;

        TextView iconView = view.findViewById(R.id.icon);
        TextView labelView = view.findViewById(R.id.label);
        TextView idLineView = view.findViewById(R.id.idLine);

        boolean isPrompt = point.getId() == -1;

        if (isPrompt) {
            // Prompt row: grey, italic, no ID line
            labelView.setText(point.getLabel());
            labelView.setTypeface(null, android.graphics.Typeface.ITALIC);
            labelView.setTextColor(ContextCompat.getColor(getContext(), R.color.prompt_text));
            idLineView.setVisibility(View.GONE);
            iconView.setText(""); // No icon for prompt
        } else {
            // Regular collection point
            labelView.setText(point.getLabel());
            labelView.setTypeface(null, android.graphics.Typeface.BOLD);
            labelView.setTextColor(ContextCompat.getColor(getContext(), R.color.label_text));
            idLineView.setText(getContext().getString(R.string.id_format, point.getId()));
            idLineView.setVisibility(View.VISIBLE);
            iconView.setText("🍄");
        }

        // In dropdown, disable the prompt row visually
        if (isDropDown && isPrompt) {
            view.setAlpha(0.5f);
        } else {
            view.setAlpha(1.0f);
        }

        return view;
    }

    /**
     * Disable the prompt row (position 0) in the dropdown so it cannot be selected.
     */
    @Override
    public boolean isEnabled(int position) {
        CollectionPoint point = getItem(position);
        return point != null && point.getId() != -1;
    }
}