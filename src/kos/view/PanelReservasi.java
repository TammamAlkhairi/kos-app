package kos.view;

import kos.model.Kamar;
import kos.model.Penghuni;
import kos.model.Reservasi;
import kos.service.KosManager;
import kos.util.Format;
import kos.util.KosException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Reservasi: check-in penghuni ke kamar kosong (dengan rincian biaya) dan check-out. */
public class PanelReservasi extends JPanel {
    private final KosManager manager;
    private final Runnable setelahDataBerubah;

    private final JComboBox<Penghuni> cbPenghuni = new JComboBox<>();
    private final JComboBox<Kamar> cbKamar = new JComboBox<>();
    private final JTextField txtTgl = new JTextField(Format.tanggal(LocalDate.now()), 12);
    private final JTextArea areaRincian = new JTextArea(9, 26);
    private final JButton btnCheckIn = new JButton("Check-In");
    private final JButton btnCheckOut = new JButton("Check-Out Penghuni Terpilih");
    private final DefaultTableModel model = UiUtil.modelTabel(
            "ID", "Penghuni", "Kamar", "Tgl Masuk", "Tgl Keluar", "Status");
    private final JTable tabel = UiUtil.buatTabel(model);
    private List<Reservasi> tampil;

    public PanelReservasi(KosManager manager, Runnable setelahDataBerubah) {
        this.manager = manager;
        this.setelahDataBerubah = setelahDataBerubah;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // WEST: form check-in
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Check-In ke Kamar Kosong"));
        UiUtil.barisForm(form, 0, "Penghuni", cbPenghuni);
        UiUtil.barisForm(form, 1, "Kamar Kosong", cbKamar);
        UiUtil.barisForm(form, 2, "Tanggal Masuk", txtTgl);
        areaRincian.setEditable(false);
        areaRincian.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        areaRincian.setBorder(BorderFactory.createTitledBorder("Rincian biaya per bulan"));
        JPanel kiri = new JPanel(new BorderLayout(0, 8));
        kiri.add(form, BorderLayout.NORTH);
        kiri.add(areaRincian, BorderLayout.CENTER);
        kiri.add(btnCheckIn, BorderLayout.SOUTH);
        kiri.setPreferredSize(new Dimension(380, 0));
        add(kiri, BorderLayout.WEST);

        // CENTER: tabel riwayat reservasi
        UiUtil.lebarKolom(tabel, 40, 150, 70, 100, 100, 80);
        UiUtil.rendererStatus(tabel, 5);
        add(new JScrollPane(tabel), BorderLayout.CENTER);
        JPanel bawah = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bawah.add(btnCheckOut);
        add(bawah, BorderLayout.SOUTH);

        cbPenghuni.setName("cbPenghuni"); cbKamar.setName("cbKamarKosong");
        btnCheckIn.setName("btnCheckIn"); btnCheckOut.setName("btnCheckOut"); txtTgl.setName("txtTglMasuk");

        cbKamar.addActionListener(e -> tampilkanRincian());
        btnCheckIn.addActionListener(e -> checkIn());
        btnCheckOut.addActionListener(e -> checkOut());
    }

    private void tampilkanRincian() {
        Kamar k = (Kamar) cbKamar.getSelectedItem();
        if (k == null) { areaRincian.setText("Tidak ada kamar kosong."); return; }
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s%n%n", k.getNamaTipe() + " " + k.getNomor()));
        sb.append(String.format("%-24s%12s%n", "Tarif dasar", Format.rupiah(k.getTarifDasar())));
        for (Map.Entry<String, Double> b : k.rincianUtilitas().entrySet()) {
            sb.append(String.format("%-24s%12s%n", "+ " + b.getKey(), Format.rupiah(b.getValue())));
        }
        sb.append(String.format("%-24s%12s%n", "------------------------", "------------"));
        sb.append(String.format("%-24s%12s", "TOTAL / BULAN", Format.rupiah(k.hitungTotalBulanan())));
        areaRincian.setText(sb.toString());
    }

    private void checkIn() {
        try {
            LocalDate tgl = Format.parseTanggal(txtTgl.getText());
            Kamar k = (Kamar) cbKamar.getSelectedItem();
            manager.checkIn((Penghuni) cbPenghuni.getSelectedItem(), k, tgl);
            UiUtil.info(this, "Check-in berhasil. Kamar " + k.getNomor() + " kini berstatus TERISI.");
            setelahDataBerubah.run();
        } catch (KosException ex) {
            UiUtil.error(this, ex.getMessage());
        }
    }

    private void checkOut() {
        int baris = tabel.getSelectedRow();
        if (baris < 0) { UiUtil.error(this, "Pilih reservasi aktif pada tabel terlebih dahulu."); return; }
        Reservasi r = tampil.get(baris);
        if (!UiUtil.konfirmasi(this, "Check-out " + r.getNamaPenghuni() + " dari kamar " + r.getNomorKamar() + "?")) return;
        try {
            manager.checkOut(r, LocalDate.now());
            setelahDataBerubah.run();
        } catch (KosException ex) {
            UiUtil.error(this, ex.getMessage());
        }
    }

    public void refresh() {
        cbPenghuni.removeAllItems();
        for (Penghuni p : manager.penghuniTanpaKamar()) cbPenghuni.addItem(p);
        cbKamar.removeAllItems();
        for (Kamar k : manager.kamarKosong()) cbKamar.addItem(k);
        tampilkanRincian();

        tampil = manager.semuaReservasi();
        model.setRowCount(0);
        for (Reservasi r : tampil) {
            model.addRow(new Object[]{r.getId(), r.getNamaPenghuni(), r.getNomorKamar(),
                    Format.tanggal(r.getTglMulai()), Format.tanggal(r.getTglSelesai()),
                    r.isAktif() ? "AKTIF" : "SELESAI"});
        }
    }
}
