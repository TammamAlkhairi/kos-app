package kos.view;

import kos.model.Kamar;
import kos.model.KamarFactory;
import kos.model.StatusKamar;
import kos.service.KosManager;
import kos.util.Format;
import kos.util.KosException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * PERTEMUAN 11 & 12 - Antarmuka utama "Data Kamar".
 *
 * Layout : BorderLayout (filter di NORTH, tabel di CENTER, form di EAST);
 *          form memakai GridBagLayout, tombol aksi memakai GridLayout.
 * Event  : ActionListener (tombol, combo), MouseListener (klik baris tabel),
 *          KeyListener/KeyAdapter (pencarian & hitung estimasi biaya saat mengetik).
 */
public class PanelKamar extends JPanel {
    private final KosManager manager;
    private final Runnable setelahDataBerubah;

    // --- komponen filter (NORTH)
    private final JTextField txtCari = new JTextField(10);
    private final JComboBox<String> cbFilterTipe = new JComboBox<>(
            new String[]{"Semua Tipe", "Kamar AC", "Kamar Non-AC", "Kamar Ensuite"});
    private final JComboBox<String> cbFilterStatus = new JComboBox<>(new String[]{"Semua Status", "KOSONG", "TERISI"});

    // --- komponen tabel (CENTER)
    private final DefaultTableModel modelTabel = UiUtil.modelTabel(
            "Nomor", "Lantai", "Tipe", "Tarif Dasar", "Utilitas", "Total/Bulan", "Wi-Fi", "Status");
    private final JTable tabel = UiUtil.buatTabel(modelTabel);

    // --- komponen form (EAST)
    private final JTextField txtNomor = new JTextField(12);
    private final JSpinner spLantai = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
    private final JComboBox<String> cbTipe = new JComboBox<>(KamarFactory.LABEL);
    private final JTextField txtTarif = new JTextField(12);
    private final JCheckBox chkWifi = new JCheckBox("Berlangganan Wi-Fi");
    private final JLabel lblStatus = new JLabel("KOSONG");
    private final JLabel lblUtilitas = new JLabel("-");
    private final JLabel lblTotal = new JLabel("-");
    private final JButton btnTambah = new JButton("Tambah");
    private final JButton btnUbah = new JButton("Ubah");
    private final JButton btnHapus = new JButton("Hapus");
    private final JButton btnBersihkan = new JButton("Bersihkan");

    private boolean sedangMengisiForm = false;

    public PanelKamar(KosManager manager, Runnable setelahDataBerubah) {
        this.manager = manager;
        this.setelahDataBerubah = setelahDataBerubah;
        susunTampilan();
        pasangEventListener();
        bersihkanForm();
    }

    // ================================================================= VIEW (P11)
    private void susunTampilan() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // NORTH: baris filter & pencarian
        JPanel filter = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filter.add(new JLabel("Cari nomor:"));
        filter.add(txtCari);
        filter.add(new JLabel("Tipe:"));
        filter.add(cbFilterTipe);
        filter.add(new JLabel("Status:"));
        filter.add(cbFilterStatus);
        add(filter, BorderLayout.NORTH);

        // CENTER: tabel data kamar
        UiUtil.lebarKolom(tabel, 70, 60, 120, 110, 100, 120, 60, 80);
        UiUtil.rataKanan(tabel, 3, 4, 5);
        UiUtil.rendererStatus(tabel, 7);
        add(new JScrollPane(tabel), BorderLayout.CENTER);

        // EAST: form detail kamar (GridBagLayout) + tombol (GridLayout)
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Detail Kamar"));
        UiUtil.barisForm(form, 0, "Nomor Kamar", txtNomor);
        UiUtil.barisForm(form, 1, "Lantai", spLantai);
        UiUtil.barisForm(form, 2, "Jenis Kamar", cbTipe);
        UiUtil.barisForm(form, 3, "Tarif Dasar (Rp)", txtTarif);
        UiUtil.barisForm(form, 4, "Wi-Fi", chkWifi);
        UiUtil.barisForm(form, 5, "Status", lblStatus);
        UiUtil.barisForm(form, 6, "Utilitas / bulan", lblUtilitas);
        UiUtil.barisForm(form, 7, "Total / bulan", lblTotal);
        UiUtil.penutupForm(form, 8);
        lblTotal.setFont(lblTotal.getFont().deriveFont(Font.BOLD));
        lblStatus.setFont(lblStatus.getFont().deriveFont(Font.BOLD));

