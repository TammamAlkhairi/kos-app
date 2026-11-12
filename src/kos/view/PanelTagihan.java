package kos.view;

import kos.model.Tagihan;
import kos.service.KosManager;
import kos.util.Format;
import kos.util.KosException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

/** Tagihan sewa + pelaporan tunggakan harian dan bulanan. */
public class PanelTagihan extends JPanel {
    private final KosManager manager;
    private final Runnable setelahDataBerubah;

    // sub-tab 1: semua tagihan
    private final JTextField txtPeriode = new JTextField(YearMonth.now().toString(), 8);
    private final JButton btnBuat = new JButton("Buat Tagihan Periode (YYYY-MM)");
    private final JButton btnBayar = new JButton("Tandai Lunas (hari ini)");
    private final DefaultTableModel modelSemua = UiUtil.modelTabel(
            "ID", "Periode", "Kamar", "Penghuni", "Jumlah", "Jatuh Tempo", "Tgl Bayar", "Status");
    private final JTable tabelSemua = UiUtil.buatTabel(modelSemua);
    private List<Tagihan> tampilSemua;

    // sub-tab 2: tunggakan harian
    private final JTextField txtTanggal = new JTextField(Format.tanggal(LocalDate.now()), 10);
    private final JButton btnTampil = new JButton("Tampilkan");
    private final DefaultTableModel modelHarian = UiUtil.modelTabel(
            "Kamar", "Penghuni", "Periode", "Jatuh Tempo", "Terlambat", "Jumlah");
    private final JTable tabelHarian = UiUtil.buatTabel(modelHarian);
    private final JLabel lblTotalHarian = new JLabel(" ");

    // sub-tab 3: rekap bulanan
    private final DefaultTableModel modelBulanan = UiUtil.modelTabel(
            "Periode", "Jml Tagihan", "Belum Lunas", "Total Tagihan", "Sudah Dibayar", "Tunggakan");
    private final JTable tabelBulanan = UiUtil.buatTabel(modelBulanan);
    private final JLabel lblTotalBulanan = new JLabel(" ");

    public PanelTagihan(KosManager manager, Runnable setelahDataBerubah) {
        this.manager = manager;
        this.setelahDataBerubah = setelahDataBerubah;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTabbedPane sub = new JTabbedPane();
        sub.addTab("Semua Tagihan", panelSemua());
        sub.addTab("Tunggakan Harian", panelHarian());
        sub.addTab("Rekap Tunggakan Bulanan", panelBulanan());
        sub.setName("subTabTagihan");
        add(sub, BorderLayout.CENTER);

        btnBuat.addActionListener(e -> buatTagihan());
        btnBayar.addActionListener(e -> bayar());
        btnTampil.addActionListener(e -> muatHarian());
        txtTanggal.addActionListener(e -> muatHarian());
        txtTanggal.setName("txtTanggalLaporan"); btnTampil.setName("btnTampilHarian");
        btnBuat.setName("btnBuatTagihan");
    }

