package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.locale.Strings
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.CameraScreen
import com.example.ui.screens.CropRotateScreen
import com.example.ui.screens.EditorScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PricingScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ProjectsHistoryScreen
import com.example.ui.screens.ToolsCatalogScreen
import com.example.ui.theme.PhotoForgeTheme
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SecondaryCyan
import com.example.ui.viewmodel.PhotoForgeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val app = application as PhotoForgeApp
            PhotoForgeTheme(darkTheme = true) {
                PhotoForgeMainApp(app = app)
            }
        }
    }
}

@Composable
fun PhotoForgeMainApp(app: PhotoForgeApp) {
    val navController = rememberNavController()
    val viewModel: PhotoForgeViewModel = viewModel(
        factory = PhotoForgeViewModel.Factory(app.repository, app.storageManager)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"
    val isBn = app.isBangla

    val navItems = listOf(
        Triple("home", Strings.home(isBn), Icons.Default.Home),
        Triple("tools", Strings.tools(isBn), Icons.Default.Build),
        Triple("editor", if (isBn) "এডিটর" else "Editor", Icons.Default.AutoAwesome),
        Triple("projects", Strings.projects(isBn), Icons.Default.Collections),
        Triple("pricing", Strings.pricing(isBn), Icons.Default.MonetizationOn),
        Triple("profile", Strings.profile(isBn), Icons.Default.Person)
    )

    // Hide bottom navigation in full-focus screens like Editor and Admin
    val showBottomBar = currentRoute != "admin" && currentRoute != "crop" && currentRoute != "camera"

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    navItems.forEach { (route, label, icon) ->
                        val isSelected = currentRoute == route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != route) {
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = SecondaryCyan,
                                selectedTextColor = SecondaryCyan,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = PrimaryIndigo.copy(alpha = 0.25f)
                            )
                        )
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            composable("home") {
                HomeScreen(
                    viewModel = viewModel,
                    isBn = isBn,
                    onToggleLanguage = { app.isBangla = !app.isBangla },
                    onNavigateToEditor = { navController.navigate("editor") },
                    onNavigateToCrop = { navController.navigate("crop") },
                    onNavigateToCamera = { navController.navigate("camera") },
                    onNavigateToPricing = { navController.navigate("pricing") },
                    onNavigateToProjects = { navController.navigate("projects") }
                )
            }

            composable("tools") {
                ToolsCatalogScreen(
                    viewModel = viewModel,
                    isBn = isBn,
                    onToolSelected = { tool ->
                        viewModel.selectTool(tool)
                        if (viewModel.sourceBitmap.value == null) {
                            viewModel.loadSamplePortrait()
                        }
                        navController.navigate("editor")
                    },
                    onNavigateToPricing = { navController.navigate("pricing") }
                )
            }

            composable("editor") {
                EditorScreen(
                    viewModel = viewModel,
                    isBn = isBn,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPricing = { navController.navigate("pricing") },
                    onNavigateToCrop = { navController.navigate("crop") }
                )
            }

            composable("crop") {
                CropRotateScreen(
                    viewModel = viewModel,
                    isBn = isBn,
                    onNavigateToEditor = {
                        navController.navigate("editor") {
                            popUpTo("crop") { inclusive = true }
                        }
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("camera") {
                CameraScreen(
                    viewModel = viewModel,
                    isBn = isBn,
                    onNavigateToCrop = {
                        navController.navigate("crop") {
                            popUpTo("camera") { inclusive = true }
                        }
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("projects") {
                ProjectsHistoryScreen(
                    viewModel = viewModel,
                    isBn = isBn,
                    onOpenProject = { project ->
                        viewModel.loadProject(project)
                        navController.navigate("editor")
                    }
                )
            }

            composable("pricing") {
                PricingScreen(
                    viewModel = viewModel,
                    isBn = isBn
                )
            }

            composable("profile") {
                ProfileScreen(
                    viewModel = viewModel,
                    isBn = isBn,
                    onToggleLanguage = { app.isBangla = !app.isBangla },
                    onNavigateToAdmin = { navController.navigate("admin") }
                )
            }

            composable("admin") {
                AdminDashboardScreen(
                    viewModel = viewModel,
                    isBn = isBn,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

// Greeting function retained for Robolectric / Screenshot testing compatibility
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
