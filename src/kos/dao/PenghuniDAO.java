package kos.dao;

import kos.model.Penghuni;
import kos.util.KoneksiDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Akses data tabel penghuni. nomor kamar diambil dari reservasi yang masih AKTIF. */
public class PenghuniDAO {

    public List<Penghuni> findAll() throws SQLException {
        String sql = "SELECT p.id, p.nama, p.nik, p.no_hp, p.alamat_asal, p.pekerjaan, r.nomor_kamar "
                   + "FROM penghuni p LEFT JOIN reservasi r ON r.id_penghuni = p.id AND r.status = 'AKTIF' "
                   + "ORDER BY p.id";
        List<Penghuni> hasil = new ArrayList<>();
        try (Connection c = KoneksiDB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                hasil.add(new Penghuni(rs.getInt("id"), rs.getString("nama"), rs.getString("nik"),
                        rs.getString("no_hp"), rs.getString("alamat_asal"), rs.getString("pekerjaan"),
                        rs.getString("nomor_kamar")));
            }
        }
        return hasil;
    }

    /** INSERT dan mengembalikan ID otomatis (AUTO_INCREMENT). */
    public int insert(Penghuni p) throws SQLException {
        String sql = "INSERT INTO penghuni (nama, nik, no_hp, alamat_asal, pekerjaan) VALUES (?,?,?,?,?)";
        try (Connection c = KoneksiDB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getNama());
            ps.setString(2, p.getNik());
            ps.setString(3, p.getNoHp());
            ps.setString(4, p.getAlamatAsal());
            ps.setString(5, p.getPekerjaan());
            ps.executeUpdate();
            try (ResultSet kunci = ps.getGeneratedKeys()) {
                return kunci.next() ? kunci.getInt(1) : -1;
            }
        }
    }

    public void update(Penghuni p) throws SQLException {
        String sql = "UPDATE penghuni SET nama=?, nik=?, no_hp=?, alamat_asal=?, pekerjaan=? WHERE id=?";
        try (Connection c = KoneksiDB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.getNama());
            ps.setString(2, p.getNik());
            ps.setString(3, p.getNoHp());
            ps.setString(4, p.getAlamatAsal());
            ps.setString(5, p.getPekerjaan());
            ps.setInt(6, p.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection c = KoneksiDB.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM penghuni WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
