package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppConfig
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val appConfig by viewModel.appConfig.collectAsState()
    val bluetoothDevices by viewModel.bluetoothDevices.collectAsState()
    val isScanningBluetooth by viewModel.isScanningBluetooth.collectAsState()
    val lastPrintedJob by viewModel.lastPrintedJob.collectAsState()
    val cloudBackups by viewModel.cloudBackups.collectAsState()

    var showBackupLoading by remember { mutableStateOf(false) }
    var showBackupSuccessSnackbar by remember { mutableStateOf(false) }
    var showSettingsGooglePrompt by remember { mutableStateOf(false) }

    // Forms
    var companyName by remember { mutableStateOf(appConfig?.companyName ?: "Toca do Espeto Ltda") }
    var cnpj by remember { mutableStateOf(appConfig?.cnpj ?: "12.345.678/0001-90") }
    var ie by remember { mutableStateOf(appConfig?.ie ?: "123.456.789.111") }
    var taxRegime by remember { mutableStateOf(appConfig?.taxRegime ?: "Simples Nacional") }
    var printerMac by remember { mutableStateOf(appConfig?.bluetoothPrinterMac ?: "") }
    var bioLogin by remember { mutableStateOf(appConfig?.biometricLoginEnabled ?: false) }

    LaunchedEffect(appConfig) {
        appConfig?.let {
            companyName = it.companyName
            cnpj = it.cnpj
            ie = it.ie
            taxRegime = it.taxRegime
            printerMac = it.bluetoothPrinterMac
            bioLogin = it.biometricLoginEnabled
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurações Gerais", fontWeight = FontWeight.Bold, color = GoldAccent) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = GoldAccent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CharcoalBlack)
            )
        },
        containerColor = CharcoalBlack
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Empresa / Dados Fiscais Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkGraySurface)
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "DADOS FISCAIS DA EMPRESA",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = GoldAccent)
                    )

                    OutlinedTextField(
                        value = companyName,
                        onValueChange = { companyName = it },
                        label = { Text("Razão Social (Empresa)", color = LightGray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldAccent, unfocusedBorderColor = LightGray.copy(alpha = 0.3f), focusedTextColor = OffWhite, unfocusedTextColor = OffWhite),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = cnpj,
                        onValueChange = { cnpj = it },
                        label = { Text("CNPJ", color = LightGray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldAccent, unfocusedBorderColor = LightGray.copy(alpha = 0.3f), focusedTextColor = OffWhite, unfocusedTextColor = OffWhite),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = ie,
                        onValueChange = { ie = it },
                        label = { Text("Inscrição Estadual (IE)", color = LightGray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldAccent, unfocusedBorderColor = LightGray.copy(alpha = 0.3f), focusedTextColor = OffWhite, unfocusedTextColor = OffWhite),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = taxRegime,
                        onValueChange = { taxRegime = it },
                        label = { Text("Regime Tributário", color = LightGray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldAccent, unfocusedBorderColor = LightGray.copy(alpha = 0.3f), focusedTextColor = OffWhite, unfocusedTextColor = OffWhite),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            // 2. Impressora Bluetooth Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkGraySurface)
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "IMPRESSORA TÉRMICA (BLUETOOTH)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = GoldAccent)
                    )

                    if (printerMac.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Print, contentDescription = null, tint = SuccessGreen)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Pareada: $printerMac",
                                    color = OffWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            IconButton(onClick = { printerMac = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Desconectar", tint = ErrorRed)
                            }
                        }
                    } else {
                        Text(
                            text = "Nenhuma impressora bluetooth pareada no momento.",
                            color = LightGray,
                            fontSize = 12.sp
                        )
                    }

                    // Scan Bluetooth printers button
                    Button(
                        onClick = { viewModel.scanBluetoothPrinters() },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkRedPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("scan_printers_button"),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isScanningBluetooth
                    ) {
                        if (isScanningBluetooth) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Buscando aparelhos...")
                        } else {
                            Icon(Icons.Default.Bluetooth, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("BUSCAR IMPRESSORAS BLUETOOTH")
                        }
                    }

                    // Display scanned printers list
                    if (bluetoothDevices.isNotEmpty()) {
                        Text("Dispositivos encontrados:", color = GoldAccent, style = MaterialTheme.typography.labelSmall)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            bluetoothDevices.forEach { dev ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF2C2C2C))
                                        .clickable { printerMac = dev }
                                        .padding(10.dp)
                                ) {
                                    Text(dev, color = OffWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // 3. Backup na Nuvem & Segurança Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkGraySurface)
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "BACKUP & SEGURANÇA",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = GoldAccent)
                    )

                    // Biometrics login switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Fingerprint, contentDescription = null, tint = GoldAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Habilitar Login Biométrico", color = OffWhite)
                        }
                        Switch(
                            checked = bioLogin,
                            onCheckedChange = { bioLogin = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = GoldAccent, checkedTrackColor = DarkRedPrimary)
                        )
                    }

                    HorizontalDivider(color = LightGray.copy(alpha = 0.1f))

                    Text("Sincronização com o Google Cloud:", color = GoldAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    if (appConfig?.isGoogleConnected == true) {
                        // Display connected account card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1E1E1E))
                                .border(1.dp, SuccessGreen.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(18.dp))
                                                .background(Color(0xFF4285F4)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = (appConfig?.googleAccountName?.take(1) ?: "G").uppercase(),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = appConfig?.googleAccountName ?: "",
                                                color = OffWhite,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                text = appConfig?.googleAccountEmail ?: "",
                                                color = LightGray,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(SuccessGreen.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "ATIVO",
                                            color = SuccessGreen,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    horizontalArrangement = Arrangement.End,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    TextButton(
                                        onClick = { viewModel.disconnectGoogleAccount() },
                                        colors = ButtonDefaults.textButtonColors(contentColor = ErrorRed),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Icon(Icons.Default.ExitToApp, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Desconectar Conta", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Google Drive Backup Trigger
                        Button(
                            onClick = {
                                showBackupLoading = true
                                viewModel.triggerGoogleDriveBackup(appConfig?.googleAccountEmail ?: "positivo15089@gmail.com") {
                                    showBackupLoading = false
                                    showBackupSuccessSnackbar = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("backup_drive_button"),
                            shape = RoundedCornerShape(8.dp),
                            enabled = !showBackupLoading
                        ) {
                            if (showBackupLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Sincronizando base Room offline...")
                            } else {
                                Icon(Icons.Default.CloudUpload, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("EFETUAR BACKUP NO GOOGLE DRIVE")
                            }
                        }

                        // Show list of recent backups on cloud
                        if (cloudBackups.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Backups Recentes no Google Cloud:", color = LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                cloudBackups.forEach { backup ->
                                    val formattedTime = java.text.SimpleDateFormat("dd/MM HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(backup.timestamp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF252525))
                                            .padding(8.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.CloudDone, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(backup.fileName, color = OffWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                Text("Sincronizado: $formattedTime", color = LightGray, fontSize = 9.sp)
                                            }
                                        }
                                        Text(backup.size, color = GoldAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                    } else {
                        // Display google connection CTA
                        Text(
                            text = "Conecte sua conta do Google para sincronizar automaticamente seus dados fiscais, produtos, clientes e mesas com segurança em tempo real na nuvem.",
                            color = LightGray,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )

                        Button(
                            onClick = { showSettingsGooglePrompt = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFF1F1F1F)
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(45.dp)
                                .testTag("settings_google_connect_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                androidx.compose.material3.Icon(
                                    painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_google),
                                    contentDescription = "Google Logo",
                                    modifier = Modifier.size(18.dp),
                                    tint = Color.Unspecified
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "CONECTAR CONTA DO GOOGLE",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        // Disabled backup button
                        Button(
                            onClick = {},
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Gray.copy(alpha = 0.15f),
                                contentColor = LightGray.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            enabled = false
                        ) {
                            Icon(Icons.Default.CloudOff, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("EFETUAR BACKUP (REQUER CONEXÃO)")
                        }
                    }
                }
            }

            // Action save button
            Button(
                onClick = {
                    viewModel.saveConfig(
                        AppConfig(
                            companyName = companyName,
                            cnpj = cnpj,
                            ie = ie,
                            taxRegime = taxRegime,
                            bluetoothPrinterMac = printerMac,
                            biometricLoginEnabled = bioLogin,
                            googleDriveBackupEnabled = appConfig?.googleDriveBackupEnabled ?: false
                        )
                    )
                    onNavigateBack()
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = CharcoalBlack),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_config_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("SALVAR CONFIGURAÇÕES", fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
            }
        }
    }

    // Backup Success Dialog / Snackbar simulator
    if (showBackupSuccessSnackbar) {
        AlertDialog(
            onDismissRequest = { showBackupSuccessSnackbar = false },
            icon = { Icon(Icons.Default.CloudDone, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(48.dp)) },
            title = { Text("Backup Concluído com Sucesso", color = OffWhite, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "A base de dados SQLite (Room) e as configurações de login foram sincronizadas na sua pasta segura do Google Drive com criptografia de ponta-a-ponta.",
                    color = LightGray,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = { showBackupSuccessSnackbar = false },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    Text("Ok")
                }
            },
            containerColor = DarkGraySurface
        )
    }

    // Printer simulation output notification
    lastPrintedJob?.let { jobText ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 32.dp, start = 16.dp, end = 16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bluetooth, contentDescription = null, tint = SuccessGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Imprimindo via Bluetooth...", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Icon(Icons.Default.Check, contentDescription = null, tint = SuccessGreen)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = jobText,
                        color = Color.DarkGray,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                    )
                }
            }
        }
    }

    // Google Account Selector Dialog in Settings Screen
    if (showSettingsGooglePrompt) {
        AlertDialog(
            onDismissRequest = { showSettingsGooglePrompt = false },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    androidx.compose.material3.Icon(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_google),
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = Color.Unspecified
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Conectar conta do Google",
                        color = OffWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "para ativar backups automáticos",
                        color = LightGray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    // Account 1: User's actual email
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF2C2C2C))
                            .border(1.dp, GoldAccent.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.connectGoogleAccount(
                                    name = "Positivo",
                                    email = "positivo15089@gmail.com",
                                    avatarUrl = "",
                                    onComplete = {
                                        showSettingsGooglePrompt = false
                                    }
                                )
                            }
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color(0xFF4285F4)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("P", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Positivo", color = OffWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("positivo15089@gmail.com", color = LightGray, fontSize = 12.sp)
                            }
                        }
                    }

                    // Account 2: Admin
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF2C2C2C))
                            .clickable {
                                viewModel.connectGoogleAccount(
                                    name = "Toca Admin",
                                    email = "admin.tocadoespeto@gmail.com",
                                    avatarUrl = "",
                                    onComplete = {
                                        showSettingsGooglePrompt = false
                                    }
                                )
                            }
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color(0xFFEA4335)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("T", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Toca Admin", color = OffWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("admin.tocadoespeto@gmail.com", color = LightGray, fontSize = 12.sp)
                            }
                        }
                    }

                    Text(
                        "Ao se conectar, você autoriza o aplicativo Toca do Espeto a criar uma pasta oculta de dados de aplicativos no seu Google Drive para gerenciar os arquivos de backup com segurança.",
                        fontSize = 11.sp,
                        color = LightGray.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = { showSettingsGooglePrompt = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = GoldAccent)
                ) {
                    Text("Cancelar")
                }
            },
            containerColor = DarkGraySurface
        )
    }
}
