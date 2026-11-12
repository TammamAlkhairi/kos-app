package kos.service;

import kos.dao.KamarDAO;
import kos.dao.PenghuniDAO;
import kos.dao.ReservasiDAO;
import kos.dao.TagihanDAO;
import kos.model.Kamar;
import kos.model.Penghuni;
import kos.model.Reservasi;
import kos.model.StatusKamar;
import kos.model.Tagihan;
import kos.util.KosException;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Pusat data aplikasi.
 *
 * Penyimpanan di memori (Java Collection Framework):
 *   - HashMap<String, Kamar>  : kamar dicari berdasarkan nomor kamar (kunci unik, akses O(1))
 *   - HashSet<String>         : NIK yang sudah terdaftar (cek duplikasi O(1))
 *   - ArrayList<Penghuni>     : daftar penghuni (berurut, mudah diindeks ke baris JTable)
 *   - ArrayList<Reservasi>, ArrayList<Tagihan> : riwayat reservasi & tagihan
 *   - TreeMap<String, Rekap>  : rekap tunggakan per periode (otomatis terurut)
 *
 * Alur setiap operasi: validasi di memori -> simpan ke database (DAO) -> perbarui koleksi.
 */
public class KosManager {

    /** Baris rekap tunggakan bulanan. */
    public static class Rekap {
        public final String periode;
        public int jumlahTagihan;
        public int jumlahBelumLunas;
        public double totalTagihan;
        public double totalDibayar;
        public double tunggakan;

        Rekap(String periode) { this.periode = periode; }
    }

    private final KamarDAO kamarDAO = new KamarDAO();
    private final PenghuniDAO penghuniDAO = new PenghuniDAO();
    private final ReservasiDAO reservasiDAO = new ReservasiDAO();
    private final TagihanDAO tagihanDAO = new TagihanDAO();

    private final Map<String, Kamar> daftarKamar = new HashMap<>();
    private final List<Penghuni> daftarPenghuni = new ArrayList<>();
    private final Set<String> nikTerdaftar = new HashSet<>();
    private final List<Reservasi> daftarReservasi = new ArrayList<>();
    private final List<Tagihan> daftarTagihan = new ArrayList<>();

    // ------------------------------------------------------------ muat data
    /** Memuat ulang seluruh koleksi dari database. */
    public void muatSemua() throws KosException {
        try {
            List<Kamar> kamar = kamarDAO.findAll();
            List<Penghuni> penghuni = penghuniDAO.findAll();
            List<Reservasi> reservasi = reservasiDAO.findAll();
            List<Tagihan> tagihan = tagihanDAO.findAll();

            daftarKamar.clear();
            for (Kamar k : kamar) daftarKamar.put(k.getNomor(), k);

            daftarPenghuni.clear();
            daftarPenghuni.addAll(penghuni);
            nikTerdaftar.clear();
            for (Penghuni p : penghuni) nikTerdaftar.add(p.getNik());

            daftarReservasi.clear();
            daftarReservasi.addAll(reservasi);
            daftarTagihan.clear();
            daftarTagihan.addAll(tagihan);
        } catch (SQLException e) {
            throw new KosException("Gagal memuat data dari database: " + e.getMessage(), e);
        }
    }

    // ---------------------------------------------------------------- kamar
    public List<Kamar> kamarTerurut() {
        List<Kamar> hasil = new ArrayList<>(daftarKamar.values());
        hasil.sort(Comparator.comparing(Kamar::getNomor));
        return hasil;
    }

    public Kamar cariKamar(String nomor) { return daftarKamar.get(nomor); }

    public List<Kamar> kamarKosong() {
        List<Kamar> hasil = new ArrayList<>();
        for (Kamar k : kamarTerurut()) {
            if (k.getStatus() == StatusKamar.KOSONG) hasil.add(k);
        }
        return hasil;
    }

    public int jumlahKamar() { return daftarKamar.size(); }

    public int jumlahTerisi() {
        int n = 0;
        for (Kamar k : daftarKamar.values()) if (k.getStatus() == StatusKamar.TERISI) n++;
        return n;
    }

    public void tambahKamar(Kamar k) throws KosException {
        if (k.getNomor().isEmpty()) throw new KosException("Nomor kamar wajib diisi.");
        if (daftarKamar.containsKey(k.getNomor()))
            throw new KosException("Nomor kamar " + k.getNomor() + " sudah terdaftar.");
        if (k.getTarifDasar() <= 0) throw new KosException("Tarif dasar harus lebih dari 0.");
        try {
            kamarDAO.insert(k);
            daftarKamar.put(k.getNomor(), k);
        } catch (SQLException e) {
            throw new KosException(pesanSql("menyimpan kamar", e), e);
        }
    }

