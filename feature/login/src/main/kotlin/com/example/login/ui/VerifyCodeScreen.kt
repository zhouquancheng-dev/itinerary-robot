package com.example.login.ui

import android.app.Activity
import android.view.Gravity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.common.util.startDeepLink
import com.example.login.R
import com.example.login.components.CustomNumericKeypad
import com.example.login.components.KeypadAction
import com.example.login.components.SeparateVerificationCodeTextField
import com.example.login.vm.LoginViewModel
import com.example.ui.components.StandardCenterTopAppBar
import com.example.ui.components.VerticalSpacer
import com.example.ui.dialog.ProgressIndicatorDialog
import com.hjq.toast.ToastParams
import com.hjq.toast.Toaster
import com.hjq.toast.style.CustomToastStyle
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.ticker

@OptIn(ExperimentalMaterial3Api::class, ObsoleteCoroutinesApi::class)
@Composable
fun VerifyCodeScreen(
    loginViewModel: LoginViewModel,
    phoneNumber: String,
    onPress: () -> Unit
) {
    val context = LocalContext.current
    val verifying by loginViewModel.verifying.collectAsStateWithLifecycle()
    val maxLength = 6
    var codeValue by remember { mutableStateOf("") }

    var timerTotalSeconds by remember { mutableIntStateOf(60) }
    LaunchedEffect(Unit) {
        val ticker = ticker(delayMillis = 1000, initialDelayMillis = 0)
        for (tick in ticker) {
            if (timerTotalSeconds > 0) {
                timerTotalSeconds--
            }
        }
        ticker.cancel()
    }

    val onVerify: () -> Unit = {
        loginViewModel.verifySmsCode(
            context, phoneNumber, codeValue,
            onFailure = { codeValue = "" },
            onSuccess = {
                startDeepLink(context, "app://main")
                (context as? Activity)?.finish()
            }
        )
    }
    val onVerifyClick by rememberUpdatedState(onVerify)

    Scaffold(
        topBar = {
            StandardCenterTopAppBar(
                title = stringResource(R.string.verify_code_login),
                onPressClick = { onPress() },
                colors = {
                    TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Image(
                painter = painterResource(R.drawable.sms_verify_img),
                contentDescription = null
            )

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                VerticalSpacer(20.dp)
                Text(
                    text = stringResource(R.string.enter_sms_code),
                    fontSize = 21.sp
                )

                val startStyle = SpanStyle(color = Color.Gray)
                val endStyle = SpanStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                val annotatedString = buildAnnotatedString {
                    append(stringResource(R.string.country_code), startStyle)
                    append(phoneNumber, endStyle)
                }
                Text(
                    text = annotatedString,
                    modifier = Modifier.padding(top = 12.dp)
                )

                VerticalSpacer(25.dp)
                SeparateVerificationCodeTextField(
                    codeValue = codeValue,
                    onValueChange = { newValue ->
                        codeValue = newValue
                    },
                    codeTextLength = maxLength
                )

                // 计时文本
                val defaultStyle = SpanStyle(color = MaterialTheme.colorScheme.primary, fontSize = 18.sp)
                val otherStyle = SpanStyle(color = colorResource(R.color.resend_color), fontSize = 18.sp)
                val resendText = if (timerTotalSeconds > 0) {
                    buildAnnotatedString {
                        append("${timerTotalSeconds}秒", defaultStyle)
                        append(
                            stringResource(R.string.resend_text1),
                            SpanStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp)
                        )
                    }
                } else {
                    buildAnnotatedString {
                        append(stringResource(R.string.resend), otherStyle)
                    }
                }
                TextButton(
                    onClick = {
                        timerTotalSeconds = 60
                        loginViewModel.sendSmsCode(phoneNumber) {
                            val params = ToastParams()
                            params.text = context.getString(R.string.resend_success)
                            params.style = CustomToastStyle(R.layout.toast_custom_view_success, Gravity.CENTER)
                            Toaster.show(params)
                        }
                    },
                    enabled = timerTotalSeconds == 0,
                    modifier = Modifier.padding(top = 15.dp)
                ) {
                    Text(text = resendText)
                }
            }

            CustomNumericKeypad { action ->
                when (action) {
                    KeypadAction.Delete -> if (codeValue.isNotEmpty()) codeValue = codeValue.dropLast(1)
                    else -> if (codeValue.length < maxLength && action.value.isNotEmpty()) {
                        codeValue += action.value
                        if (codeValue.length == maxLength) {
                            onVerifyClick()
                        }
                    }
                }
            }
        }
    }

    ProgressIndicatorDialog(verifying, "正在验证")
}

private fun AnnotatedString.Builder.append(text: String, style: SpanStyle) {
    withStyle(style = style) {
        append(text)
    }
}