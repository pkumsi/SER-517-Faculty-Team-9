package com.example.carma_android_app;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.carma_android_app.utils.Constants;
import com.example.carma_android_app.utils.PreferencesManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * SettingsActivity — lets the user enable/disable each default auto-response
 * rule and add/remove custom rules. Changes are persisted via PreferencesManager
 * and sent to the backend with the next auto-response request.
 */
public class SettingsActivity extends AppCompatActivity {

    private LinearLayout containerDefaultRules;
    private LinearLayout containerCustomRules;
    private EditText etCustomRule;
    private MaterialButton btnAddRule;
    private MaterialButton btnSave;

    private final CheckBox[] defaultRuleCheckBoxes = new CheckBox[Constants.DEFAULT_RULES.length];
    private final List<String> customRulesList = new ArrayList<>();

    private PreferencesManager prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = PreferencesManager.getInstance(this);

        containerDefaultRules = findViewById(R.id.container_default_rules);
        containerCustomRules  = findViewById(R.id.container_custom_rules);
        etCustomRule          = findViewById(R.id.et_custom_rule);
        btnAddRule            = findViewById(R.id.btn_add_rule);
        btnSave               = findViewById(R.id.btn_save_rules);

        buildDefaultRuleRows();
        loadCustomRules();
        buildCustomRuleRows();

        btnAddRule.setOnClickListener(v -> addCustomRule());
        btnSave.setOnClickListener(v -> saveAndFinish());
    }

    // ── Default rules ─────────────────────────────────────────────────────────

    private void buildDefaultRuleRows() {
        Set<Integer> disabled = prefs.getDisabledRuleIndices();
        for (int i = 0; i < Constants.DEFAULT_RULES.length; i++) {
            CheckBox cb = new CheckBox(this);
            cb.setText(Constants.DEFAULT_RULES[i]);
            cb.setChecked(!disabled.contains(i));
            cb.setTextSize(13f);
            cb.setPadding(8, 12, 8, 12);
            defaultRuleCheckBoxes[i] = cb;
            containerDefaultRules.addView(cb);
        }
    }

    // ── Custom rules ──────────────────────────────────────────────────────────

    private void loadCustomRules() {
        customRulesList.clear();
        customRulesList.addAll(prefs.getCustomRules());
    }

    private void buildCustomRuleRows() {
        containerCustomRules.removeAllViews();
        if (customRulesList.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No custom rules added yet.");
            empty.setTextSize(13f);
            empty.setPadding(8, 8, 8, 8);
            containerCustomRules.addView(empty);
            return;
        }
        for (int i = 0; i < customRulesList.size(); i++) {
            containerCustomRules.addView(buildCustomRuleRow(customRulesList.get(i), i));
        }
    }

    private View buildCustomRuleRow(String rule, int index) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(8, 8, 8, 8);

        TextView tv = new TextView(this);
        tv.setText(rule);
        tv.setTextSize(13f);
        LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        tv.setLayoutParams(tvParams);

        ImageButton btnDelete = new ImageButton(this);
        btnDelete.setImageResource(android.R.drawable.ic_delete);
        btnDelete.setBackgroundResource(android.R.color.transparent);
        btnDelete.setContentDescription("Delete rule");
        btnDelete.setOnClickListener(v -> {
            customRulesList.remove(index);
            buildCustomRuleRows();
        });

        row.addView(tv);
        row.addView(btnDelete);
        return row;
    }

    private void addCustomRule() {
        String text = etCustomRule.getText().toString().trim();
        if (TextUtils.isEmpty(text)) {
            Toast.makeText(this, "Rule cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        customRulesList.add(text);
        etCustomRule.setText("");
        buildCustomRuleRows();
    }

    // ── Save ──────────────────────────────────────────────────────────────────

    private void saveAndFinish() {
        // Collect disabled default rule indices
        Set<Integer> disabled = new HashSet<>();
        for (int i = 0; i < defaultRuleCheckBoxes.length; i++) {
            if (!defaultRuleCheckBoxes[i].isChecked()) disabled.add(i);
        }
        prefs.setDisabledRuleIndices(disabled);

        // Persist custom rules
        prefs.setCustomRules(new HashSet<>(customRulesList));

        Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show();
        finish();
    }
}
