# Sistem Reservasi & Pengelolaan Manajemen Kos-kosan (Java Swing + JDBC + MySQL)

## Menjalankan
1. Aktifkan MySQL, lalu: `mysql -u root < sql/db_kos.sql`
2. Unduh MySQL Connector/J (mysql-connector-j-8.x.jar) ke folder `lib/`
3. Kompilasi (Linux/Mac): `javac -encoding UTF-8 -d out $(find src -name "*.java")`
   Windows (cmd): `dir /s /b src\*.java > sources.txt` lalu `javac -encoding UTF-8 -d out @sources.txt`
4. Jalankan: `java -cp "out:lib/*" kos.Main`   (Windows: `out;lib/*`)
   Uji koneksi saja: `java -cp "out:lib/*" kos.UjiKoneksi`

User/password bawaan: root / (kosong). Ubah lewat environment variable
KOS_DB_USER, KOS_DB_PASS, KOS_DB_URL. Bisa juga dibuka sebagai proyek di NetBeans/IntelliJ
(folder `src`, tambahkan JAR Connector/J sebagai library).

Folder `tools/` berisi skrip pengambil screenshot otomatis (opsional, hanya untuk dokumentasi).
