import kos.service.KosManager;
import kos.view.MainFrame;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;

/** Menjalankan GUI sungguhan lalu memotret tiap skenario. Hanya untuk dokumentasi. */
public class Screenshot {
    static MainFrame frame;
    static Robot robot;
    static File out = new File(System.getProperty("out", "shots"));

    public static void main(String[] a) throws Exception {
        out.mkdirs();
        for (UIManager.LookAndFeelInfo i : UIManager.getInstalledLookAndFeels())
            if ("Nimbus".equals(i.getName())) UIManager.setLookAndFeel(i.getClassName());
        robot = new Robot();

        KosManager m = new KosManager();
        m.muatSemua();
        edt(() -> {
            frame = new MainFrame(m);
            frame.setSize(1180, 720);
            frame.setLocation(0, 0);
            frame.setVisible(true);
        });
        pause(1200);

        // 01 denah
        tab(0); shot("01_denah");
        // 02 klik kartu kamar -> dialog rincian
        klikKartu("kartu-203"); shot("02_denah_rincian"); tutupDialog();

        // 03 data kamar (P11)
        tab(1); shot("03_data_kamar");

        // 04 klik baris tabel -> form terisi (MouseListener)
        edt(() -> { JTable t = (JTable) find("tblKamar"); t.setRowSelectionInterval(8, 8);
            t.dispatchEvent(new MouseEvent(t, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, 40, 8 * 26 + 10, 1, false)); });
        shot("04_klik_baris");

        // 05 pencarian + filter (KeyAdapter + ActionListener combo)
        edt(() -> { JTextField t = (JTextField) find("txtCari"); t.setText("20");
            t.dispatchEvent(new KeyEvent(t, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, KeyEvent.VK_0, '0')); });
        edt(() -> ((JComboBox<?>) find("cbFilterTipe")).setSelectedItem("Kamar Ensuite"));
        shot("05_filter_cari");
        edt(() -> { JTextField t = (JTextField) find("txtCari"); t.setText("");
            t.dispatchEvent(new KeyEvent(t, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, KeyEvent.VK_BACK_SPACE, '\b'));
            ((JComboBox<?>) find("cbFilterTipe")).setSelectedIndex(0); });

        // 06 isi form kamar baru (estimasi biaya dihitung otomatis)
        edt(() -> ((AbstractButton) find("btnBersihkanKamar")).doClick());
        edt(() -> { ((JTextField) find("txtNomor")).setText("301");
            ((JSpinner) find("spLantai")).setValue(3);
            ((JComboBox<?>) find("cbTipe")).setSelectedIndex(2);
            JTextField tarif = (JTextField) find("txtTarif"); tarif.setText("2000000");
            tarif.dispatchEvent(new KeyEvent(tarif, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, KeyEvent.VK_0, '0')); });
        shot("06_form_kamar_baru");

        // 07 klik Tambah -> dialog sukses, lalu tabel
        later(() -> ((AbstractButton) find("btnTambahKamar")).doClick());
        pause(900); shot("07_tambah_sukses"); tutupDialog(); pause(500); shot("08_tabel_setelah_tambah");
        sql("db_setelah_insert", "SELECT * FROM kamar WHERE nomor = '301';");

        // 09 tambah lagi nomor yang sama -> validasi duplikat
        edt(() -> { ((JTextField) find("txtNomor")).setText("301"); ((JTextField) find("txtTarif")).setText("2000000"); });
        later(() -> ((AbstractButton) find("btnTambahKamar")).doClick());
        pause(900); shot("09_validasi_duplikat"); tutupDialog();

        // 09b ubah data kamar 301 (klik baris -> ubah tarif -> Ubah)
        edt(() -> { JTable t = (JTable) find("tblKamar"); t.setRowSelectionInterval(12, 12);
            t.dispatchEvent(new MouseEvent(t, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, 40, 12 * 26 + 10, 1, false));
            JTextField tarif = (JTextField) find("txtTarif"); tarif.setText("2100000");
            tarif.dispatchEvent(new KeyEvent(tarif, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, KeyEvent.VK_0, '0')); });
        later(() -> ((AbstractButton) find("btnUbahKamar")).doClick());
        pause(900); shot("09b_ubah_sukses"); tutupDialog(); pause(400); shot("09c_tabel_setelah_ubah");
        // 09d hapus kamar 301 (dialog konfirmasi -> Yes)
        later(() -> ((AbstractButton) find("btnHapusKamar")).doClick());
        pause(900); shot("09d_hapus_konfirmasi"); klikTombolDialog("Yes"); pause(700); shot("09e_setelah_hapus");

        // 10 penghuni
        tab(2); shot("10_penghuni");

        // 11 reservasi: pilih kamar & lihat rincian, lalu check-in
        tab(3);
        edt(() -> { JComboBox<?> k = (JComboBox<?>) find("cbKamarKosong"); k.setSelectedIndex(Math.min(2, k.getItemCount() - 1)); });
        shot("11_reservasi_form");
        later(() -> ((AbstractButton) find("btnCheckIn")).doClick());
        pause(900); shot("12_checkin_sukses"); tutupDialog(); pause(500);
        sql("db_setelah_checkin", "SELECT r.id, p.nama, r.nomor_kamar, r.tgl_mulai, r.status AS status_reservasi, k.status AS status_kamar FROM reservasi r JOIN penghuni p ON p.id=r.id_penghuni JOIN kamar k ON k.nomor=r.nomor_kamar WHERE r.id > 7;");
        tab(0); shot("13_denah_setelah_checkin");

        // 14-16 tagihan
        tab(4); shot("14_tagihan_semua");
        edt(() -> ((JTabbedPane) find("subTabTagihan")).setSelectedIndex(1)); shot("15_tunggakan_harian");
        edt(() -> ((JTabbedPane) find("subTabTagihan")).setSelectedIndex(2)); shot("16_rekap_bulanan");

        System.exit(0);
    }

