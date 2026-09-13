package com.paybridge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.paybridge.ui.claim.ClaimResultScreen
import com.paybridge.ui.claim.NewClaimScreen
import com.paybridge.ui.history.HistoryScreen
import com.paybridge.ui.home.HomeScreen
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
            val pendingClaims by app.claimRepository.observeAllClaims().collectAsState(initial = emptyList())
            val unparsedCounts by app.unparsedNotificationRepository.countsLast24h()
                .collectAsState(initial = emptyMap())

            HomeScreen(
                listenerEnabled = listenerEnabled,
                unparsedCounts = unparsedCounts,
                pendingClaims = pendingClaims.filter { it.status == "PENDING" },
                onNewClaim = { navController.navigate("new-claim") },
                onClaimClick = { id -> navController.navigate("claim/$id") },
                onViewHistory = { navController.navigate("history") },
            )
        }
        composable("new-claim") {
            NewClaimScreen(onStartWaiting = { amount, provider, timeoutMillis ->
                coroutineScope.launch {
                    val id = app.claimRepository.createClaim(amount, provider, timeoutMillis)
                    navController.navigate("claim/$id") { popUpTo("home") }
                }
            })
        }
        composable(
            "claim/{claimId}",
            arguments = listOf(navArgument("claimId") { type = NavType.LongType }),
        ) { backStackEntry ->
            val claimId = backStackEntry.arguments?.getLong("claimId") ?: return@composable
            val claim by app.claimRepository.observeClaim(claimId).collectAsState(initial = null)
            ClaimResultScreen(claim = claim, onTick = { app.matchingEngine.resolveTimeouts() })
        }
        composable("history") {
            val claims by app.claimRepository.observeAllClaims().collectAsState(initial = emptyList())
            val unparsedCounts by app.unparsedNotificationRepository.countsLast24h()
                .collectAsState(initial = emptyMap())
            HistoryScreen(
                claims = claims,
                unparsedCounts = unparsedCounts,
                onClaimClick = { id -> navController.navigate("claim/$id") },
            )
        }
    }
}
