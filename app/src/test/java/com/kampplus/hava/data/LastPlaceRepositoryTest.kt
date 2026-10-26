package com.kampplus.hava.data

import com.kampplus.hava.testing.InMemoryDataStore
import com.kampplus.hava.testing.sampleCity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** "Son bakılan yer" deposunun testi: şehir JSON'a çevrilip DataStore'a yazılıyor ve geri okunuyor mu? */
@OptIn(ExperimentalCoroutinesApi::class)
class LastPlaceRepositoryTest {

    private fun TestScope.createRepository() = DataStoreLastPlaceRepository(InMemoryDataStore(), StandardTestDispatcher(testScheduler))

    @Test
    fun `hic kayit yoksa null doner`() = runTest {
        assertNull(createRepository().lastPlace())
    }

    @Test
    fun `kaydedilen yer geri okunur`() = runTest {
        val repository = createRepository()

        repository.saveLastPlace(sampleCity(name = "İzmir"))

        val place = repository.lastPlace()
        assertEquals("İzmir", place?.name)
        assertEquals("TR", place?.countryCode)
        assertEquals("Ankara, Türkiye", place?.subtitle)
    }

    @Test
    fun `yeni kayit eskisinin yerine gecer`() = runTest {
        val repository = createRepository()

        repository.saveLastPlace(sampleCity(name = "İzmir"))
        repository.saveLastPlace(sampleCity(name = "Rize"))

        assertEquals("Rize", repository.lastPlace()?.name)
    }
}
