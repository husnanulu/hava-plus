# Hava+ — Tasarım ve iş planı

**Slogan:** Dereceden fazlası.
**Amaç:** Sayıyı değil kararı göstermek ("Ceket yeterli. 18:00'den sonra şemsiyeni al."), açınca 3 saniyede bakılıp kapatılabilen, Türkçe, sade bir hava uygulaması. Paylaşım (BiP vb.) ve havaya göre müzik (fizy) ile "hayata bağlanan" bir deneyim.

Ekran taslakları: `docs/tasarim/`
| Dosya | Ne |
|---|---|
| `Home.dc.html` | **Ana ekran (esas tasarım).** İçindeki `weather` = `gunesli` / `yagmurlu`, `located` = true / false durumlarına göre değişir |
| `Home-Yellow.dc.html` | Ana ekranın alternatifi: güneşli üst alan sarı (henüz karar verilmedi, esas tasarım `Home.dc.html`) |
| `Cities.dc.html` | Şehir seç ekranı (konumum, favoriler, arama sonuçları) |
| `Main.dc.html` | Sistem açılış ekranı (Android 12+ SplashScreen) |
| `Icons.dc.html` | Uygulama ikonu; **seçilen: A** (lacivert zemin, ışınlı sarı güneş, içinde lacivert artı) |

Taslaklar HTML'dir; değerleri (renk, boşluk, köşe yarıçapı, yazı boyutu) Compose'a çevir, HTML'i projeye koyma. `px` değerleri `dp`/`sp` olarak aynen alınabilir.

---

## 1. Görsel kimlik

### Renkler (`ui/theme/Color.kt`)
**Görsel kimlik v2 marka renkleri** (`res/values/havaplus_colors.xml` → `hava_sky`, `hava_yellow`, `hava_ink`, `ic_launcher_background`; Compose → `HavaSky`, `HavaYellow`, `HavaInk`):
| Ad | Hex | Kullanım |
|---|---|---|
| HavaSky | `#4FB0EE` | Ana marka rengi: ikon ve açılış ekranı zemini, büyük sıcaklıktaki derece halkası (açık ve parçalı bulutlu gökyüzünde halka beyaz) |
| HavaYellow | `#FFD21F` | Logodaki artı, birincil eylem butonları (Konumumu kullan) |
| Yumuşak sarı | `#FFE27A` | Parçalı bulut ikonlarında güneş, üst alandaki fırtına şimşeği (renk ikon dosyalarının içinde) |
| HavaInk | `#1F2430` | Koyu metin: karlı gökyüzü, öneri kartı |

Ekranların geri kalanında kullanılan yardımcı renkler:
| Ad | Hex | Kullanım |
|---|---|---|
| Lacivert (Navy) | `#1A2A6C` | Şehir seç ve kartlardaki ana metin, fizy kartı, birincil buton çerçevesi |
| Mavi (Blue) | `#2D55D6` | Vurgu ikonları, aralık çubukları, yağış yüzdeleri |
| Koyu sarı | `#E0A800` | Küçük güneş ikonlarının çizgisi |
| Zemin | `#F4F6FC` | Ekran arka planı |
| Kart | `#FFFFFF` | Kartlar (öneri kartı dahil) |
| Öneri kartı (`RecommendationCardColors`) | zemin `#FFFFFF`, çerçeve `#E3E7F0`, metin `#1F2430`, hap `#F1F3F8` | Öneri kartı, her havada aynı |
| İkincil metin | `#4A5788` | Etiketler, alt yazılar |
| Ayırıcı | `#E8ECF6` / `#E3E7F4` | Çizgiler, aralık çubuğu zemini, kart çerçevesi |
| Açık metin (lacivert kartta) | `#C9D3F5` | fizy ve Konumum kartlarındaki ikincil yazılar |

