package kos.view;

import kos.service.KosManager;
import kos.util.KosException;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

/** Jendela utama: header + JTabbedPane berisi seluruh modul + status bar. */
public class MainFrame extends JFrame {
    private final KosManager manager;
    private final JTabbedPane tabs = new JTabbedPane();
    private final JLabel lblStatusBar = new JLabel(" ");

    private final PanelDenah panelDenah;
    private final PanelKamar panelKamar;
    private final PanelPenghuni panelPenghuni;
    private final PanelReservasi panelReservasi;
    private final PanelTagihan panelTagihan;

    public MainFrame(KosManager manager) {
        super("Sistem Reservasi & Manajemen Kos-kosan");
        this.manager = manager;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 680));
        setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UiUtil.BIRU);
        header.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        JLabel judul = new JLabel("KOS SEJAHTERA - Reservasi & Pengelolaan Kamar");
        judul.setForeground(Color.WHITE);
        judul.setFont(judul.getFont().deriveFont(Font.BOLD, 18f));
        header.add(judul, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        Runnable segarkan = this::refreshSemua;   // dipanggil setiap data berubah
        panelDenah = new PanelDenah(manager);
        panelKamar = new PanelKamar(manager, segarkan);
        panelPenghuni = new PanelPenghuni(manager, segarkan);
        panelReservasi = new PanelReservasi(manager, segarkan);
        panelTagihan = new PanelTagihan(manager, segarkan);

        tabs.addTab("Denah Kamar", panelDenah);
        tabs.addTab("Data Kamar", panelKamar);
        tabs.addTab("Penghuni", panelPenghuni);
        tabs.addTab("Reservasi", panelReservasi);
        tabs.addTab("Tagihan & Tunggakan", panelTagihan);
        tabs.setName("tabUtama");
        add(tabs, BorderLayout.CENTER);

        lblStatusBar.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        add(lblStatusBar, BorderLayout.SOUTH);

        refreshSemua();
        pack();
        setLocationRelativeTo(null);
    }

    public JTabbedPane getTabs() { return tabs; }

    /** Memuat ulang data dari DB lalu menyegarkan semua panel. */
    public void refreshSemua() {
        try {
            manager.muatSemua();
        } catch (KosException e) {
            UiUtil.error(this, e.getMessage());
        }
        panelDenah.refresh();
        panelKamar.refresh();
        panelPenghuni.refresh();
        panelReservasi.refresh();
        panelTagihan.refresh();
        lblStatusBar.setText("Terhubung ke database  |  " + manager.jumlahKamar() + " kamar, "
                + manager.jumlahTerisi() + " terisi, " + (manager.jumlahKamar() - manager.jumlahTerisi()) + " kosong");
    }
}
