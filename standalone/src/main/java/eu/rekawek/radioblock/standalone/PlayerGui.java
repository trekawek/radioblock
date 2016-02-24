package eu.rekawek.radioblock.standalone;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

public class PlayerGui extends JPanel implements ActionListener {

    private static final long serialVersionUID = -1162567178177329163L;

    private final JButton startButton;

    private final JButton stopButton;

    private final Player player;

    public PlayerGui(Player player) {
        this.player = player;

        startButton = new JButton("Start");
        startButton.setActionCommand("start");
        startButton.setEnabled(true);

        stopButton = new JButton("Stop");
        stopButton.setActionCommand("stop");
        stopButton.setEnabled(false);

        // Listen for actions on buttons 1 and 3.
        startButton.addActionListener(this);
        stopButton.addActionListener(this);

        // Add Components to this container, using the default FlowLayout.
        add(startButton);
        add(stopButton);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if ("start".equals(e.getActionCommand())) {
            player.start();
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
        } else {
            player.stop();
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
        }
    }
}
