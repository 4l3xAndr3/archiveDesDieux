package data

import models.Card
import models.Faction
import models.Rarity
import kotlin.random.Random

/**
 * Repository pour gérer les cartes et les boosters
 */
object CardRepository {
    
    private val allCards = mutableListOf<Card>()
    private val userCollection = mutableListOf<Card>()
    
    init {
        initializeCards()
    }
    
    private fun initializeCards() {
        // Cartes Grecques (basées sur les images disponibles)
        allCards.addAll(listOf(
            Card("gr_001", "Zeus", Faction.GREC, Rarity.LEGENDAIRE, 10, 10, "Le roi des dieux de l'Olympe", "cards/grec/Zeus (Légendaire).png"),
            Card("gr_002", "Hoplite", Faction.GREC, Rarity.COMMUNE, 4, 4, "Guerrier grec d'élite", "cards/grec/Hoplite.png"),
            Card("gr_003", "Hydre", Faction.GREC, Rarity.RARE, 7, 8, "Monstre à plusieurs têtes", "cards/grec/hydre.png"),
            Card("gr_004", "Méduse", Faction.GREC, Rarity.RARE, 5, 4, "Sort pétrifiant du regard", "cards/grec/Méduse (Sort).png"),
            Card("gr_005", "Pégase", Faction.GREC, Rarity.COMMUNE, 5, 5, "Cheval ailé divin", "cards/grec/Pégase.png")
        ))
        
        // Cartes Égyptiennes (basées sur les images disponibles)
        allCards.addAll(listOf(
            Card("eg_001", "Râ", Faction.EGYPTIEN, Rarity.LEGENDAIRE, 10, 9, "Dieu du soleil", "cards/egyptien/ra.png"),
            Card("eg_002", "Garde Anubis", Faction.EGYPTIEN, Rarity.RARE, 6, 7, "Gardien des âmes", "cards/egyptien/Garde Anubis.png"),
            Card("eg_003", "Malédiction", Faction.EGYPTIEN, Rarity.RARE, 7, 5, "Sort de malédiction ancienne", "cards/egyptien/Malédiction.png"),
            Card("eg_004", "Sphinx", Faction.EGYPTIEN, Rarity.RARE, 6, 7, "Gardien des énigmes", "cards/egyptien/phinx.png"),
            Card("eg_005", "Sobek", Faction.EGYPTIEN, Rarity.COMMUNE, 5, 6, "Dieu crocodile du Nil", "cards/egyptien/sobek.png")
        ))
        
        // Cartes Nordiques (basées sur les images disponibles)
        allCards.addAll(listOf(
            Card("no_001", "Odin", Faction.NORDIQUE, Rarity.LEGENDAIRE, 9, 10, "Le Père de Toutes Choses", "cards/viking/Odin.png"),
            Card("no_002", "Fenrir", Faction.NORDIQUE, Rarity.RARE, 7, 5, "Loup géant prophétique", "cards/viking/Fenrir.png"),
            Card("no_003", "Géant de Givre", Faction.NORDIQUE, Rarity.RARE, 8, 8, "Colosse des terres glacées", "cards/viking/Géant de Givre.png"),
            Card("no_004", "Raider Viking", Faction.NORDIQUE, Rarity.COMMUNE, 5, 4, "Guerrier des mers du Nord", "cards/viking/Raider Viking.png"),
            Card("no_005", "Valkyrie", Faction.NORDIQUE, Rarity.RARE, 6, 6, "Sort d'invocation des guerrières", "cards/viking/Valkyrie.png")
        ))
    }
    
    /**
     * Génère un booster aléatoire pour une faction donnée
     */
    fun generateBooster(faction: Faction, packType: models.PackType = models.PackType.STANDARD): List<Card> {
        val factionCards = allCards.filter { it.faction == faction }
        val boosterCards = mutableListOf<Card>()
        
        when (packType) {
            models.PackType.STANDARD -> {
                // Distribution: 3 communes, 1 rare, 1 chance de légendaire (20%)
                val legendaryChance = Random.nextFloat()
                
                if (legendaryChance < 0.2f) {
                    // 1 légendaire
                    factionCards.filter { it.rarity == Rarity.LEGENDAIRE }.randomOrNull()?.let {
                        boosterCards.add(it)
                    }
                } else {
                    // 1 rare supplémentaire
                    factionCards.filter { it.rarity == Rarity.RARE }.randomOrNull()?.let {
                        boosterCards.add(it)
                    }
                }
                
                // 1 rare
                factionCards.filter { it.rarity == Rarity.RARE }.randomOrNull()?.let {
                    boosterCards.add(it)
                }
                
                // 3 communes
                repeat(3) {
                    factionCards.filter { it.rarity == Rarity.COMMUNE }.randomOrNull()?.let {
                        boosterCards.add(it)
                    }
                }
            }
            
            models.PackType.LEGENDARY -> {
                // Pack légendaire: 1 légendaire garanti + 2 rares + 2 communes
                // 1 légendaire garanti
                factionCards.filter { it.rarity == Rarity.LEGENDAIRE }.randomOrNull()?.let {
                    boosterCards.add(it)
                }
            }
        }
        
        return boosterCards.shuffled()
    }
    
    /**
     * Ajoute une carte à la collection du joueur
     */
    fun addToCollection(card: Card) {
        userCollection.add(card)
    }
    
    /**
     * Récupère toutes les cartes de la collection (uniques avec compteur)
     */
    fun getCollection(): List<Pair<Card, Int>> {
        return userCollection
            .groupBy { it.id }
            .map { (_, cards) -> cards.first() to cards.size }
            .sortedBy { it.first.id }
    }
    
    /**
     * Récupère les cartes de la collection filtrées par faction (uniques avec compteur)
     */
    fun getCollectionByFaction(faction: Faction): List<Pair<Card, Int>> {
        return userCollection
            .filter { it.faction == faction }
            .groupBy { it.id }
            .map { (_, cards) -> cards.first() to cards.size }
            .sortedBy { it.first.id }
    }
    
    /**
     * Compte le nombre total de cartes dans la collection
     */
    fun getTotalCardCount(): Int = userCollection.size
}
