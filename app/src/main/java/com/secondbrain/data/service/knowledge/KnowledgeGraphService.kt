package com.secondbrain.data.service.knowledge

import android.util.Log
import com.secondbrain.data.model.Card
import com.secondbrain.data.repository.CardRepository
import com.secondbrain.data.service.ai.AiServiceManager
import com.secondbrain.data.service.ai.SummarizationOptions
import com.secondbrain.data.service.ai.SummaryType
import com.secondbrain.data.service.ai.content.Entity
import com.secondbrain.data.service.ai.content.EntityExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for managing the knowledge graph
 */
@Singleton
class KnowledgeGraphService @Inject constructor(
    private val cardRepository: CardRepository,
    private val entityExtractor: EntityExtractor,
    private val aiServiceManager: AiServiceManager
) {
    companion object {
        private const val TAG = "KnowledgeGraphService"
    }

    /**
     * Build the knowledge graph for a card
     */
    suspend fun buildKnowledgeGraph(cardId: String): Result<KnowledgeGraph> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Building knowledge graph for card: $cardId")

            // Get the card
            val card = cardRepository.getCardById(cardId).first()
                ?: return@withContext Result.failure(Exception("Card not found"))

            // Extract entities from the card content
            val entitiesResult = entityExtractor.extractEntities(card.content)
            if (entitiesResult.isFailure) {
                return@withContext Result.failure(entitiesResult.exceptionOrNull() ?: Exception("Failed to extract entities"))
            }

            val entities = entitiesResult.getOrNull() ?: emptyList()

            // Find related cards
            val relatedCards = findRelatedCards(card, entities)

            // Create connections between entities and cards
            val connections = createConnections(card, entities, relatedCards)

            // Create knowledge graph
            val knowledgeGraph = KnowledgeGraph(
                centralCard = card,
                entities = entities,
                relatedCards = relatedCards,
                connections = connections
            )

            Result.success(knowledgeGraph)
        } catch (e: Exception) {
            Log.e(TAG, "Error building knowledge graph", e)
            Result.failure(e)
        }
    }

    /**
     * Find cards related to the given card and entities
     */
    private suspend fun findRelatedCards(card: Card, entities: List<Entity>): List<Card> {
        val relatedCards = mutableListOf<Card>()

        // Get all cards
        val allCards = cardRepository.getCards().first()

        // Filter out the current card
        val otherCards = allCards.filter { it.id != card.id }

        // Find cards with matching entities
        for (entity in entities) {
            val matchingCards = otherCards.filter { otherCard ->
                otherCard.content.contains(entity.name, ignoreCase = true) ||
                        otherCard.title.contains(entity.name, ignoreCase = true) ||
                        otherCard.tags.any { it.contains(entity.name, ignoreCase = true) }
            }

            relatedCards.addAll(matchingCards)
        }

        // Find cards with matching tags
        val matchingTagCards = otherCards.filter { otherCard ->
            otherCard.tags.any { tag -> card.tags.contains(tag) }
        }

        relatedCards.addAll(matchingTagCards)

        // Remove duplicates and limit to 10 cards
        return relatedCards.distinctBy { it.id }.take(10)
    }

    /**
     * Create connections between entities and cards
     */
    private fun createConnections(
        centralCard: Card,
        entities: List<Entity>,
        relatedCards: List<Card>
    ): List<Connection> {
        val connections = mutableListOf<Connection>()

        // Create connections between central card and entities
        for (entity in entities) {
            connections.add(
                Connection(
                    sourceId = centralCard.id,
                    sourceType = NodeType.CARD,
                    targetId = entity.name,
                    targetType = NodeType.ENTITY,
                    strength = calculateConnectionStrength(centralCard, entity),
                    type = ConnectionType.CONTAINS
                )
            )
        }

        // Create connections between entities and related cards
        for (entity in entities) {
            for (relatedCard in relatedCards) {
                if (relatedCard.content.contains(entity.name, ignoreCase = true) ||
                    relatedCard.title.contains(entity.name, ignoreCase = true) ||
                    relatedCard.tags.any { it.contains(entity.name, ignoreCase = true) }
                ) {
                    connections.add(
                        Connection(
                            sourceId = entity.name,
                            sourceType = NodeType.ENTITY,
                            targetId = relatedCard.id,
                            targetType = NodeType.CARD,
                            strength = calculateConnectionStrength(relatedCard, entity),
                            type = ConnectionType.APPEARS_IN
                        )
                    )
                }
            }
        }

        // Create connections between central card and related cards
        for (relatedCard in relatedCards) {
            val commonTags = centralCard.tags.filter { relatedCard.tags.contains(it) }
            if (commonTags.isNotEmpty()) {
                connections.add(
                    Connection(
                        sourceId = centralCard.id,
                        sourceType = NodeType.CARD,
                        targetId = relatedCard.id,
                        targetType = NodeType.CARD,
                        strength = commonTags.size.toFloat() / maxOf(centralCard.tags.size, relatedCard.tags.size),
                        type = ConnectionType.RELATED_BY_TAGS
                    )
                )
            }
        }

        return connections
    }

    /**
     * Calculate connection strength between a card and an entity
     */
    private fun calculateConnectionStrength(card: Card, entity: Entity): Float {
        // Count occurrences of entity in card content
        val contentOccurrences = card.content.split(entity.name, ignoreCase = true).size - 1

        // Check if entity appears in title
        val titleOccurrence = if (card.title.contains(entity.name, ignoreCase = true)) 3 else 0

        // Check if entity appears in tags
        val tagOccurrence = if (card.tags.any { it.contains(entity.name, ignoreCase = true) }) 2 else 0

        // Calculate total score
        val totalScore = contentOccurrences + titleOccurrence + tagOccurrence

        // Normalize to 0-1 range
        return minOf(totalScore / 10f, 1f)
    }

    /**
     * Find connections between two cards
     */
    suspend fun findConnections(cardId1: String, cardId2: String): Result<List<Connection>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Finding connections between cards: $cardId1 and $cardId2")

            // Get the cards
            val card1 = cardRepository.getCardById(cardId1).first()
                ?: return@withContext Result.failure(Exception("Card 1 not found"))

            val card2 = cardRepository.getCardById(cardId2).first()
                ?: return@withContext Result.failure(Exception("Card 2 not found"))

            // Extract entities from both cards
            val entities1Result = entityExtractor.extractEntities(card1.content)
            val entities2Result = entityExtractor.extractEntities(card2.content)

            if (entities1Result.isFailure || entities2Result.isFailure) {
                return@withContext Result.failure(Exception("Failed to extract entities"))
            }

            val entities1 = entities1Result.getOrNull() ?: emptyList()
            val entities2 = entities2Result.getOrNull() ?: emptyList()

            // Find common entities
            val commonEntities = entities1.filter { entity1 ->
                entities2.any { entity2 -> entity1.name.equals(entity2.name, ignoreCase = true) }
            }

            // Create connections
            val connections = mutableListOf<Connection>()

            // Common entities connections
            for (entity in commonEntities) {
                connections.add(
                    Connection(
                        sourceId = card1.id,
                        sourceType = NodeType.CARD,
                        targetId = entity.name,
                        targetType = NodeType.ENTITY,
                        strength = calculateConnectionStrength(card1, entity),
                        type = ConnectionType.CONTAINS
                    )
                )

                connections.add(
                    Connection(
                        sourceId = entity.name,
                        sourceType = NodeType.ENTITY,
                        targetId = card2.id,
                        targetType = NodeType.CARD,
                        strength = calculateConnectionStrength(card2, entity),
                        type = ConnectionType.APPEARS_IN
                    )
                )
            }

            // Common tags connections
            val commonTags = card1.tags.filter { card2.tags.contains(it) }
            if (commonTags.isNotEmpty()) {
                connections.add(
                    Connection(
                        sourceId = card1.id,
                        sourceType = NodeType.CARD,
                        targetId = card2.id,
                        targetType = NodeType.CARD,
                        strength = commonTags.size.toFloat() / maxOf(card1.tags.size, card2.tags.size),
                        type = ConnectionType.RELATED_BY_TAGS
                    )
                )
            }

            // If no connections found, use AI to find semantic connections
            if (connections.isEmpty()) {
                val semanticConnectionsResult = findSemanticConnections(card1, card2)
                if (semanticConnectionsResult.isSuccess) {
                    connections.addAll(semanticConnectionsResult.getOrNull() ?: emptyList())
                }
            }

            Result.success(connections)
        } catch (e: Exception) {
            Log.e(TAG, "Error finding connections between cards", e)
            Result.failure(e)
        }
    }

    /**
     * Find semantic connections between two cards using AI
     */
    private suspend fun findSemanticConnections(card1: Card, card2: Card): Result<List<Connection>> {
        try {
            val prompt = """
                Analyze the following two pieces of content and identify semantic connections between them.

                Content 1: ${card1.content.take(1000)}

                Content 2: ${card2.content.take(1000)}

                Identify the top 3 conceptual connections between these two pieces of content.
                For each connection, provide:
                1. The concept name
                2. A brief description of how this concept connects the two pieces of content
                3. A strength score from 0.0 to 1.0 indicating how strong the connection is

                Format your response as a JSON array of objects with the following structure:
                [
                  {
                    "concept": "Concept name",
                    "description": "Description of connection",
                    "strength": 0.8
                  },
                  ...
                ]
            """.trimIndent()

            val options = SummarizationOptions(
                summaryType = SummaryType.CONCISE,
                language = "en",
                maxLength = 1000
            )

            val result = aiServiceManager.summarize(prompt, options, null)

            if (result.isSuccess) {
                val jsonString = result.getOrNull() ?: "[]"
                return parseSemanticConnections(jsonString, card1.id, card2.id)
            } else {
                return Result.failure(result.exceptionOrNull() ?: Exception("Failed to find semantic connections"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error finding semantic connections", e)
            return Result.failure(e)
        }
    }

    /**
     * Parse semantic connections from JSON string
     */
    private fun parseSemanticConnections(
        jsonString: String,
        card1Id: String,
        card2Id: String
    ): Result<List<Connection>> {
        return try {
            val connections = mutableListOf<Connection>()

            // Try to find JSON array in the string
            val jsonArrayString = extractJsonArray(jsonString)
            if (jsonArrayString.isNotEmpty()) {
                val jsonArray = JSONArray(jsonArrayString)

                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)

                    val concept = jsonObject.getString("concept")
                    val description = jsonObject.getString("description")
                    val strength = jsonObject.getDouble("strength").toFloat()

                    // Create entity for the concept
                    val entity = Entity(
                        name = concept,
                        type = com.secondbrain.data.service.ai.content.EntityType.CONCEPT,
                        description = description
                    )

                    // Create connections
                    connections.add(
                        Connection(
                            sourceId = card1Id,
                            sourceType = NodeType.CARD,
                            targetId = concept,
                            targetType = NodeType.ENTITY,
                            strength = strength,
                            type = ConnectionType.SEMANTIC_RELATION,
                            description = description
                        )
                    )

                    connections.add(
                        Connection(
                            sourceId = concept,
                            sourceType = NodeType.ENTITY,
                            targetId = card2Id,
                            targetType = NodeType.CARD,
                            strength = strength,
                            type = ConnectionType.SEMANTIC_RELATION,
                            description = description
                        )
                    )
                }
            }

            Result.success(connections)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing semantic connections", e)
            Result.failure(e)
        }
    }

    /**
     * Extract JSON array from string
     */
    private fun extractJsonArray(text: String): String {
        val startIndex = text.indexOf('[')
        val endIndex = text.lastIndexOf(']')

        return if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            text.substring(startIndex, endIndex + 1)
        } else {
            "[]"
        }
    }
}

/**
 * Knowledge graph data class
 */
data class KnowledgeGraph(
    val centralCard: Card,
    val entities: List<Entity>,
    val relatedCards: List<Card>,
    val connections: List<Connection>
)

/**
 * Node type enum
 */
enum class NodeType {
    CARD,
    ENTITY
}

/**
 * Connection type enum
 */
enum class ConnectionType {
    CONTAINS,
    APPEARS_IN,
    RELATED_BY_TAGS,
    SEMANTIC_RELATION
}

/**
 * Connection data class
 */
data class Connection(
    val sourceId: String,
    val sourceType: NodeType,
    val targetId: String,
    val targetType: NodeType,
    val strength: Float,
    val type: ConnectionType,
    val description: String? = null
)
