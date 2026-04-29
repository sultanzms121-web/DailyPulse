package com.dailypulse.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument

// --- INTERNAL PROJECT IMPORTS ---
import com.dailypulse.data.NewsRepository
import com.dailypulse.ui.screens.* import com.dailypulse.viewmodel.*

import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

private val DeepNavy = Color(0xFF002147)

class ArticleDetailViewModelFactory(private val repository: NewsRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ArticleDetailViewModel(repository) as T
    }
}

// --- 🗺️ SCREEN DEFINITIONS ---
sealed class Screen(val route: String, val title: String? = null, val icon: androidx.compose.ui.graphics.vector.ImageVector? = null) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home", "Home", Icons.Filled.Home)
    object Explore : Screen("explore", "Explore", Icons.Filled.Search)
    object Bookmarks : Screen("bookmarks", "Bookmarks", Icons.Filled.Bookmark)
    object Profile : Screen("profile", "Profile", Icons.Filled.Person)
    object Search : Screen("search")
    object UserSearch : Screen("user_search")
    object FollowingList : Screen("following_list")

    // 🌟 Standardized to {id}
    object PublicProfile : Screen("public_profile/{id}") {
        fun createRoute(u: String) = "public_profile/$u"
    }

    object PostDetail : Screen("post_detail/{postId}") {
        fun createRoute(p: String) = "post_detail/$p"
    }

    object ArticleDetail : Screen("article/{url}") {
        fun createRoute(url: String) = "article/${URLEncoder.encode(url, StandardCharsets.UTF_8.name())}"
    }

    object SourceDetail : Screen("source_detail/{sourceId}/{sourceName}") {
        fun createRoute(id: String, name: String) = "source_detail/$id/${URLEncoder.encode(name, StandardCharsets.UTF_8.name())}"
    }
}

