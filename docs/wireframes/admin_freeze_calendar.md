# Wireframe: Admin - Rental Freeze/Maintenance Calendar

## Layout Description

```
+----------------------------------------------------+
|  Header                                           ← |
|  Kalender Freeze Rental                            |
+----------------------------------------------------+
|                                                    |
|  [Month Navigation]                                |
|  <  Agustus 2025  >                                |
|                                                    |
|  [Calendar View]                                   |
|  Min Sen Sel Rab Kam Jum Sab                       |
|   27  28  29  30  31   1   2                       |
|    3   4   5   6   7   8   9                       |
|   10  11  12  13  14  15  16                       |
|   17  18  19  20  21  22  23                       |
|   24  25  26  27  28  29  30                       |
|   31   1   2   3   4   5   6                       |
|                                                    |
|  Keterangan:                                       |
|  [■] Tanggal saat ini                              |
|  [█] Tanggal freeze                                |
|  [░] Tanggal tersedia                              |
|                                                    |
|  [Pilih Tanggal untuk Freeze]                      |
|                                                    |
|  Tanggal Freeze Aktif:                             |
|                                                    |
|  +----------------------------------------------+  |
|  | 15-20 September 2025                         |  |
|  | Libur Lebaran                                 |  |
|  | [Batalkan]                                   |  |
|  +----------------------------------------------+  |
|                                                    |
|  +----------------------------------------------+  |
|  | 25-30 Desember 2025                          |  |
|  | Libur Natal & Tahun Baru                     |  |
|  | [Batalkan]                                   |  |
|  +----------------------------------------------+  |
|                                                    |
|  Alasan Freeze:                                    |
|  [Libur Lebaran               ]                   |
|                                                    |
+----------------------------------------------------+
|  Navigation Bar                                    |
|  📊 Dashboard  🚗 Mobil  📅 Kalender  ⚙️ Pengaturan |
+----------------------------------------------------+
```

## UI Elements

1. **Header**
   - Back button (←)
   - Page title "Kalender Freeze Rental"

2. **Month Navigation**
   - Previous month button (<)
   - Current month and year display
   - Next month button (>)

3. **Calendar View**
   - Day headers (Min, Sen, Sel, Rab, Kam, Jum, Sab)
   - Calendar grid showing dates
   - Current date highlighted (■)
   - Frozen dates marked (█)
   - Available dates marked (░)

4. **Legend**
   - Explanation of color coding
   - Current date indicator
   - Frozen date indicator
   - Available date indicator

5. **Action Button**
   - "Pilih Tanggal untuk Freeze" button

6. **Active Freeze Dates List**
   - List of active freeze periods with:
     - Date range
     - Reason for freeze
     - Cancel button

7. **Reason Input**
   - Text input for specifying the reason for freeze

8. **Navigation Bar**
   - Dashboard (📊)
   - Cars (🚗)
   - Calendar (📅)
   - Settings (⚙️)