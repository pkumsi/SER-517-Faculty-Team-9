package com.example.carma_android_app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashSet;
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
}
