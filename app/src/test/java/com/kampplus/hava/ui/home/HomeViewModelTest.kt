package com.kampplus.hava.ui.home

import androidx.lifecycle.SavedStateHandle
import com.kampplus.hava.model.Coordinates
import com.kampplus.hava.testing.FakeFavoritesRepository
import com.kampplus.hava.testing.FakeLastPlaceRepository
import com.kampplus.hava.testing.FakeLocationRepository
import com.kampplus.hava.testing.FakeWeatherRepository
import com.kampplus.hava.testing.MainDispatcherRule
import com.kampplus.hava.testing.sampleCity
import com.kampplus.hava.ui.LoadState
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Kamptaki ForecastViewModelTest'in Ana ekran (HomeViewModel) hâli + konum kuralı testleri. */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeWeatherRepository()
    private val location = FakeLocationRepository()
    private val lastPlace = FakeLastPlaceRepository()
    private val favorites = FakeFavoritesRepository()

    /** Kadıköy civarı. */
    private val kadikoy = Coordinates(40.99, 29.03)

    private fun createViewModel(
        name: String = "Ankara",
        subtitle: String = "Ankara, Türkiye",
        latitude: String = "39.91987",
        longitude: String = "32.85427"
    ) = createViewModel(
        SavedStateHandle(
            mapOf(
                HomeViewModel.ARG_ID to "323786",
                HomeViewModel.ARG_NAME to name,
                HomeViewModel.ARG_SUBTITLE to subtitle,
                HomeViewModel.ARG_LATITUDE to latitude,
                HomeViewModel.ARG_LONGITUDE to longitude
            )
        )
    )

    private fun createViewModel(savedStateHandle: SavedStateHandle) = HomeViewModel(
        savedStateHandle = savedStateHandle,
        weatherRepository = repository,
        locationRepository = location,
        lastPlaceRepository = lastPlace,
        favoritesRepository = favorites
    )

    /** Rota parametresi olmadan (uygulama ilk açıldığında olduğu gibi). */
    private fun createViewModelWithoutRoute() = createViewModel(SavedStateHandle())

    @Test
    fun `rota parametreleri okunur`() {
        val viewModel = createViewModel()

        val place = checkNotNull(viewModel.place.value)
        assertEquals("Ankara", place.name)
        assertEquals("Ankara, Türkiye", place.subtitle)
        assertEquals(323786L, place.id)
    }

    @Test
    fun `baslangicta Loading gosterilir`() = runTest {
        val viewModel = createViewModel()

        assertEquals(LoadState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `tahmin gelince Success durumuna gecilir`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Success bekleniyordu, gelen: $state", state is LoadState.Success)
        assertEquals(21.0, (state as LoadState.Success).data.current.temperatureC, 0.01)
    }

    @Test
    fun `baglanti hatasi Error durumuna cevrilir`() = runTest {
        repository.error = IOException("no network")

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(
            "İnternet bağlantını kontrol et.",
            (viewModel.uiState.value as LoadState.Error).message
        )
    }

    @Test
    fun `eksik current blogu Error durumuna cevrilir`() = runTest {
        repository.error = IllegalStateException("Forecast response has no current block")

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is LoadState.Error)
    }

    @Test
    fun `tekrar dene yeni istek baslatir`() = runTest {
        repository.error = IOException("no network")
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is LoadState.Error)

        repository.error = null
        viewModel.onRetry()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is LoadState.Success)
        assertEquals(2, repository.callCount)
    }

    @Test
    fun `gecersiz koordinat uygulamayi cokertmez`() = runTest {
        val viewModel = createViewModel(latitude = "abc", longitude = "")
        advanceUntilIdle()

        // Koordinat sayıya çevrilemezse rota yok sayılır; varsayılan yer (İstanbul) yüklenir.
        assertTrue(viewModel.uiState.value is LoadState.Success)
        assertEquals("İstanbul", viewModel.place.value?.name)
    }

    // --- Konum kuralı (Bölüm 4) ---

    @Test
    fun `parametresiz acilista hicbir bilgi yoksa Istanbul gosterilir`() = runTest {
        val viewModel = createViewModelWithoutRoute()
        advanceUntilIdle()

        assertEquals(HomeViewModel.DEFAULT_CITY, viewModel.place.value)
        assertEquals(41.0082, repository.lastLatitude, 0.0001)
    }

    @Test
    fun `rotadan gelen sehir konumdan once gelir`() = runTest {
        location.permission = true
        location.coordinates = kadikoy

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals("Ankara", viewModel.place.value?.name)
        assertFalse(viewModel.isLocated.value)
        assertEquals(0, location.locationRequests)
    }

    @Test
    fun `izin var ve konum bulunduysa konumdaki yer gosterilir`() = runTest {
        location.permission = true
        location.coordinates = kadikoy
        location.name = "Kadıköy, İstanbul"
        lastPlace.saved = sampleCity()

        val viewModel = createViewModelWithoutRoute()
        advanceUntilIdle()

        assertEquals("Kadıköy, İstanbul", viewModel.place.value?.name)
        assertTrue(viewModel.isLocated.value)
        assertFalse(viewModel.showLocationBanner.value)
        assertEquals(40.99, repository.lastLatitude, 0.0001)
    }

    @Test
    fun `konumun adi bulunamazsa Konumum yazar`() = runTest {
        location.permission = true
        location.coordinates = kadikoy

        val viewModel = createViewModelWithoutRoute()
        advanceUntilIdle()

        assertEquals("Konumum", viewModel.place.value?.name)
    }

    @Test
    fun `izin yoksa son bakilan yer ve bant gosterilir`() = runTest {
        lastPlace.saved = sampleCity(name = "İzmir")

        val viewModel = createViewModelWithoutRoute()
        advanceUntilIdle()

        assertEquals("İzmir", viewModel.place.value?.name)
        assertTrue(viewModel.showLocationBanner.value)
        assertFalse(viewModel.isLocated.value)
    }

    @Test
    fun `izin var ama konum bulunamazsa son yer gosterilir ve bant cikmaz`() = runTest {
        location.permission = true
        location.coordinates = null
        lastPlace.saved = sampleCity(name = "İzmir")

        val viewModel = createViewModelWithoutRoute()
        advanceUntilIdle()

        assertEquals("İzmir", viewModel.place.value?.name)
        assertFalse(viewModel.showLocationBanner.value)
    }

    @Test
    fun `gosterilen yer son bakilan yer olarak kaydedilir`() = runTest {
        createViewModel()
        advanceUntilIdle()

        assertEquals("Ankara", lastPlace.saved?.name)
    }

    @Test
    fun `izin sonradan verilirse konumdaki yere gecilir`() = runTest {
        lastPlace.saved = sampleCity(name = "İzmir")
        val viewModel = createViewModelWithoutRoute()
        advanceUntilIdle()
        assertTrue(viewModel.showLocationBanner.value)

        // Kullanıcı "Konumumu kullan"a dokunup izin verdi.
        location.permission = true
        location.coordinates = kadikoy
        location.name = "Kadıköy, İstanbul"
        viewModel.onLocationPermissionResult(granted = true)
        advanceUntilIdle()

        assertEquals("Kadıköy, İstanbul", viewModel.place.value?.name)
        assertTrue(viewModel.isLocated.value)
        assertFalse(viewModel.showLocationBanner.value)
    }

    @Test
    fun `izin reddedilirse yer degismez`() = runTest {
        lastPlace.saved = sampleCity(name = "İzmir")
        val viewModel = createViewModelWithoutRoute()
        advanceUntilIdle()

        viewModel.onLocationPermissionResult(granted = false)
        advanceUntilIdle()

        assertEquals("İzmir", viewModel.place.value?.name)
        assertTrue(viewModel.showLocationBanner.value)
    }

    @Test
    fun `izin penceresi yalnizca bir kez ve sehir secilmediyse istenir`() {
        val withoutRoute = createViewModelWithoutRoute()
        assertTrue(withoutRoute.shouldAskLocationPermission())
        assertFalse(withoutRoute.shouldAskLocationPermission())

        // Kullanıcı Şehir seç'ten bir şehir seçtiyse izin sorulmaz.
        assertFalse(createViewModel().shouldAskLocationPermission())
    }

    // --- Favoriler (Bölüm 5) ---

    @Test
    fun `favoride olmayan yerde yildiz bos`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertFalse(viewModel.isFavorite.value)
    }

    @Test
    fun `yildiza dokununca yer favorilere eklenir ve yildiz dolar`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onFavoriteClick()
        advanceUntilIdle()

        assertTrue(viewModel.isFavorite.value)
    }

    @Test
    fun `ikinci dokunus favoriden cikarir`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onFavoriteClick()
        advanceUntilIdle()
        viewModel.onFavoriteClick()
        advanceUntilIdle()

        assertFalse(viewModel.isFavorite.value)
    }

    @Test
    fun `baska ekranda eklenen favori burada da gorunur`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Şehir seç ekranı aynı depoya Ankara'yı ekledi (kimlik 323786).
        favorites.toggle(sampleCity())
        advanceUntilIdle()

        assertTrue(viewModel.isFavorite.value)
    }
}
