package models

/**
 * Représente une carte mythologique dans L'Archive des Dieux
 */
data class Card(
    val id: String,
    val name: String,
    val faction: Faction,
    val rarity: Rarity,
    val attack: Int,
    val health: Int,
    val description: String,
    val imagePath: String
)

/**
 * Les différentes factions mythologiques
 */
enum class Faction(val displayName: String) {
    GREC("Grec"),
    EGYPTIEN("Égyptien"),
    NORDIQUE("Nordique")
}

/**
 * Les niveaux de rareté des cartes
 */
enum class Rarity(val displayName: String) {
    COMMUNE("Commune"),
    RARE("Rare"),
    LEGENDAIRE("Légendaire")
}

/**
 * Types de packs disponibles
 */
enum class PackType(val displayName: String, val icon: String, val description: String) {
    STANDARD("Pack Standard", "", "1 rare garanti"),
    LEGENDARY("Pack Légendaire", "", "1 légendaire garanti")
}

/**
 * Représente un pack de cartes à ouvrir
 */
data class BoosterPack(
    val id: String,
    val faction: Faction,
    val packType: PackType = PackType.STANDARD,
    val cards: List<Card> = emptyList()
)
