# Hava+ — Proje kuralları (Claude Code için)

Bu proje Bilişim Kampı (KAMP+) Android eğitiminin `weather-app` reposudur. Eğitimdeki
Uygulama 2–5 tamamlandı; şimdi uygulama **Hava+** adıyla yeniden tasarlanıyor.
Tasarım ve iş planı: `docs/HAVAPLUS_TASARIM.md`. Ekran kaynakları: `docs/tasarim/*.dc.html`
(HTML tasarım taslaklarıdır; renk, ölçü ve düzen için referans alınır, projeye kopyalanmaz).

## En önemli hedef: anlatılabilir olsun
Bu, kampta öğretilenlerin **kişiselleştirilmiş hâli**. Proje sahibi bir öğrenci ve sunumda her satırı
kendisi anlatacak. Bu yüzden:
- Kampta görülen kalıpların DIŞINA çıkma; yenisini icat etme, var olanı tekrar kullan:
  `data class`, `object`, `enum class`, `sealed interface LoadState`, `Route + Screen` ayrımı, state hoisting,
  `ViewModel + MutableStateFlow/StateFlow`, `viewModelScope.launch`, `Repository interface + gerçek uygulama`,
  Retrofit + Gson, `withContext(Dispatchers.IO)`, Navigation Compose rotaları (`?name=&lat=` deseni, `Uri.encode`),
  `SavedStateHandle`, `FakeRepository` + `MainDispatcherRule` ile unit test.
- Hilt/Koin, Room, RxJava, MVI kütüphaneleri, karmaşık Flow operatör zincirleri, reflection, ileri seviye generic hileleri KULLANMA.
- Kodu sade tut: kısa fonksiyonlar, açık isimler, her yeni dosyanın başında 2–4 satırlık Türkçe açıklama
  ("Bu dosya ne yapar, kampın hangi konusuna dayanır").
- Her fazın sonunda `docs/OGRENME_NOTLARI.md` dosyasına o faz için bir bölüm EKLE: ne değişti, hangi dosyalar,
  kampın hangi konusuna (Uygulama 2/3/4/5) dayanıyor, yeni bir kavram geldiyse (DataStore, konum izni, Intent,
  SplashScreen) 3–5 cümlelik sade açıklaması ve "sunumda nasıl anlatırım" için 1–2 cümle.

## Teknoloji çerçevesi
- Kotlin + Jetpack Compose (Material 3). XML layout, Fragment, View sistemi YOK.
- Mimari aynen korunur: `UI (Route + Screen) → ViewModel (StateFlow) → Repository (interface + gerçek uygulama) → Retrofit / Android servisleri`.
- Ekran durumu `ui/LoadState` ile; gerekiyorsa yeni `sealed` durumlar.
- Eklenebilecek yeni bağımlılıklar YALNIZCA: `androidx.datastore:datastore-preferences`, `androidx.core:core-splashscreen`.
  Başka bir şeye ihtiyaç duyarsan kullanıcıya sormadan, bu çerçevedeki bir alternatifle çöz
  (ör. konum için Google Play Services değil Android `LocationManager` + `Geocoder`).
- Bağımlılıklar `gradle/libs.versions.toml` + `app/build.gradle.kts` üzerinden. AGP / Kotlin / Compose BOM sürümlerini YÜKSELTME.

## Kod kuralları
- Kullanıcıya görünen bütün metinler `res/values/strings.xml` içinde, Türkçe (öneri motorunun cümleleri Kotlin içinde kalabilir).
- Kullanıcıya teknik hata mesajı gösterme; ayrıntı `Log.e` ile Logcat'e.
- Ağ ve disk işleri Repository içinde `withContext(Dispatchers.IO)`.
- Her Repository bir `interface`; sahtesi `app/src/test/java/.../testing/FakeRepositories.kt` içine.
- Mevcut 32 unit test geçmeye devam etmeli (yeniden adlandırma gerekiyorsa testleri de uyarla). Yeni mantık için unit test yaz.
- Test dosyaları `app/src/test/java/...` altına; asla `src/main` altına.
- Saatlik tahminde "şimdi" = `forecast.current.observedAt` (şehrin yerel saati), `LocalDateTime.now()` değil.

## Çalışma kuralları
- Kullanıcıdan izin ya da onay BEKLEME; kararları bu dosya ve `docs/HAVAPLUS_TASARIM.md` doğrultusunda kendin ver, verdiğin kararları öğrenme notlarına yaz.
- Her fazın sonunda `./gradlew assembleDebug` ve `./gradlew testDebugUnitTest` çalıştır; hata varsa düzelt, ikisi de geçmeden bir sonraki faza geçme.
- `git commit`, `git push`, branch değiştirme YAPMA (kullanıcı bunu eğitmenine danışacak). Dosyaları silmeden önce gerçekten kullanılmadığından emin ol.
- Yazı tipi dosyaları yoksa Plus Jakarta Sans'ı (OFL lisanslı) Google Fonts'un GitHub deposundan indirip `res/font` altına koyabilirsin; indirilemezse varsayılan yazı tipiyle devam et ve notlara yaz.
- Yalnızca kullanıcıdan gelmesi gereken şeyler için dur ama işi bekletme: fizy çalma listesi linkleri (şimdilik `https://fizy.com`). Bunları en sonda "senden gerekenler" listesinde topla.