        JPanel tombol = new JPanel(new GridLayout(2, 2, 8, 8));
        tombol.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        tombol.add(btnTambah);
        tombol.add(btnUbah);
        tombol.add(btnHapus);
        tombol.add(btnBersihkan);

        JPanel timur = new JPanel(new BorderLayout());
        timur.add(form, BorderLayout.CENTER);
        timur.add(tombol, BorderLayout.SOUTH);
        timur.setPreferredSize(new Dimension(340, 0));
        add(timur, BorderLayout.EAST);

        // nama komponen (dipakai skrip pengujian/screenshot)
        txtNomor.setName("txtNomor"); txtTarif.setName("txtTarif"); txtCari.setName("txtCari");
        cbTipe.setName("cbTipe"); cbFilterTipe.setName("cbFilterTipe"); chkWifi.setName("chkWifi");
        btnTambah.setName("btnTambahKamar"); btnUbah.setName("btnUbahKamar");
        btnHapus.setName("btnHapusKamar"); btnBersihkan.setName("btnBersihkanKamar");
        tabel.setName("tblKamar"); spLantai.setName("spLantai");
    }

    // ============================================================ CONTROLLER (P12)
    private void pasangEventListener() {
        // 1) ActionListener pada tombol
        btnTambah.addActionListener(e -> tambahKamar());
        btnUbah.addActionListener(e -> ubahKamar());
        btnHapus.addActionListener(e -> hapusKamar());
        btnBersihkan.addActionListener(e -> bersihkanForm());

        // 2) ActionListener pada combo: saring tabel / usulkan tarif
        cbFilterTipe.addActionListener(e -> muatTabel());
        cbFilterStatus.addActionListener(e -> muatTabel());
        cbTipe.addActionListener(e -> {
            if (!sedangMengisiForm && txtNomor.isEditable()) {
                txtTarif.setText(String.valueOf((long) KamarFactory.TARIF_USULAN[cbTipe.getSelectedIndex()]));
            }
            hitungEstimasi();
        });
        chkWifi.addActionListener(e -> hitungEstimasi());

        // 3) MouseListener: klik baris tabel -> isi form
        tabel.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { isiFormDariBaris(); }
        });

        // 4) KeyAdapter: cari saat mengetik & hitung estimasi saat tarif diubah
        txtCari.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { muatTabel(); }
        });
        txtTarif.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { hitungEstimasi(); }
        });
    }

    /** Event: tombol Tambah ditekan. */
    private void tambahKamar() {
        try {
            Kamar baru = bacaForm(StatusKamar.KOSONG);          // 1. ambil input dari komponen
            manager.tambahKamar(baru);                          // 2. validasi + simpan (koleksi & DB)
            UiUtil.info(this, "Kamar " + baru.getNomor() + " berhasil ditambahkan.");
            bersihkanForm();
            setelahDataBerubah.run();                           // 3. perbarui tabel & denah
        } catch (KosException ex) {
            UiUtil.error(this, ex.getMessage());
        }
    }

    private void ubahKamar() {
        String nomor = txtNomor.getText().trim();
        Kamar lama = manager.cariKamar(nomor);
        if (lama == null) {
            UiUtil.error(this, "Pilih kamar pada tabel terlebih dahulu.");
            return;
        }
        try {
            manager.ubahKamar(bacaForm(lama.getStatus()));
            UiUtil.info(this, "Data kamar " + nomor + " berhasil diubah.");
            setelahDataBerubah.run();
        } catch (KosException ex) {
            UiUtil.error(this, ex.getMessage());
        }
    }

    private void hapusKamar() {
        String nomor = txtNomor.getText().trim();
        if (manager.cariKamar(nomor) == null) {
            UiUtil.error(this, "Pilih kamar pada tabel terlebih dahulu.");
            return;
        }
        if (!UiUtil.konfirmasi(this, "Hapus kamar " + nomor + "?")) return;
        try {
            manager.hapusKamar(nomor);
            bersihkanForm();
            setelahDataBerubah.run();
        } catch (KosException ex) {
            UiUtil.error(this, ex.getMessage());
        }
    }

    /** Event: baris tabel diklik -> salin isi baris ke form. */
    private void isiFormDariBaris() {
        int baris = tabel.getSelectedRow();
        if (baris < 0) return;
        String nomor = (String) modelTabel.getValueAt(tabel.convertRowIndexToModel(baris), 0);
        Kamar k = manager.cariKamar(nomor);
        if (k == null) return;
        sedangMengisiForm = true;
        txtNomor.setText(k.getNomor());
        txtNomor.setEditable(false);                      // nomor = primary key, tidak boleh diubah
        spLantai.setValue(k.getLantai());
        for (int i = 0; i < KamarFactory.KODE.length; i++) {
            if (KamarFactory.KODE[i].equals(k.getKodeTipe())) cbTipe.setSelectedIndex(i);
        }
        txtTarif.setText(String.valueOf((long) k.getTarifDasar()));
        chkWifi.setSelected(k.isWifiAktif());
        lblStatus.setText(k.getStatus().name());
        sedangMengisiForm = false;
        hitungEstimasi();
    }

    private void bersihkanForm() {
        sedangMengisiForm = true;
        txtNomor.setText("");
        txtNomor.setEditable(true);
        spLantai.setValue(1);
        cbTipe.setSelectedIndex(0);
        txtTarif.setText(String.valueOf((long) KamarFactory.TARIF_USULAN[0]));
        chkWifi.setSelected(true);
        lblStatus.setText("KOSONG");
        tabel.clearSelection();
        sedangMengisiForm = false;
        hitungEstimasi();
    }

    /** Menghitung estimasi biaya secara polimorfik dari objek Kamar sementara. */
    private void hitungEstimasi() {
        try {
            Kamar sementara = bacaForm(StatusKamar.KOSONG);
            lblUtilitas.setText(Format.rupiah(sementara.hitungBiayaUtilitas()));
            lblTotal.setText(Format.rupiah(sementara.hitungTotalBulanan()));
        } catch (KosException | NumberFormatException ex) {
            lblUtilitas.setText("-");
            lblTotal.setText("-");
        }
    }

    private Kamar bacaForm(StatusKamar status) throws KosException {
        double tarif;
        try {
            tarif = Double.parseDouble(txtTarif.getText().trim().replace(".", "").replace(",", ""));
        } catch (NumberFormatException e) {
            throw new KosException("Tarif dasar harus berupa angka.");
        }
        return KamarFactory.buat(KamarFactory.KODE[cbTipe.getSelectedIndex()],
                txtNomor.getText().trim().toUpperCase(), (Integer) spLantai.getValue(),
                tarif, chkWifi.isSelected(), status);
    }

    // ============================================================== isi tabel
    /** Mengisi ulang tabel dengan memperhatikan pencarian & filter. */
    public void muatTabel() {
        String cari = txtCari.getText().trim().toUpperCase();
        int tipe = cbFilterTipe.getSelectedIndex();           // 0 = semua
        String status = (String) cbFilterStatus.getSelectedItem();
        modelTabel.setRowCount(0);
        for (Kamar k : manager.kamarTerurut()) {
            if (!cari.isEmpty() && !k.getNomor().toUpperCase().contains(cari)) continue;
            if (tipe > 0 && !k.getNamaTipe().equals(cbFilterTipe.getSelectedItem())) continue;
            if (!status.startsWith("Semua") && !k.getStatus().name().equals(status)) continue;
            modelTabel.addRow(new Object[]{
                    k.getNomor(), k.getLantai(), k.getNamaTipe(),
                    Format.rupiah(k.getTarifDasar()), Format.rupiah(k.hitungBiayaUtilitas()),
                    Format.rupiah(k.hitungTotalBulanan()), k.isWifiAktif() ? "Ya" : "Tidak",
                    k.getStatus().name()});
        }
    }

    public void refresh() { muatTabel(); }
}
