package com.kampplus.hava.ui

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kampplus.hava.model.City
import com.kampplus.hava.ui.city.CityRoute
import com.kampplus.hava.ui.home.HomeRoute
import com.kampplus.hava.ui.home.HomeViewModel

/**
 * Uygulamanın iki ekranı ve aralarındaki geçiş: Ana ekran ("Bugün") ⇄ Şehir seç.
 * Başlangıç rotası Ana ekrandır; alt menü yoktur.
 *
 * @param onHomeReady Ana ekranın ilk tahmini gelince çağrılır (açılış ekranı kapanır).
 */
@Composable
fun HavaNavHost(onHomeReady: () -> Unit, modifier: Modifier = Modifier, navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = HOME_ROUTE,
        modifier = modifier
    ) {
        composable(
            route = HOME_ROUTE,
            arguments = listOf(
                // Hepsi String ve isteğe bağlı: parametresiz açılışta boş kalırlar.
                navArgument(HomeViewModel.ARG_ID) { stringArgument() },
                navArgument(HomeViewModel.ARG_NAME) { stringArgument() },
                navArgument(HomeViewModel.ARG_SUBTITLE) { stringArgument() },
                navArgument(HomeViewModel.ARG_LATITUDE) { stringArgument() },
                navArgument(HomeViewModel.ARG_LONGITUDE) { stringArgument() }
            )
        ) {
            HomeRoute(
                onPlaceClick = { navController.navigate(CITIES_ROUTE) },
                onReady = onHomeReady
            )
        }
        composable(CITIES_ROUTE) {
            CityRoute(
                onBack = { navController.popBackStack() },
                onCityClick = { city -> navController.goHome(homeRoute(city)) },
                // Parametresiz Ana ekran = konum kuralı (izin varsa telefonun konumu).
                onMyLocationClick = { navController.goHome("home") }
            )
        }
    }
}

/** Boş metin varsayılanlı String parametresi. */
private fun androidx.navigation.NavArgumentBuilder.stringArgument() {
    type = NavType.StringType
    defaultValue = ""
}

/**
 * Ana ekrana yeni yerle döner. popUpTo(HOME_ROUTE) { inclusive = true }: eski Ana ekran da
 * yığından çıkarılır; böylece geri yığınında hep TEK bir Ana ekran kalır.
 */
private fun NavHostController.goHome(route: String) {
    navigate(route) {
        popUpTo(HOME_ROUTE) { inclusive = true }
    }
}

/** Şehir seç ekranının adresi. */
private const val CITIES_ROUTE = "cities"

/** Ana ekranın adres şablonu; süslü parantezler çalışma anında doldurulur. */
private const val HOME_ROUTE = "home?id={id}&name={name}&subtitle={subtitle}&lat={lat}&lon={lon}"

/** Bir şehir için Ana ekranın gerçek adresini üretir. Uri.encode: "İstanbul, Türkiye" gibi metinler adreste bozulmasın. */
private fun homeRoute(city: City): String = buildString {
    append("home")
    append("?id=").append(city.id)
    append("&name=").append(Uri.encode(city.name))
    append("&subtitle=").append(Uri.encode(city.subtitle))
    append("&lat=").append(city.latitude)
    append("&lon=").append(city.longitude)
}
