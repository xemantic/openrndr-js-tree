import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.math.mod_
import kotlin.math.sin
import kotlin.random.Random
import kotlin.random.nextInt

val startPoint = Vector2(0.0, -1.0) // bottom of the normalized screen
const val startAngle = 90.0 // we are starting by pointing up
const val lifespan = 40.0    // in seconds
const val growthSpeed = .01 // for the stem, 1 = half of the screen (because cartesian) per second
      //val growth: Double get() = (time * growthSpeed).mod_(lifespan * growthSpeed)
      val growth: Double get() = (sin(time * .1) + 1.0) / 4.0
const val branchingThreshold = .03
const val branchAngleVariation = 60.0
      val branchCountRange = 1..8 // no more than 10 for now
const val windTwistMaxAngle = 7.0
const val windTwistSpeed = .5
      val windTwist: Double get() = sin(time * windTwistSpeed) * windTwistMaxAngle


/*
  Explanation:

   - val defines a value which cannot be changed (like const in JS or final in Java)
   - additionally primitive types can be declared as const
   - there is no "new" anymore, you can type Vector2(0.0) like in GLSL
   - most of the time you don't have to specify the type - it's inferred and still strongly typed
   - floating point values must be specified with point between (inferred to Double by default)
   - growthFormula and windTwistFormula are regular functions, similar to JS arrow functions

   Note: all the values above could be also provided directly in the draw() function below
   using so called named parameters, like "generation" and "random" now, this would make
   the code even more concise while retaining readability. But I want to provide a UI
   later for changing these values.   -

 */

// the draw function, executed for each frame, is already drawing using normalized cartesian
// coordinates -1..1 in minimal screen dimension
fun draw() {
  canvas.clearScreen("black")
  val random = Random(0)
  tree(
    startPoint,
    startAngle + windTwist,
    windTwist,
    growth,
    branchingThreshold,
    branchAngleVariation,
    generation = 0, // this is called "named parameter", you can even reorder named arguments
    random = Random(0)
  )
}

// tree is a recursive function, here we need to specify types of our parameters, unlike
// in Java, in Kotlin the type follows variable name.
fun tree(
  start: Vector2,
  angle: Double,
  windTwist: Double,
  growth: Double,
  branchingThreshold: Double,
  branchAngleVariation: Double,
  generation: Int,
  random: Random
) {
  // look, we don't need to specify any type here - .cartesian returns Polar as a Vector2
  // and Vector2 + Vector2 is still Vector2, this is the famous "type inference"
  val end = start + Polar(angle, growth).cartesian
  drawBranch(start, end, width = growth * 20.0)
  if (growth > branchingThreshold) {
    val branchCount = random.nextInt(branchCountRange)
    (0 until branchCount).forEach { branchIndex ->
      tree(
        start = end,
        angle = random.nextDouble(
          angle - branchAngleVariation, angle + branchAngleVariation
        ) + windTwist,
        windTwist,
        (growth - branchingThreshold) * random.nextDouble(.6, .9),
        branchingThreshold,
        branchAngleVariation,
        generation = generation + 1,
        random = Random(generation * 10 + branchIndex)
      )
    }
  }
}

fun drawBranch(start: Vector2, end: Vector2, width: Double) {
  with(canvas) {
    beginPath()
    moveTo(toScreenCoord(start))
    lineWidth = width
    strokeStyle = "white"
    lineTo(toScreenCoord(end))
    stroke()
    closePath()
  }
}
