package kos.util;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/** Utilitas pemformatan rupiah dan tanggal (dd-MM-yyyy). */
public final class Format {
    private static final DateTimeFormatter TGL = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private Format() { }

    public static String rupiah(double nilai) {
        NumberFormat nf = NumberFormat.getInstance(Locale.forLanguageTag("id-ID"));
        nf.setMaximumFractionDigits(0);
        return "Rp " + nf.format(nilai);
    }

    public static String tanggal(LocalDate tgl) {
        return tgl == null ? "-" : tgl.format(TGL);
    }

    public static LocalDate parseTanggal(String teks) throws KosException {
        try {
            return LocalDate.parse(teks.trim(), TGL);
        } catch (DateTimeParseException e) {
            throw new KosException("Format tanggal harus dd-MM-yyyy, contoh 05-11-2026.");
        }
    }
}
