package kos.dao;

import kos.model.Reservasi;
import kos.util.KoneksiDB;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Akses data reservasi. Check-in/check-out memakai transaksi agar reservasi & status kamar konsisten. */
public class ReservasiDAO {

    public List<Reservasi> findAll() throws SQLException {
        String sql = "SELECT r.id, r.id_penghuni, p.nama, r.nomor_kamar, r.tgl_mulai, r.tgl_selesai, r.status "
                   + "FROM reservasi r JOIN penghuni p ON p.id = r.id_penghuni "
                   + "ORDER BY r.status, r.nomor_kamar, r.tgl_mulai DESC";
        List<Reservasi> hasil = new ArrayList<>();
        try (Connection c = KoneksiDB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Date selesai = rs.getDate("tgl_selesai");
                hasil.add(new Reservasi(rs.getInt("id"), rs.getInt("id_penghuni"), rs.getString("nama"),
                        rs.getString("nomor_kamar"), rs.getDate("tgl_mulai").toLocalDate(),
                        selesai == null ? null : selesai.toLocalDate(), rs.getString("status")));
            }
        }
        return hasil;
    }

    /** Transaksi: kunci baris kamar, pastikan KOSONG, tambah reservasi, ubah status kamar jadi TERISI. */
    public void checkIn(int idPenghuni, String nomorKamar, LocalDate tglMulai) throws SQLException {
        try (Connection c = KoneksiDB.getConnection()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement cek = c.prepareStatement("SELECT status FROM kamar WHERE nomor = ? FOR UPDATE")) {
                    cek.setString(1, nomorKamar);
                    try (ResultSet rs = cek.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Kamar " + nomorKamar + " tidak ditemukan.");
                        if (!"KOSONG".equals(rs.getString(1))) throw new SQLException("Kamar " + nomorKamar + " sudah terisi.");
                    }
                }
                try (PreparedStatement ins = c.prepareStatement(
                        "INSERT INTO reservasi (id_penghuni, nomor_kamar, tgl_mulai, status) VALUES (?,?,?, 'AKTIF')")) {
                    ins.setInt(1, idPenghuni);
                    ins.setString(2, nomorKamar);
                    ins.setDate(3, Date.valueOf(tglMulai));
                    ins.executeUpdate();
                }
                try (PreparedStatement upd = c.prepareStatement("UPDATE kamar SET status = 'TERISI' WHERE nomor = ?")) {
                    upd.setString(1, nomorKamar);
                    upd.executeUpdate();
                }
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    /** Transaksi: akhiri reservasi dan kosongkan kamar. */
    public void checkOut(int idReservasi, String nomorKamar, LocalDate tglSelesai) throws SQLException {
        try (Connection c = KoneksiDB.getConnection()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement upd = c.prepareStatement(
                        "UPDATE reservasi SET status = 'SELESAI', tgl_selesai = ? WHERE id = ?")) {
                    upd.setDate(1, Date.valueOf(tglSelesai));
                    upd.setInt(2, idReservasi);
                    upd.executeUpdate();
                }
                try (PreparedStatement upd = c.prepareStatement("UPDATE kamar SET status = 'KOSONG' WHERE nomor = ?")) {
                    upd.setString(1, nomorKamar);
                    upd.executeUpdate();
                }
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }
}
