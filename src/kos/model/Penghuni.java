package kos.model;

/** Identitas penghuni kos. nomorKamar = null bila belum/tidak sedang menempati kamar. */
public class Penghuni {
    private int id;
    private String nama;
    private String nik;
    private String noHp;
    private String alamatAsal;
    private String pekerjaan;
    private String nomorKamar;

    public Penghuni(int id, String nama, String nik, String noHp, String alamatAsal,
                    String pekerjaan, String nomorKamar) {
        this.id = id; this.nama = nama; this.nik = nik; this.noHp = noHp;
        this.alamatAsal = alamatAsal; this.pekerjaan = pekerjaan; this.nomorKamar = nomorKamar;
    }

    public int getId() { return id; }
    public String getNama() { return nama; }
    public String getNik() { return nik; }
    public String getNoHp() { return noHp; }
    public String getAlamatAsal() { return alamatAsal; }
    public String getPekerjaan() { return pekerjaan; }
    public String getNomorKamar() { return nomorKamar; }
    public void setId(int id) { this.id = id; }

    @Override
    public String toString() { return nama + " (" + nik + ")"; }
}
