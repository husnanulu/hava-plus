package com.kampplus.hava.ui.city

import com.kampplus.hava.model.Coordinates
import com.kampplus.hava.testing.FakeCityRepository
import com.kampplus.hava.testing.FakeFavoritesRepository
import com.kampplus.hava.testing.FakeLocationRepository
import com.kampplus.hava.testing.FakeRecentSearchRepository
import com.kampplus.hava.testing.FakeWeatherRepository
import com.kampplus.hava.testing.MainDispatcherRule
import com.kampplus.hava.testing.sampleCity
import com.kampplus.hava.ui.LoadState
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class CityViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeCityRepository()

    private val weather = FakeWeatherRepository()
    private val location = FakeLocationRepository()
    private val favorites = FakeFavoritesRepository()
    private val recent = FakeRecentSearchRepository()

    private fun createViewModel() = CityViewModel(repository, weather, location, favorites, recent)

    /** Kullanıcı arama kutusuna [text] yazmış ve yazmayı bırakmış gibi. */
    private fun CityViewModel.typeAndWait(text: String, scope: TestScope) {
        onQueryChange(text)
        scope.advanceUntilIdle()
    }

    @Test
    fun `baslangicta query bos`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals("", viewModel.query.value)
    }

    @Test
    fun `acilista arama yapilmaz`() = runTest {
        createViewModel()
        advanceUntilIdle()

        assertEquals(0, repository.callCount)
    }

    @Test
    fun `yazinca once Loading gosterilir`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onQueryChange("İstanbul")

        // Bekleme süresi dolmadan arama henüz bitmemiştir.
        assertEquals(LoadState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `veri gelince Success durumuna gecilir`() = runTest {
        repository.result = listOf(sampleCity(), sampleCity(id = 2L, name = "İzmir"))

        val viewModel = createViewModel()
        viewModel.typeAndWait("İstanbul", this)

        val state = viewModel.uiState.value
        assertTrue("Success bekleniyordu, gelen: $state", state is LoadState.Success)
        assertEquals(2, (state as LoadState.Success).data.size)
    }

    @Test
    fun `bos liste Empty durumuna cevrilir`() = runTest {
        repository.result = emptyList()

        val viewModel = createViewModel()
        viewModel.typeAndWait("xyz", this)

        assertEquals(LoadState.Empty, viewModel.uiState.value)
    }

    @Test
    fun `baglanti hatasi anlasilir mesaja cevrilir`() = runTest {
        repository.error = IOException("no network")

        val viewModel = createViewModel()
        viewModel.typeAndWait("İstanbul", this)

        val state = viewModel.uiState.value
        assertTrue("Error bekleniyordu, gelen: $state", state is LoadState.Error)
        assertEquals("İnternet bağlantını kontrol et.", (state as LoadState.Error).message)
    }

    @Test
    fun `sunucu hatasi farkli mesaj gosterir`() = runTest {
        repository.error = HttpException(
            Response.error<Unit>(500, "".toResponseBody("application/json".toMediaType()))
        )

        val viewModel = createViewModel()
        viewModel.typeAndWait("İstanbul", this)

        assertEquals(
            "Sunucu şu an cevap vermiyor.",
            (viewModel.uiState.value as LoadState.Error).message
        )
    }

    @Test
    fun `onQueryChange her harfte degil yazma durunca tek istek atar`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        listOf("R", "Ri", "Riz", "Rize").forEach { viewModel.onQueryChange(it) }
        advanceUntilIdle()

        assertEquals("Rize", viewModel.query.value)
        // Harfler arka arkaya geldi; bekleyen aramalar iptal edildi, yalnızca sonuncusu gitti.
        assertEquals(1, repository.callCount)
        assertEquals("Rize", repository.lastQuery)
    }

    @Test
    fun `metin silinince arama yapilmaz`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onQueryChange("Ri")
        viewModel.onQueryChange("")
        advanceUntilIdle()

        assertEquals("", viewModel.query.value)
        assertEquals(0, repository.callCount)
    }

    @Test
    fun `search guncel metinle hemen istek atar`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onQueryChange("Rize")
        viewModel.search()
        advanceUntilIdle()

        assertEquals("Rize", repository.lastQuery)
        // Klavyedeki "Ara" bekleyen aramanın yerine geçer; iki kez istek atılmaz.
        assertEquals(1, repository.callCount)
    }

    @Test
    fun `tekrar dene yeni bir istek baslatir ve basarili olabilir`() = runTest {
        repository.error = IOException("no network")
        val viewModel = createViewModel()
        viewModel.typeAndWait("İstanbul", this)
        assertTrue(viewModel.uiState.value is LoadState.Error)

        // Bağlantı geri geldi.
        repository.error = null
        repository.result = listOf(sampleCity())
        viewModel.search()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is LoadState.Success)
        assertEquals(2, repository.callCount)
    }

    // --- Konumum kartı ---

    @Test
    fun `izin yoksa konum aranmaz ve kart Konumumu kullan olur`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertFalse(viewModel.hasLocationPermission.value)
        assertNull(viewModel.myLocation.value)
        assertEquals(0, location.locationRequests)
    }

    @Test
    fun `izin varsa konumum adi ve sicakligiyla gelir`() = runTest {
        location.permission = true
        location.coordinates = Coordinates(40.99, 29.03)
        location.name = "Kadıköy, İstanbul"

        val viewModel = createViewModel()
        advanceUntilIdle()

        val myLocation = checkNotNull(viewModel.myLocation.value)
        assertEquals("Kadıköy, İstanbul", myLocation.city.name)
        assertEquals(21.0, checkNotNull(myLocation.current).temperatureC, 0.01)
    }

    @Test
    fun `izin sonradan verilince konum yuklenir`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        location.permission = true
        location.coordinates = Coordinates(40.99, 29.03)
        viewModel.onLocationPermissionResult(granted = true)
        advanceUntilIdle()

        assertTrue(viewModel.hasLocationPermission.value)
        assertEquals("Konumum", viewModel.myLocation.value?.city?.name)
    }

    // --- Favoriler ---

    @Test
    fun `favori yoksa liste bos`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.favorites.value.isEmpty())
    }

    @Test
    fun `favoriler anlik sicakliklariyla listelenir`() = runTest {
        favorites.toggle(sampleCity(id = 1L, name = "İzmir", latitude = 38.4))
        favorites.toggle(sampleCity(id = 2L, name = "Elazığ", latitude = 38.7))

        val viewModel = createViewModel()
        advanceUntilIdle()

        val list = viewModel.favorites.value
        assertEquals(listOf("İzmir", "Elazığ"), list.map { it.city.name })
        assertEquals(21.0, checkNotNull(list[0].current).temperatureC, 0.01)
        assertEquals(21.0, checkNotNull(list[1].current).temperatureC, 0.01)
    }

    @Test
    fun `bir favorinin sicakligi gelmezse liste yine gorunur`() = runTest {
        favorites.toggle(sampleCity(id = 1L, name = "İzmir", latitude = 38.4))
        favorites.toggle(sampleCity(id = 2L, name = "Elazığ", latitude = 38.7))
        weather.failingLatitudes += 38.7

        val viewModel = createViewModel()
        advanceUntilIdle()

        val list = viewModel.favorites.value
        assertEquals(2, list.size)
        assertEquals(21.0, checkNotNull(list[0].current).temperatureC, 0.01)
        assertNull(list[1].current)
    }

    @Test
    fun `yildiza dokununca favori eklenir ve cikarilir`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onToggleFavorite(sampleCity(name = "Rize"))
        advanceUntilIdle()
        assertEquals(listOf("Rize"), viewModel.favorites.value.map { it.city.name })

        viewModel.onToggleFavorite(sampleCity(name = "Rize"))
        advanceUntilIdle()
        assertTrue(viewModel.favorites.value.isEmpty())
    }

    @Test
    fun `sicakligi bilinen favori icin tekrar istek atilmaz`() = runTest {
        favorites.toggle(sampleCity(id = 1L, name = "İzmir", latitude = 38.4))
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onToggleFavorite(sampleCity(id = 2L, name = "Rize", latitude = 41.0))
        advanceUntilIdle()

        // İzmir bir kez, Rize bir kez: toplam iki hafif istek.
        assertEquals(listOf(38.4, 41.0), weather.currentRequests)
    }

    // --- Son aramalar ---

    @Test
    fun `arama sonucundan secilen sehir son aramalara eklenir sonra gecis yapilir`() = runTest {
        val viewModel = createViewModel()
        var navigated = false

        viewModel.onSearchResultClick(sampleCity(name = "Rize")) { navigated = true }
        advanceUntilIdle()

        assertEquals(listOf("Rize"), viewModel.recentSearches.value.map { it.name })
        assertTrue(navigated)
    }

    @Test
    fun `yazilan yarim metin son aramalara eklenmez`() = runTest {
        val viewModel = createViewModel()

        viewModel.onQueryChange("Ri")
        advanceUntilIdle()

        assertTrue(viewModel.recentSearches.value.isEmpty())
    }

    @Test
    fun `son arama silinir ve temizlenir`() = runTest {
        val viewModel = createViewModel()
        viewModel.onSearchResultClick(sampleCity(id = 1, name = "İzmir")) {}
        viewModel.onSearchResultClick(sampleCity(id = 2, name = "Rize")) {}
        advanceUntilIdle()

        viewModel.onRemoveRecent(sampleCity(id = 1, name = "İzmir"))
        advanceUntilIdle()
        assertEquals(listOf("Rize"), viewModel.recentSearches.value.map { it.name })

        viewModel.onClearRecent()
        advanceUntilIdle()
        assertTrue(viewModel.recentSearches.value.isEmpty())
    }
}
