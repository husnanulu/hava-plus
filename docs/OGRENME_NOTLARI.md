# Hava+ — Öğrenme notları

Bu dosya, kampın bitmiş `weather-app` projesinin **Hava+**'ya dönüşürken her fazda neyin
değiştiğini, hangi kamp konusuna dayandığını ve sunumda nasıl anlatılabileceğini toplar.
Tasarım ve iş planı: `docs/HAVAPLUS_TASARIM.md`.

---

## Faz 0 — Yedek

**Ne yapıldı:** Hiçbir şeye dokunmadan önce proje klasörünün tamamı (`.git` dahil) bir üst klasöre
`weather-app-yedek-kamp` adıyla kopyalandı. `build`, `app/build`, `.gradle` ve `.kotlin` klasörleri
kopyalanmadı; bunlar Gradle'ın her derlemede yeniden ürettiği önbelleklerdir.

**Neden:** Commit atmadık (eğitmene danışılacak). Kopya, kampın bitmiş hâline her an dönebilmenin en basit yolu.

**Sunumda:** "Yeniden tasarıma başlamadan önce kampın bitmiş hâlini ayrı bir klasöre yedekledim;
yani Hava+ her zaman kamp projesiyle karşılaştırılabilir."

---

## Faz 1 — Kimlik (ad, renk, yazı tipi, ikon, açılış ekranı)

**Ne değişti**
- Uygulama adı `Hava` → `Hava+` (`res/values/strings.xml` → `app_name`).
- Renkler: `ui/theme/Color.kt` artık marka renklerini isimleriyle tutuyor (Navy, Blue, Yellow,
  StormGray…). `ui/theme/Theme.kt` bu renkleri Material 3 rollerine bağlıyor
  (`primary = Blue`, `background = #F4F6FC`, `onSurface = Navy`…).
- Karanlık tema ve `dynamicColor` yok: marka renkleri her telefonda aynı görünsün diye
  `HavaTheme` tek bir `lightColorScheme` kullanıyor.
- Yazı tipi: Plus Jakarta Sans'ın 4 ağırlığı `res/font/` altında
  (`plus_jakarta_sans_regular / _semibold / _bold / _extrabold.ttf`). `ui/theme/Type.kt` bunları
  bir `FontFamily` içinde topluyor ve Material'ın tüm yazı rollerine (displayLarge 88sp…labelSmall 11sp) dağıtıyor.
- Uygulama ikonu: `res/drawable/ic_launcher_foreground.xml` (vektör) + `res/mipmap-anydpi/ic_launcher*.xml`
  (adaptive icon), arka plan `#1A2A6C`.
- Açılış ekranı: `androidx.core:core-splashscreen` bağımlılığı (`gradle/libs.versions.toml` +
  `app/build.gradle.kts`), `res/values/themes.xml` içinde `Theme.Hava.Starting`, manifest'te bu tema,
  `MainActivity` içinde `installSplashScreen()`.

**Kampın hangi konusu:** Kampın başlangıç iskeleti (`cp1-baslangic`): tema üçlüsü (Color / Theme / Type), `strings.xml`,
manifest ve `MainActivity` yaşam döngüsü; Uygulama 2'den itibaren bunların üzerine ekran kuruldu.

**Verilen kararlar**
- Google Fonts deposunda (`google/fonts`) bu yazı tipinin yalnızca *değişken* (tek dosyada bütün ağırlıklar)
  sürümü var. Tasarım belgesi dört ayrı dosya istediği için sabit ağırlıklı dosyalar, Google Fonts'un
  kaynak olarak gösterdiği resmi depodan (`tokotype/PlusJakartaSans`, aynı OFL lisansı) indirildi.
  Lisans metni: `docs/PlusJakartaSans-OFL.txt`.
- İkon klasörü belgede `mipmap-anydpi-v26` diye geçiyor; projede zaten `mipmap-anydpi` vardı.
  `minSdk = 26` olduğu için ikisi aynı işi görür, mevcut klasör korundu.
- İkon çizimi taslaktaki 120×120 SVG'den aynen alındı; `<group scaleX=0.5 translateX=24>` ile
  108dp tuvalin ortasına küçültüldü (güneş + ışınlar ≈ 54dp, 66dp güvenli alanın içinde).
  Artının iki kolu "yuvarlak uçlu kalın çizgi" olarak çizildi (köşesi 4 olan 32×8 dikdörtgenle aynı görüntü).
- `Main.dc.html`'deki alttaki "Hava+" yazısı açılış ekranına konmadı: `core-splashscreen` bu "marka resmi"ni
  desteklemiyor. Ayrı bir slogan ekranı da yok (belgede istenmiyor).

**Yeni kavram — SplashScreen (açılış ekranı)**
Android 12'den itibaren her uygulama açılırken sistem kısa bir açılış ekranı gösterir. `core-splashscreen`
kütüphanesi bu ekranı eski Android sürümlerinde de aynı şekilde gösterir ve rengini/ikonunu bizim
seçmemizi sağlar. Ayarlar bir XML temasında yapılır (`windowSplashScreenBackground`,
`windowSplashScreenAnimatedIcon`), uygulama hazır olunca pencere `postSplashScreenTheme` ile normal temaya geçer.
Kodda tek gereken, `MainActivity.onCreate` içinde `super.onCreate`'ten önce `installSplashScreen()` çağırmaktır.

**Sunumda nasıl anlatırım:** "Kamptaki tema dosyalarını koruyup içlerini Hava+'nın renkleri ve yazı tipiyle
doldurdum; açılış ekranı için tek bir tema ve tek satır kod yetti."

---

## Faz 2 — Ana ekran + Şehir seç görünümü

**Ne değişti**
- Gezinme: başlangıç rotası artık **Ana ekran** (`home?id=&name=&subtitle=&lat=&lon=`), Şehir seç `cities`.
  Şehir seçilince `popUpTo(HOME_ROUTE) { inclusive = true }` ile eski Ana ekran yığından atılır;
  geri yığınında hep tek bir Ana ekran kalır (`ui/HavaNavHost.kt`).
