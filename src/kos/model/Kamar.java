package kos.model;

import java.util.Map;

/**
 * Kelas abstrak induk untuk semua jenis kamar.
 * Perhitungan biaya utilitas didelegasikan ke subclass (polimorfisme),
 * sedangkan total biaya bulanan dihitung di sini (template method).
 */
public abstract class Kamar {
    private String nomor;
    private int lantai;
    private double tarifDasar;
    private boolean wifiAktif;
    private StatusKamar status;

    protected Kamar(String nomor, int lantai, double tarifDasar, boolean wifiAktif, StatusKamar status) {
        this.nomor = nomor;
        this.lantai = lantai;
        this.tarifDasar = tarifDasar;
        this.wifiAktif = wifiAktif;
        this.status = status;
    }

    /** Kode tipe untuk disimpan di database: AC / NON_AC / ENSUITE. */
    public abstract String getKodeTipe();

    /** Nama tipe yang ramah dibaca di GUI. */
    public abstract String getNamaTipe();

    /** Rincian biaya tambahan (utility) per bulan, urut sesuai penambahan. */
    public abstract Map<String, Double> rincianUtilitas();

    /** Total seluruh biaya tambahan (listrik + air + Wi-Fi). */
    public double hitungBiayaUtilitas() {
        double total = 0;
        for (double biaya : rincianUtilitas().values()) {
            total += biaya;
        }
        return total;
    }

    /** Biaya yang ditagihkan setiap bulan = tarif dasar + utilitas. */
    public double hitungTotalBulanan() {
        return tarifDasar + hitungBiayaUtilitas();
    }

    public String getNomor() { return nomor; }
    public int getLantai() { return lantai; }
    public double getTarifDasar() { return tarifDasar; }
    public boolean isWifiAktif() { return wifiAktif; }
    public StatusKamar getStatus() { return status; }

    public void setNomor(String nomor) { this.nomor = nomor; }
    public void setLantai(int lantai) { this.lantai = lantai; }
    public void setTarifDasar(double tarifDasar) { this.tarifDasar = tarifDasar; }
    public void setWifiAktif(boolean wifiAktif) { this.wifiAktif = wifiAktif; }
    public void setStatus(StatusKamar status) { this.status = status; }

    @Override
    public String toString() {
        return nomor + " - " + getNamaTipe();
    }
}
