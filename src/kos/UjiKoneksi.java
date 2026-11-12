package kos;

import kos.dao.KamarDAO;
import kos.model.Kamar;
import kos.model.KamarFactory;
import kos.model.StatusKamar;
import kos.util.KoneksiDB;

import java.sql.Connection;
import java.sql.SQLException;

/** Program konsol kecil untuk menguji koneksi JDBC dan siklus INSERT -> SELECT -> DELETE. */
public class UjiKoneksi {
    public static void main(String[] args) {
        System.out.println("URL  : " + KoneksiDB.getUrl());
        System.out.println("User : " + KoneksiDB.getUser());
        try (Connection c = KoneksiDB.getConnection()) {
            System.out.println("[OK] Terhubung ke " + c.getMetaData().getDatabaseProductName()
                    + " " + c.getMetaData().getDatabaseProductVersion());

            KamarDAO dao = new KamarDAO();
            System.out.println("[OK] SELECT  : " + dao.findAll().size() + " kamar terbaca.");

            Kamar uji = KamarFactory.buat("ENSUITE", "UJI1", 3, 2_000_000, true, StatusKamar.KOSONG);
            dao.insert(uji);
            System.out.println("[OK] INSERT  : kamar UJI1 ditambahkan, total sekarang " + dao.findAll().size());
            System.out.println("     Total/bulan UJI1 = Rp " + (long) uji.hitungTotalBulanan());
            dao.delete("UJI1");
            System.out.println("[OK] DELETE  : kamar UJI1 dihapus, total kembali " + dao.findAll().size());
        } catch (SQLException e) {
            System.out.println("[GAGAL] SQLState=" + e.getSQLState() + ", ErrorCode=" + e.getErrorCode());
            System.out.println("        Pesan: " + e.getMessage().split("\n")[0]);
            System.out.println("        Periksa: MySQL aktif? database db_kos ada? username/password benar?");
        }
    }
}
