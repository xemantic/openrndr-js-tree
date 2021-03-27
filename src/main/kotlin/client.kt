import kotlinx.browser.document
import kotlinx.browser.window
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.math.mod
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

val       startPoint = Vector2(0.0, -1.0) // bottom of the normalized screen
const val startAngle = 90.0 // we are starting by pointing up
const val lifespan = .6
const val growthSpeed = .05
fun       growthFormula() = mod(time * growthSpeed, lifespan)
const val branchCount = 9 // no more than 10 for now
const val branchingThreshold = .03
const val branchAngleVariation = 60.0
const val windTwistMaxAngle = 10.0
fun       windTwistFormula() = sin(time) * windTwistMaxAngle


fun drawBranch(start: Vector2, end: Vector2, width: Double) {
  with(drawer) {
    drawer.beginPath()
    moveTo(toScreenCoord(start))
    lineWidth = width
    strokeStyle = "white"
    lineTo(toScreenCoord(end))
    stroke()
    drawer.closePath()
  }
}

fun tree(
  start: Vector2,
  angle: Double,
  windTwist: Double,
  growth: Double,
  branchCount: Int,
  branchingThreshold: Double,
  branchAngleVariation: Double,
  generation: Int,
  random: Random
) {
  val end = start + Polar(angle, growth).cartesian
  drawBranch(start, end, growth * 5.0)
  if (growth > branchingThreshold) {
    (0 until branchCount).forEach {
      tree(
        start = end,
        angle = random.nextDouble(
          angle - branchAngleVariation, angle + branchAngleVariation
        ) + windTwist,
        windTwist,
        (growth - branchingThreshold) * .7,
        branchCount,
        branchingThreshold,
        branchAngleVariation,
        generation = generation + 1,
        random = Random(generation * 10 + branchCount)
      )
    }
  }
}

// already drawing on normalized cartesian coordinates
// -1..1 in minimal screen dimension
fun draw() {
  drawer.clearScreen()
  tree(
    startPoint,
    startAngle,
    windTwistFormula(),
    growthFormula(),
    branchCount,
    branchingThreshold,
    branchAngleVariation,
    generation = 0,
    random = Random(0)
  )
}


// candidates for common library code
val FLIP_Y = Vector2(1.0, -1.0)

val resolution: Vector2 get() = Vector2(width.toDouble(), height.toDouble())

val minDimension: Double get() = min(width.toDouble(), height.toDouble())

fun toScreenCoord(coord: Vector2) = (coord * FLIP_Y * minDimension + resolution) / 2.0

fun CanvasRenderingContext2D.moveTo(coord: Vector2) = this.moveTo(coord.x, coord.y)
fun CanvasRenderingContext2D.lineTo(coord: Vector2) = this.lineTo(coord.x, coord.y)
fun CanvasRenderingContext2D.clearScreen() {
    drawer.fillStyle = "black"
    drawer.fillRect(0.0, 0.0, width.toDouble(), height.toDouble())
}


// The code below is pretty standard, so I guess in the future it will be a common openrndr-js lib
// here it is just abstracting canvas initialization, as if it is also happening in p5.js


fun main() {
  window.onload = {
    painter = Painter { draw() }
    true
  }
}


lateinit var painter: Painter
val drawer: CanvasRenderingContext2D get() = painter.drawer

val time: Double get() = painter.time
val width: Int get() = painter.width
val height: Int get() = painter.height


class Painter(val backgroundFillStyle: String = "black", private val drawFunction: () -> Unit) {

  val canvas: HTMLCanvasElement = document.body?.appendChild(
    document.createElement("canvas")
  ) as HTMLCanvasElement

  val drawer = canvas.getContext("2d") as CanvasRenderingContext2D

  init {
    with(canvas.style) {
      position = "fixed"
      width = "100vw"
      height = "100vh"
      top = "0"
      left = "0"
      zIndex = "-10"
    }
    window.requestAnimationFrame { render() }
  }

  var time = 0.0
    private set
  var width: Int = 0
    private set
  var height: Int = 0
    private set

  private var cssWidth: Int = 0
  private var cssHeight: Int = 0

  private fun render() {
    window.requestAnimationFrame { render() }
    if (maybeResize()) {
      resize()
    }
    time = window.performance.now() / 1000
    drawFunction()
  }

  private fun maybeResize() =
    (cssWidth != canvas.clientWidth) || (cssHeight != canvas.clientHeight)

  private fun resize() {
    val pixelRatio = window.devicePixelRatio
    cssWidth = canvas.clientWidth
    cssHeight = canvas.clientHeight
    width = floor(cssWidth * pixelRatio).toInt()
    height = floor(cssHeight * pixelRatio).toInt()
    canvas.width = width
    canvas.height = height
    drawer.fillStyle = backgroundFillStyle
    drawer.fillRect(0.0, 0.0, width.toDouble(), height.toDouble())
  }

}