- `ForecastScreen` / `ForecastViewModel` → `ui/home/HomeScreen.kt`, `HomeSections.kt`, `HomeViewModel.kt`.
  Parametresiz açılışta İstanbul gösterilir (`HomeViewModel.DEFAULT_CITY`).
- Üst alan rengi ve öneri kartının sarı/beyaz olması: `ui/home/WeatherStyle.kt` (`skyColor`, `isSunnyCard`),
  7 günlük aralık çubuğu hesabı: `rangeBarPosition`.
- **Öneri motoru:** `model/Advice.kt` → `buildAdvice(forecast): Advice(text, chips)`. Saf Kotlin.
- Emoji yerine çizgi ikonlar: `ui/component/HavaIcons.kt` (taslaktaki SVG yolları `addPathNodes` ile `ImageVector`'e çevrildi).
- Şehir seç (`ui/city/CityScreen.kt`, `CityRow.kt`): geri oku + başlık, mavi çerçeveli arama kutusu,
  "FAVORİLERİM" (şimdilik boş durum metni), "ARAMA SONUÇLARI" kartı. Arama mantığı (CityViewModel) aynen korundu.
- Açılış ekranı ilk tahmin gelene kadar (en fazla 2 sn) tutuluyor: `MainActivity` → `setKeepOnScreenCondition`.
- Durum çubuğu ikon rengi ekrana göre: `ui/component/StatusBarIcons.kt`.
- Ortak `degrees()` / `percent()` fonksiyonları `ui/Format.kt` dosyasına taşındı.
- Testler: `ForecastViewModelTest` → `HomeViewModelTest` (7 eski test uyarlandı + 1 yeni), yeni `AdviceTest` (20),
  `WeatherStyleTest` (6). Toplam 59 test.

**Kampın hangi konusu:** Uygulama 2 (sabit şehir listesi: `CityCard`, `LazyColumn` → burada `LazyRow` ve kart listeleri),
Uygulama 3 (`CityViewModel` ile ViewModel + StateFlow, state hoisting: `HomeRoute` → `HomeScreen`), Uygulama 5 (tahmin ekranı,
Navigation Compose, `?name=` parametreleri, `Uri.encode`, `SavedStateHandle`, `FakeRepository` + `MainDispatcherRule` testleri).

**Verilen kararlar**
- Rotaya `id` parametresi de eklendi (belgede yoktu): favori yıldızı için şehri kimliğiyle tanımamız gerekiyor.
  Rotadaki hazır alt yazı ("Ankara, Türkiye") `City.country` alanına konuyor; `City.subtitle` aynı metni geri veriyor.
- Koordinat sayıya çevrilemezse (bozuk rota) uygulama çökmez, İstanbul gösterilir (eski testin yeni hâli).
- Öneri kartının üst alana binmesi için kart ve altındaki her şey `Modifier.offset(y = -52.dp)` ile kaydırıldı.
- "18:00'den / 19:00'dan / 15:00'ten" eki Türkçe ses uyumuna göre seçiliyor (`ablativeSuffix`).
- Hata durumunda da üstte yer adı görünüyor; kullanıcı başka şehre geçebilsin diye.
- Fırtına ve kar ekleri, şu anki kod **ve** önümüzdeki 12 saatin kodlarına bakıyor ("bekleniyor" dediği için).
- Hata mesajları (`LoadState.Error`) kamptaki gibi ViewModel'de Türkçe metin olarak kaldı; testler bu metinleri kontrol ediyor.

**Yeni kavram — saf fonksiyonla iş kuralı**
Öneri motoru Android'e hiç dokunmayan bir Kotlin fonksiyonudur: tahmini alır, cümle ve hap listesi döndürür.
Ekran yalnızca sonucu çizer. Bu ayrım sayesinde "5° ile 6° arasındaki sınır doğru mu?" sorusunu emülatör açmadan,
saniyeler içinde unit test ile cevaplayabiliyoruz.

**Sunumda nasıl anlatırım:** "Kamptaki tahmin ekranını Ana ekrana çevirdim; yeni olan tek mantık öneri motoru,
o da saf Kotlin olduğu için 20 testle güvence altında."

---

## Faz 3 — Konum

**Ne değişti**
- İzin: manifest'e yalnızca `ACCESS_COARSE_LOCATION` (yaklaşık konum) eklendi.
- `data/LocationRepository.kt`: `interface LocationRepository` + `AndroidLocationRepository`
  (Android'in kendi `LocationManager`'ı: önce 10 dakikadan yeni "son konum", yoksa tek seferlik istek, en fazla 5 sn;
  yer adı için `Geocoder`: "ilçe, il").
- `data/LastPlaceRepository.kt` + `data/HavaDataStore.kt`: "son bakılan yer" DataStore Preferences'ta, Gson ile JSON olarak.
- `data/AppContainer.kt`: Context isteyen repository'lerin tek adresi (`object` + `by lazy`, `MainActivity`'de `init`).
- `model/Coordinates.kt`: konum ve konumu `City`'ye çeviren `toCity()`.
- `HomeViewModel`: konum kuralı (`choosePlace`), `isLocated`, `showLocationBanner`, izin sonucu (`onLocationPermissionResult`).
- `HomeRoute`: `rememberLauncherForActivityResult(RequestPermission())` ile izin penceresi; açılışta bir kez,
  banttaki "Konumumu kullan" ile yeniden.
- Şehir seç: lacivert **Konumum** kartı (yer adı + anlık sıcaklık); izin yoksa "Konumumu kullan". Karta dokununca
  parametresiz Ana ekrana dönülür (= konum kuralı).
- `WeatherRepository.getCurrent()` + `ForecastApi.getCurrent()`: yalnızca `current` alanlarını isteyen hafif tahmin.
- Testler: `HomeViewModelTest` +9 konum kuralı testi, `CityViewModelTest` +3, `LastPlaceRepositoryTest` 3.
  Sahteler: `FakeLocationRepository`, `FakeLastPlaceRepository`, `InMemoryDataStore`. Toplam 74 test.