    // ------------------------------------------------------------ helper
    static void tab(int i) throws Exception { edt(() -> frame.getTabs().setSelectedIndex(i)); pause(500); }

    static void klikKartu(String nama) throws Exception {
        Component c = find(nama);
        later(() -> {
            for (java.awt.event.MouseListener l : c.getMouseListeners())
                l.mouseClicked(new MouseEvent(c, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, 5, 5, 1, false));
        });
        pause(900);
    }

    static void tutupDialog() throws Exception {
        edt(() -> { for (Window w : Window.getWindows()) if (w instanceof JDialog && w.isShowing()) w.dispose(); });
        pause(400);
    }

    static void klikTombolDialog(String teks) throws Exception {
        edt(() -> { for (Window w : Window.getWindows()) if (w instanceof JDialog && w.isShowing()) {
            AbstractButton b = cari((Container) w, teks); if (b != null) b.doClick(); } });
    }

    static AbstractButton cari(Container root, String teks) {
        for (Component c : root.getComponents()) {
            if (c instanceof AbstractButton && teks.equals(((AbstractButton) c).getText())) return (AbstractButton) c;
            if (c instanceof Container) { AbstractButton r = cari((Container) c, teks); if (r != null) return r; }
        }
        return null;
    }

    static Component find(String name) { return find(frame, name); }

    static Component find(Container root, String name) {
        for (Component c : root.getComponents()) {
            if (name.equals(c.getName())) return c;
            if (c instanceof Container) { Component r = find((Container) c, name); if (r != null) return r; }
        }
        return null;
    }

    static void sql(String nama, String q) throws Exception {
        Process pr = new ProcessBuilder("mariadb", "-uroot", "-t", "db_kos", "-e", q).redirectErrorStream(true).start();
        String hasil = new String(pr.getInputStream().readAllBytes());
        java.nio.file.Files.writeString(new File(out, nama + ".txt").toPath(), "MariaDB [db_kos]> " + q + "\n" + hasil);
    }

    static void edt(Runnable r) throws Exception { SwingUtilities.invokeAndWait(r); pause(150); }
    static void later(Runnable r) { SwingUtilities.invokeLater(r); }
    static void pause(int ms) throws Exception { Thread.sleep(ms); }

    static void shot(String nama) throws Exception {
        pause(500);
        Rectangle r = frame.getBounds();
        BufferedImage img = robot.createScreenCapture(r);
        ImageIO.write(img, "png", new File(out, nama + ".png"));
        System.out.println("tersimpan: " + nama + ".png");
    }
}
