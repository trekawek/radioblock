package eu.rekawek.radioblock.standalone;

import java.util.prefs.Preferences;

public class PlayerPrefs {

    private static final String SHOW_WINDOW_PROP = "showWindow";

    private static final String OPENING_THRESHOLD_PROP = "openingThreshold";

    private static final String CLOSING_THRESHOLD_PROP = "closingThreshold";

    private final Preferences prefs;

    public PlayerPrefs() {
        prefs = Preferences.userRoot().node(PlayerPrefs.class.getName());
    }

    public void setShowWindow(boolean showWindow) {
        prefs.putBoolean(SHOW_WINDOW_PROP, showWindow);
    }

    public void setOpeningThreshold(int openingThreshold) {
        prefs.putInt(OPENING_THRESHOLD_PROP, openingThreshold);
    }

    public void setClosingThreshold(int closingThreshold) {
        prefs.putInt(CLOSING_THRESHOLD_PROP, closingThreshold);
    }

    public boolean isShowWindow() {
        return prefs.getBoolean(SHOW_WINDOW_PROP, true);
    }

    public int getOpeningThreshold() {
        return prefs.getInt(OPENING_THRESHOLD_PROP, 500);
    }

    public int getClosingThreshold() {
        return prefs.getInt(CLOSING_THRESHOLD_PROP, 550);
    }
}