val bottomNavItems = listOf(Screen.Home, Screen.Explore, Screen.Bookmarks, Screen.Profile)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    homeViewModel: HomeViewModel,
    searchViewModel: SearchViewModel,
    socialViewModel: SocialViewModel,
    newsRepository: NewsRepository
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar = currentDestination?.route in bottomNavItems.map { it.route }
    val selectedLanguage by homeViewModel.selectedLanguage.collectAsState()

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon!!, contentDescription = screen.title) },
                            label = { Text(screen.title!!) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // --- 1. AUTH ---
            composable(Screen.Splash.route) {
                SplashScreen(onNavigateToLogin = {
                    val dest = if (authViewModel.getCurrentUser() != null) Screen.Home.route else Screen.Login.route
                    navController.navigate(dest) { popUpTo(Screen.Splash.route) { inclusive = true } }
                })
            }

            composable(Screen.Login.route) {
                LoginScreen(viewModel = authViewModel,
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                    onLoginSuccess = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Login.route) { inclusive = true } } },
                    onGuestLogin = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Login.route) { inclusive = true } } }
                )
            }

            composable(Screen.Register.route) {
                RegisterScreen(viewModel = authViewModel, onNavigateToLogin = { navController.popBackStack() },
                    onRegisterSuccess = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Register.route) { inclusive = true } } }
                )
            }

            // --- 2. MAIN ---
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = homeViewModel,
                    authViewModel = authViewModel,
                    socialViewModel = socialViewModel,
                    onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                    onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                    onNavigateToArticle = { url -> navController.navigate(Screen.ArticleDetail.createRoute(url)) },
                    onNavigateToSource = { id, name -> navController.navigate(Screen.SourceDetail.createRoute(id, name)) },
                    onNavigateToPublicProfile = { profileId -> navController.navigate(Screen.PublicProfile.createRoute(profileId)) },
                    onNavigateToPostDetail = { pid -> navController.navigate(Screen.PostDetail.createRoute(pid)) }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    viewModel = authViewModel,
                    homeViewModel = homeViewModel,
                    socialViewModel = socialViewModel,
                    onNavigateToUserSearch = { navController.navigate(Screen.UserSearch.route) },
                    onNavigateToAuth = { navController.navigate(Screen.Login.route) { popUpTo(Screen.Home.route) { inclusive = true } } },
                    onNavigateToPostDetail = { pid -> navController.navigate(Screen.PostDetail.createRoute(pid)) },
                    onNavigateToFollowingList = { navController.navigate(Screen.FollowingList.route) }
                )
            }

            // --- 3. SOCIAL ---
            composable(Screen.UserSearch.route) {
                UserSearchScreen(
                    viewModel = socialViewModel,
                    onNavigateToProfile = { profileId -> navController.navigate(Screen.PublicProfile.createRoute(profileId)) }
                )
            }

            composable(Screen.FollowingList.route) {
                FollowingListScreen(
                    currentUserId = authViewModel.getCurrentUser()?.uid ?: "",
                    viewModel = socialViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPublicProfile = { targetId -> navController.navigate(Screen.PublicProfile.createRoute(targetId)) }
                )
            }

            composable(
                route = Screen.PublicProfile.route,
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { backStackEntry ->
                val profileId = backStackEntry.arguments?.getString("id") ?: ""
                PublicProfileScreen(
                    targetUserId = profileId,
                    currentUserId = authViewModel.getCurrentUser()?.uid ?: "",
                    viewModel = socialViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPostDetail = { pid -> navController.navigate(Screen.PostDetail.createRoute(pid)) }
                )
            }

            composable(
                route = Screen.PostDetail.route,
                arguments = listOf(navArgument("postId") { type = NavType.StringType })
            ) { backStackEntry ->
                val pid = backStackEntry.arguments?.getString("postId") ?: ""
                PostDetailScreen(postId = pid, onBack = { navController.popBackStack() })
            }

            // --- 4. NEWS & DISCOVERY ---
            composable(Screen.Search.route) {
                SearchScreen(viewModel = searchViewModel, onNavigateBack = { navController.popBackStack() },
                    onNavigateToArticle = { url -> navController.navigate(Screen.ArticleDetail.createRoute(url)) },
                    onNavigateToSource = { id, name -> navController.navigate(Screen.SourceDetail.createRoute(id, name)) }
                )
            }

            composable(Screen.Explore.route) {
                ExploreScreen(viewModel = homeViewModel,
                    onNavigateToArticle = { url -> navController.navigate(Screen.ArticleDetail.createRoute(url)) },
                    onNavigateToSource = { id, name -> navController.navigate(Screen.SourceDetail.createRoute(id, name)) },
                    onNavigateToSearch = { navController.navigate(Screen.UserSearch.route) }
                )
            }

            composable(Screen.Bookmarks.route) {
                BookmarksScreen(viewModel = homeViewModel, onNavigateBack = { navController.popBackStack() },
                    onNavigateToArticle = { url -> navController.navigate(Screen.ArticleDetail.createRoute(url)) },
                    onNavigateToSource = { id, name -> navController.navigate(Screen.SourceDetail.createRoute(id, name)) }
                )
            }

            composable(
                route = Screen.SourceDetail.route,
                arguments = listOf(
                    navArgument("sourceId") { type = NavType.StringType },
                    navArgument("sourceName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val sid = backStackEntry.arguments?.getString("sourceId") ?: ""
                val sname = URLDecoder.decode(backStackEntry.arguments?.getString("sourceName") ?: "", StandardCharsets.UTF_8.name())
                SourceDetailScreen(
                    sourceId = sid,
                    sourceName = sname,
                    viewModel = homeViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToArticle = { url -> navController.navigate(Screen.ArticleDetail.createRoute(url)) }
                )
            }

            composable(
                route = Screen.ArticleDetail.route,
                arguments = listOf(navArgument("url") { type = NavType.StringType })
            ) { backStackEntry ->
                val decodedUrl = URLDecoder.decode(backStackEntry.arguments?.getString("url") ?: "", StandardCharsets.UTF_8.name())
                val detailViewModel: ArticleDetailViewModel = viewModel(factory = ArticleDetailViewModelFactory(newsRepository))
                val allArticles by homeViewModel.getArticles("All", selectedLanguage).collectAsState(initial = emptyList())
                val article = allArticles.find { it.contentUrl == decodedUrl }

                if (article != null) {
                    ArticleDetailScreen(
                        article = article,
                        languageCode = selectedLanguage,
                        viewModel = detailViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToSource = { id, name -> navController.navigate(Screen.SourceDetail.createRoute(id, name)) }
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = DeepNavy)
                    }
                }
            }
        }
    }
}