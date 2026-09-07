package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DebtsScreen
import com.example.ui.screens.FixedExpensesScreen
import com.example.ui.screens.IncomesScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SavingsScreen
import com.example.ui.screens.VariableExpensesScreen
import com.example.ui.theme.HomeAccountantTheme
import com.example.ui.viewmodel.AccountantViewModel

class MainActivity : ComponentActivity() {

    private val accountantViewModel: AccountantViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            HomeAccountantTheme {
                HomeAccountantNavigation(viewModel = accountantViewModel)
            }
        }
    }
}

@Composable
fun HomeAccountantNavigation(viewModel: AccountantViewModel) {
    val navController = rememberNavController()
    val currentUser by viewModel.currentUser.collectAsState()

    val startRoute = if (currentUser != null) "dashboard" else "login"

    NavHost(
        navController = navController,
        startDestination = startRoute,
        modifier = Modifier.fillMaxSize()
    ) {
        composable("login") {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("dashboard") {
            DashboardScreen(
                viewModel = viewModel,
                onNavigateToIncomes = { navController.navigate("incomes") },
                onNavigateToFixedExpenses = { navController.navigate("fixed_expenses") },
                onNavigateToVariableExpenses = { navController.navigate("variable_expenses") },
                onNavigateToDebts = { navController.navigate("debts") },
                onNavigateToSavings = { navController.navigate("savings") },
                onNavigateToReports = { navController.navigate("reports") },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable("incomes") {
            IncomesScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("fixed_expenses") {
            FixedExpensesScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("variable_expenses") {
            VariableExpensesScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("debts") {
            DebtsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("savings") {
            SavingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("reports") {
            ReportsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
