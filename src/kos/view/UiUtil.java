package kos.view;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;

/** Pembantu tampilan: warna, tabel, dialog, dan renderer yang dipakai bersama semua panel. */
public final class UiUtil {
    public static final Color BIRU = new Color(0x1F4E79);
    public static final Color BIRU_MUDA = new Color(0xDCE9F8);
    public static final Color HIJAU_MUDA = new Color(0xD8F2DF);
    public static final Color HIJAU_TUA = new Color(0x1E7B3A);
    public static final Color MERAH_MUDA = new Color(0xF9D9DC);
    public static final Color MERAH_TUA = new Color(0xA52834);

    private UiUtil() { }

    public static DefaultTableModel modelTabel(String... kolom) {
        return new DefaultTableModel(kolom, 0) {
            @Override public boolean isCellEditable(int baris, int kolom) { return false; }
        };
    }

    public static JTable buatTabel(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setRowHeight(26);
        t.setFillsViewportHeight(true);
        t.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        t.getTableHeader().setReorderingAllowed(false);
        t.getTableHeader().setFont(t.getTableHeader().getFont().deriveFont(Font.BOLD));
        return t;
    }

    public static void rataKanan(JTable t, int... kolom) {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        r.setHorizontalAlignment(SwingConstants.RIGHT);
        for (int k : kolom) t.getColumnModel().getColumn(k).setCellRenderer(r);
    }

    /** Mewarnai sel status: KOSONG/LUNAS = hijau, TERISI/BELUM LUNAS/TERLAMBAT = merah. */
    public static void rendererStatus(JTable t, int kolom) {
        t.getColumnModel().getColumn(kolom).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object v, boolean sel, boolean fokus, int r, int c) {
                super.getTableCellRendererComponent(tbl, v, sel, fokus, r, c);
                setHorizontalAlignment(SwingConstants.CENTER);
                String s = String.valueOf(v);
                boolean baik = s.equals("KOSONG") || s.equals("LUNAS") || s.equals("SELESAI");
                boolean netral = s.equals("AKTIF");
                setFont(getFont().deriveFont(Font.BOLD));
                if (!sel) {
                    setBackground(netral ? BIRU_MUDA : (baik ? HIJAU_MUDA : MERAH_MUDA));
                    setForeground(netral ? BIRU : (baik ? HIJAU_TUA : MERAH_TUA));
                }
                return this;
            }
        });
    }

    public static void lebarKolom(JTable t, int... lebar) {
        for (int i = 0; i < lebar.length; i++) t.getColumnModel().getColumn(i).setPreferredWidth(lebar[i]);
    }

    public static JLabel judul(String teks) {
        JLabel l = new JLabel(teks);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 15f));
        l.setForeground(BIRU);
        return l;
    }

    public static JPanel kartuBerjudul(String judul) {
        JPanel p = new JPanel();
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(judul), BorderFactory.createEmptyBorder(4, 6, 6, 6)));
        return p;
    }

    /** Menambah satu baris "label : isi" pada panel ber-GridBagLayout. */
    public static void barisForm(JPanel p, int baris, String label, Component isi) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.gridy = baris; gc.anchor = GridBagConstraints.WEST;
        gc.insets = new Insets(4, 4, 4, 10);
        p.add(new JLabel(label), gc);
        gc.gridx = 1; gc.weightx = 1; gc.fill = GridBagConstraints.HORIZONTAL;
        p.add(isi, gc);
    }

    /** Baris pengisi agar isi form menempel di atas (bukan melayang di tengah panel). */
    public static void penutupForm(JPanel p, int baris) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.gridy = baris; gc.gridwidth = 2; gc.weighty = 1; gc.fill = GridBagConstraints.VERTICAL;
        p.add(new JPanel(), gc);
    }

    public static void info(Component induk, String pesan) {
        JOptionPane.showMessageDialog(induk, pesan, "Informasi", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void error(Component induk, String pesan) {
        JOptionPane.showMessageDialog(induk, pesan, "Terjadi Kesalahan", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean konfirmasi(Component induk, String pesan) {
        return JOptionPane.showConfirmDialog(induk, pesan, "Konfirmasi", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }
}
