package com.example.arrayadapter;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Keys for saved instance state
    private static final String KEY_SELECTED_ID = "selected_id";
    private static final String KEY_CHECKBOX_STATE = "checkbox_state";
    private static final String KEY_CONFIRMATION_VISIBLE = "confirmation_visible";

    // UI components
    private Spinner spinnerCollectionPoint;
    private TextView tvError;
    private CheckBox cbReminder;
    private MaterialButton btnSubmit;
    private MaterialCardView cardConfirmation;
    private TextView tvConfirmLabel;
    private TextView tvConfirmId;
    private TextView tvConfirmReminder;
    private MaterialButton btnReverse;
    private MaterialButton btnRemoveSelected;
    private MaterialButton btnEmptyList;
    private TextView tvEmptyState;

    // Data
    private List<CollectionPoint> collectionPoints;
    private List<CollectionPoint> originalCollectionPoints; // For restore
    private CollectionPointAdapter adapter;
    private int selectedId = -1; // -1 means prompt selected
    private boolean checkboxState = false;
    private boolean confirmationVisible = false;
    private boolean isListEmpty = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        initData();
        setupSpinner();
        setupListeners();

        if (savedInstanceState != null) {
            restoreState(savedInstanceState);
        } else {
            updateUiForDataState();
        }
    }

    private void initViews() {
        spinnerCollectionPoint = findViewById(R.id.spinnerCollectionPoint);
        tvError = findViewById(R.id.tvError);
        cbReminder = findViewById(R.id.cbReminder);
        btnSubmit = findViewById(R.id.btnSubmit);
        cardConfirmation = findViewById(R.id.cardConfirmation);
        tvConfirmLabel = findViewById(R.id.tvConfirmLabel);
        tvConfirmId = findViewById(R.id.tvConfirmId);
        tvConfirmReminder = findViewById(R.id.tvConfirmReminder);
        btnReverse = findViewById(R.id.btnReverse);
        btnRemoveSelected = findViewById(R.id.btnRemoveSelected);
        btnEmptyList = findViewById(R.id.btnEmptyList);
        tvEmptyState = findViewById(R.id.tvEmptyState);
    }

    private void initData() {
        // Create synthetic data with stable IDs
        collectionPoints = new ArrayList<>();
        collectionPoints.add(new CollectionPoint(-1, getString(R.string.spinner_prompt))); // Prompt row
        collectionPoints.add(new CollectionPoint(101, "Karura Forest"));
        collectionPoints.add(new CollectionPoint(102, "Ngong Hills"));
        collectionPoints.add(new CollectionPoint(103, "Aberdare Edge"));
        collectionPoints.add(new CollectionPoint(104, "Kakamega Forest"));
        collectionPoints.add(new CollectionPoint(105, "Mt. Kenya Lower Slopes"));

        // Keep a copy for restore
        originalCollectionPoints = new ArrayList<>(collectionPoints);
    }

    private void setupSpinner() {
        adapter = new CollectionPointAdapter(this, collectionPoints);
        spinnerCollectionPoint.setAdapter(adapter);

        spinnerCollectionPoint.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                CollectionPoint point = adapter.getItem(position);
                if (point != null) {
                    selectedId = point.getId();
                    Log.d("SelectionTrace", "position=" + position + " id=" + point.getId() + " label=" + point.getLabel());
                    // Clear error when user selects a valid item
                    if (selectedId != -1) {
                        tvError.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedId = -1;
            }
        });
    }

    private void setupListeners() {
        // Checkbox is independent of spinner - single-choice vs independent boolean
        cbReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            checkboxState = isChecked;
            updateConfirmationCard();
        });

        btnSubmit.setOnClickListener(v -> handleSubmit());

        btnReverse.setOnClickListener(v -> handleReverse());

        btnRemoveSelected.setOnClickListener(v -> handleRemoveSelected());

        btnEmptyList.setOnClickListener(v -> handleToggleEmptyList());
    }

    private void handleSubmit() {
        if (selectedId == -1) {
            // Show inline error
            tvError.setVisibility(View.VISIBLE);
            cardConfirmation.setVisibility(View.GONE);
            confirmationVisible = false;
        } else {
            tvError.setVisibility(View.GONE);
            confirmationVisible = true;
            updateConfirmationCard();
            cardConfirmation.setVisibility(View.VISIBLE);
        }
    }

    private void updateConfirmationCard() {
        CollectionPoint selectedPoint = findPointById(selectedId);
        if (selectedPoint != null) {
            tvConfirmLabel.setText(getString(R.string.confirmation_label, selectedPoint.getLabel()));
            tvConfirmId.setText(getString(R.string.confirmation_id, selectedPoint.getId()));
            tvConfirmReminder.setText(checkboxState
                    ? getString(R.string.confirmation_reminder_on)
                    : getString(R.string.confirmation_reminder_off));
        }
    }

    private void handleReverse() {
        if (isListEmpty || collectionPoints.size() <= 1) return; // Only prompt or empty

        // Reverse only the real options (skip prompt at index 0)
        List<CollectionPoint> realOptions = new ArrayList<>(collectionPoints.subList(1, collectionPoints.size()));
        Collections.reverse(realOptions);

        collectionPoints = new ArrayList<>();
        collectionPoints.add(new CollectionPoint(-1, getString(R.string.spinner_prompt)));
        collectionPoints.addAll(realOptions);

        rebuildAdapter();
    }

    private void handleRemoveSelected() {
        if (isListEmpty || selectedId == -1) return;

        // Remove the selected item from the list
        collectionPoints.removeIf(point -> point.getId() == selectedId);

        // If we removed the last real option, only prompt remains
        rebuildAdapter();

        // After removal, selectedId may no longer exist in list
        // The spinner will show prompt (position 0) and onItemSelected will fire with id=-1
    }

    private void handleToggleEmptyList() {
        if (isListEmpty) {
            // Restore list
            collectionPoints = new ArrayList<>(originalCollectionPoints);
            isListEmpty = false;
            btnEmptyList.setText(R.string.btn_empty_list);
        } else {
            // Empty list - keep only prompt
            collectionPoints = new ArrayList<>();
            collectionPoints.add(new CollectionPoint(-1, getString(R.string.spinner_prompt)));
            isListEmpty = true;
            btnEmptyList.setText(R.string.btn_restore_list);
        }
        rebuildAdapter();
    }

    private void rebuildAdapter() {
        // Save current selection before rebuilding
        int currentSelectedId = selectedId;

        adapter = new CollectionPointAdapter(this, collectionPoints);
        spinnerCollectionPoint.setAdapter(adapter);

        // Re-select by matching stable ID (not position)
        int positionToSelect = findPositionById(currentSelectedId);
        spinnerCollectionPoint.setSelection(positionToSelect);

        // The onItemSelected will fire and update selectedId
        // If ID not found, it will select prompt (position 0) and selectedId becomes -1

        updateUiForDataState();
    }

    private int findPositionById(int id) {
        for (int i = 0; i < collectionPoints.size(); i++) {
            if (collectionPoints.get(i).getId() == id) {
                return i;
            }
        }
        return 0; // Default to prompt
    }

    private CollectionPoint findPointById(int id) {
        for (CollectionPoint point : collectionPoints) {
            if (point.getId() == id) {
                return point;
            }
        }
        return null;
    }

    private void updateUiForDataState() {
        boolean hasRealOptions = collectionPoints.size() > 1; // More than just prompt

        spinnerCollectionPoint.setEnabled(hasRealOptions);
        btnSubmit.setEnabled(hasRealOptions);
        btnRemoveSelected.setEnabled(hasRealOptions && selectedId != -1);
        btnReverse.setEnabled(hasRealOptions);

        if (isListEmpty || !hasRealOptions) {
            tvEmptyState.setVisibility(View.VISIBLE);
            cardConfirmation.setVisibility(View.GONE);
            confirmationVisible = false;
        } else {
            tvEmptyState.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SELECTED_ID, selectedId);
        outState.putBoolean(KEY_CHECKBOX_STATE, checkboxState);
        outState.putBoolean(KEY_CONFIRMATION_VISIBLE, confirmationVisible);
    }

    private void restoreState(@NonNull Bundle savedInstanceState) {
        selectedId = savedInstanceState.getInt(KEY_SELECTED_ID, -1);
        checkboxState = savedInstanceState.getBoolean(KEY_CHECKBOX_STATE, false);
        confirmationVisible = savedInstanceState.getBoolean(KEY_CONFIRMATION_VISIBLE, false);

        cbReminder.setChecked(checkboxState);

        // Rebuild list first (in case data changed), then find position by ID
        int positionToSelect = findPositionById(selectedId);

        // Check if saved ID still exists in current list
        boolean idExists = findPointById(selectedId) != null;
        if (selectedId != -1 && !idExists) {
            // Previous choice no longer available
            selectedId = -1;
            positionToSelect = 0;
            Toast.makeText(this, R.string.toast_choice_unavailable, Toast.LENGTH_SHORT).show();
        }

        spinnerCollectionPoint.setSelection(positionToSelect);

        if (confirmationVisible && selectedId != -1) {
            updateConfirmationCard();
            cardConfirmation.setVisibility(View.VISIBLE);
        } else {
            cardConfirmation.setVisibility(View.GONE);
        }

        updateUiForDataState();
    }
}