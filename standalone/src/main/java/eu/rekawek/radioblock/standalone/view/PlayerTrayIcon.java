package eu.rekawek.radioblock.standalone.view;

import eu.rekawek.radioblock.standalone.PlayerController;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;

public class PlayerTrayIcon {

    private static final Image NORMAL_ICON = getIcon("icon215x215.png");

    private static final Image MUTE_ICON = getIcon("icon215x215-mute.png");

    private final TrayIcon icon;

    private final MenuItem startItem;

    private final MenuItem stopItem;

    private final CheckboxMenuItem showWindowItem;

    private PlayerTrayListener listener = new PlayerTrayListener() {};

    public PlayerTrayIcon(boolean windowVisible) {
        icon = new TrayIcon(NORMAL_ICON, "Radioblock");

        startItem = new MenuItem("Start");
        startItem.setActionCommand("start");
        startItem.setEnabled(true);
        startItem.addActionListener(e -> listener.startPlayer());

        stopItem = new MenuItem("Stop");
        stopItem.setActionCommand("stop");
        stopItem.setEnabled(false);
        stopItem.addActionListener(e -> listener.stopPlayer());

        showWindowItem = new CheckboxMenuItem("Show window");
        showWindowItem.setState(windowVisible);
        showWindowItem.addItemListener(e -> listener.setWindowVisibility(showWindowItem.getState()));

        MenuItem quitItem = new MenuItem("Quit");
        quitItem.setActionCommand("quit");
        quitItem.addActionListener(e -> System.exit(0));

        final PopupMenu popup = new PopupMenu();
        popup.add(startItem);
        popup.add(stopItem);
        popup.add(showWindowItem);
        popup.add(quitItem);

        icon.setPopupMenu(popup);

        try {
            SystemTray.getSystemTray().add(icon);
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
    }

    public void toggleButton(boolean startEnabled) {
        startItem.setEnabled(startEnabled);
        stopItem.setEnabled(!startEnabled);
        icon.setImage(NORMAL_ICON);
    }

    public void setIcon(boolean normal) {
        icon.setImage(normal ? NORMAL_ICON : MUTE_ICON);
    }

    public void setListener(PlayerTrayListener listener) {
        this.listener = listener;
    }

    private static Image getIcon(String name) {
        try {
            BufferedImage trayIconImage = ImageIO.read(PlayerController.class.getResource("/" + name));
            int trayIconWidth = new TrayIcon(trayIconImage).getSize().width;
            return trayIconImage.getScaledInstance(trayIconWidth, -1, Image.SCALE_SMOOTH);
        } catch(IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public interface PlayerTrayListener {

        default void startPlayer() {
        }

        default void stopPlayer() {
        }

        default void setWindowVisibility(boolean show) {
        }
    }

}