    public void ubahKamar(Kamar k) throws KosException {
        if (!daftarKamar.containsKey(k.getNomor())) throw new KosException("Kamar tidak ditemukan.");
        if (k.getTarifDasar() <= 0) throw new KosException("Tarif dasar harus lebih dari 0.");
        try {
            kamarDAO.update(k);
            daftarKamar.put(k.getNomor(), k);
        } catch (SQLException e) {
            throw new KosException(pesanSql("mengubah kamar", e), e);
        }
    }

    public void hapusKamar(String nomor) throws KosException {
        Kamar k = daftarKamar.get(nomor);
        if (k == null) throw new KosException("Kamar tidak ditemukan.");
        if (k.getStatus() == StatusKamar.TERISI)
            throw new KosException("Kamar " + nomor + " sedang terisi dan tidak dapat dihapus.");
        try {
            kamarDAO.delete(nomor);
            daftarKamar.remove(nomor);
        } catch (SQLException e) {
            throw new KosException(pesanSql("menghapus kamar", e), e);
        }
    }

    /** Reservasi aktif pada suatu kamar (null bila kosong). */
    public Reservasi reservasiAktifDiKamar(String nomor) {
        for (Reservasi r : daftarReservasi) {
            if (r.isAktif() && r.getNomorKamar().equals(nomor)) return r;
        }
        return null;
    }

    // ------------------------------------------------------------- penghuni
    public List<Penghuni> semuaPenghuni() { return new ArrayList<>(daftarPenghuni); }

    public List<Penghuni> penghuniTanpaKamar() {
        List<Penghuni> hasil = new ArrayList<>();
        for (Penghuni p : daftarPenghuni) if (p.getNomorKamar() == null) hasil.add(p);
        return hasil;
    }

    public void tambahPenghuni(Penghuni p) throws KosException {
        validasiPenghuni(p);
        if (nikTerdaftar.contains(p.getNik()))
            throw new KosException("NIK " + p.getNik() + " sudah terdaftar atas penghuni lain.");
        try {
            p.setId(penghuniDAO.insert(p));
            daftarPenghuni.add(p);
            nikTerdaftar.add(p.getNik());
        } catch (SQLException e) {
            throw new KosException(pesanSql("menyimpan penghuni", e), e);
        }
    }

    public void ubahPenghuni(Penghuni baru) throws KosException {
        validasiPenghuni(baru);
        Penghuni lama = cariPenghuni(baru.getId());
        if (lama == null) throw new KosException("Penghuni tidak ditemukan.");
        if (!lama.getNik().equals(baru.getNik()) && nikTerdaftar.contains(baru.getNik()))
            throw new KosException("NIK " + baru.getNik() + " sudah terdaftar atas penghuni lain.");
        try {
            penghuniDAO.update(baru);
            muatSemua();
        } catch (SQLException e) {
            throw new KosException(pesanSql("mengubah penghuni", e), e);
        }
    }

    public void hapusPenghuni(int id) throws KosException {
        Penghuni p = cariPenghuni(id);
        if (p == null) throw new KosException("Penghuni tidak ditemukan.");
        if (p.getNomorKamar() != null)
            throw new KosException(p.getNama() + " masih menempati kamar " + p.getNomorKamar() + ". Lakukan check-out dahulu.");
        try {
            penghuniDAO.delete(id);
            daftarPenghuni.remove(p);
            nikTerdaftar.remove(p.getNik());
        } catch (SQLException e) {
            throw new KosException(pesanSql("menghapus penghuni", e), e);
        }
    }

    private Penghuni cariPenghuni(int id) {
        for (Penghuni p : daftarPenghuni) if (p.getId() == id) return p;
        return null;
    }

    private void validasiPenghuni(Penghuni p) throws KosException {
        if (p.getNama().isEmpty()) throw new KosException("Nama penghuni wajib diisi.");
        if (!p.getNik().matches("\\d{16}")) throw new KosException("NIK harus terdiri dari 16 digit angka.");
        if (!p.getNoHp().matches("\\d{9,15}")) throw new KosException("No. HP harus 9-15 digit angka.");
    }

    // ----------------------------------------------------------- reservasi
    public List<Reservasi> semuaReservasi() { return new ArrayList<>(daftarReservasi); }

