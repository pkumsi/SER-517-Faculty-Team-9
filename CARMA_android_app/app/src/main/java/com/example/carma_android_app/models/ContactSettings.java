package com.example.carma_android_app.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class for contact settings (whitelist/blacklist)
 */
public class ContactSettings {

    private List<String> whitelistedContacts;
    private List<String> blacklistedContacts;
    private boolean autoRespondEnabled;

    public ContactSettings() {
        this.whitelistedContacts = new ArrayList<>();
        this.blacklistedContacts = new ArrayList<>();
        this.autoRespondEnabled = true;
    }

    public void addToWhitelist(String contact) {
        if (!whitelistedContacts.contains(contact)) {
            whitelistedContacts.add(contact);
        }
    }

    public void removeFromWhitelist(String contact) {
        whitelistedContacts.remove(contact);
    }

    public void addToBlacklist(String contact) {
        if (!blacklistedContacts.contains(contact)) {
            blacklistedContacts.add(contact);
        }
    }

    public void removeFromBlacklist(String contact) {
        blacklistedContacts.remove(contact);
    }

    public boolean isWhitelisted(String contact) {
        return whitelistedContacts.contains(contact);
    }

    public boolean isBlacklisted(String contact) {
        return blacklistedContacts.contains(contact);
    }

    // Getters
    public List<String> getWhitelistedContacts() {
        return whitelistedContacts;
    }

    public List<String> getBlacklistedContacts() {
        return blacklistedContacts;
    }

    public boolean isAutoRespondEnabled() {
        return autoRespondEnabled;
    }

    public void setAutoRespondEnabled(boolean enabled) {
        this.autoRespondEnabled = enabled;
    }
}