**Kampın hangi konusu:** Repository interface + gerçek uygulama, `withContext(Dispatchers.IO)`, `viewModelScope.launch` (Uygulama 4),
ViewModel + StateFlow ve state hoisting (Uygulama 3; Route izni ister, Screen yalnızca çizer), sahte Repository ile testler (Uygulama 5).

**Verilen kararlar**
- Konum kuralı sırası: rotadaki şehir → (izin + konum) → son bakılan yer (+ izin yoksa bant) → İstanbul.
  İzin var ama konum bulunamazsa bant gösterilmez (izin kapalı değil çünkü).
- Konumun Open-Meteo kimliği olmadığı için koordinattan negatif bir kimlik üretiliyor (`Coordinates.toCity`).
- Yer adı bulunamazsa "Konumum" yazar (sabit `HomeViewModel.LOCATION_FALLBACK_NAME`).
- Kullanıcı Şehir seç'ten şehir seçtiyse o şehir rota parametresinde durur; uygulama kapanana kadar o gösterilir.
  Uygulama yeniden açılınca rota parametresiz başlar → yine konum.
- ViewModel'lere Context vermek için `AndroidViewModel` ya da Hilt yerine `AppContainer` nesnesi seçildi; kamptaki
  `ApiClient` nesnesiyle birebir aynı fikir olduğu için anlatması kolay.
- DataStore testi gerçek dosya yerine bellekte duran `InMemoryDataStore` ile yapıldı: gerçek dosyalı DataStore
  Windows'ta JVM testinde dosyayı yeniden adlandıramıyor (telefonda bu sorun yok).
- `LocationManagerCompat.getCurrentLocation` 5 saniyede cevap vermezse `CancellationSignal` ile iptal ediliyor.

**Yeni kavramlar**
- **Çalışma anı izni (runtime permission):** Android 6'dan beri konum gibi hassas izinler manifest'e yazmakla yetmez,
  kullanıcıya pencereyle sorulur. Compose'da `rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission())`
  bir "başlatıcı" verir; `launch(izin)` pencereyi açar, kullanıcının cevabı `true/false` olarak geri gelir.
  Biz bu cevabı doğrudan ViewModel'e iletiyoruz; karar orada veriliyor ve test ediliyor.
- **DataStore Preferences:** Telefonda küçük anahtar–değer verilerini (ayarlar, son yer, favoriler) kalıcı saklayan
  Jetpack kütüphanesi. `data` bir Flow'dur (değişince haber verir), yazmak için `edit { }` kullanılır; ikisi de
  coroutine ile çalışır, ana iş parçacığını kilitlemez. Uygulama kapanıp açılsa da veri durur.
- **LocationManager + Geocoder:** Android'in yerleşik konum servisi ve koordinatı adrese çeviren sınıfı.
  Google Play Services'e bağımlı olmadıkları için her cihazda (emülatör dahil) aynı kodla çalışırlar.

**Sunumda nasıl anlatırım:** "Hangi şehrin gösterileceği kararı ViewModel'de dört basamaklı bir kural; izin penceresi
ise sadece cevabı ViewModel'e taşıyan bir köprü. Bu sayede kuralın her durumunu sahte konum ile test edebildim."

---

## Faz 4 — Favoriler

