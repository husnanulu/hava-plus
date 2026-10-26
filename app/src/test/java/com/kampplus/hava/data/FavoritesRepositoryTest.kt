package com.kampplus.hava.data

import com.kampplus.hava.model.Coordinates
import com.kampplus.hava.model.toCity
import com.kampplus.hava.testing.InMemoryDataStore
import com.kampplus.hava.testing.sampleCity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Favori deposu: ekle/çıkar, sıra, en fazla 10 ve "uygulama kapanıp açılınca duruyor mu?". */
@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesRepositoryTest {

    private val dataStore = InMemoryDataStore()

    private fun TestScope.createRepository() = DataStoreFavoritesRepository(dataStore, StandardTestDispatcher(testScheduler))

    @Test
    fun `baslangicta favori yok`() = runTest {
        assertTrue(createRepository().favorites.first().isEmpty())
    }

    @Test
    fun `toggle ekler ve ikinci kez cikarir`() = runTest {
        val repository = createRepository()

        repository.toggle(sampleCity())
        assertEquals(listOf("Ankara"), repository.favorites.first().map { it.name })

        repository.toggle(sampleCity())
        assertTrue(repository.favorites.first().isEmpty())
    }

    @Test
    fun `siralama eklenme sirasidir`() = runTest {
        val repository = createRepository()

        repository.toggle(sampleCity(id = 1, name = "İzmir"))
        repository.toggle(sampleCity(id = 2, name = "Elazığ"))
        repository.toggle(sampleCity(id = 3, name = "Ankara"))

        assertEquals(listOf("İzmir", "Elazığ", "Ankara"), repository.favorites.first().map { it.name })
    }

    @Test
    fun `en fazla 10 favori tutulur`() = runTest {
        val repository = createRepository()

        (1L..11L).forEach { id -> repository.toggle(sampleCity(id = id, name = "Şehir $id")) }

        val list = repository.favorites.first()
        assertEquals(FavoritesRepository.MAX_FAVORITES, list.size)
        assertEquals("Şehir 10", list.last().name)
    }

    @Test
    fun `yeni depo nesnesi ayni kaydi okur`() = runTest {
        // Uygulama kapanıp açılınca yeni bir repository oluşur ama DataStore'daki kayıt aynıdır.
        createRepository().toggle(sampleCity(name = "Rize"))

        val reopened = createRepository()

        assertEquals(listOf("Rize"), reopened.favorites.first().map { it.name })
    }

    @Test
    fun `konum yeri koordinat ve adiyla saklanir`() = runTest {
        val repository = createRepository()
        val here = Coordinates(40.99, 29.03).toCity("Kadıköy, İstanbul")

        repository.toggle(here)

        val saved = repository.favorites.first().single()
        assertEquals("Kadıköy, İstanbul", saved.name)
        assertEquals(40.99, saved.latitude, 0.0001)
        assertTrue("Konum kimliği negatif olmalı", saved.id < 0)
    }
}
