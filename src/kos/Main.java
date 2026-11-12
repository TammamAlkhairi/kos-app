package kos;

import kos.service.KosManager;
import kos.util.KosException;
import kos.view.MainFrame;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Titik masuk aplikasi. */
public class Main {
    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) { UIManager.setLookAndFeel(info.getClassName()); break; }
            }
        } catch (Exception ignored) { /* pakai tampilan bawaan */ }

        SwingUtilities.invokeLater(() -> {
            KosManager manager = new KosManager();
            try {
                manager.muatSemua();            // uji koneksi sekaligus muat data awal
            } catch (KosException e) {
                JOptionPane.showMessageDialog(null,
                        e.getMessage() + "\n\nPastikan MySQL aktif dan skrip sql/db_kos.sql sudah dijalankan.",
                        "Koneksi Database Gagal", JOptionPane.ERROR_MESSAGE);
                return;
            }
            new MainFrame(manager).setVisible(true);
        });
    }
}
