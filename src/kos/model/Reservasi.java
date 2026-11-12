package kos.model;

import java.time.LocalDate;

/** Satu masa huni: penghuni X menempati kamar Y mulai tanggal tertentu. */
public class Reservasi {
    private final int id;
    private final int idPenghuni;
    private final String namaPenghuni;
    private final String nomorKamar;
    private final LocalDate tglMulai;
    private final LocalDate tglSelesai;
    private final String status; // AKTIF / SELESAI

    public Reservasi(int id, int idPenghuni, String namaPenghuni, String nomorKamar,
                     LocalDate tglMulai, LocalDate tglSelesai, String status) {
        this.id = id; this.idPenghuni = idPenghuni; this.namaPenghuni = namaPenghuni;
        this.nomorKamar = nomorKamar; this.tglMulai = tglMulai;
        this.tglSelesai = tglSelesai; this.status = status;
    }

    public int getId() { return id; }
    public int getIdPenghuni() { return idPenghuni; }
    public String getNamaPenghuni() { return namaPenghuni; }
    public String getNomorKamar() { return nomorKamar; }
    public LocalDate getTglMulai() { return tglMulai; }
    public LocalDate getTglSelesai() { return tglSelesai; }
    public String getStatus() { return status; }
    public boolean isAktif() { return "AKTIF".equals(status); }
}
