# Wireframe: User - Edit Booking

## Layout Description

```
+----------------------------------------------------+
|  Header                                           ← |
|  Edit Booking                                      |
+----------------------------------------------------+
|                                                    |
|  [Car Information]                                 |
|  Toyota Brio                                       |
|  Rp 500.000/hari                                   |
|                                                    |
|  Tanggal Sewa:                                     |
|  [📅 25 Agustus 2025]  →  [📅 28 Agustus 2025]      |
|  (4 hari)                                          |
|                                                    |
|  Waktu Pengambilan:                                |
|  [⏰ 10:00]                                        |
|                                                    |
|  Lokasi Pengambilan:                               |
|  [📍 Cibubur]                                      |
|                                                    |
|  Waktu Pengembalian:                               |
|  [⏰ 18:00]                                        |
|                                                    |
|  Lokasi Pengembalian:                              |
|  [📍 Cibubur]                                      |
|                                                    |
|  Driver:                                           |
|  ( ) Dengan Driver  (+ Rp 150.000/hari)            |
|  (●) Tanpa Driver                                  |
|                                                    |
|  Informasi Tambahan:                               |
|  [Tulis catatan tambahan untuk penyewa...]         |
|                                                    |
|  Ringkasan Biaya:                                  |
|  Biaya Sewa:            Rp 2.000.000               |
|  Biaya Driver:          Rp 0                       |
|  -------------------------------------             |
|  Total:                 Rp 2.000.000               |
|                                                    |
|  [Batal]  [Simpan Perubahan]                       |
|                                                    |
+----------------------------------------------------+
|  Navigation Bar                                    |
|  🏠 Beranda  📅 Kalender  📋 Riwayat  👤 Profil    |
+----------------------------------------------------+
```

## UI Elements

1. **Header**
   - Back button (←)
   - Page title "Edit Booking"

2. **Car Information**
   - Car name/model (non-editable)
   - Daily rental price (non-editable)

3. **Rental Date Selection**
   - Start date picker (📅) - editable
   - End date picker (📅) - editable
   - Duration display (number of days) - updates automatically

4. **Pickup Time**
   - Time picker (⏰) - editable
   - Default time (10:00 in this example)

5. **Pickup Location**
   - Location selector (📍) - editable
   - Default location (Cibubur)

6. **Return Time**
   - Time picker (⏰) - editable
   - Default time (18:00 in this example)

7. **Return Location**
   - Location selector (📍) - editable
   - Default location (Cibubur)

8. **Driver Option**
   - Radio buttons for driver selection - editable
   - With driver option (+ Rp 150.000/hari)
   - Without driver option (default selected)

9. **Additional Information**
   - Text input for additional notes - editable

10. **Cost Summary**
    - Rental cost breakdown (updates automatically)
    - Driver cost (if applicable)
    - Total cost (updates automatically)

11. **Action Buttons**
    - "Batal" button to cancel changes
    - "Simpan Perubahan" button to save changes

12. **Navigation Bar**
    - Home (🏠)
    - Calendar (📅)
    - History (📋)
    - Profile (👤)