package kos.model;

import java.util.LinkedHashMap;
import java.util.Map;

/** Kamar ber-AC: biaya listrik lebih tinggi karena pemakaian pendingin. */
public class KamarAC extends Kamar {
    public static final double LISTRIK = 200_000;
    public static final double WIFI = 50_000;

    public KamarAC(String nomor, int lantai, double tarifDasar, boolean wifiAktif, StatusKamar status) {
        super(nomor, lantai, tarifDasar, wifiAktif, status);
    }

    @Override public String getKodeTipe() { return "AC"; }
    @Override public String getNamaTipe() { return "Kamar AC"; }

    @Override
    public Map<String, Double> rincianUtilitas() {
        Map<String, Double> rincian = new LinkedHashMap<>();
        rincian.put("Listrik (AC)", LISTRIK);
        if (isWifiAktif()) rincian.put("Wi-Fi standar", WIFI);
        return rincian;
    }
}
