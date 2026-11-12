package kos.model;

import java.util.LinkedHashMap;
import java.util.Map;

/** Kamar tanpa pendingin: listrik flat (kipas & lampu), Wi-Fi standar. */
public class KamarNonAC extends Kamar {
    public static final double LISTRIK = 100_000;
    public static final double WIFI = 50_000;

    public KamarNonAC(String nomor, int lantai, double tarifDasar, boolean wifiAktif, StatusKamar status) {
        super(nomor, lantai, tarifDasar, wifiAktif, status);
    }

    @Override public String getKodeTipe() { return "NON_AC"; }
    @Override public String getNamaTipe() { return "Kamar Non-AC"; }

    @Override
    public Map<String, Double> rincianUtilitas() {
        Map<String, Double> rincian = new LinkedHashMap<>();
        rincian.put("Listrik (kipas & lampu)", LISTRIK);
        if (isWifiAktif()) rincian.put("Wi-Fi standar", WIFI);
        return rincian;
    }
}
