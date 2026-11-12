package kos.view;

import kos.model.Kamar;
import kos.model.Reservasi;
import kos.model.StatusKamar;
import kos.service.KosManager;
import kos.util.Format;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Denah kamar: kartu berwarna per kamar (hijau = kosong, merah = terisi), dikelompokkan per lantai. */
public class PanelDenah extends JPanel {
    private final KosManager manager;
    private final JLabel lblTotal = new JLabel();
    private final JLabel lblTerisi = new JLabel();
    private final JLabel lblKosong = new JLabel();
    private final JLabel lblOkupansi = new JLabel();
    private final JPanel areaLantai = new JPanel();

    public PanelDenah(KosManager manager) {
        this.manager = manager;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel ringkasan = new JPanel(new GridLayout(1, 4, 10, 0));
        ringkasan.add(kartuRingkasan("Total Kamar", lblTotal, new Color(0xE3ECF7), UiUtil.BIRU));
        ringkasan.add(kartuRingkasan("Terisi", lblTerisi, UiUtil.MERAH_MUDA, UiUtil.MERAH_TUA));
        ringkasan.add(kartuRingkasan("Kosong", lblKosong, UiUtil.HIJAU_MUDA, UiUtil.HIJAU_TUA));
        ringkasan.add(kartuRingkasan("Okupansi", lblOkupansi, new Color(0xFFF3CD), new Color(0x7A5B00)));
        add(ringkasan, BorderLayout.NORTH);

        areaLantai.setLayout(new BoxLayout(areaLantai, BoxLayout.Y_AXIS));
        add(new JScrollPane(areaLantai), BorderLayout.CENTER);

        JPanel legenda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        legenda.add(new JLabel("Klik kartu kamar untuk melihat rincian biaya dan penghuni."));
        add(legenda, BorderLayout.SOUTH);
    }

    private JPanel kartuRingkasan(String judul, JLabel nilai, Color latar, Color teks) {
        JPanel p = new JPanel(new GridLayout(2, 1));
        p.setBackground(latar);
        p.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        JLabel l = new JLabel(judul);
        l.setForeground(teks);
        nilai.setForeground(teks);
        nilai.setFont(nilai.getFont().deriveFont(Font.BOLD, 24f));
        p.add(l);
        p.add(nilai);
        return p;
    }

    public void refresh() {
        int total = manager.jumlahKamar();
        int terisi = manager.jumlahTerisi();
        lblTotal.setText(String.valueOf(total));
        lblTerisi.setText(String.valueOf(terisi));
        lblKosong.setText(String.valueOf(total - terisi));
        lblOkupansi.setText(total == 0 ? "0%" : Math.round(terisi * 100.0 / total) + "%");

        // Kelompokkan kamar per lantai; TreeMap membuat lantai selalu terurut.
        Map<Integer, List<Kamar>> perLantai = new TreeMap<>();
        for (Kamar k : manager.kamarTerurut()) {
            perLantai.computeIfAbsent(k.getLantai(), x -> new ArrayList<>()).add(k);
        }
        areaLantai.removeAll();
        for (Map.Entry<Integer, List<Kamar>> e : perLantai.entrySet()) {
            JPanel baris = new JPanel(new GridLayout(0, 6, 10, 10));
            baris.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder("Lantai " + e.getKey()),
                    BorderFactory.createEmptyBorder(6, 6, 8, 6)));
            for (Kamar k : e.getValue()) baris.add(new KartuKamar(k));
            areaLantai.add(baris);
        }
        areaLantai.revalidate();
        areaLantai.repaint();
    }

    private void tampilkanRincian(Kamar k) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Kamar %s  (%s, Lantai %d)%n", k.getNomor(), k.getNamaTipe(), k.getLantai()));
        sb.append(String.format("Status       : %s%n", k.getStatus()));
        Reservasi r = manager.reservasiAktifDiKamar(k.getNomor());
        if (r != null) {
            sb.append(String.format("Penghuni     : %s%n", r.getNamaPenghuni()));
            sb.append(String.format("Masuk sejak  : %s%n", Format.tanggal(r.getTglMulai())));
        }
        sb.append(String.format("%nTarif dasar    : %s%n", Format.rupiah(k.getTarifDasar())));
        for (Map.Entry<String, Double> b : k.rincianUtilitas().entrySet()) {
            sb.append(String.format("  + %-22s %s%n", b.getKey(), Format.rupiah(b.getValue())));
        }
        sb.append(String.format("%nTOTAL / BULAN : %s", Format.rupiah(k.hitungTotalBulanan())));
        JTextArea area = new JTextArea(sb.toString());
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setEditable(false);
        javax.swing.JOptionPane.showMessageDialog(this, area, "Rincian Kamar " + k.getNomor(),
                javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }

    /** Kartu satu kamar. Klik = tampilkan rincian (MouseListener). */
    private class KartuKamar extends JPanel {
        KartuKamar(Kamar k) {
            boolean kosong = k.getStatus() == StatusKamar.KOSONG;
            Color latar = kosong ? UiUtil.HIJAU_MUDA : UiUtil.MERAH_MUDA;
            Color teks = kosong ? UiUtil.HIJAU_TUA : UiUtil.MERAH_TUA;
            setLayout(new GridLayout(3, 1));
            setBackground(latar);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(teks, 1, true), BorderFactory.createEmptyBorder(6, 8, 6, 8)));
            setName("kartu-" + k.getNomor());

            JLabel nomor = new JLabel(k.getNomor(), SwingConstants.CENTER);
            nomor.setFont(nomor.getFont().deriveFont(Font.BOLD, 22f));
            nomor.setForeground(teks);
            JLabel tipe = new JLabel(k.getNamaTipe(), SwingConstants.CENTER);
            JLabel status = new JLabel(k.getStatus().name(), SwingConstants.CENTER);
            status.setFont(status.getFont().deriveFont(Font.BOLD));
            status.setForeground(teks);
            add(nomor);
            add(tipe);
            add(status);

            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { tampilkanRincian(k); }
            });
        }
    }
}
