
CREATE DATABASE IF NOT EXISTS db_kos CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE db_kos;

DROP TABLE IF EXISTS tagihan;
DROP TABLE IF EXISTS reservasi;
DROP TABLE IF EXISTS penghuni;
DROP TABLE IF EXISTS kamar;

-- Tabel kamar: satu baris = satu kamar fisik. Kolom 'tipe' menentukan subclass di Java.
CREATE TABLE kamar (
    nomor        VARCHAR(10)   NOT NULL PRIMARY KEY,
    lantai       TINYINT       NOT NULL,
    tipe         ENUM('AC','NON_AC','ENSUITE') NOT NULL,
    tarif_dasar  DECIMAL(12,2) NOT NULL,
    wifi_aktif   TINYINT(1)    NOT NULL DEFAULT 0,
    status       ENUM('KOSONG','TERISI') NOT NULL DEFAULT 'KOSONG'
) ENGINE=InnoDB;

-- Tabel penghuni: pencatatan identitas (NIK unik).
CREATE TABLE penghuni (
    id           INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nama         VARCHAR(100) NOT NULL,
    nik          CHAR(16)     NOT NULL UNIQUE,
    no_hp        VARCHAR(20)  NOT NULL,
    alamat_asal  VARCHAR(200),
    pekerjaan    VARCHAR(60)
) ENGINE=InnoDB;

-- Tabel reservasi: relasi penghuni <-> kamar beserta masa huni.
CREATE TABLE reservasi (
    id           INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_penghuni  INT          NOT NULL,
    nomor_kamar  VARCHAR(10)  NOT NULL,
    tgl_mulai    DATE         NOT NULL,
    tgl_selesai  DATE         NULL,
    status       ENUM('AKTIF','SELESAI') NOT NULL DEFAULT 'AKTIF',
    CONSTRAINT fk_res_penghuni FOREIGN KEY (id_penghuni) REFERENCES penghuni(id),
    CONSTRAINT fk_res_kamar    FOREIGN KEY (nomor_kamar) REFERENCES kamar(nomor)
) ENGINE=InnoDB;

