package com.secondbrain.ui.knowledge

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.core.content.ContextCompat
import com.secondbrain.R
import com.secondbrain.data.model.Card
import com.secondbrain.data.service.ai.content.Entity
import com.secondbrain.data.service.ai.content.EntityType
import com.secondbrain.data.service.knowledge.Connection
import com.secondbrain.data.service.knowledge.ConnectionType
import com.secondbrain.data.service.knowledge.KnowledgeGraph
import com.secondbrain.data.service.knowledge.NodeType
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Custom view for visualizing knowledge graphs
 */
class KnowledgeGraphView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Graph data
    private var knowledgeGraph: KnowledgeGraph? = null

    // Node positions
    private val nodePositions = mutableMapOf<String, PointF>()

    // Node sizes
    private val nodeSizes = mutableMapOf<String, Float>()

    // Node colors
    private val nodeColors = mutableMapOf<String, Int>()

    // Selected node
    private var selectedNodeId: String? = null

    // Paints
    private val cardNodePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.colorPrimary)
        style = Paint.Style.FILL
    }

    private val entityNodePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.colorAccent)
        style = Paint.Style.FILL
    }

    private val selectedNodePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.colorSecondary)
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    private val connectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 24f
        textAlign = Paint.Align.CENTER
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 18f
        textAlign = Paint.Align.CENTER
    }

    // Path for drawing connections
    private val connectionPath = Path()

    // Gesture detectors
    private val gestureDetector = GestureDetector(context, GestureListener())
    private val scaleGestureDetector = ScaleGestureDetector(context, ScaleListener())

    // View transformation
    private var scaleFactor = 1f
    private var translateX = 0f
    private var translateY = 0f

    // Node click listener
    var onNodeClickListener: ((String, NodeType) -> Unit)? = null

    /**
     * Set knowledge graph data
     */
    fun setKnowledgeGraph(graph: KnowledgeGraph) {
        knowledgeGraph = graph
        calculateNodePositions()
        invalidate()
    }

    /**
     * Calculate node positions using a force-directed layout algorithm
     */
    private fun calculateNodePositions() {
        val graph = knowledgeGraph ?: return

        // Clear previous positions
        nodePositions.clear()
        nodeSizes.clear()
        nodeColors.clear()

        // Set initial positions
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = min(width, height) / 3f

        // Position central card at center
        nodePositions[graph.centralCard.id] = PointF(centerX, centerY)
        nodeSizes[graph.centralCard.id] = 80f
        nodeColors[graph.centralCard.id] = ContextCompat.getColor(context, R.color.colorPrimary)

        // Position entities in a circle around the central card
        val entityCount = graph.entities.size
        for (i in graph.entities.indices) {
            val entity = graph.entities[i]
            val angle = 2 * Math.PI * i / entityCount
            val x = centerX + radius * cos(angle).toFloat()
            val y = centerY + radius * sin(angle).toFloat()

            nodePositions[entity.name] = PointF(x, y)
            nodeSizes[entity.name] = 60f

            // Set color based on entity type
            val color = when (entity.type) {
                EntityType.PERSON -> ContextCompat.getColor(context, R.color.entityPerson)
                EntityType.ORGANIZATION -> ContextCompat.getColor(context, R.color.entityOrganization)
                EntityType.LOCATION -> ContextCompat.getColor(context, R.color.entityLocation)
                EntityType.CONCEPT -> ContextCompat.getColor(context, R.color.entityConcept)
                EntityType.TECHNOLOGY -> ContextCompat.getColor(context, R.color.entityTechnology)
                EntityType.EVENT -> ContextCompat.getColor(context, R.color.entityEvent)
                EntityType.OTHER -> ContextCompat.getColor(context, R.color.entityOther)
            }
            nodeColors[entity.name] = color
        }

        // Position related cards in a larger circle
        val cardCount = graph.relatedCards.size
        val outerRadius = radius * 1.8f
        for (i in graph.relatedCards.indices) {
            val card = graph.relatedCards[i]
            val angle = 2 * Math.PI * i / cardCount
            val x = centerX + outerRadius * cos(angle).toFloat()
            val y = centerY + outerRadius * sin(angle).toFloat()

            nodePositions[card.id] = PointF(x, y)
            nodeSizes[card.id] = 70f
            nodeColors[card.id] = ContextCompat.getColor(context, R.color.colorPrimaryVariant)
        }

        // Apply force-directed layout algorithm
        applyForceDirectedLayout()
    }

    /**
     * Apply force-directed layout algorithm to improve node positions
     */
    private fun applyForceDirectedLayout() {
        val graph = knowledgeGraph ?: return

        // Parameters
        val iterations = 100
        val k = sqrt(width * height / (1 + graph.entities.size + graph.relatedCards.size).toFloat())
        val temperature = width / 10f

        // Collect all nodes
        val allNodes = mutableListOf<String>()
        allNodes.add(graph.centralCard.id)
        allNodes.addAll(graph.entities.map { it.name })
        allNodes.addAll(graph.relatedCards.map { it.id })

        // Run iterations
        for (iteration in 0 until iterations) {
            // Calculate repulsive forces
            val displacement = mutableMapOf<String, PointF>()
            for (v in allNodes) {
                displacement[v] = PointF(0f, 0f)

                for (u in allNodes) {
                    if (v != u) {
                        val posV = nodePositions[v] ?: continue
                        val posU = nodePositions[u] ?: continue

                        val dx = posV.x - posU.x
                        val dy = posV.y - posU.y
                        val distance = max(0.1f, sqrt(dx * dx + dy * dy))

                        // Repulsive force
                        val force = k * k / distance
                        val displacementX = dx / distance * force
                        val displacementY = dy / distance * force

                        val disp = displacement[v] ?: PointF(0f, 0f)
                        disp.x += displacementX
                        disp.y += displacementY
                        displacement[v] = disp
                    }
                }
            }

            // Calculate attractive forces
            for (connection in graph.connections) {
                val source = connection.sourceId
                val target = connection.targetId

                val posSource = nodePositions[source] ?: continue
                val posTarget = nodePositions[target] ?: continue

                val dx = posSource.x - posTarget.x
                val dy = posSource.y - posTarget.y
                val distance = max(0.1f, sqrt(dx * dx + dy * dy))

                // Attractive force
                val force = distance * distance / k
                val attractiveX = dx / distance * force
                val attractiveY = dy / distance * force

                // Apply to source
                val dispSource = displacement[source] ?: PointF(0f, 0f)
                dispSource.x -= attractiveX
                dispSource.y -= attractiveY
                displacement[source] = dispSource

                // Apply to target
                val dispTarget = displacement[target] ?: PointF(0f, 0f)
                dispTarget.x += attractiveX
                dispTarget.y += attractiveY
                displacement[target] = dispTarget
            }

            // Apply displacements
            val currentTemperature = temperature * (1 - iteration / iterations.toFloat())
            for (v in allNodes) {
                val pos = nodePositions[v] ?: continue
                val disp = displacement[v] ?: continue

                // Limit displacement by temperature
                val dispLength = max(0.1f, sqrt(disp.x * disp.x + disp.y * disp.y))
                val limitedDisp = min(dispLength, currentTemperature)

                pos.x += disp.x / dispLength * limitedDisp
                pos.y += disp.y / dispLength * limitedDisp

                // Keep within bounds
                pos.x = max(100f, min(width - 100f, pos.x))
                pos.y = max(100f, min(height - 100f, pos.y))

                nodePositions[v] = pos
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        calculateNodePositions()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val graph = knowledgeGraph ?: return

        // Apply transformations
        canvas.save()
        canvas.translate(translateX, translateY)
        canvas.scale(scaleFactor, scaleFactor, width / 2f, height / 2f)

        // Draw connections
        for (connection in graph.connections) {
            val sourcePos = nodePositions[connection.sourceId] ?: continue
            val targetPos = nodePositions[connection.targetId] ?: continue
            val sourceSize = nodeSizes[connection.sourceId] ?: 60f
            val targetSize = nodeSizes[connection.targetId] ?: 60f

            // Set connection color and width based on type and strength
            when (connection.type) {
                ConnectionType.CONTAINS -> {
                    connectionPaint.color = Color.BLUE
                }
                ConnectionType.APPEARS_IN -> {
                    connectionPaint.color = Color.GREEN
                }
                ConnectionType.RELATED_BY_TAGS -> {
                    connectionPaint.color = Color.MAGENTA
                }
                ConnectionType.SEMANTIC_RELATION -> {
                    connectionPaint.color = Color.RED
                }
            }
            connectionPaint.strokeWidth = 2f + 3f * connection.strength
            connectionPaint.alpha = (128 + 127 * connection.strength).toInt()

            // Calculate connection points on the edge of nodes
            val dx = targetPos.x - sourcePos.x
            val dy = targetPos.y - sourcePos.y
            val distance = sqrt(dx * dx + dy * dy)
            val dirX = dx / distance
            val dirY = dy / distance

            val sourceX = sourcePos.x + dirX * (sourceSize / 2)
            val sourceY = sourcePos.y + dirY * (sourceSize / 2)
            val targetX = targetPos.x - dirX * (targetSize / 2)
            val targetY = targetPos.y - dirY * (targetSize / 2)

            // Draw connection line
            connectionPath.reset()
            connectionPath.moveTo(sourceX, sourceY)
            connectionPath.lineTo(targetX, targetY)
            canvas.drawPath(connectionPath, connectionPaint)

            // Draw arrow at target
            val arrowSize = 10f
            val angle = Math.atan2(targetY - sourceY.toDouble(), targetX - sourceX.toDouble())
            val arrowX1 = targetX - arrowSize * cos(angle - Math.PI / 6).toFloat()
            val arrowY1 = targetY - arrowSize * sin(angle - Math.PI / 6).toFloat()
            val arrowX2 = targetX - arrowSize * cos(angle + Math.PI / 6).toFloat()
            val arrowY2 = targetY - arrowSize * sin(angle + Math.PI / 6).toFloat()

            connectionPath.reset()
            connectionPath.moveTo(targetX, targetY)
            connectionPath.lineTo(arrowX1, arrowY1)
            connectionPath.lineTo(arrowX2, arrowY2)
            connectionPath.close()
            canvas.drawPath(connectionPath, connectionPaint)
        }

        // Draw nodes
        // First draw card nodes
        drawCardNode(canvas, graph.centralCard)
        for (card in graph.relatedCards) {
            drawCardNode(canvas, card)
        }

        // Then draw entity nodes
        for (entity in graph.entities) {
            drawEntityNode(canvas, entity)
        }

        // Draw selected node highlight
        selectedNodeId?.let { id ->
            val pos = nodePositions[id] ?: return@let
            val size = nodeSizes[id] ?: 60f
            canvas.drawCircle(pos.x, pos.y, size / 2 + 4, selectedNodePaint)
        }

        canvas.restore()
    }

    /**
     * Draw a card node
     */
    private fun drawCardNode(canvas: Canvas, card: Card) {
        val pos = nodePositions[card.id] ?: return
        val size = nodeSizes[card.id] ?: 60f
        val color = nodeColors[card.id] ?: ContextCompat.getColor(context, R.color.colorPrimary)

        // Draw node
        cardNodePaint.color = color
        canvas.drawCircle(pos.x, pos.y, size / 2, cardNodePaint)

        // Draw label
        val label = card.title.take(20) + if (card.title.length > 20) "..." else ""
        textPaint.textSize = size / 4
        canvas.drawText(label, pos.x, pos.y + 5, textPaint)
    }

    /**
     * Draw an entity node
     */
    private fun drawEntityNode(canvas: Canvas, entity: Entity) {
        val pos = nodePositions[entity.name] ?: return
        val size = nodeSizes[entity.name] ?: 60f
        val color = nodeColors[entity.name] ?: ContextCompat.getColor(context, R.color.entityConcept)

        // Draw node
        entityNodePaint.color = color
        canvas.drawCircle(pos.x, pos.y, size / 2, entityNodePaint)

        // Draw label
        val label = entity.name.take(15) + if (entity.name.length > 15) "..." else ""
        textPaint.textSize = size / 4
        canvas.drawText(label, pos.x, pos.y + 5, textPaint)

        // Draw entity type label
        labelPaint.textSize = size / 6
        canvas.drawText(entity.type.name, pos.x, pos.y + size / 3, labelPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)
        return true
    }

    /**
     * Find node at position
     */
    private fun findNodeAt(x: Float, y: Float): Pair<String, NodeType>? {
        val graph = knowledgeGraph ?: return null

        // Adjust for transformations
        val adjustedX = (x - translateX) / scaleFactor
        val adjustedY = (y - translateY) / scaleFactor

        // Check central card
        val centralCardPos = nodePositions[graph.centralCard.id] ?: return null
        val centralCardSize = nodeSizes[graph.centralCard.id] ?: 60f
        if (distance(adjustedX, adjustedY, centralCardPos.x, centralCardPos.y) <= centralCardSize / 2) {
            return Pair(graph.centralCard.id, NodeType.CARD)
        }

        // Check entities
        for (entity in graph.entities) {
            val pos = nodePositions[entity.name] ?: continue
            val size = nodeSizes[entity.name] ?: 60f
            if (distance(adjustedX, adjustedY, pos.x, pos.y) <= size / 2) {
                return Pair(entity.name, NodeType.ENTITY)
            }
        }

        // Check related cards
        for (card in graph.relatedCards) {
            val pos = nodePositions[card.id] ?: continue
            val size = nodeSizes[card.id] ?: 60f
            if (distance(adjustedX, adjustedY, pos.x, pos.y) <= size / 2) {
                return Pair(card.id, NodeType.CARD)
            }
        }

        return null
    }

    /**
     * Calculate distance between two points
     */
    private fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x1 - x2
        val dy = y1 - y2
        return sqrt(dx * dx + dy * dy)
    }

    /**
     * Gesture listener for handling touch events
     */
    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            val node = findNodeAt(e.x, e.y)
            if (node != null) {
                selectedNodeId = node.first
                onNodeClickListener?.invoke(node.first, node.second)
                invalidate()
                return true
            }
            return false
        }

        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            translateX -= distanceX
            translateY -= distanceY
            invalidate()
            return true
        }
    }

    /**
     * Scale gesture listener for handling pinch-to-zoom
     */
    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleFactor *= detector.scaleFactor
            scaleFactor = max(0.5f, min(scaleFactor, 3.0f))
            invalidate()
            return true
        }
    }
}