    public void checkIn(Penghuni p, Kamar k, LocalDate tglMulai) throws KosException {
        if (p == null) throw new KosException("Pilih penghuni terlebih dahulu.");
        if (k == null) throw new KosException("Pilih kamar kosong terlebih dahulu.");
        try {
            reservasiDAO.checkIn(p.getId(), k.getNomor(), tglMulai);
            muatSemua();
        } catch (SQLException e) {
            throw new KosException(pesanSql("memproses check-in", e), e);
        }
    }

    public void checkOut(Reservasi r, LocalDate tglSelesai) throws KosException {
        if (r == null) throw new KosException("Pilih reservasi aktif yang akan di-check-out.");
        if (!r.isAktif()) throw new KosException("Reservasi ini sudah selesai.");
        if (tglSelesai.isBefore(r.getTglMulai()))
            throw new KosException("Tanggal check-out tidak boleh sebelum tanggal masuk.");
        try {
            reservasiDAO.checkOut(r.getId(), r.getNomorKamar(), tglSelesai);
            muatSemua();
        } catch (SQLException e) {
            throw new KosException(pesanSql("memproses check-out", e), e);
        }
    }

    // -------------------------------------------------------------- tagihan
    public List<Tagihan> semuaTagihan() { return new ArrayList<>(daftarTagihan); }

    /**
     * Membuat tagihan satu periode untuk semua reservasi aktif.
     * Jumlah tagihan dihitung secara polimorfik: kamar.hitungTotalBulanan().
     */
    public int buatTagihanBulanan(YearMonth periode) throws KosException {
        List<Tagihan> baru = new ArrayList<>();
        for (Reservasi r : daftarReservasi) {
            if (!r.isAktif() || r.getTglMulai().isAfter(periode.atEndOfMonth())) continue;
            Kamar kamar = daftarKamar.get(r.getNomorKamar());
            baru.add(new Tagihan(0, r.getId(), r.getNomorKamar(), r.getNamaPenghuni(), periode.toString(),
                    kamar.hitungTotalBulanan(), periode.atDay(5), null));
        }
        try {
            int dibuat = tagihanDAO.buatTagihan(baru);
            muatSemua();
            return dibuat;
        } catch (SQLException e) {
            throw new KosException(pesanSql("membuat tagihan", e), e);
        }
    }

    public void bayarTagihan(Tagihan t, LocalDate tanggal) throws KosException {
        if (t == null) throw new KosException("Pilih tagihan yang akan dibayar.");
        if (t.isLunas()) throw new KosException("Tagihan ini sudah lunas.");
        try {
            tagihanDAO.bayar(t.getId(), tanggal);
            muatSemua();
        } catch (SQLException e) {
            throw new KosException(pesanSql("mencatat pembayaran", e), e);
        }
    }

    /** Laporan tunggakan HARIAN: semua tagihan belum lunas yang jatuh tempo pada/sebelum tanggal acuan. */
    public List<Tagihan> tunggakanHarian(LocalDate tanggal) {
        List<Tagihan> hasil = new ArrayList<>();
        for (Tagihan t : daftarTagihan) if (t.isTunggakan(tanggal)) hasil.add(t);
        hasil.sort(Comparator.comparing(Tagihan::getJatuhTempo).thenComparing(Tagihan::getNomorKamar));
        return hasil;
    }

    /** Laporan tunggakan BULANAN: rekap per periode, terurut dari periode terlama (TreeMap). */
    public Map<String, Rekap> rekapBulanan(LocalDate acuan) {
        Map<String, Rekap> rekap = new TreeMap<>();
        for (Tagihan t : daftarTagihan) {
            Rekap r = rekap.computeIfAbsent(t.getPeriode(), Rekap::new);
            r.jumlahTagihan++;
            r.totalTagihan += t.getJumlah();
            if (t.isLunas()) {
                r.totalDibayar += t.getJumlah();
            } else {
                r.jumlahBelumLunas++;
                if (t.isTunggakan(acuan)) r.tunggakan += t.getJumlah();
            }
        }
        return rekap;
    }

    // --------------------------------------------------------------- helper
    private String pesanSql(String aksi, SQLException e) {
        switch (e.getErrorCode()) {
            case 1062: return "Gagal " + aksi + ": data duplikat (nomor/NIK sudah ada).";
            case 1451: return "Gagal " + aksi + ": data masih dipakai oleh riwayat reservasi/tagihan.";
            case 1045: return "Gagal " + aksi + ": username/password database salah.";
            default:   return "Gagal " + aksi + ": " + e.getMessage();
        }
    }
}
