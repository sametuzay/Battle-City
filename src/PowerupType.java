// Oyundaki powerup türlerini tanımlayan Enum sınıfı
public enum PowerupType {
    SHIELD,   // Kalkan: Oyuncuya geçici hasar almazlık verir
    FREEZE,   // Saat: Tüm düşmanları birkaç saniye dondurur
    SHOVEL,   // Kazma: Üssün etrafındaki tuğlaları geçici olarak çeliğe çevirir
    STAR,     // Yıldız: Oyuncunun aynı anda 2 mermi atabilmesini sağlar
    GRENADE,  // El Bombası: Ekrandaki tüm düşmanları anında yok eder
    LIFE      // 1UP: Oyuncuya +1 can ekler
}
