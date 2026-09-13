package com.paybridge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.paybridge.ui.onboarding.PermissionSetupScreen
import com.paybridge.ui.onboarding.TrustScreen
import com.paybridge.ui.theme.PayBridgeTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as PayBridgeApp

        setContent {
            PayBridgeTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PayBridgeNavHost(app)
                }
            }
        }
    }
}

@Composable
private fun PayBridgeNavHost(app: PayBridgeApp) {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()
    val listenerEnabled by app.listenerStatusRepository.status.collectAsState()

    // Read once, before NavHost mounts — startDestination is fixed at first composition and
    // isn't reactive, so a returning user must get the real value up front rather than a
    // default that briefly routes them back through onboarding.
    val hasSeenTrustScreen by produceState<Boolean?>(initialValue = null) {
        value = app.onboardingPreferences.hasSeenTrustScreen.first()
    }

    // Re-check listener status every time the app resumes, not just once at grant time — an
    // OEM privacy-cleanup prompt or the shopkeeper themselves can revoke access at any point.
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                coroutineScope.launch { app.listenerStatusRepository.checkNow() }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
    }

    val seenTrustScreen = hasSeenTrustScreen ?: return // preferences not loaded yet

    NavHost(
        navController = navController,
        startDestination = if (seenTrustScreen) "home" else "trust",
    ) {
        composable("trust") {
            TrustScreen(onContinue = {
                coroutineScope.launch {
                    app.onboardingPreferences.markTrustScreenSeen()
                    navController.navigate("permission") { popUpTo("trust") { inclusive = true } }
                }
            })
        }
        composable("permission") {
            PermissionSetupScreen(
                isEnabled = listenerEnabled,
                onContinue = {
                    navController.navigate("home") { popUpTo("permission") { inclusive = true } }
                },
            )
        }
        composable("home") {
            // Replaced by the real HomeScreen (listener banner, unparsed-notification warning,
            // pending claims, "+ New Expected Payment") in the next commit.
            PlaceholderHome()
        }
    }
}

@Composable
private fun PlaceholderHome() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("PayBridge — home screen coming up next")
    }
}