    private JPanel panelSemua() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JPanel atas = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        atas.add(new JLabel("Periode:"));
        atas.add(txtPeriode);
        atas.add(btnBuat);
        atas.add(btnBayar);
        p.add(atas, BorderLayout.NORTH);
        UiUtil.lebarKolom(tabelSemua, 40, 80, 60, 150, 110, 100, 100, 110);
        UiUtil.rataKanan(tabelSemua, 4);
        UiUtil.rendererStatus(tabelSemua, 7);
        p.add(new JScrollPane(tabelSemua), BorderLayout.CENTER);
        return p;
    }

    private JPanel panelHarian() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JPanel atas = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        atas.add(new JLabel("Per tanggal (dd-MM-yyyy):"));
        atas.add(txtTanggal);
        atas.add(btnTampil);
        p.add(atas, BorderLayout.NORTH);
        UiUtil.rataKanan(tabelHarian, 4, 5);
        p.add(new JScrollPane(tabelHarian), BorderLayout.CENTER);
        lblTotalHarian.setFont(lblTotalHarian.getFont().deriveFont(Font.BOLD, 14f));
        lblTotalHarian.setForeground(UiUtil.MERAH_TUA);
        p.add(lblTotalHarian, BorderLayout.SOUTH);
        return p;
    }

    private JPanel panelBulanan() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        UiUtil.rataKanan(tabelBulanan, 1, 2, 3, 4, 5);
        p.add(new JScrollPane(tabelBulanan), BorderLayout.CENTER);
        lblTotalBulanan.setFont(lblTotalBulanan.getFont().deriveFont(Font.BOLD, 14f));
        lblTotalBulanan.setForeground(UiUtil.MERAH_TUA);
        p.add(lblTotalBulanan, BorderLayout.SOUTH);
        return p;
    }

    private void buatTagihan() {
        try {
            YearMonth periode;
            try {
                periode = YearMonth.parse(txtPeriode.getText().trim());
            } catch (java.time.format.DateTimeParseException e) {
                throw new KosException("Periode harus berformat YYYY-MM, contoh 2026-11.");
            }
            int baru = manager.buatTagihanBulanan(periode);
            UiUtil.info(this, baru + " tagihan baru dibuat untuk periode " + periode + ".");
            setelahDataBerubah.run();
        } catch (KosException ex) {
            UiUtil.error(this, ex.getMessage());
        }
    }

    private void bayar() {
        int baris = tabelSemua.getSelectedRow();
        if (baris < 0) { UiUtil.error(this, "Pilih tagihan pada tabel terlebih dahulu."); return; }
        try {
            manager.bayarTagihan(tampilSemua.get(baris), LocalDate.now());
            setelahDataBerubah.run();
        } catch (KosException ex) {
            UiUtil.error(this, ex.getMessage());
        }
    }

    private void muatHarian() {
        try {
            LocalDate acuan = Format.parseTanggal(txtTanggal.getText());
            modelHarian.setRowCount(0);
            double total = 0;
            List<Tagihan> daftar = manager.tunggakanHarian(acuan);
            for (Tagihan t : daftar) {
                long telat = t.hariTerlambat(acuan);
                modelHarian.addRow(new Object[]{t.getNomorKamar(), t.getNamaPenghuni(), t.getPeriode(),
                        Format.tanggal(t.getJatuhTempo()), telat == 0 ? "Jatuh tempo hari ini" : telat + " hari",
                        Format.rupiah(t.getJumlah())});
                total += t.getJumlah();
            }
            lblTotalHarian.setText(daftar.size() + " tagihan menunggak per " + Format.tanggal(acuan)
                    + "  |  Total tunggakan: " + Format.rupiah(total));
        } catch (KosException ex) {
            UiUtil.error(this, ex.getMessage());
        }
    }

    private void muatBulanan() {
        modelBulanan.setRowCount(0);
        double totalTunggakan = 0;
        for (KosManager.Rekap r : manager.rekapBulanan(LocalDate.now()).values()) {
            modelBulanan.addRow(new Object[]{r.periode, r.jumlahTagihan, r.jumlahBelumLunas,
                    Format.rupiah(r.totalTagihan), Format.rupiah(r.totalDibayar), Format.rupiah(r.tunggakan)});
            totalTunggakan += r.tunggakan;
        }
        lblTotalBulanan.setText("Total tunggakan seluruh periode per " + Format.tanggal(LocalDate.now())
                + ": " + Format.rupiah(totalTunggakan));
    }

    public void refresh() {
        tampilSemua = manager.semuaTagihan();
        modelSemua.setRowCount(0);
        LocalDate hariIni = LocalDate.now();
        for (Tagihan t : tampilSemua) {
            String status = t.isLunas() ? "LUNAS" : (t.isTunggakan(hariIni) ? "TERLAMBAT" : "BELUM LUNAS");
            modelSemua.addRow(new Object[]{t.getId(), t.getPeriode(), t.getNomorKamar(), t.getNamaPenghuni(),
                    Format.rupiah(t.getJumlah()), Format.tanggal(t.getJatuhTempo()),
                    Format.tanggal(t.getTglBayar()), status});
        }
        muatHarian();
        muatBulanan();
    }
}
