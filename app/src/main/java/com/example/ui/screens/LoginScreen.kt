package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun LoginScreen(
    viewModel: MainViewModel,
    onLoginSuccess: () -> Unit
) {
    var username by remember { mutableStateOf("admin") }
    var password by remember { mutableStateOf("123456") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showBioPrompt by remember { mutableStateOf(false) }
    var showGooglePrompt by remember { mutableStateOf(false) }
    val appConfig by viewModel.appConfig.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        CharcoalBlack,
                        DarkRedSecondary,
                        CharcoalBlack
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .widthIn(max = 450.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(DarkGraySurface)
                .border(1.dp, GoldAccent.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Stylized Logo Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(DarkRedPrimary, CharcoalBlack)
                        )
                    )
                    .border(2.dp, GoldAccent, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageName = Icons.Default.LocalFireDepartment,
                    contentDescription = "Logo Toca do Espeto",
                    modifier = Modifier.size(48.dp),
                    tint = GoldAccent
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "TOCA DO ESPETO",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = GoldAccent,
                    letterSpacing = 2.sp
                ),
                textAlign = TextAlign.Center
            )

            Text(
                text = "SISTEMA INTEGRADO DE PDV & FISCAL",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = OffWhite.copy(alpha = 0.6f),
                    letterSpacing = 1.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Username Input
            OutlinedTextField(
                value = username,
                onValueChange = {
                    username = it
                    errorMessage = null
                },
                label = { Text("Usuário", color = LightGray) },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = GoldAccent) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldAccent,
                    unfocusedBorderColor = LightGray.copy(alpha = 0.5f),
                    focusedLabelColor = GoldAccent,
                    unfocusedLabelColor = LightGray,
                    focusedTextColor = OffWhite,
                    unfocusedTextColor = OffWhite
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("username_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Input
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = null
                },
                label = { Text("Senha", color = LightGray) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = GoldAccent) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle password visibility",
                            tint = LightGray
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldAccent,
                    unfocusedBorderColor = LightGray.copy(alpha = 0.5f),
                    focusedLabelColor = GoldAccent,
                    unfocusedLabelColor = LightGray,
                    focusedTextColor = OffWhite,
                    unfocusedTextColor = OffWhite
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("password_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            errorMessage?.let {
                Text(
                    text = it,
                    color = ErrorRed,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Login Button
            Button(
                onClick = {
                    if (username.trim() == "admin" && password == "123456") {
                        onLoginSuccess()
                    } else {
                        errorMessage = "Usuário ou senha incorretos."
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkRedPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("login_button")
            ) {
                Text(
                    text = "ACESSAR SISTEMA",
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (appConfig?.isGoogleConnected == true) {
                // Quick Login
                OutlinedButton(
                    onClick = {
                        onLoginSuccess()
                    },
                    border = borderStrokeOrNull(1.dp, GoldAccent),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = OffWhite),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("quick_google_login_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(GoldAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (appConfig?.googleAccountName?.take(1) ?: "G").uppercase(),
                                color = CharcoalBlack,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "ENTRAR COMO ${appConfig?.googleAccountName?.uppercase()}",
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = LightGray.copy(alpha = 0.2f))
                Text(
                    text = "OU CONECTAR",
                    color = LightGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp),
                    letterSpacing = 1.sp
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = LightGray.copy(alpha = 0.2f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Google Login Button
            Button(
                onClick = { showGooglePrompt = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF1F1F1F)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("google_login_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    androidx.compose.material3.Icon(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_google),
                        contentDescription = "Google Logo",
                        modifier = Modifier.size(20.dp),
                        tint = Color.Unspecified
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "ENTRAR COM O GOOGLE",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Biometric Simulation Trigger
            OutlinedButton(
                onClick = { showBioPrompt = true },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = GoldAccent
                ),
                border = borderStrokeOrNull(1.dp, GoldAccent),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("biometric_login_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Biometria",
                        tint = GoldAccent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ENTRAR COM BIOMETRIA",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }

    // Simulated Biometrics Prompt Dialog
    if (showBioPrompt) {
        AlertDialog(
            onDismissRequest = { showBioPrompt = false },
            icon = {
                Icon(
                    Icons.Default.Fingerprint,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    "Autenticação Biométrica",
                    color = OffWhite,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Toque no sensor de impressão digital do dispositivo para entrar na Toca do Espeto.",
                    color = LightGray,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showBioPrompt = false
                        onLoginSuccess()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = GoldAccent)
                ) {
                    Text("Simular Sucesso Biometria")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showBioPrompt = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = LightGray)
                ) {
                    Text("Cancelar")
                }
            },
            containerColor = DarkGraySurface,
            textContentColor = LightGray,
            titleContentColor = OffWhite
        )
    }

    // Google Account Selector Dialog
    if (showGooglePrompt) {
        AlertDialog(
            onDismissRequest = { showGooglePrompt = false },
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
                        "Fazer login com o Google",
                        color = OffWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "para vincular com a nuvem da Toca do Espeto",
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
                                        showGooglePrompt = false
                                        onLoginSuccess()
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
                                        showGooglePrompt = false
                                        onLoginSuccess()
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
                        "Para continuar, o Google compartilhará seu nome, endereço de e-mail e foto do perfil com o aplicativo Toca do Espeto.",
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
                    onClick = { showGooglePrompt = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = GoldAccent)
                ) {
                    Text("Cancelar")
                }
            },
            containerColor = DarkGraySurface
        )
    }
}

// Simple helper to avoid border errors in different versions
@Composable
fun borderStrokeOrNull(width: androidx.compose.ui.unit.Dp, color: Color) = androidx.compose.foundation.BorderStroke(width, color)

@Composable
fun Icon(imageName: androidx.compose.ui.graphics.vector.ImageVector, contentDescription: String?, modifier: Modifier = Modifier, tint: Color = LocalContentColor.current) {
    androidx.compose.material3.Icon(imageVector = imageName, contentDescription = contentDescription, modifier = modifier, tint = tint)
}
