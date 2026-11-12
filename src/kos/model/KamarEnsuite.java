package kos.model;

import java.util.LinkedHashMap;
import java.util.Map;

/** Kamar ensuite (kamar mandi dalam, ber-AC): ada biaya air + Wi-Fi premium. */
public class KamarEnsuite extends Kamar {
    public static final double LISTRIK = 200_000;
    public static final double AIR = 75_000;
    public static final double WIFI = 75_000;

    public KamarEnsuite(String nomor, int lantai, double tarifDasar, boolean wifiAktif, StatusKamar status) {
        super(nomor, lantai, tarifDasar, wifiAktif, status);
    }

    @Override public String getKodeTipe() { return "ENSUITE"; }
    @Override public String getNamaTipe() { return "Kamar Ensuite"; }

    @Override
    public Map<String, Double> rincianUtilitas() {
        Map<String, Double> rincian = new LinkedHashMap<>();
        rincian.put("Listrik (AC)", LISTRIK);
        rincian.put("Air kamar mandi dalam", AIR);
        if (isWifiAktif()) rincian.put("Wi-Fi premium", WIFI);
        return rincian;
    }
}