-- Tabel tagihan: satu tagihan per reservasi per periode (YYYY-MM).
-- Tunggakan = tagihan yang tgl_bayar-nya masih NULL dan jatuh_tempo sudah lewat.
CREATE TABLE tagihan (
    id            INT           NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_reservasi  INT           NOT NULL,
    periode       CHAR(7)       NOT NULL,
    jumlah        DECIMAL(12,2) NOT NULL,
    jatuh_tempo   DATE          NOT NULL,
    tgl_bayar     DATE          NULL,
    UNIQUE KEY uq_tagihan_periode (id_reservasi, periode),
    CONSTRAINT fk_tagihan_res FOREIGN KEY (id_reservasi) REFERENCES reservasi(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ------------------------------- DATA AWAL ---------------------------
INSERT INTO kamar (nomor, lantai, tipe, tarif_dasar, wifi_aktif, status) VALUES
 ('101',1,'NON_AC',  800000,1,'TERISI'),
 ('102',1,'NON_AC',  800000,0,'KOSONG'),
 ('103',1,'NON_AC',  850000,1,'TERISI'),
 ('104',1,'AC',     1200000,1,'TERISI'),
 ('105',1,'AC',     1200000,1,'TERISI'),
 ('106',1,'AC',     1250000,0,'KOSONG'),
 ('201',2,'AC',     1300000,1,'TERISI'),
 ('202',2,'AC',     1300000,1,'KOSONG'),
 ('203',2,'ENSUITE',1800000,1,'TERISI'),
 ('204',2,'ENSUITE',1800000,1,'KOSONG'),
 ('205',2,'ENSUITE',1900000,1,'TERISI'),
 ('206',2,'ENSUITE',1900000,1,'KOSONG');

INSERT INTO penghuni (nama, nik, no_hp, alamat_asal, pekerjaan) VALUES
 ('Aditya Pratama',  '3171010101000001','081211110001','Jakarta Selatan','Mahasiswa'),
 ('Bunga Lestari',   '3273020202000002','081211110002','Bandung','Mahasiswa'),
 ('Candra Wijaya',   '3374030303000003','081211110003','Semarang','Karyawan Swasta'),
 ('Dewi Anggraini',  '3578040404000004','081211110004','Surabaya','Desainer Grafis'),
 ('Eko Saputra',     '1271050505000005','081211110005','Medan','Mahasiswa'),
 ('Fitri Handayani', '5171060606000006','081211110006','Denpasar','Perawat'),
 ('Gilang Ramadhan', '7371070707000007','081211110007','Makassar','Programmer'),
 ('Hana Kurniasih',  '3471080808000008','081211110008','Yogyakarta','Mahasiswa'),
 ('Indra Setiawan',  '1371090909000009','081211110009','Padang','Wirausaha');

INSERT INTO reservasi (id_penghuni, nomor_kamar, tgl_mulai, tgl_selesai, status) VALUES
 (1,'101', CURDATE() - INTERVAL 5 MONTH, NULL, 'AKTIF'),
 (2,'103', CURDATE() - INTERVAL 4 MONTH, NULL, 'AKTIF'),
 (3,'104', CURDATE() - INTERVAL 3 MONTH, NULL, 'AKTIF'),
 (4,'105', CURDATE() - INTERVAL 2 MONTH, NULL, 'AKTIF'),
 (5,'201', CURDATE() - INTERVAL 2 MONTH, NULL, 'AKTIF'),
 (6,'203', CURDATE() - INTERVAL 1 MONTH, NULL, 'AKTIF'),
 (7,'205', CURDATE() - INTERVAL 1 MONTH, NULL, 'AKTIF'),
 (8,'102', CURDATE() - INTERVAL 6 MONTH, CURDATE() - INTERVAL 2 MONTH, 'SELESAI');

-- Tagihan 3 bulan terakhir (jatuh tempo tanggal 5) untuk reservasi aktif.
INSERT INTO tagihan (id_reservasi, periode, jumlah, jatuh_tempo, tgl_bayar)
SELECT r.id, DATE_FORMAT(p.tgl,'%Y-%m'),
       k.tarif_dasar + CASE k.tipe
            WHEN 'NON_AC'  THEN 100000 + 50000 * k.wifi_aktif
            WHEN 'AC'      THEN 200000 + 50000 * k.wifi_aktif
            ELSE                275000 + 75000 * k.wifi_aktif END,
       DATE_FORMAT(p.tgl,'%Y-%m-05'), NULL
FROM reservasi r
JOIN kamar k ON k.nomor = r.nomor_kamar
CROSS JOIN (SELECT CURDATE() - INTERVAL 2 MONTH AS tgl
            UNION SELECT CURDATE() - INTERVAL 1 MONTH
            UNION SELECT CURDATE()) p
WHERE r.status = 'AKTIF'
  AND DATE_FORMAT(r.tgl_mulai,'%Y-%m') <= DATE_FORMAT(p.tgl,'%Y-%m');

-- Simulasi pembayaran: periode lama lunas, sebagian periode berjalan menunggak.
UPDATE tagihan SET tgl_bayar = jatuh_tempo - INTERVAL 1 DAY
 WHERE periode < DATE_FORMAT(CURDATE() - INTERVAL 1 MONTH,'%Y-%m');
UPDATE tagihan SET tgl_bayar = jatuh_tempo + INTERVAL 1 DAY
 WHERE periode = DATE_FORMAT(CURDATE() - INTERVAL 1 MONTH,'%Y-%m') AND id_reservasi IN (1,2,3,5);
UPDATE tagihan SET tgl_bayar = jatuh_tempo
 WHERE periode = DATE_FORMAT(CURDATE(),'%Y-%m') AND id_reservasi IN (1,3);
