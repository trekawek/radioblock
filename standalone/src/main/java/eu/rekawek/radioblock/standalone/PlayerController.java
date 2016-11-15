package eu.rekawek.radioblock.standalone;

import eu.rekawek.analyzer.AnalysisListener;
import eu.rekawek.radioblock.standalone.view.PlayerTrayIcon;
import eu.rekawek.radioblock.standalone.view.PlayerWindow;

import javax.sound.sampled.LineUnavailableException;
import javax.swing.*;
import java.io.IOException;

public class PlayerController implements PlayerWindow.PlayerWindowListener, PlayerTrayIcon.PlayerTrayListener {

    private final Player player;

    private final PlayerWindow window;

    private final PlayerTrayIcon trayIcon;

    private final float[][] xcorrResults = new float[2][15];

    private final int[] xcorrResultIndex = new int[2];

    private final PlayerPrefs prefs;

    private int nextJingle;

    public PlayerController(PlayerPrefs prefs) throws IOException, LineUnavailableException {
        this.prefs = prefs;
        player = new Player(prefs);
        window = new PlayerWindow(prefs);
        window.setListener(this);
        trayIcon = new PlayerTrayIcon(prefs.isShowWindow());
        trayIcon.setListener(this);

        this.player.addListener(new AnalysisListener() {
            @Override
            public void gotJingle(String id, int index, int[] levels) {
                trayIcon.setIcon(index == 1);
                clear(xcorrResults[1 - index]);
                updateStats();
            }

            @Override
            public void analysisInProgress(String id, int index, int[] levels) {
                int i = xcorrResultIndex[index] % xcorrResults[index].length;
                xcorrResults[index][i] = levels[0];
                xcorrResultIndex[index] = i + 1;
                nextJingle = index;
                updateStats();
            }
        });
    }

    @Override
    public void startPlayer() {
        window.toggleButton(false);
        trayIcon.toggleButton(false);
        clearStats();
        player.start(() -> {
            JOptionPane.showMessageDialog(new JFrame(), "Can't start streaming.", "Error", JOptionPane.ERROR_MESSAGE);
            stopPlayer();
        });
    }

    @Override
    public void stopPlayer() {
        window.toggleButton(true);
        trayIcon.toggleButton(true);
        player.stop();
    }

    @Override
    public void setWindowVisibility(boolean show) {
        window.setVisible(show);
        prefs.setShowWindow(show);
    }

    @Override
    public void thresholdChanged(int jingleIndex, int newLevel) {
        player.setThreshold(jingleIndex, newLevel);
        if (jingleIndex == 0) {
            prefs.setOpeningThreshold(newLevel);
        } else {
            prefs.setClosingThreshold(newLevel);
        }
    }

    private void clearStats() {
        nextJingle = 0;
        for (int i = 0; i < xcorrResults.length; i++) {
            clear(xcorrResults[i]);
            xcorrResultIndex[i] = 0;
        }
    }

    private void updateStats() {
        window.setNextJingleType(nextJingle);
        window.setRecentMaxValue(0, (int) max(xcorrResults[0]));
        window.setRecentMaxValue(1, (int) max(xcorrResults[1]));
    }

    private void clear(float[] array) {
        for (int i = 0; i < array.length; i++) {
            array[i] = 0;
        }
    }

    private float max(float[] array) {
        Float m = null;
        for (int i = 0; i < array.length; i++) {
            if (m == null || m < array[i]) {
                m = array[i];
            }
        }
        return m == null ? 0 : m;
    }

}
