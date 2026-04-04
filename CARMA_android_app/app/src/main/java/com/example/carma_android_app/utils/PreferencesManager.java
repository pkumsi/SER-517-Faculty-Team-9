package com.example.carma_android_app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.example.carma_android_app.utils.Constants;

public class PreferencesManager {

    private static PreferencesManager instance;
    private SharedPreferences preferences;

    private PreferencesManager(Context context) {
        preferences = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized PreferencesManager getInstance(Context context) {
        if (instance == null) {
            instance = new PreferencesManager(context.getApplicationContext());
        }
        return instance;
    }

    // Auto-respond settings
    public void setAutoRespondEnabled(boolean enabled) {
        preferences.edit().putBoolean(Constants.KEY_AUTO_RESPOND_ENABLED, enabled).apply();
    }

    public boolean isAutoRespondEnabled() {
        return preferences.getBoolean(Constants.KEY_AUTO_RESPOND_ENABLED, true);
    }

    // Whitelisted contacts
    public void saveWhitelistedContacts(Set<String> contacts) {
        preferences.edit().putStringSet(Constants.KEY_WHITELISTED_CONTACTS, contacts).apply();
    }

    public Set<String> getWhitelistedContacts() {
        return preferences.getStringSet(Constants.KEY_WHITELISTED_CONTACTS, new HashSet<>());
    }

    public void addWhitelistedContact(String contact) {
        Set<String> contacts = new HashSet<>(getWhitelistedContacts());
        contacts.add(contact);
        saveWhitelistedContacts(contacts);
    }

    public void removeWhitelistedContact(String contact) {
        Set<String> contacts = new HashSet<>(getWhitelistedContacts());
        contacts.remove(contact);
        saveWhitelistedContacts(contacts);
    }

    // Blacklisted contacts
    public void saveBlacklistedContacts(Set<String> contacts) {
        preferences.edit().putStringSet(Constants.KEY_BLACKLISTED_CONTACTS, contacts).apply();
    }

    public Set<String> getBlacklistedContacts() {
        return preferences.getStringSet(Constants.KEY_BLACKLISTED_CONTACTS, new HashSet<>());
    }

    public void addBlacklistedContact(String contact) {
        Set<String> contacts = new HashSet<>(getBlacklistedContacts());
        contacts.add(contact);
        saveBlacklistedContacts(contacts);
    }

    public void removeBlacklistedContact(String contact) {
        Set<String> contacts = new HashSet<>(getBlacklistedContacts());
        contacts.remove(contact);
        saveBlacklistedContacts(contacts);
    }

    // Response tone preference
    public void setPreferredTone(String tone) {
        preferences.edit().putString("preferred_tone", tone).apply();
    }

    public String getPreferredTone() {
        return preferences.getString("preferred_tone", Constants.TONE_CASUAL);
    }

    // Clear all preferences
    public void clearAll() {
        preferences.edit().clear().apply();
    }

    // ── Rules management ──────────────────────────────────────────────────────

    /** Persists the set of rule indices (0-based) that the user has disabled. */
    public void setDisabledRuleIndices(Set<Integer> disabledIndices) {
        Set<String> asStrings = new HashSet<>();
        for (int i : disabledIndices) asStrings.add(String.valueOf(i));
        preferences.edit().putStringSet(Constants.KEY_DISABLED_RULES, asStrings).apply();
    }

    /** Returns the set of rule indices (0-based) that the user has disabled. */
    public Set<Integer> getDisabledRuleIndices() {
        Set<String> raw = preferences.getStringSet(Constants.KEY_DISABLED_RULES, new HashSet<>());
        Set<Integer> result = new HashSet<>();
        for (String s : raw) result.add(Integer.parseInt(s));
        return result;
    }

    /** Persists user-added custom rules. */
    public void setCustomRules(Set<String> customRules) {
        preferences.edit().putStringSet(Constants.KEY_CUSTOM_RULES, customRules).apply();
    }

    /** Returns user-added custom rules. */
    public Set<String> getCustomRules() {
        return preferences.getStringSet(Constants.KEY_CUSTOM_RULES, new HashSet<>());
    }

    /**
     * Returns the effective rule list to send to the backend:
     * enabled default rules (those not in disabledRuleIndices) + all custom rules.
     */
    public List<String> getEffectiveRules() {
        Set<Integer> disabled = getDisabledRuleIndices();
        List<String> result = new ArrayList<>();
        for (int i = 0; i < Constants.DEFAULT_RULES.length; i++) {
            if (!disabled.contains(i)) result.add(Constants.DEFAULT_RULES[i]);
        }
        result.addAll(getCustomRules());
        return result;
    }
}
