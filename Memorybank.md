Seninle "Battle City (Tank 1990)" Java dönem projemi geliştireceğiz. Bu sohbette sen benim kodlama partnerim ve geliştiricim (Developer) rolündesin, ben ise süreci yöneteceğim (PM). Projeyi yönetmek, bağlamı kaybetmemek ve kuralların dışına çıkmamak için aşağıdaki "MEMORY BANK (HAFIZA BANKASI)" yapısını kullanacağız. 

Görevimiz: Aşağıdaki kurallara KESİNLİKLE uyarak projeyi aşama aşama kodlamak. Bana projenin tamamını tek seferde yazıp verme. Ben sana hangi aşamayı yapacağımızı söyledikçe, sadece o spesifik kısım için destek ol.

Senden her bir aşamayı tamamladığımızda veya kodda önemli bir değişiklik yaptığımızda bu Memory Bank'i güncellemeni ve bana güncel halini sunmanı istiyorum. Ayrıca yazdığın her kod parçası için bana aşağıdaki formatta bir açıklama yapmak ZORUNDASIN:
- Yapılan İşlem: (Ne eklendi/değiştirildi?)
- İşlemin Sebebi: (Bu koda neden ihtiyaç duyduk?)
- Kullanılan Yöntem ve Sebebi: (Müfredattaki hangi konu kullanıldı ve neden başka bir yol değil de bu yol seçildi?)

Lütfen aşağıdaki Hafıza Bankasını incele, kuralları anladığını onayla ve "Hafıza Bankası yüklendi. Aşama 1'e başlamak için hazırım." şeklinde cevap ver. Başka hiçbir kod yazma.

======================================================================
💾 BATTLE CITY PROJESİ - MEMORY BANK (v3 - Ana Menü ve Kararlı Sürüm)
======================================================================

1. PROJE KURALLARI VE SINIRLAMALAR (KESİN UYULACAK)
* Kullanılması Gereken Konular: Sınıflar, Kalıtım (Inheritance), Çok Biçimlilik (Polymorphism), Arayüzler (Interfaces), Generic Collections (List, Set vb.), GUI Components (Swing), Event Handling, Exception Handling, File I/O, Multithreading (Thread, Runnable).
* YASAKLI 1: `javax.swing.Timer` kesinlikle kullanılmayacak. Oyun döngüsü (Game Loop) ve animasyonlar için müfredata uygun olarak Multithreading (`Thread`, `Runnable` ve `Thread.sleep()`) yapısı kullanılacak.
* YASAKLI 2: Lambda expressions (`->`) kesinlikle kullanılmayacak. Tüm Event Listener'lar (ActionListener, KeyListener vb.) ve Runnable implementasyonları Anonymous Inner Classes (Anonim İç Sınıflar) kullanılarak yazılacak.
* YASAKLI 3: Düşman tankı çeşitleri geliştirilmeyecektir. Sadece tek tip standart gri tank kullanılacaktır.
* Dil: Java terimleri (Inheritance, Thread, Interface vb.) İngilizce bırakılacak, kod içi yorumlar ve bana yapılan tüm açıklamalar Türkçe olacak.

2. PROJE AŞAMALARI VE DURUM TAKİBİ
(Durum Kodları: [ ] Başlamadı, [>] Devam Ediyor, [X] Tamamlandı)

* [x] Aşama 1: Arayüz (GUI) İskeleti ve Menüler
  - Ana `JFrame` oluşturulması. Menu Bar (New Game, Map Editor, Options, High Scores, Help, About, Exit) eklenmesi.
* [x] Aşama 2: Temel Oyun Nesneleri ve OOP Mimarisi
  - Genel `GameObject` üst sınıfı. `Tank`, `Brick`, `Base`, `Bullet` alt sınıfları. X, Y koordinat mantığının kurulması.
* [x] Aşama 3: Görselleri Yükleme ve Ekrana Çizdirme
  - ImagePack PNG dosyalarının Exception Handling kullanılarak belleğe yüklenmesi. `paintComponent` ile JPanel üzerine çizdirilmesi.
* [x] Aşama 4: Klavye Girdileri, Multithreading ve Oyun Döngüsü
  - `Thread` ve `Runnable` kullanılarak ana oyun döngüsünün (Game Loop) kurulması ve `Thread.sleep()` ile ekranın sürekli yenilenmesi.
  - Anonymous Inner Class ile `KeyListener` implementasyonu ve WASD ile tank hareketinin döngüye entegre edilmesi.
* [x] Aşama 5: Çarpışma Kontrolü ve Oyun Mantığı
  - Nesnelerin oyun döngüsü içinde birbiriyle kesişme kontrolleri.
* [x] Aşama 6: Skor Sistemi ve Dosya Okuma/Yazma
  - Oyun sonu isim alma, skoru CSV dosyasına yazma ve okuma. Skorları sıralayarak (Comparator) ilk 10'u ekranda gösterme.
* [x] Aşama 7: Dialoglar, Map Editor ve Son Rötuşlar
  - Help ve About menüleri için `JOptionPane` popup pencereleri. JFileChooser kullanılmadan JOptionPane dosya girdisi yardımıyla harita kaydetme/yükleme.
* [x] Aşama 8: Özel Harita Oynama Desteği (Load Custom Map)
  - Map Editor'den kaydedilen özel haritanın zorluk seviyesi seçilerek oynanabilmesi. Seviye bitişinde oyunun başarıyla sonlanması.
* [x] Aşama 9: Orijinal NES Tarzı Ana Menü (Main Menu) Entegrasyonu
  - BATTLE CITY logosunu kırpan ve altındaki seçenekleri metin olarak dinamik çizip klavyeyle kontrol ettiren retro menü. Menü barının ana menüdeyken gizlenmesi, oyundayken görünmesi. Ana menüye dönüldüğünde arka plandaki oyun thread ve nesnelerinin dondurulması.
* [x] Aşama 10: Gelişmiş Özellikler (Power-up'lar ve Kalkanlar)
  - Oyuncu spawn kalkanı (doğuş koruması) kodlandı ve animasyonları eklendi. Power-up (kalkan, saat, bomba, can vb.) mekanikleri oyuna entegre edildi.
* [x] Aşama 11: Görsel İyileştirmeler (Animasyonlar)
  - Palet (tread) hareket efekti, mermi çarpışma kıvılcımları, tank patlama animasyonları, kalkan koruması animasyonu ve yıkık üs görseli tamamlandı.

3. SON İŞLEM GÜNLÜĞÜ (LOG)
======================================================================
- Tank-Tank Çarpışma Mekanizması Düzeltildi: Oyuncunun düşman tanklarının içinden geçmesi engellendi. Oyuncu ile düşman tankları arasındaki çarpışmalarda hem tahmini adım (`hitBox`) hem de mevcut anlık konum çakışması (`currentBox`) kontrol edilerek iç içe girme durumu tamamen çözüldü.
- Düşman-Düşman Çarpışması İyileştirildi: Düşman tanklarının birbiriyle karşılaşmasında kilitlenip hareketsiz kalmalarını engellemek için, aralarındaki çarpışma denetimi sadece hareket yönündeki tahmini adım (`hitBox`) ile sınırlandırıldı.
- Projede herhangi bir kural ihlali bulunmadığı doğrulandı (`javax.swing.Timer` ve lambda expression `->` kullanımı yoktur; multithreading, anonymous inner class, inheritance, interface ve exception handling gibi tüm müfredat kurallarına tam uyum sağlanmıştır).