**Ne değişti**
- `data/FavoritesRepository.kt`: `interface FavoritesRepository` (`favorites: Flow<List<City>>`, `toggle(city)`)
  + `DataStoreFavoritesRepository` (liste DataStore'da Gson ile JSON). En fazla 10 favori, sıra = eklenme sırası.
- `AppContainer.favoritesRepository`: Ana ekran ve Şehir seç **aynı** nesneyi kullanır.
- `HomeViewModel`: `isFavorite` StateFlow'u ve `onFavoriteClick()`. Favori listesi `collect` ile dinlenir; liste ya da
  gösterilen yer değişince yıldız yeniden hesaplanır.
- `CityViewModel`: `favorites: StateFlow<List<CityWeather>>`, `onToggleFavorite(city)`. Sıcaklığı henüz bilinmeyen
  favoriler için hafif `getCurrent` istekleri **paralel** atılır (`async` + `awaitAll`); hata veren satırda sıcaklık
  gösterilmez, liste yine görünür.
- Şehir seç: "FAVORİLERİM" kartı (ad + "il, ülke", sıcaklık, ikon, dolu ★), arama sonuçlarında ☆/★.
  Yardımcı bileşenler: `RowsCard`, `RowDivider`, `FavoriteRow`, `StarButton`.
- Testler: `FavoritesRepositoryTest` (6), `HomeViewModelTest` +4, `CityViewModelTest` +5; sahte `FakeFavoritesRepository`.
  Toplam 89 test.

**Kampın hangi konusu:** Repository (Uygulama 4) + Flow, ViewModel'de `viewModelScope.launch { flow.collect { } }` (Uygulama 4),
StateFlow ile ekranın kendiliğinden güncellenmesi (Uygulama 3), `data class` + `copy` (Uygulama 2).

**Verilen kararlar**
- Belgedeki `isFavorite(id)` fonksiyonu repository'ye ayrıca eklenmedi: iki ekran da zaten tüm listeyi dinliyor,
  "favoride mi?" sorusu listeden (`any { it.id == id }`) cevaplanıyor. Böylece tek bir doğru kaynağı var.
- Liste 10'a ulaşınca yeni ekleme sessizce yok sayılıyor (yıldız boş kalır).
- Konum yeri favoriye eklenirse koordinatından üretilen negatif kimlik ve yer adıyla saklanıyor; Şehir seç'te alt
  satırı olmadan görünür.
- Gson'da liste için `TypeToken` (ileri seviye generic) yerine `Array<City>` çözümlenip `toList()` yapılıyor.

**Yeni kavramlar**
- **Flow ile iki ekranın konuşması:** DataStore'daki favori listesi bir akış (Flow). Ana ekran da Şehir seç de bu akışı
  dinliyor. Bir ekranda yıldıza basılınca DataStore'a yazılıyor, DataStore yeni listeyi akıştan yayınlıyor, öbür ekran
  hiçbir şey yapmadan güncelleniyor. Ekranlar birbirini tanımıyor; ortak kaynak repository.
- **async / awaitAll:** `async { }` bir işi başlatıp "sonucu sonra alacağım" der; `awaitAll()` hepsinin bitmesini bekler.
  Böylece 5 favorinin sıcaklığı sırayla değil aynı anda istenir.

**Sunumda nasıl anlatırım:** "Favoriler DataStore'da duruyor ve bir Flow olarak yayınlanıyor; iki ekran aynı akışı
dinlediği için birinde yıldızı değiştirince öbürü kendiliğinden güncelleniyor."

---

## Faz 5 — Paylaş + fizy

**Ne değişti**
- `model/WeatherCondition.kt`: her duruma `musicTitle`, `musicLabel`, `fizyUrl` eklendi; kart metni `musicText`
  ("Güneşli günün müziği: Pop").
- `model/FizyPlaylists.kt`: altı çalma listesi linki tek dosyada (şimdilik hepsi `https://fizy.com`).
- `ui/home/HomeActions.kt`: `shareForecast()` (ACTION_SEND + createChooser) ve `openLink()` (ACTION_VIEW,
  `ActivityNotFoundException` yakalanır → "Bağlantı açılamadı" Toast'u).
- `ui/home/HomeSections.kt`: `FizyCard` (lacivert kart, sarı "fizy'de dinle") ve `ShareButton` (lacivert çerçeveli).
  Üst alandaki yuvarlak paylaş butonu da aynı işi yapıyor.
- Paylaşılan metin (`strings.xml` → `share_text`): `Hava+ · {Yer} bugün {sıcaklık}, {durum}. {Öneri cümlesi}`.
- Düzeltmeler: Plus Jakarta Sans'ın "fi" ligatürü i'nin noktasını yutuyordu ("fızy"); `Type.kt`'de bütün stillerde
  `fontFeatureSettings = "liga 0"`. Ana ekranda aşağı kaydırınca içerik durum çubuğunun altından geçmesin diye
  üst alan renginde bir şerit eklendi.
- Testler: `WeatherConditionTest` +3 (müzik tablosu, kart metni, her durumun linki). Toplam 92 test.

**Kampın hangi konusu:** `enum class` alanları (Uygulama 2), `object` sabitleri, `strings.xml` biçim parametreleri
(`%1$s`), Route'ta `LocalContext` kullanıp Screen'i saf tutmak (state hoisting).

**Verilen kararlar**
- BiP'i doğrudan hedeflemiyoruz (paket adı yok): paylaş menüsü telefonda yüklü her uygulamayı gösterir, BiP de aralarında çıkar.
- fizy / BiP logosu kullanılmadı (belge gereği); kulaklık ve paylaş ikonları kendi çizgi ikonlarımız.
- Durum etiketi paylaşım metninde küçük harfle ("parçalı bulutlu"), Türkçe kurallarıyla (`Locale("tr")`).
- Paylaşım metni `getString` gerektirdiği için ayrı unit testi yok; emülatörde doğrulandı.

**Yeni kavram — Intent**
Intent, Android'e "şunu yapmak istiyorum" diye gönderilen bir mesajdır. `ACTION_SEND` "bu metni paylaş" demektir;
`Intent.createChooser` kullanıcıya hangi uygulamayla paylaşacağını seçtiren menüyü açar. `ACTION_VIEW` + bir web
adresi ise "bu linki aç" demektir; fizy uygulaması yüklüyse o, değilse tarayıcı açılır. Hiçbir uygulama
karşılayamazsa `ActivityNotFoundException` fırlar; biz bunu yakalayıp kullanıcıya kısa bir uyarı gösteriyoruz.

**Sunumda nasıl anlatırım:** "Uygulama hayata iki Intent ile bağlanıyor: biri tahmini BiP'e ya da istenen yere
gönderiyor, öteki havaya göre seçilmiş fizy listesini açıyor. Linkler tek bir dosyada, değiştirmesi bir dakika."

---

## Faz 6 — Toparlama

**Ne değişti**
- Kullanılmayan kod temizlendi: `WeatherCondition.emoji` (çizgi ikonlara geçildiği için) kaldırıldı;
  ilgili test "her durumun etiketi ve müzik türü vardır" olarak uyarlandı. Eski `ui/forecast` paketi Faz 2'de,
  `CityCard` Faz 2'de (`CityRow` oldu) kaldırılmıştı. Kullanılmayan string, renk, ikon ve import kalmadığı kontrol edildi.
- `HavaIcons.kt` içinde tekrar yazılmış hex renkler yerine tema sabitleri (`Yellow`, `DarkYellow`, `CloudGray`, `Blue`).
- `./gradlew spotlessApply` ile ktlint biçimi uygulandı (kamptan kalan `build.gradle.kts`'teki çift boş satır ve
  `ApiClient.kt`'de bir boş satır dahil; içerik değişmedi).
- `README.md` Hava+ için yeniden yazıldı (özellikler, mimari, klasörler, çalıştırma, lisanslar).
- Son durum: `assembleDebug`, `spotlessCheck` ve 92 unit testin tamamı geçiyor.

**Test sayıları (kamptan bugüne)**
| Test dosyası | Kamp | Hava+ |
|---|---|---|
| CityApiTest | 5 | 5 |
| ForecastApiTest | 6 | 6 |
| WeatherConditionTest | 5 | 8 |
| CityViewModelTest | 9 | 17 |
| ForecastViewModelTest → HomeViewModelTest | 7 | 21 |
| AdviceTest | – | 20 |
| WeatherStyleTest | – | 6 |
| LastPlaceRepositoryTest | – | 3 |
| FavoritesRepositoryTest | – | 6 |
| **Toplam** | **32** | **92** |

**Sunumda nasıl anlatırım:** "Kamptaki 32 testin hepsi duruyor (yalnızca ForecastViewModel → HomeViewModel adı değişti);
yeni her kural için test ekledim, toplam 92."

---

## Görsel kimlik v2

**Ne değişti, neden:** Hava+'nın logosu ve renkleri yenilendi. Yeni işaret "+°": büyük sarı bir artı, sağ üstünde
beyaz bir derece halkası. Tasarımcıdan hazır dosyalar geldi (ikon XML'leri, renk paleti, `DegreeRing`); bunlar
çizilmeden olduğu gibi projeye kondu, kod yalnızca bunları kullanacak şekilde bağlandı.

- **Renkler:** `res/values/havaplus_colors.xml` → `hava_sky #4FB0EE` (ana renk), `hava_yellow #FFD21F`, `hava_ink #1F2430`,
  `ic_launcher_background`. Aynı renkler Compose'ta `Color.kt` → `HavaSky`, `HavaYellow`, `HavaInk`.
  Eski `colors.xml` silindi; içindeki renkler yeni dosyada vardı, `splash_background` ise artık kullanılmıyordu.
  Eski `Yellow` sabitinin adı `HavaYellow` oldu; kullanılmayan `StormGray`, `ChipGray`, `OnDarkText` silindi.
- **İkon:** `drawable/ic_launcher_foreground.xml`, `ic_launcher_monochrome.xml`, `splash_icon.xml` ve
  `mipmap-anydpi-v26/ic_launcher(_round).xml` hazır dosyalardan kopyalandı. Eski güneşli/lacivert çizim ve onun
  `mipmap-anydpi` klasörü kaldırıldı. Projede PNG/webp mipmap yoktu.
- **Açılış ekranı:** mevcut `Theme.Hava.Starting` temasında yalnızca iki değer değişti: zemin `@color/hava_sky`, ikon `@drawable/splash_icon`.
  Ekranda tutma mantığı (ilk tahmin ya da en fazla 2 sn) aynı.
- **Ana ekran üst alanı:** renkler `ui/theme/WeatherPalette.kt` içinde (`HeaderColors(background, ink, sub)`). Hangi havanın hangi
  renge düşeceğini `WeatherStyle.kt` → `headerColors(condition)` seçiyor. Tanınmayan kod (`UNKNOWN`) için paletin nötr rengi,
  yani parçalı bulutlu seçildi. Yer adı, iğne, yıldız, paylaş ve büyük hava ikonu `ink` renginde. Fırtınada şimşek
  `StormBolt` (sarı). `isDarkHeader()` koyu zemini bilir; durum çubuğu ikonları ona göre açık/koyu olur.
  Güneşli havada zemin de sarı olduğu için "Konumumu kullan" butonu orada koyu zemin + sarı yazıyla gösteriliyor.
- **Öneri kartı her zaman beyaz:** `RecommendationCardColors`. Havaya göre sarı kart seçen eski `isSunnyCard` mantığı kaldırıldı.
- **Derece halkası:** `ui/component/DegreeRing.kt` (projede paket adı tekil: `ui/component`). İçinde `DegreeRing` ve `BigTemperature` var.
  Yalnızca ana ekrandaki büyük sıcaklık `BigTemperature` ile çiziliyor. Saatlik, 7 günlük, favoriler ve
  en yüksek/en düşük yazılarında normal "°" kaldı. `HavaSky` zaten `Color.kt`'de olduğu için dosyada yeniden tanımlanmadı.
- **Testler:** `WeatherStyleTest` yeni palete göre: altı hava grubu, durum çubuğu kuralı, fırtınada sarı şimşek. Toplam 96 test, hepsi geçiyor.

**Dosyalar:** `res/values/havaplus_colors.xml` (yeni), `res/values/colors.xml` (silindi), `res/values/themes.xml`,
`res/drawable/ic_launcher_foreground.xml`, `ic_launcher_monochrome.xml` (yeni), `splash_icon.xml` (yeni),
`res/mipmap-anydpi-v26/ic_launcher*.xml` (yeni; `mipmap-anydpi` silindi), `ui/theme/WeatherPalette.kt` (yeni), `ui/theme/Color.kt`,
`ui/component/DegreeRing.kt` (yeni), `ui/home/WeatherStyle.kt`, `ui/home/HomeScreen.kt`, `ui/home/HomeSections.kt`,
`ui/city/CityScreen.kt` ve `ui/component/HavaIcons.kt` (yalnızca `HavaYellow` adı), `WeatherStyleTest.kt`, `docs/HAVAPLUS_TASARIM.md`, `README.md`.

**Kamp bağlantısı:** renk seçimi yine saf bir fonksiyon + `data class` + `object` (Uygulama 2), unit testle doğrulanıyor (Uygulama 5);
ekran bileşenleri state hoisting ile renkleri parametre olarak alıyor (Uygulama 3).

**Yeni kavramlar**
- **Adaptive icon katmanları:** Android 8+ simgeyi tek resim olarak değil katman katman ister. `background` düz mavi zemin,
  `foreground` "+°" işareti, `monochrome` aynı işaretin tek renkli hâli. Telefonun launcher'ı bu katmanları yuvarlak, kare ya da
  damla biçimli bir maskeyle keser. O yüzden işaret 108dp'lik tuvalin ortadaki 66dp'lik güvenli alanında durur.
  Android 13+'te "temalı simgeler" açıksa sistem monochrome katmanı duvar kâğıdı rengine boyar.
- **Vector drawable:** ikonlar PNG değil XML: `<path android:pathData="M50,38 a9,9 …">` komutlarıyla çizilen şekiller.
  Her ekran yoğunluğunda keskin görünür ve tek dosya yeter. `<group>` ile bütün çizim ölçeklenip kaydırılabiliyor: 120×120'lik
  tasarım 0.56 ölçekle 108'lik tuvale oturtuldu.
- **Canvas ile halka çizimi:** Compose'ta `Canvas` kendi çizim alanını verir. `drawCircle(style = Stroke(width))` içi boş,
  yalnızca çizgisi olan bir daire çizer. Çizgi kalınlığı çapın %22'si. Yarıçaptan yarım kalınlık düşülür ki halka kutunun dışına taşmasın.
  İkon dosyası yerine böyle çizildiği için rengi ve boyutu parametreyle değişebiliyor.

**Sunumda nasıl anlatırım:** "Logodaki derece halkasını uygulamanın içine de taşıdım: büyük sıcaklıktaki ° işareti aslında
Canvas ile çizilmiş mavi bir halka. Uygulama simgesi üç katmanlı bir adaptive icon; renkleri tek bir paletten geliyor ve
hangi havanın hangi rengi alacağını test edilen küçük bir fonksiyon seçiyor."

---

## Hava durumu ikonları (hazır dosyalar)

**Ne değişti:** Kendi yazdığım hava ikonları (`HavaIcons.kt` içindeki `weatherIcon()`, `IconColors`, Kotlin'de çizilen bulut/güneş yolları)
kaldırıldı. Yerine tasarımdan birebir çıkarılmış 12 hazır vector drawable geldi (`res/drawable/ic_wx_*.xml`):
renkli üst alan için 6 ikon, beyaz kartlar için 6 `_card` sürümü.
- Yeni `ui/component/WeatherIcons.kt`: `WeatherGroup` (altı grup), `weatherGroup()`, `headerIcon()`, `cardIcon()`.
  Üst alan rengi (`headerColors`) de aynı `weatherGroup()`'u kullanıyor; ikon ile zemin rengi hiçbir havada ayrışamaz.
- Eşleme düzeldi: kod 1 (az bulutlu) artık sarı "açık" grubunda değil, parçalı bulutlu grubunda. Tanınmayan kod parçalı gruba düşer.
  `WeatherCondition` enum'ındaki kod → durum eşlemesi zaten tabloya uyuyordu, değişmedi.
- Üst alan ikonu 104dp'den 76dp'ye indi. Listelerde boyutlar aynı.
- Lacivert "Konumum" kartı için ayrı ikon yoktu. `_card` ikonu orada küçük beyaz bir dairenin içinde gösteriliyor, ikon yeniden çizilmedi.
- Kullanılmayan `CloudGray` rengi silindi. `WeatherPalette.StormBolt` artık kodda kullanılmıyor (şimşek rengi ikon dosyasının içinde);
  hazır palet dosyası olduğu için olduğu gibi bırakıldı.
- Testler: yeni `WeatherIconsTest` (kod → grup tablosu, ikon seçimi, ikonla rengin hep aynı gruptan gelmesi). `WeatherStyleTest` kod 1 için güncellendi,
  eski ikon rengi testi kaldırıldı. Toplam 98 test, hepsi geçiyor.

**Dosyalar:** `res/drawable/ic_wx_*.xml` (12 yeni), `ui/component/WeatherIcons.kt` (yeni), `ui/component/HavaIcons.kt`,
`ui/home/WeatherStyle.kt`, `ui/home/HomeScreen.kt`, `ui/home/HomeSections.kt`, `ui/city/CityScreen.kt`, `ui/theme/Color.kt`,
`WeatherIconsTest.kt` (yeni), `WeatherStyleTest.kt`, `docs/HAVAPLUS_TASARIM.md`.

**Yeni kavram — Icon ile Image farkı:** `Icon` tek renkli simgeler içindir: çizimdeki bütün renkleri yok sayıp her pikseli
`tint` rengine (verilmezse `LocalContentColor`'a) boyar. Bu ikonlarda bulutun içi beyaz, çizgisi koyu, şimşeği sarı;
`Icon` ile gösterilse hepsi aynı renge döner ve bulutun içi ile çizgisi birbirine karışır. `Image(painterResource(...))`
çizimi dosyadaki renkleriyle olduğu gibi gösterir. Bu yüzden çok renkli ikonlarda `Image` kullanılır, `tint` ya da
`colorFilter` verilmez. Arayüzdeki tek renkli ikonlar (yıldız, paylaş, iğne) ise `Icon` ile kalır.

**Sunumda nasıl anlatırım:** "Hava ikonları tasarımcının hazır dosyaları; hangisinin gösterileceğine tek bir `weatherGroup`
fonksiyonu karar veriyor, üst alanın rengi de aynı yerden geliyor. Çok renkli oldukları için `Icon` değil `Image` kullandım."

---

## Şehir seç: arama kutusu boş başlar

**Sorun ve nedeni:** Şehir seç ekranı her açılışta "İstanbul" yazılı geliyordu. Kamptan kalan
`CityViewModel` içinde `INITIAL_QUERY = "İstanbul"` başlangıç değeri vardı ve `init` bloğu açılışta bu metinle arama yapıyordu.
SavedStateHandle, DataStore ya da route parametresiyle ilgisi yoktu.

**Ne değişti**
- `_query = MutableStateFlow("")`: arama metni her girişte boş başlar ve hiçbir yere (DataStore, route) yazılmaz.
  Seçilen şehir zaten Ana ekranın rotasında ve "son bakılan yer" deposunda tutuluyor.
- Açılışta arama yapılmaz. Kutu boşken ekranda Konumum kartı ve favoriler, yazmaya başlayınca yalnızca arama sonuçları görünür (`CityScreen`).
- Yazarken sonuç gelsin diye: `onQueryChange` her harfte istek atmaz; bekleyen aramayı (`Job`) iptal eder,
  400 ms `delay` sonra tek bir arama başlatır. Kullanıcı yazmayı bırakınca yalnızca son metin aranır. Klavyedeki "Ara" ve
  "Tekrar dene" (`search()`) beklemeden arar. Metin silinirse arama yapılmaz.
- Placeholder "Şehir ara" (`R.string.search_hint`) ve yalnızca metin varken görünen temizle (X) butonu zaten vardı; aynen kaldı.
- Testler: `baslangicta query bos` ve `acilista arama yapilmaz` eklendi. Açılıştaki otomatik aramaya dayanan kamp testleri
  (Success/Empty/hata/tekrar dene) artık önce metin yazıp bekliyor. "Her harfte istek atılmaz" testi
  "yazma durunca tek istek atılır" olarak güncellendi. "Metin silinince arama yapılmaz" testi eklendi. Toplam 100 test, hepsi geçiyor.

**Dosyalar:** `ui/city/CityViewModel.kt`, `ui/city/CityScreen.kt` (+ boş kutu önizlemesi), `CityViewModelTest.kt`.

**Kamp bağlantısı:** Uygulama 3'teki `MutableStateFlow` + state hoisting ile arama kutusu, Uygulama 4'teki `viewModelScope.launch` ve dört durum.
Yeni olan tek şey `Job`: `launch` bir `Job` döndürür, `cancel()` ile henüz bitmemiş iş durdurulur. `delay` sırasında iptal edilen arama hiç istek atmaz.

**Sunumda nasıl anlatırım:** "Arama kutusu kampta İstanbul ile başlıyordu; artık boş başlıyor ve boşken favorileri gösteriyor.
Her harfte sunucuya gitmemek için bekleyen aramayı iptal edip kullanıcı yazmayı bırakınca tek istek atıyorum."

---

## Şehir seç: Son aramalar

**Ne değişti**
- Arama kutusu boşken sıra: Konumum kartı → **SON ARAMALAR** → FAVORİLERİM. Son aramalar, arama sonucundan dokunularak seçilen
  son 5 şehirdir; en yenisi en üstte. Hiç son arama yoksa bölüm hiç görünmez.
- Yalnızca arama sonuçlarındaki satıra dokunulunca eklenir. Yazılan yarım metinler, favoriler ya da son aramalardan yapılan seçimler listeye eklenmez.
- Aynı şehir yeniden seçilirse kopyası oluşmaz, en üste taşınır. 6. şehir gelince en eskisi düşer.
- Her satırda küçük X (tek şehri siler), başlığın yanında "Temizle" (hepsini siler). Metinler `strings.xml`'de
  (`recent_header`, `recent_clear`, `action_remove_recent`).
- Yeni `data/RecentSearchRepository.kt`: favorilerle aynı kalıp. Interface + `DataStoreRecentSearchRepository`,
  liste DataStore'da Gson ile JSON (`recent_searches` anahtarı), `recentSearches` bir Flow. `AppContainer.recentSearchRepository` ile tek nesne.
- `CityViewModel.onSearchResultClick(city, onSaved)`: önce şehri son aramalara yazar, **sonra** Ana ekrana geçer.
  Ekran kapanınca `viewModelScope` iptal olduğu için önce geçseydik kayıt yarıda kalabilirdi.

**Dosyalar:** `data/RecentSearchRepository.kt` (yeni), `data/AppContainer.kt`, `data/HavaDataStore.kt` (yorum), `ui/city/CityViewModel.kt`,
`ui/city/CityScreen.kt`, `res/values/strings.xml`, `testing/FakeRepositories.kt` (`FakeRecentSearchRepository`),
`data/RecentSearchRepositoryTest.kt` (yeni, 7 test), `ui/city/CityViewModelTest.kt` (+3 test). Toplam 110 test, hepsi geçiyor.

**Kamp bağlantısı:** Repository interface + gerçek uygulama (Uygulama 4), sahtesiyle unit test (Uygulama 5), `MutableStateFlow` ile ekran durumu (Uygulama 3).
DataStore + Gson kalıbı Faz 3–4'te favoriler için kurulmuştu; burada aynen tekrar kullanıldı.

**Sunumda nasıl anlatırım:** "Son aramalar favorilerin kardeşi: aynı DataStore + Gson kalıbı, yalnızca kuralları farklı.
En yenisi başa, kopya yok, en fazla 5. Kuralların her biri için test var."

---

## fizy kartı yeniden tasarlandı

**Ne değişti:** Yalnızca görünüm. Hangi havada hangi listenin açılacağı (`WeatherCondition.fizyUrl`) ve linkin açılması (`openLink`) aynı.
- Kart kendi dosyasında: `ui/home/FizyCard.kt` (`FizyCard` + `@Preview`). `HomeSections.kt`'deki eski lacivert kart ve
  artık kullanılmayan kulaklık ikonu (`HavaIcons.Headphones`) silindi.
- Koyu zemin, solda mor → pembe geçişli daire içinde beyaz nota, ortada liste adı ve "Havaya göre seçildi", sağda geçişli hap buton.
  Kartın tamamı da tıklanabilir (`Surface(onClick = …)`), aynı linki açar.
- Nota ikonu yeni bir vector drawable (`res/drawable/ic_music_note.xml`): beyaz çizgi, dolgu yok; `Image(painterResource(...))` ile gösteriliyor.
- Butonun görünen hapı küçük, ama çevresindeki `Box` en az 48dp yüksek ve tıklanabilir; parmakla kolay basılsın diye.
- `fizy_subtitle` metni "Havaya göre seçildi" oldu.

**Dosyalar:** `ui/home/FizyCard.kt` (yeni), `ui/theme/FizyCardColors.kt` (yeni), `res/drawable/ic_music_note.xml` (yeni),
`ui/home/HomeSections.kt`, `ui/component/HavaIcons.kt`, `ui/theme/Color.kt` (yorum), `res/values/strings.xml`, `docs/HAVAPLUS_TASARIM.md`.

**Yeni kavram — Brush.linearGradient:** `Modifier.background` düz bir renk yerine bir `Brush` (fırça) da alabilir.
`Brush.linearGradient(listOf(başlangıç, bitiş))` iki renk arasında yumuşak bir geçiş boyar. Başlangıç ve bitiş noktası verilmezse
geçiş sol üstten sağ alta, yani çapraz gider: dairede bunu kullandık. Butonda ise soldan sağa geçiş istendiği için
`Brush.horizontalGradient` kullandık; bu da yatay yönlü bir linearGradient'tir.

**Renkleri neden ayrı bir object'te tuttuk:** `FizyCardColors` kartın beş rengini tek yerde toplar. Tasarım değişirse
(ör. fizy'nin marka renkleri güncellenirse) yalnızca bu dosya değişir; kartın koduna dokunmak gerekmez. Ayrıca kodda
`Color(0xFF7B2CBF)` gibi anlamsız sayılar yerine `FizyCardColors.AccentStart` gibi ne işe yaradığını söyleyen adlar okunur.
Öneri kartındaki `RecommendationCardColors` ile aynı fikir.

**Sunumda nasıl anlatırım:** "fizy kartının mantığı aynı, yalnızca kılığı değişti. Renkler tek bir object'te, geçişler
Brush.linearGradient ile; kartın tamamına ya da butona dokunmak aynı listeyi açıyor."

---

## Konum bandı: açık mı koyu mu?

`HeaderColors`'a `isDark` alanı eklendi (yalnızca yağmur ve fırtınada `true`). "Konumumu kullan" butonu, bandın zemini ve yazısı
artık `locationBannerColors()` ile bu bilgiye göre seçiliyor: açık zeminde koyu buton + beyaz yazı, koyu zeminde sarı buton + koyu yazı.
Durum çubuğu ikonları da aynı `isDark`'a bakıyor. Renk koduna bakan iki eski kontrol kaldırıldı: `sky.background == HavaYellow` ve `ink == Color.White`.

**Neden renk koduna değil "açık mı koyu mu" bilgisine baktık:** Renk kodu tasarım değiştikçe değişir. Açık havanın sarısı `#FFD21F`'ten
`#FFE27A`'ya geçince eski `== HavaYellow` kontrolü hiçbir hata vermeden çalışmaz oldu. "Zemin koyu mu" ise rengin anlamıdır;
renk ne olursa olsun doğru kalır.

---

## Üst alan: düz renk yerine gökyüzü

**Ne değişti:** Ana ekranın üst alanı (şehir adı, butonlar, büyük sıcaklık, durum, en yüksek/en düşük, konum bandı) artık
havaya göre geçişli bir gökyüzü üzerine çiziliyor. Renkler tasarımcıdan hazır geldi (`WeatherSky.kt`); hiçbiri değiştirilmedi.
- `ui/theme/WeatherSky.kt` (yeni): `SkyGlow` ve `SkyStyle` data class'ları, altı hazır stil (`object WeatherSky`) ve onları çizen `Modifier.weatherSky(style)`.
  Dosyadaki geçici `HavaSkyBlue` silindi, aynı renk olan `HavaSky` (`Color.kt`) kullanılıyor.
- `WeatherStyle.kt`: `headerColors()` → `skyStyle()`; eşleme aynı `weatherGroup()`'tan (açık → Clear, parçalı → PartlyCloudy,
  kapalı/sis → Overcast, yağmur → Rain, kar → Snow, fırtına → Storm). `locationBannerColors()` artık `SkyStyle` alıyor.
- Yazı renkleri stilden: şehir adı, butonlar, sıcaklık, durum → `style.ink`; en yüksek/en düşük → `style.sub`.
- Derece halkası: `BigTemperature`'a `ringColor` parametresi eklendi, `style.ring` veriliyor (açık ve parçalı bulutluda beyaz, diğerlerinde `#4FB0EE`).
- `isDark` artık "yazı beyaz mı" demek; yalnızca karda `false`. Durum çubuğu ikonları ve "Konumumu kullan" butonu eskisi gibi buna bakıyor.
  Sonuç: kar dışındaki bütün havalarda ikonlar beyaz, buton sarı.
- Durum çubuğu arkasındaki şerit gökyüzünün `top` renginde.
- `WeatherPalette.kt`'deki `HeaderColors` ve `WeatherPalette` (StormBolt dahil) artık kullanılmadığı için silindi; `RecommendationCardColors` aynen duruyor.
- `ic_wx_clear`, `ic_wx_partly`, `ic_wx_cloudy`, `ic_wx_snow` beyaz çizgili sürümleriyle değiştirildi (rain/storm ve `_card` dosyalarına dokunulmadı).
- Testler: `WeatherStyleTest` yeni yapıya göre yazıldı (her grup doğru stil, isDark yalnızca karda false, halka renkleri, konum butonu);
  `WeatherIconsTest`'te ikon–gökyüzü eşlemesi `WeatherSky` ile kontrol ediliyor.

**Dosyalar:** `ui/theme/WeatherSky.kt` (yeni), `ui/theme/WeatherPalette.kt`, `ui/home/WeatherStyle.kt`, `ui/home/HomeScreen.kt`,
`ui/component/DegreeRing.kt`, `res/drawable/ic_wx_clear.xml`, `ic_wx_partly.xml`, `ic_wx_cloudy.xml`, `ic_wx_snow.xml`,
`test/.../ui/home/WeatherStyleTest.kt`, `test/.../ui/component/WeatherIconsTest.kt`, `docs/HAVAPLUS_TASARIM.md`, `docs/ekran/gokyuzu-*.png` (yeni).

**Yeni kavramlar:**
- **Brush.verticalGradient:** fizy kartındaki `linearGradient`'in yukarıdan aşağı olanı. `0f to üst, 0.55f to orta, 1f to alt` yazınca
  renkler alanın yüksekliğine göre o oranlarda yerleşir; gökyüzünün üstten koyu, ufka doğru açık görünmesi buradan gelir.
- **Brush.radialGradient:** bir merkezden dışa doğru yayılan daire geçişi. Merkezde parlak renk, kenarda aynı rengin tamamen saydamı;
  böylece güneşin ya da bulutun "parlaması" kenarı belli olmadan zemine karışır. Merkez ve yarıçap alanın boyutuna göre hesaplanır.
- **drawWithCache:** fırçalar alanın boyutuna (`size`) ihtiyaç duyar ama her karede yeniden oluşturulmaları gereksizdir.
  `drawWithCache { ... }` bloğu fırçaları yalnızca boyut ya da stil değişince bir kez hazırlar; içindeki `onDrawBehind` her çizimde
  sadece hazır fırçalarla boyar. `Modifier.background` tek bir fırça alabildiği için, geçiş + birden çok parlamayı üst üste koymak için bu gerekti.

**Sunumda nasıl anlatırım:** "Üst alanın zemini artık tek renk değil, bir gökyüzü: dikey geçiş üstüne radyal parlamalar.
Hangi gökyüzünün seçileceğini ikonla aynı `weatherGroup()` belirliyor, renkler tek bir object'te; çizim drawWithCache ile bir kez hazırlanıyor."
