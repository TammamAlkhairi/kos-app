package kos.view;

import kos.model.Penghuni;
import kos.service.KosManager;
import kos.util.KosException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Pencatatan identitas penghuni (nama, NIK, kontak, asal, pekerjaan). */
public class PanelPenghuni extends JPanel {
    private final KosManager manager;
    private final Runnable setelahDataBerubah;

    private final JTextField txtNama = new JTextField(14);
    private final JTextField txtNik = new JTextField(14);
    private final JTextField txtHp = new JTextField(14);
    private final JTextField txtAlamat = new JTextField(14);
    private final JTextField txtPekerjaan = new JTextField(14);
    private final DefaultTableModel model = UiUtil.modelTabel(
            "ID", "Nama", "NIK", "No. HP", "Alamat Asal", "Pekerjaan", "Kamar");
    private final JTable tabel = UiUtil.buatTabel(model);
    private final JButton btnSimpan = new JButton("Simpan Baru");
    private final JButton btnUbah = new JButton("Ubah");
    private final JButton btnHapus = new JButton("Hapus");
    private final JButton btnBersihkan = new JButton("Bersihkan");
    private int idTerpilih = -1;

    public PanelPenghuni(KosManager manager, Runnable setelahDataBerubah) {
        this.manager = manager;
        this.setelahDataBerubah = setelahDataBerubah;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        UiUtil.lebarKolom(tabel, 40, 140, 150, 110, 120, 120, 60);
        add(new JScrollPane(tabel), BorderLayout.CENTER);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Identitas Penghuni"));
        UiUtil.barisForm(form, 0, "Nama Lengkap", txtNama);
        UiUtil.barisForm(form, 1, "NIK (16 digit)", txtNik);
        UiUtil.barisForm(form, 2, "No. HP", txtHp);
        UiUtil.barisForm(form, 3, "Alamat Asal", txtAlamat);
        UiUtil.barisForm(form, 4, "Pekerjaan", txtPekerjaan);
        JPanel tombol = new JPanel(new GridLayout(2, 2, 8, 8));
        tombol.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        tombol.add(btnSimpan); tombol.add(btnUbah); tombol.add(btnHapus); tombol.add(btnBersihkan);
        JPanel atas = new JPanel(new BorderLayout());
        atas.add(form, BorderLayout.CENTER);
        atas.add(tombol, BorderLayout.SOUTH);
        JPanel timur = new JPanel(new BorderLayout());
        timur.add(atas, BorderLayout.NORTH);
        timur.setPreferredSize(new Dimension(340, 0));
        add(timur, BorderLayout.EAST);

        txtNama.setName("txtNama"); txtNik.setName("txtNik"); txtHp.setName("txtHp");
        txtAlamat.setName("txtAlamat"); txtPekerjaan.setName("txtPekerjaan");
        btnSimpan.setName("btnSimpanPenghuni");

        btnSimpan.addActionListener(e -> simpan());
        btnUbah.addActionListener(e -> ubah());
        btnHapus.addActionListener(e -> hapus());
        btnBersihkan.addActionListener(e -> bersihkan());
        tabel.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { isiFormDariBaris(); }
        });
    }

    private Penghuni bacaForm(int id) {
        return new Penghuni(id, txtNama.getText().trim(), txtNik.getText().trim(), txtHp.getText().trim(),
                txtAlamat.getText().trim(), txtPekerjaan.getText().trim(), null);
    }

    private void simpan() {
        try {
            manager.tambahPenghuni(bacaForm(0));
            UiUtil.info(this, "Penghuni berhasil disimpan.");
            bersihkan();
            setelahDataBerubah.run();
        } catch (KosException ex) {
            UiUtil.error(this, ex.getMessage());
        }
    }

    private void ubah() {
        if (idTerpilih < 0) { UiUtil.error(this, "Pilih penghuni pada tabel terlebih dahulu."); return; }
        try {
            manager.ubahPenghuni(bacaForm(idTerpilih));
            UiUtil.info(this, "Data penghuni berhasil diubah.");
            setelahDataBerubah.run();
        } catch (KosException ex) {
            UiUtil.error(this, ex.getMessage());
        }
    }

    private void hapus() {
        if (idTerpilih < 0) { UiUtil.error(this, "Pilih penghuni pada tabel terlebih dahulu."); return; }
        if (!UiUtil.konfirmasi(this, "Hapus data penghuni ini?")) return;
        try {
            manager.hapusPenghuni(idTerpilih);
            bersihkan();
            setelahDataBerubah.run();
        } catch (KosException ex) {
            UiUtil.error(this, ex.getMessage());
        }
    }

    private void isiFormDariBaris() {
        int baris = tabel.getSelectedRow();
        if (baris < 0) return;
        idTerpilih = (Integer) model.getValueAt(baris, 0);
        txtNama.setText((String) model.getValueAt(baris, 1));
        txtNik.setText((String) model.getValueAt(baris, 2));
        txtHp.setText((String) model.getValueAt(baris, 3));
        txtAlamat.setText((String) model.getValueAt(baris, 4));
        txtPekerjaan.setText((String) model.getValueAt(baris, 5));
    }

    private void bersihkan() {
        idTerpilih = -1;
        txtNama.setText(""); txtNik.setText(""); txtHp.setText("");
        txtAlamat.setText(""); txtPekerjaan.setText("");
        tabel.clearSelection();
    }

    public void refresh() {
        model.setRowCount(0);
        for (Penghuni p : manager.semuaPenghuni()) {
            model.addRow(new Object[]{p.getId(), p.getNama(), p.getNik(), p.getNoHp(),
                    nz(p.getAlamatAsal()), nz(p.getPekerjaan()), p.getNomorKamar() == null ? "-" : p.getNomorKamar()});
        }
    }

    private static String nz(String s) { return s == null ? "" : s; }
}
