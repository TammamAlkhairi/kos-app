package kos.dao;

import kos.model.Kamar;
import kos.model.KamarFactory;
import kos.model.StatusKamar;
import kos.util.KoneksiDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Akses data tabel kamar (CRUD). Setiap method membuka & menutup koneksinya sendiri. */
public class KamarDAO {

    /** SELECT: ambil semua kamar. */
    public List<Kamar> findAll() throws SQLException {
        String sql = "SELECT nomor, lantai, tipe, tarif_dasar, wifi_aktif, status FROM kamar ORDER BY nomor";
        List<Kamar> hasil = new ArrayList<>();
        try (Connection c = KoneksiDB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                hasil.add(KamarFactory.buat(rs.getString("tipe"), rs.getString("nomor"),
                        rs.getInt("lantai"), rs.getDouble("tarif_dasar"),
                        rs.getBoolean("wifi_aktif"), StatusKamar.valueOf(rs.getString("status"))));
            }
        }
        return hasil;
    }

    /** INSERT: tambah kamar baru. */
    public void insert(Kamar k) throws SQLException {
        String sql = "INSERT INTO kamar (nomor, lantai, tipe, tarif_dasar, wifi_aktif, status) VALUES (?,?,?,?,?,?)";
        try (Connection c = KoneksiDB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, k.getNomor());
            ps.setInt(2, k.getLantai());
            ps.setString(3, k.getKodeTipe());
            ps.setDouble(4, k.getTarifDasar());
            ps.setBoolean(5, k.isWifiAktif());
            ps.setString(6, k.getStatus().name());
            ps.executeUpdate();
        }
    }

    /** UPDATE: ubah data kamar berdasarkan nomor (primary key). */
    public void update(Kamar k) throws SQLException {
        String sql = "UPDATE kamar SET lantai=?, tipe=?, tarif_dasar=?, wifi_aktif=? WHERE nomor=?";
        try (Connection c = KoneksiDB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, k.getLantai());
            ps.setString(2, k.getKodeTipe());
            ps.setDouble(3, k.getTarifDasar());
            ps.setBoolean(4, k.isWifiAktif());
            ps.setString(5, k.getNomor());
            ps.executeUpdate();
        }
    }

    /** DELETE: hapus kamar. */
    public void delete(String nomor) throws SQLException {
        try (Connection c = KoneksiDB.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM kamar WHERE nomor = ?")) {
            ps.setString(1, nomor);
            ps.executeUpdate();
        }
    }
}
