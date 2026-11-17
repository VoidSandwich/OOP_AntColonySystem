package antcolony;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Точка входа в приложение. */
public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Используется стандартное оформление Swing.
            }
            new MainFrame().setVisible(true);
        });
    }
}
