package kos.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Modul koneksi JDBC ke MySQL/MariaDB.
 * Nilai bawaan setara XAMPP (user root tanpa password) dan dapat ditimpa
 * lewat environment variable KOS_DB_URL, KOS_DB_USER, KOS_DB_PASS.
 */
public final class KoneksiDB {
    private static final String URL = ambil("KOS_DB_URL",
            "jdbc:mysql://localhost:3306/db_kos?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Jakarta");
    private static final String USER = ambil("KOS_DB_USER", "root");
    private static final String PASSWORD = ambil("KOS_DB_PASS", "");

    private KoneksiDB() { }

    /** Membuka koneksi baru. Pemanggil wajib menutupnya (try-with-resources). */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static String getUrl() { return URL; }
    public static String getUser() { return USER; }

    private static String ambil(String kunci, String bawaan) {
        String nilai = System.getenv(kunci);
        return (nilai == null || nilai.isEmpty()) ? bawaan : nilai;
    }
}
