# Wireframe: User - Riwayat Penyewaan (Rental History)

## Layout Description

```
+----------------------------------------------------+
|  Header                                           ← |
|  Riwayat Penyewaan                                 |
+----------------------------------------------------+
|                                                    |
|  [Filter Options]                                  |
|  [Semua] [Aktif] [Selesai] [Dibatalkan]            |
|                                                    |
|  [Booking List]                                    |
|                                                    |
|  +----------------------------------------------+  |
|  | Toyota Brio                                  |  |
|  | Booking ID: #BR20250825001                   |  |
|  | Tanggal: 25-27 Agustus 2025                  |  |
|  | Status: Aktif                                |  |
|  | Total: Rp 1.500.000                          |  |
|  | [Lihat Detail]  [Batalkan]                   |  |
|  +----------------------------------------------+  |
|                                                    |
|  +----------------------------------------------+  |
|  | Honda BRV                                    |  |
|  | Booking ID: #BR20250820001                   |  |
|  | Tanggal: 20-23 Agustus 2025                  |  |
|  | Status: Selesai                              |  |
|  | Total: Rp 2.100.000                          |  |
|  | [Lihat Detail]                               |  |
|  +----------------------------------------------+  |
|                                                    |
|  +----------------------------------------------+  |
|  | Daihatsu Xenia                               |  |
|  | Booking ID: #BR20250815001                   |  |
|  | Tanggal: 15-18 Agustus 2025                  |  |
|  | Status: Dibatalkan                           |  |
|  | Total: Rp 1.800.000                          |  |
|  | [Lihat Detail]                               |  |
|  +----------------------------------------------+  |
|                                                    |
|  [Booking Kosong]                                  |
|  Anda belum memiliki riwayat penyewaan           |
|                                                    |
+----------------------------------------------------+
|  Navigation Bar                                    |
|  🏠 Beranda  📅 Kalender  📋 Riwayat  👤 Profil    |
+----------------------------------------------------+
```

## UI Elements

1. **Header**
   - Back button (←)
   - Page title "Riwayat Penyewaan"

2. **Filter Options**
   - Tabs to filter bookings by status
   - Options: Semua (All), Aktif (Active), Selesai (Completed), Dibatalkan (Cancelled)

3. **Booking List**
   - List of booking cards showing:
     - Car name/model
     - Booking ID
     - Rental dates
     - Status
     - Total cost
     - Action buttons (Lihat Detail, Batalkan)

4. **Empty State**
   - Message when there are no bookings
   - "Booking Kosong" with description

5. **Navigation Bar**
   - Home (🏠)
   - Calendar (📅)
   - History (📋)
   - Profile (👤)