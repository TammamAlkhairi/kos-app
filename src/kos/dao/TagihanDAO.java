package kos.dao;

import kos.model.Tagihan;
import kos.util.KoneksiDB;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Akses data tagihan sewa bulanan. */
public class TagihanDAO {

    public List<Tagihan> findAll() throws SQLException {
        String sql = "SELECT t.id, t.id_reservasi, r.nomor_kamar, p.nama, t.periode, t.jumlah, t.jatuh_tempo, t.tgl_bayar "
                   + "FROM tagihan t JOIN reservasi r ON r.id = t.id_reservasi "
                   + "JOIN penghuni p ON p.id = r.id_penghuni ORDER BY t.periode DESC, r.nomor_kamar";
        List<Tagihan> hasil = new ArrayList<>();
        try (Connection c = KoneksiDB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Date bayar = rs.getDate("tgl_bayar");
                hasil.add(new Tagihan(rs.getInt("id"), rs.getInt("id_reservasi"), rs.getString("nomor_kamar"),
                        rs.getString("nama"), rs.getString("periode"), rs.getDouble("jumlah"),
                        rs.getDate("jatuh_tempo").toLocalDate(), bayar == null ? null : bayar.toLocalDate()));
            }
        }
        return hasil;
    }

    /**
     * Membuat tagihan satu periode untuk banyak reservasi dalam SATU transaksi.
     * INSERT IGNORE + UNIQUE(id_reservasi, periode) mencegah tagihan ganda.
     * @return jumlah tagihan yang benar-benar baru dibuat
     */
    public int buatTagihan(List<Tagihan> daftar) throws SQLException {
        String sql = "INSERT IGNORE INTO tagihan (id_reservasi, periode, jumlah, jatuh_tempo) VALUES (?,?,?,?)";
        int baru = 0;
        try (Connection c = KoneksiDB.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                for (Tagihan t : daftar) {
                    ps.setInt(1, t.getIdReservasi());
                    ps.setString(2, t.getPeriode());
                    ps.setDouble(3, t.getJumlah());
                    ps.setDate(4, Date.valueOf(t.getJatuhTempo()));
                    baru += ps.executeUpdate();
                }
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
        return baru;
    }

    /** UPDATE: tandai tagihan lunas pada tanggal tertentu. */
    public void bayar(int idTagihan, LocalDate tanggal) throws SQLException {
        try (Connection c = KoneksiDB.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE tagihan SET tgl_bayar = ? WHERE id = ? AND tgl_bayar IS NULL")) {
            ps.setDate(1, Date.valueOf(tanggal));
            ps.setInt(2, idTagihan);
            ps.executeUpdate();
        }
    }
}