> Canlı sarı (#FFD21F) sadece marka vurgusu, yumuşak sarı (#FFE27A) zemin ve hava ikonları için.

Üst alanın havaya göre gökyüzü renkleri `ui/theme/WeatherSky.kt` içinde (`WeatherSky`, `SkyStyle`); tablo için bkz. 2.1 → "Üst alan gökyüzü ve hava ikonu".

Material 3 `colorScheme` bu renklere bağlansın; `dynamicColor` (Android 12 duvar kâğıdı renkleri) KAPALI olsun ki marka renkleri her telefonda aynı görünsün. Karanlık tema şimdilik yok (ileride).

### Yazı tipi
Plus Jakarta Sans (Google Fonts, OFL lisanslı). Ağırlıklar: 400, 600, 700, 800.
- Dosyalar `app/src/main/res/font/` altına küçük harfle: `plus_jakarta_sans_regular.ttf`, `_semibold`, `_bold`, `_extrabold`. Dosyalar yoksa kullanıcıdan indirmesini iste; o zamana kadar varsayılan yazı tipiyle devam et.
- Ölçek: büyük sıcaklık 88sp/800 (letterSpacing -3), başlıklar 17–22sp/800, öneri cümlesi 20sp/800, gövde 15–16sp, etiket 12–13sp.

### Uygulama ikonu
**Görsel kimlik v2 — "+°" logosu:** büyük sarı artı, sağ üstünde beyaz derece halkası. Geometri 120×120 koordinat sisteminde:
- Artı, dikey kol: x=41 y=38, 18×64, köşe yarıçapı 9 → `M50,38 a9,9 0 0 1 9,9 v46 a9,9 0 0 1 -18,0 v-46 a9,9 0 0 1 9,-9 z`
- Artı, yatay kol: x=18 y=61, 64×18, köşe yarıçapı 9 → `M27,61 h46 a9,9 0 0 1 0,18 h-46 a9,9 0 0 1 0,-18 z` (iki kol `#FFD21F`)
- Derece halkası: merkez (92,26), r=13, içi boş, çizgi `#FFFFFF` kalınlık 10 → `M92,13 a13,13 0 1 1 0,26 a13,13 0 1 1 0,-26 z`

Adaptive icon (`mipmap-anydpi-v26/ic_launcher.xml`, `ic_launcher_round.xml`) üç katmanlı:
- **background:** `@color/ic_launcher_background` = düz `#4FB0EE`.
- **foreground:** `drawable/ic_launcher_foreground.xml`, viewport 108×108, üç yol `translateX=18.2 translateY=23.2 scaleX=0.56 scaleY=0.56` olan bir `<group>` içinde (işaret 66dp güvenli alanda kalır).
- **monochrome:** `drawable/ic_launcher_monochrome.xml`, aynı yollar tek renk (beyaz); Android 13+ "temalı simge" açıkken sistem bunu kendi rengine boyar.
Hazır dosyalar tasarımcıdan geldi, elle çizilmedi.

### Açılış ekranı
`androidx.core:core-splashscreen`. Zemin (`windowSplashScreenBackground`) `@color/hava_sky`, ortada "+°" logosu (`windowSplashScreenAnimatedIcon` = `@drawable/splash_icon`, ön planla aynı çizim). Tema adı `Theme.Hava.Starting`, `postSplashScreenTheme` = `Theme.Hava`. Ayrı bir slogan ekranı YOK (yapay bekletme yapılmaz). `setKeepOnScreenCondition` ile ilk tahmin yüklenene kadar açılış ekranı tutulur, en fazla 2 saniye.

### Uygulama adı
`strings.xml` → `app_name` = `Hava+`.

---

## 2. Ekranlar ve gezinme

```
Açılış (sistem) → Ana ekran ("Bugün") ⇄ Şehir seç
```
- Başlangıç rotası: Ana ekran. Alt menü (bottom navigation) YOK.
- Ana ekranın üstündeki şehir adına dokununca Şehir seç açılır.
- Şehir seç'te bir şehre / favoriye / "Konumum"a dokununca Ana ekrana o yerle dönülür (geri yığınında tek bir Ana ekran kalsın: `popUpTo` ile).
- Ana ekran rotası isteğe bağlı parametre alır: `home?name=&subtitle=&lat=&lon=` (mevcut `HavaNavHost` desenindeki gibi, `Uri.encode`). Parametre yoksa "konum kuralı" (Bölüm 4) uygulanır.
- Mevcut `ForecastScreen` / `ForecastViewModel` Ana ekrana dönüşür (yeniden adlandır: `HomeScreen`, `HomeViewModel`). Mevcut `CityScreen` Şehir seç'e dönüşür.

### 2.1 Ana ekran (`Home.dc.html`)
Yukarıdan aşağıya:
1. **Üst alan** (havaya göre geçişli gökyüzü `Modifier.weatherSky(style)`, bkz. tablo). Padding 20dp, alt padding 76dp (öneri kartı üstüne taşar).
   - Sol: (konum kullanılıyorsa konum iğnesi) + yer adı ("Kadıköy, İstanbul") + aşağı ok → Şehir seç. Dokunma alanı ≥ 44dp.
   - Sağ: iki yuvarlak 44dp buton (yazı renginin yarı saydam zemini): ☆ favori (favorideyse dolu ★), paylaş. İğne, yer adı, yıldız ve paylaş ikonu üst alanın ana metin rengindedir.
   - Konum izni yoksa: yarı saydam bant "Konum izni kapalı. Son baktığın şehir gösteriliyor." + sarı **Konumumu kullan** butonu.
   - Büyük sıcaklık (`BigTemperature`, 88sp; "°" karakteri yerine `DegreeRing` ile halka, rengi `style.ring` — çap yazı boyutunun %34'ü, çizgi çapın %22'si, sayının sağ üstünde; küçük yazılarda normal "°" kalır), altında durum etiketi (`WeatherCondition.label`), sağda 76dp hava ikonu (`ic_wx_*`).
   - "En yüksek 23° · En düşük 14°" (bugünün günlük değerleri).
2. **Öneri kartı:** üst alana -52dp taşar, köşe 24dp, padding 20dp. Başlık "BUGÜN İÇİN ÖNERİ" (şemsiye ikonu), cümle (20sp/800), altında etiket hapları (chip). Zemin her havada beyaz `#FFFFFF` + 1dp `#E3E7F0` çerçeve, metin `#1F2430`; haplar `#F1F3F8` zeminde `#1F2430` yazı (görsel kimlik v2; havaya göre renk değiştirmez). İçerik Bölüm 3'teki kurallardan.
3. **Üç küçük kart** (eşit genişlik, aralık 10dp, köşe 18dp): Hissedilen, Nem, Rüzgâr.
4. **Saatlik:** başlık + yatay `LazyRow`; kart 64dp genişlik, köşe 18dp: saat, ikon, sıcaklık, yağış olasılığı (%0 ise boş).
5. **7 Günlük:** beyaz kart içinde satırlar (min 52dp): gün ("Bugün", sonra kısa gün adı "Pzt"…), yağış %, ikon, en düşük, **aralık çubuğu**, en yüksek. Aralık çubuğu: 6dp yüksek zemin `#E3E7F4`; içindeki mavi parça haftanın en düşüğü–en yükseğine göre konumlanır (`left = (min - haftaMin) / (haftaMax - haftaMin)`, `width = (max - min) / (...)`).
6. **fizy kartı** (`ui/home/FizyCard.kt`, renkler `ui/theme/FizyCardColors.kt`): koyu `#17151F` zemin, köşe 24dp; mor → pembe (`#7B2CBF` → `#E0218A`) çapraz geçişli 44dp daire içinde beyaz nota ikonu, "{Güneşli günün müziği: Pop}" (15sp Bold), "Havaya göre seçildi" (12sp), sağda aynı renklerde yatay geçişli hap **fizy'de dinle** butonu (dokunma alanı ≥ 48dp). Kartın tamamı da aynı linki açar.
7. **Paylaş butonu:** tam genişlik, 52dp, beyaz zemin, lacivert 1.5dp çerçeve: "Tahmini BiP'te ya da başka yerde paylaş".
8. Alt not: "Veri: Open-Meteo".
Yükleniyor: mevcut Shimmer yaklaşımı; Hata: mevcut `ErrorView` + Tekrar dene.

### Üst alan gökyüzü ve hava ikonu
Hava durumu gökyüzü stili (stiller `ui/theme/WeatherSky.kt`, seçim `ui/home/WeatherStyle.kt` → `skyStyle`, çizim `Modifier.weatherSky(style)`).
Zemin üç renkli dikey geçiştir (üst → %55'te orta → alt), üstüne radyal ışık parlamaları biner:
| Durum | `WeatherCondition` | `SkyStyle` | Geçiş (üst · orta · alt) | Parlamalar | `ink` | `sub` | Halka (`ring`) | `isDark` |
|---|---|---|---|---|---|---|---|---|
| Açık/güneşli (kod 0) | CLEAR | `WeatherSky.Clear` | `#1E78D6` · `#4EA6EC` · `#F6D9A0` | sağ üstte güneş (`#FFF4C8`, `#8CFFD678`) | `#FFFFFF` | `#F2F8FF` | `#FFFFFF` | evet |
| Az / parçalı bulutlu (1, 2) | MAINLY_CLEAR, PARTLY_CLOUDY, UNKNOWN | `WeatherSky.PartlyCloudy` | `#4F86BF` · `#8FB5DA` · `#D6E3EF` | sağ üst güneş `#8CFFF0BE`, sol alt bulut `#73FFFFFF` | `#FFFFFF` | `#EEF4FA` | `#FFFFFF` | evet |
| Kapalı/sisli | OVERCAST, FOG | `WeatherSky.Overcast` | `#6F7A89` · `#9AA3B0` · `#C9CED6` | üst ortada `#40FFFFFF` | `#FFFFFF` | `#F0F2F5` | `#4FB0EE` | evet |
| Yağmurlu (çisenti dahil) | DRIZZLE, RAIN, RAIN_SHOWERS | `WeatherSky.Rain` | `#1F2A3D` · `#3E4D66` · `#7C8BA3` | alt ortada `#59AABED7` | `#FFFFFF` | `#DCE3EE` | `#4FB0EE` | evet |
| Karlı | SNOW, SNOW_SHOWERS | `WeatherSky.Snow` | `#8FA7C2` · `#C4D3E3` · `#F2F6FA` | alt ortada `#E6FFFFFF` | `#1F2430` | `#34495F` | `#4FB0EE` | hayır |
| Fırtınalı | THUNDERSTORM | `WeatherSky.Storm` | `#120F24` · `#2C2848` · `#55557A` | sol üstte `#59BEAAFF` | `#FFFFFF` | `#CFCDE3` | `#4FB0EE` | evet |

- `ink`: şehir adı, butonlar, büyük sıcaklık, durum yazısı. `sub`: en yüksek / en düşük satırı.
- İkon ve gökyüzü aynı gruptan gelir: `ui/component/WeatherIcons.kt` → `weatherGroup()` (0 açık · 1, 2 parçalı · 3, 45, 48 kapalı/sisli · 51–67, 80–82 yağmur · 71–77, 85, 86 kar · 95–99 fırtına).
- `isDark` = yazılar beyaz mı. Durum çubuğu ikonları ve "Konumumu kullan" butonu buna bakar: koyu gökyüzünde açık ikon + sarı buton, karlı (açık) gökyüzünde koyu ikon + koyu buton.
- Durum çubuğunun arkasındaki şerit gökyüzünün `top` rengindedir.
- Öneri kartı her havada beyaz (`RecommendationCardColors`).

Hava ikonları tasarımdan birebir çıkarılmış 12 hazır vector drawable'dır (`res/drawable/ic_wx_*.xml`), elle çizilmez:
- Gökyüzü üst alanı → `ic_wx_clear`, `_partly`, `_cloudy`, `_rain`, `_snow`, `_storm` (76dp; beyaz çizgili sürümler).
- Beyaz kartlar (saatlik, 7 günlük, favoriler, Konumum) → aynı adların `_card` sürümleri (listelerdeki mevcut boyut). Lacivert Konumum kartında `_card` ikonu küçük beyaz bir dairenin üstünde durur.
- İkonlar çok renklidir: `Image(painterResource(...))` ile, `tint`/`colorFilter` verilmeden gösterilir; `Icon` kullanılmaz.

### 2.2 Şehir seç (`Cities.dc.html`)
- Üstte geri oku + "Şehir seç" başlığı.
- Arama kutusu (mavi 1.5dp çerçeve, 52dp, köşe 16dp). Mevcut arama mantığı (Ara tuşuyla Open-Meteo araması, dört durum) korunur.
- **Konumum** kartı: lacivert zemin, sarı daire içinde konum ikonu, "Konumum" + yer adı, anlık sıcaklık + ikon. Konum izni yoksa: "Konumumu kullan" eylemi.
- **FAVORİLERİM:** beyaz kart içinde satırlar: ad + "il, ülke", anlık sıcaklık, ikon, dolu sarı ★ (dokununca favoriden çıkar). Favori yoksa kısa bir açıklama: "Yıldıza dokunarak şehir ekleyebilirsin."
- **ARAMA SONUÇLARI:** ülke kodu rozeti, ad + alt yazı, boş ☆ (favorideyse dolu ★).

---

## 3. Öneri motoru (saf Kotlin, test edilebilir)

`fun buildAdvice(forecast: Forecast): Advice` — `Advice(text: String, chips: List<String>)`. Android'e bağımlı olmasın (metinleri döndüren basit bir fonksiyon; ileride `strings.xml`'e taşınabilir ama ilk sürümde Kotlin içinde olabilir). "Şimdi" = `forecast.current.observedAt`; "önümüzdeki saatler" = şimdiden itibaren 12 saat.

**Giysi** (hissedilen sıcaklığa göre; yoksa gerçek sıcaklık):
| Hissedilen | Cümle | Hap |
|---|---|---|
| ≤ 5° | Mont, atkı ve bere zamanı | Mont |
| 6–12° | Mont giy | Mont |
| 13–18° | Ceket yeterli | Ceket |
| 19–24° | İnce bir üst yeterli | İnce üst |
| ≥ 25° | Tişört havası, bol su iç | Su |

**Yağış** (önümüzdeki 12 saatte yağış olasılığı):
- Şu anki saat ≥ %50 ya da şu anki kod yağmur/çisenti/sağanak → "Şemsiyesiz çıkma." · hap "Şemsiye"
- Daha sonraki bir saatte ilk kez ≥ %50 → "HH:00'den sonra şemsiyeni al." · hap "Şemsiye HH:00+"
- En yüksek %30–49 → "Yanına şemsiye almakta fayda var." · hap "Şemsiye"
- Değilse yağış cümlesi yok.

**Ekler** (en fazla bir tane, öncelik sırasıyla): fırtına kodu → "Fırtına bekleniyor, dışarıda uzun kalma."; kar kodu → "Yollar kaygan olabilir." + hap "Bot"; rüzgâr ≥ 40 km/sa → hap "Rüzgârlık"; hissedilen ≥ 30° → hap "Güneş kremi".

**Birleştirme:** "{Giysi}. {Yağış}" → örn. "Ceket yeterli. 18:00'den sonra şemsiyeni al." Haplar en fazla 3.
Testler: her sıcaklık aralığı, üç yağış durumu, sınır değerleri (5/6, 12/13, 18/19, 24/25, %49/%50).

---

## 4. Konum kuralı

- İzin: yalnızca `ACCESS_COARSE_LOCATION` (yaklaşık konum yeterli). Compose'da `rememberLauncherForActivityResult(RequestPermission)`.
- `LocationRepository` (interface): `suspend fun currentLocation(): Coordinates?` (Android `LocationManager`, son bilinen konum ya da tek seferlik güncelleme, zaman aşımı ~5 sn) ve `suspend fun placeName(coords): String?` (`Geocoder`; ilçe + il, örn. "Kadıköy, İstanbul"; olmazsa `null` → "Konumum").
- Açılışta yer seçimi (öncelik sırası):
  1. Rota parametresiyle gelen şehir (kullanıcı Şehir seç'ten seçtiyse) → o.
  2. İzin var ve konum bulundu → konum.
  3. DataStore'daki **son bakılan yer** → o, üstte "Konum izni kapalı" bandı (izin yoksa) gösterilir.
  4. Hiçbiri yoksa İstanbul (41.0082, 28.9784).
- Gösterilen her yer "son bakılan yer" olarak DataStore'a yazılır.
- Kullanıcı başka şehir seçtiyse uygulama kapanana kadar o gösterilir; yeniden açılışta tekrar konum.
- Bu seçim mantığı ViewModel'de, sahte `LocationRepository` ve sahte depolama ile unit test edilir.
- Emülatörde test: ⋯ → Location ile konum ver.

## 5. Favoriler

- `FavoritesRepository` (interface): `val favorites: Flow<List<City>>`, `suspend fun toggle(city: City)`, `fun isFavorite(id)`. Gerçek uygulama: DataStore Preferences, liste Gson ile JSON olarak saklanır (Gson zaten projede).
- En fazla 10 favori. Sıra: eklenme sırası.
- Ana ekrandaki ☆ ve Şehir seç'teki yıldızlar aynı akışı dinler (birinde değişince diğeri kendiliğinden güncellenir).
- Şehir seç'te favorilerin anlık sıcaklığı: her favori için `current` alanlarıyla hafif bir tahmin isteği (paralel). Hata olursa o satırda sıcaklık gösterilmez, liste yine görünür.
- Konum yerinin favoriye eklenmesi: koordinat + yer adıyla bir `City` olarak saklanır.

## 6. Paylaşım ve fizy

**Paylaş:** `Intent.ACTION_SEND` + `Intent.createChooser` (BiP yüklüyse listede çıkar; doğrudan paket hedefleme YOK). Metin:
`Hava+ · {Yer} bugün {sıcaklık}, {durum}. {Öneri cümlesi}`

**fizy:** `WeatherCondition`'a `musicLabel` ve `fizyUrl` eklenir; kart `Intent.ACTION_VIEW` ile linki açar, `ActivityNotFoundException` yakalanır ("Bağlantı açılamadı").
| Durum | musicLabel | Kart metni |
|---|---|---|
| CLEAR, MAINLY_CLEAR | Pop | Güneşli günün müziği: Pop |
| PARTLY_CLOUDY | Akustik | Bulutlu günün müziği: Akustik |
| OVERCAST, FOG | Lo-fi | Kapalı günün müziği: Lo-fi |
| DRIZZLE, RAIN, RAIN_SHOWERS | Sakin | Yağmurlu günün müziği: Sakin |
| SNOW, SNOW_SHOWERS | Kış | Karlı günün müziği: Kış |
| THUNDERSTORM | Rock | Fırtınalı günün müziği: Rock |
Linkler kullanıcı tarafından fizy'den seçilip verilecek; gelene kadar hepsi `https://fizy.com` olsun (tek bir sabit listede, kolay değiştirilebilir). fizy / BiP logosu KULLANILMAZ.

---

## 7. İş planı (fazlar)

Her faz sonunda: `assembleDebug` + `testDebugUnitTest` geçmeli ve `docs/OGRENME_NOTLARI.md`'ye o fazın bölümü eklenmeli. Fazlar arasında kullanıcıyı bekleme; hepsi bitince tek bir özet ver: ne yapıldı, emülatörde sırayla neler denenmeli, senden gerekenler.

- **Faz 0 — Yedek:** commit YOK. Başlamadan önce proje klasörünün tamamını (`.git` dahil, `build` klasörleri hariç) bir üst klasöre `weather-app-yedek-kamp` adıyla kopyala; böylece kampın bitmiş hâli her zaman geri alınabilir.
- **Faz 1 — Kimlik:** `app_name` = Hava+, renkler + tema (`dynamicColor` kapalı), yazı tipi (dosyalar varsa), adaptive ikon, `core-splashscreen`. Kabul: emülatörde ikon ve açılış ekranı yeni; mevcut ekranlar yeni renklerle çalışıyor; 32 test geçiyor.
- **Faz 2 — Ana ekran + Şehir seç görünümü:** Gezinme yeniden düzeni (başlangıç Ana ekran), `HomeScreen` Bölüm 2.1'e göre, üst alan renk kuralı, öneri motoru + testleri, `Cities` Bölüm 2.2'ye göre (favori bölümü şimdilik boş durum metniyle). Konum gelene kadar parametresiz açılışta İstanbul. Kabul: İstanbul açılıyor, öneri kartı doğru, başka şehir seçilince ana ekran o şehirle geliyor, yağmurlu bir şehirde üst alan koyu ve kart beyaz.
- **Faz 3 — Konum:** Bölüm 4. Kabul: izin verince konumdaki yer, reddedince son bakılan yer + bant, "Konumumu kullan" izni yeniden istiyor; seçim mantığı testli.
- **Faz 4 — Favoriler:** Bölüm 5. Kabul: ☆ ile ekle/çıkar, uygulama kapanıp açılınca duruyor, Şehir seç'te sıcaklıklarıyla listeleniyor; repository + ViewModel testli.
- **Faz 5 — Paylaş + fizy:** Bölüm 6. Kabul: paylaş menüsü açılıyor, metin doğru; fizy kartı havaya göre değişiyor ve link açılıyor.
- **Faz 6 — Toparlama:** kullanılmayan kodu temizle, README'yi Hava+ için güncelle, tüm testler geçiyor.
