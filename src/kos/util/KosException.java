package kos.util;

/** Exception khusus aplikasi: pesannya aman ditampilkan langsung ke pengguna. */
public class KosException extends Exception {
    public KosException(String pesan) { super(pesan); }
    public KosException(String pesan, Throwable penyebab) { super(pesan, penyebab); }
}
