package kos.model;

/** Pabrik objek Kamar: memilih subclass yang tepat dari kode tipe di database. */
public final class KamarFactory {
    public static final String[] KODE  = {"AC", "NON_AC", "ENSUITE"};
    public static final String[] LABEL = {"Kamar AC", "Kamar Non-AC", "Kamar Ensuite"};
    public static final double[] TARIF_USULAN = {1_200_000, 800_000, 1_800_000};

    private KamarFactory() { }

    public static Kamar buat(String kodeTipe, String nomor, int lantai, double tarif,
                             boolean wifi, StatusKamar status) {
        switch (kodeTipe) {
            case "AC":      return new KamarAC(nomor, lantai, tarif, wifi, status);
            case "NON_AC":  return new KamarNonAC(nomor, lantai, tarif, wifi, status);
            case "ENSUITE": return new KamarEnsuite(nomor, lantai, tarif, wifi, status);
            default: throw new IllegalArgumentException("Tipe kamar tidak dikenal: " + kodeTipe);
        }
    }
}
