package com.iamnaran.firefly.ui.auth.login

import android.app.Activity.RESULT_OK
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.iamnaran.firefly.R
import com.iamnaran.firefly.ui.appcomponent.common.EmailInput
import com.iamnaran.firefly.ui.appcomponent.common.PasswordInput
import com.iamnaran.firefly.ui.appcomponent.snackbar.SnackEvent
import com.iamnaran.firefly.ui.appcomponent.snackbar.SnackbarManager
import com.iamnaran.firefly.ui.theme.AppIcons
import com.iamnaran.firefly.ui.theme.FireflyComposeTheme
import com.iamnaran.firefly.ui.theme.dimens
import com.iamnaran.firefly.utils.AppLog
import com.iamnaran.firefly.utils.effects.AppCircularProgressBar
import com.iamnaran.firefly.utils.effects.ProgressType
import com.iamnaran.firefly.utils.effects.disableMutipleTouchEvents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    navigateToHome: () -> Unit,
    navigateToSignUp: () -> Unit,
) {
    val loginState by viewModel.loginState.collectAsState()
    val coroutineScope: CoroutineScope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = { activityResult ->
            if (activityResult.resultCode == RESULT_OK) {
                coroutineScope.launch {
                    viewModel.onSignInResult(activityResult.data)
                }
            } else {
                AppLog.showLog("Error SignIn")
            }

        })

    LaunchedEffect(key1 = loginState.isLoginSuccessful) {
        if (loginState.isLoginSuccessful) {
            navigateToHome()
        }
    }

    LaunchedEffect(loginState.loginErrorState.serverErrorState.hasError) {
        if (loginState.loginErrorState.serverErrorState.hasError) {
            SnackbarManager.sendEvent(
                event = SnackEvent(
                    message = loginState.loginErrorState.serverErrorState.serverErrorMsg
                )
            )

        }
    }


    LaunchedEffect(key1 = loginState.loginErrorState.serverErrorState.hasError) {
        if (loginState.loginErrorState.serverErrorState.serverErrorMsg.isNotEmpty()) {
            coroutineScope.launch {
                SnackbarManager.sendEvent(
                    SnackEvent(loginState.loginErrorState.serverErrorState.serverErrorMsg)
                )
            }
        }
    }

    LoginContent(
        email = loginState.email,
        password = loginState.password,
        onEmailChange = {
            viewModel.handleLoginUIEvent(LoginUIEvent.EmailChanged(it))
        },
        onPasswordChange = {
            viewModel.handleLoginUIEvent(LoginUIEvent.PasswordChanged(it))
        },
        onLoginClick = {
            viewModel.handleLoginUIEvent(LoginUIEvent.OnSubmit)
        },
        onGoogleLogin = {
            coroutineScope.launch {
                launcher.launch(
                    IntentSenderRequest.Builder(
                        viewModel.signInIntentSender() ?: return@launch
                    ).build()
                )
            }
        },
        onSignUpClick = navigateToSignUp,
        isLoginProgress = loginState.isLoading
    )


}

@Composable
fun LoginContent(
    email: String,
    password: String,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onGoogleLogin: () -> Unit,
    onSignUpClick: () -> Unit,
    isLoginProgress: Boolean
) {
    val passwordFocusRequester = FocusRequester()
    val focusManager: FocusManager = LocalFocusManager.current

    Column(
        Modifier
            .padding(MaterialTheme.dimens.extraLarge)
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier
                .weight(2f)
                .padding(MaterialTheme.dimens.medium), contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_app_logo),
                contentDescription = "logo",
                Modifier.padding(10.dp)
            )
        }

        Box(
            modifier = Modifier.weight(3f),
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Column(verticalArrangement = Arrangement.Center) {
                EmailInput(
                    currentValue = email,
                    keyboardActions = KeyboardActions(onNext = { passwordFocusRequester.requestFocus() }),
                    onValueChange = onEmailChange,
                    icon = AppIcons.Email,
                    label = stringResource(id = R.string.label_email),
                )

                Spacer(modifier = Modifier.height(20.dp))

                PasswordInput(
                    currentValue = password,
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    focusRequester = passwordFocusRequester,
                    onValueChange = onPasswordChange,
                    icon = AppIcons.Password,
                    label = stringResource(id = R.string.label_password),
                )

                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = {
                        onLoginClick()
                    },
                    Modifier
                        .fillMaxWidth()
                        .disableMutipleTouchEvents()
                ) {
                    Box {
                        if (isLoginProgress) {
                            AppCircularProgressBar(progressType = ProgressType.SMALL)
                        } else {
                            Text(text = "Sign In", Modifier.padding(8.dp))

                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))



                Button(
                    onClick = {
                        onGoogleLogin()
                    },
                    Modifier
                        .fillMaxWidth()
                        .disableMutipleTouchEvents()
                ) {
                    Box {
                        Text(text = "SignIn with Google", Modifier.padding(8.dp))
                    }
                }
            }
        }

        Box(
            modifier = Modifier.weight(0.5f)
        ) {

            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Don't have an account?", color = Color.Black)
                    TextButton(onClick = {

                        onSignUpClick()

                    }) {
                        Text(text = "Sign Up")
                    }

                }
            }

        }


    }


}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    FireflyComposeTheme {

    }
}

