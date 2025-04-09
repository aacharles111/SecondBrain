package com.secondbrain.data.service.ai.content

import android.util.Log
import com.secondbrain.data.service.ai.AiServiceManager
import com.secondbrain.data.service.ai.SummarizationOptions
import com.secondbrain.data.service.ai.SummaryType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for extracting entities from content
 */
@Singleton
class EntityExtractor @Inject constructor(
    private val aiServiceManager: AiServiceManager
) {
    companion object {
        private const val TAG = "EntityExtractor"
    }

    /**
     * Extract entities from content
     */
    suspend fun extractEntities(content: String): Result<List<Entity>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Extracting entities from content: ${content.take(100)}...")

            // For short content, use a simple approach
            if (content.length < 500) {
                return@withContext Result.success(extractEntitiesSimple(content))
            }

            // For longer content, use AI to extract entities
            val prompt = """
                Extract the key entities from the following content. For each entity, provide:
                1. The entity name
                2. The entity type (PERSON, ORGANIZATION, LOCATION, CONCEPT, TECHNOLOGY, EVENT, or OTHER)
                3. A brief description of the entity based on the content

                Format your response as a JSON array of objects with the following structure:
                [
                  {
                    "name": "Entity name",
                    "type": "ENTITY_TYPE",
                    "description": "Brief description"
                  },
                  ...
                ]

                Only include significant entities that are central to understanding the content. Limit to 10 most important entities.

                Content:
                $content
            """.trimIndent()

            val options = SummarizationOptions(
                summaryType = SummaryType.CONCISE,
                language = "en",
                maxLength = 2000
            )

            val result = aiServiceManager.summarize(prompt, options, null)

            if (result.isSuccess) {
                val jsonString = result.getOrNull() ?: "[]"
                return@withContext parseEntitiesJson(jsonString)
            } else {
                // Fallback to simple approach if AI fails
                Result.success(extractEntitiesSimple(content))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting entities", e)
            Result.failure(e)
        }
    }

    /**
     * Extract entities using a simple approach
     */
    private fun extractEntitiesSimple(content: String): List<Entity> {
        val entities = mutableListOf<Entity>()

        // Extract potential entities (capitalized words or phrases)
        val words = content.split(" ", "\n", ".", ",", ";", ":", "!", "?", "(", ")", "[", "]", "{", "}")
        val potentialEntities = words.filter { it.isNotEmpty() && it[0].isUpperCase() }

        // Count occurrences and keep only frequent entities
        val entityCounts = potentialEntities.groupingBy { it }.eachCount()
        val frequentEntities = entityCounts.filter { it.value > 1 }.keys.toList()

        // Create entity objects
        for (entityName in frequentEntities.take(10)) {
            val entityType = guessEntityType(entityName, content)
            entities.add(
                Entity(
                    name = entityName,
                    type = entityType,
                    description = "Mentioned in the content"
                )
            )
        }

        return entities
    }

    /**
     * Guess entity type based on name and context
     */
    private fun guessEntityType(name: String, context: String): EntityType {
        // Check for common person indicators
        val personIndicators = listOf(" he ", " she ", " his ", " her ", " Mr. ", " Mrs. ", " Ms. ", " Dr. ")
        for (indicator in personIndicators) {
            if (context.contains("$indicator$name") || context.contains("$name$indicator")) {
                return EntityType.PERSON
            }
        }

        // Check for common organization indicators
        val orgIndicators = listOf(" company ", " organization ", " corporation ", " Inc. ", " LLC ", " Ltd. ", " Corp. ")
        for (indicator in orgIndicators) {
            if (context.contains("$indicator$name") || context.contains("$name$indicator")) {
                return EntityType.ORGANIZATION
            }
        }

        // Check for common location indicators
        val locationIndicators = listOf(" in ", " at ", " from ", " to ", " city ", " country ", " state ", " region ")
        for (indicator in locationIndicators) {
            if (context.contains("$indicator$name") || context.contains("$name$indicator")) {
                return EntityType.LOCATION
            }
        }

        // Check for common technology indicators
        val techIndicators = listOf(" technology ", " software ", " hardware ", " platform ", " system ", " app ", " application ")
        for (indicator in techIndicators) {
            if (context.contains("$indicator$name") || context.contains("$name$indicator")) {
                return EntityType.TECHNOLOGY
            }
        }

        // Default to concept
        return EntityType.CONCEPT
    }

    /**
     * Parse entities from JSON string
     */
    private fun parseEntitiesJson(jsonString: String): Result<List<Entity>> {
        return try {
            val entities = mutableListOf<Entity>()

            // Try to find JSON array in the string
            val jsonArrayString = extractJsonArray(jsonString)
            if (jsonArrayString.isNotEmpty()) {
                val jsonArray = JSONArray(jsonArrayString)

                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)

                    val name = jsonObject.getString("name")
                    val typeString = jsonObject.getString("type").uppercase()
                    val description = jsonObject.getString("description")

                    val type = try {
                        EntityType.valueOf(typeString)
                    } catch (e: Exception) {
                        EntityType.OTHER
                    }

                    entities.add(
                        Entity(
                            name = name,
                            type = type,
                            description = description
                        )
                    )
                }
            }

            Result.success(entities)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing entities JSON", e)
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
 * Entity type enum
 */
enum class EntityType {
    PERSON,
    ORGANIZATION,
    LOCATION,
    CONCEPT,
    TECHNOLOGY,
    EVENT,
    OTHER
}

/**
 * Entity data class
 */
data class Entity(
    val name: String,
    val type: EntityType,
    val description: String
)
