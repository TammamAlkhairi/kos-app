package kos.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Tagihan sewa bulanan. Disebut tunggakan bila belum dibayar dan sudah lewat jatuh tempo. */
public class Tagihan {
    private final int id;
    private final int idReservasi;
    private final String nomorKamar;
    private final String namaPenghuni;
    private final String periode;      // format YYYY-MM
    private final double jumlah;
    private final LocalDate jatuhTempo;
    private final LocalDate tglBayar;  // null = belum dibayar

    public Tagihan(int id, int idReservasi, String nomorKamar, String namaPenghuni, String periode,
                   double jumlah, LocalDate jatuhTempo, LocalDate tglBayar) {
        this.id = id; this.idReservasi = idReservasi; this.nomorKamar = nomorKamar;
        this.namaPenghuni = namaPenghuni; this.periode = periode; this.jumlah = jumlah;
        this.jatuhTempo = jatuhTempo; this.tglBayar = tglBayar;
    }

    public boolean isLunas() { return tglBayar != null; }

    /** Jumlah hari keterlambatan pada tanggal acuan (0 jika belum jatuh tempo). */
    public long hariTerlambat(LocalDate acuan) {
        return Math.max(0, ChronoUnit.DAYS.between(jatuhTempo, acuan));
    }

    /** Tunggakan pada tanggal acuan: belum lunas dan jatuh tempo sudah tercapai. */
    public boolean isTunggakan(LocalDate acuan) {
        return !isLunas() && !jatuhTempo.isAfter(acuan);
    }

    public int getId() { return id; }
    public int getIdReservasi() { return idReservasi; }
    public String getNomorKamar() { return nomorKamar; }
    public String getNamaPenghuni() { return namaPenghuni; }
    public String getPeriode() { return periode; }
    public double getJumlah() { return jumlah; }
    public LocalDate getJatuhTempo() { return jatuhTempo; }
    public LocalDate getTglBayar() { return tglBayar; }
}
