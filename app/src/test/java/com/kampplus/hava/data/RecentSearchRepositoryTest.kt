package com.kampplus.hava.data

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

/** Son aramalar deposu: ekleme, aynı şehrin en üste taşınması, 5 sınırı, silme ve temizleme. */
@OptIn(ExperimentalCoroutinesApi::class)
class RecentSearchRepositoryTest {

    private val dataStore = InMemoryDataStore()

    private fun TestScope.createRepository() = DataStoreRecentSearchRepository(dataStore, StandardTestDispatcher(testScheduler))

    private suspend fun RecentSearchRepository.names() = recentSearches.first().map { it.name }

    @Test
    fun `baslangicta son arama yok`() = runTest {
        assertTrue(createRepository().recentSearches.first().isEmpty())
    }

    @Test
    fun `eklenen sehir en uste gelir`() = runTest {
        val repository = createRepository()

        repository.add(sampleCity(id = 1, name = "İzmir"))
        repository.add(sampleCity(id = 2, name = "Rize"))

        assertEquals(listOf("Rize", "İzmir"), repository.names())
    }

    @Test
    fun `ayni sehir tekrar secilirse kopya olmadan en uste tasinir`() = runTest {
        val repository = createRepository()
        repository.add(sampleCity(id = 1, name = "İzmir"))
        repository.add(sampleCity(id = 2, name = "Rize"))
        repository.add(sampleCity(id = 3, name = "Ankara"))

        repository.add(sampleCity(id = 1, name = "İzmir"))

        assertEquals(listOf("İzmir", "Ankara", "Rize"), repository.names())
    }

    @Test
    fun `en fazla 5 sehir tutulur ve en eskisi duser`() = runTest {
        val repository = createRepository()

        (1L..6L).forEach { id -> repository.add(sampleCity(id = id, name = "Şehir $id")) }

        assertEquals(listOf("Şehir 6", "Şehir 5", "Şehir 4", "Şehir 3", "Şehir 2"), repository.names())
    }

    @Test
    fun `tek sehir silinir digerleri kalir`() = runTest {
        val repository = createRepository()
        repository.add(sampleCity(id = 1, name = "İzmir"))
        repository.add(sampleCity(id = 2, name = "Rize"))

        repository.remove(sampleCity(id = 1, name = "İzmir"))

        assertEquals(listOf("Rize"), repository.names())
    }

    @Test
    fun `temizle butun listeyi siler`() = runTest {
        val repository = createRepository()
        repository.add(sampleCity(id = 1, name = "İzmir"))
        repository.add(sampleCity(id = 2, name = "Rize"))

        repository.clear()

        assertTrue(repository.recentSearches.first().isEmpty())
    }

    @Test
    fun `yeni depo nesnesi ayni kaydi okur`() = runTest {
        createRepository().add(sampleCity(name = "Rize"))

        assertEquals(listOf("Rize"), createRepository().names())
    }
}
