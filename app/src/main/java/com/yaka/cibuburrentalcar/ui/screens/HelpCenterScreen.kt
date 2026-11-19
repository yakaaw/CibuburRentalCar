package com.yaka.cibuburrentalcar.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ContactSupport
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.yaka.cibuburrentalcar.ui.theme.MediumGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpCenterScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val helpItems = listOf(
        HelpItem(
            icon = Icons.Default.DirectionsCar,
            title = "Cara Memesan Mobil",
            description = "Panduan lengkap untuk memesan mobil rental",
            steps = listOf(
                "1. Pilih mobil yang diinginkan dari katalog",
                "2. Tentukan tanggal mulai dan selesai sewa",
                "3. Isi data diri dan informasi kontak",
                "4. Konfirmasi pesanan melalui Whatsapp",
                "5. Tunggu verifikasi",
                "6. Mobil siap diambil sesuai jadwal"
            )
        ),
        HelpItem(
            icon = Icons.Default.Payment,
            title = "Metode Pembayaran",
            description = "Cara pembayaran yang tersedia",
            steps = listOf(
                "• Transfer Bank (BCA, Mandiri, BRI, BNI)",
                "• E-Wallet (GoPay, OVO, DANA, ShopeePay)",
                "• Bayar di Tempat (Cash/Kartu)"
            )
        ),
        HelpItem(
            icon = Icons.Default.Schedule,
            title = "Syarat & Ketentuan",
            description = "Persyaratan untuk menyewa mobil",
            steps = listOf(
                "• Usia minimal 21 tahun",
                "• Memiliki SIM A yang masih berlaku",
                "• KTP/Identitas yang masih berlaku",
                "• Tidak boleh dibawa keluar kota tanpa izin",
                "• Wajib mengembalikan dalam kondisi bersih"
            )
        ),
        HelpItem(
            icon = Icons.Default.Support,
            title = "Bantuan & Dukungan",
            description = "Hubungi kami jika ada masalah",
            steps = listOf(
                "📞 Telepon: 0851-5777-8099",
                "💬 WhatsApp: 0851-5777-8099",
                "🕒 Jam Operasional: 08:00 - 22:00 WIB",
                "📍 Alamat: Jl. Alternatif Cibubur No. 123"
            )
        ),
        HelpItem(
            icon = Icons.Default.Cancel,
            title = "Pembatalan Pesanan",
            description = "Cara membatalkan pesanan rental",
            steps = listOf(
                "• Pembatalan H-1: Gratis",
                "• Pembatalan H-0: Dikenakan biaya 20%",
                "• Pembatalan setelah ambil: Tidak dapat dibatalkan",
                "• Hubungi customer service untuk pembatalan",
                "• Refund akan diproses 1-3 hari kerja"
            )
        ),
        HelpItem(
            icon = Icons.Default.Security,
            title = "Keamanan & Asuransi",
            description = "Perlindungan selama masa sewa",
            steps = listOf(
                "• Semua mobil diasuransikan",
                "• Asuransi mencakup kecelakaan & kehilangan",
                "• Tidak menanggung kelalaian pengemudi",
                "• Wajib lapor polisi jika terjadi kecelakaan & kehilangan",
                "• Biaya perbaikan ditanggung sesuai kesepakatan"
            )
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header diseragamkan
        com.yaka.cibuburrentalcar.ui.components.AppTopBar(
            title = "Pusat Bantuan",
            onBackClick = null,
            centerTitle = true
        )
        Column(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Temukan jawaban untuk pertanyaan Anda",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // Content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(helpItems) { helpItem ->
                HelpItemCard(helpItem = helpItem)
            }
            
            // Contact Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ContactSupport,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Masih Butuh Bantuan?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "Tim customer service kami siap membantu Anda 24/7",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = {
                                // Open WhatsApp with predefined message
                                val phoneNumber = "6285157778099" // Format internasional tanpa +
                                val message = "Halo Admin Cibubur Rental Car, saya butuh bantuan terkait layanan rental mobil."
                                val whatsappUrl = "https://wa.me/$phoneNumber?text=${Uri.encode(message)}"

                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(whatsappUrl))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Hubungi Kami")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HelpItemCard(
    helpItem: HelpItem,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = helpItem.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = helpItem.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Text(
                        text = helpItem.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MediumGray
                    )
                }
                
                IconButton(
                    onClick = { expanded = !expanded }
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Tutup" else "Buka",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        helpItem.steps.forEach { step ->
                            Text(
                                text = step,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

data class HelpItem(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val steps: List<String>
)
