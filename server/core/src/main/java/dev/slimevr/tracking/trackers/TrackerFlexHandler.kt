package dev.slimevr.tracking.trackers

import com.jme3.math.FastMath
import io.github.axisangles.ktmath.EulerAngles
import io.github.axisangles.ktmath.EulerOrder
import kotlin.math.*

/**
 * Class handling flex sensor data (angle and resistance)
 * Resistance is expected to go up with bend by default, but a mounting reset allows the contrary
 */
class TrackerFlexHandler(val tracker: Tracker) {
	private var minResistance = Float.MIN_VALUE
	private var maxResistance = Float.MAX_VALUE
	private var lastResistance = 0f

	/**
	 * Resets the min resistance from the last resistance value received
	 */
	fun resetMin() {
		minResistance = lastResistance

		setFlexResistance(lastResistance)
		tracker.dataTick()
	}

	/**
	 * Resets the max resistance from the last resistance value received
	 */
	fun resetMax() {
		maxResistance = lastResistance

		setFlexResistance(lastResistance)
		tracker.dataTick()
	}

	/**
	 * Sets the flex resistance which is then calculated into an angle
	 */
	// TODO support resistance going both ways
	fun setFlexResistance(resistance: Float) {
		// Update min and max if needed
		minResistance = if (minResistance == Float.MIN_VALUE) {
			resistance
		} else if (minResistance > maxResistance) {
			max(minResistance, resistance)
		} else {
			min(minResistance, resistance)
		}
		maxResistance = if (maxResistance == Float.MAX_VALUE) {
			resistance
		} else if (maxResistance < minResistance) {
			min(maxResistance, resistance)
		} else {
			max(maxResistance, resistance)
		}

		// Get max angle
		val maxBend = getMaxAngleForTrackerPosition(tracker.trackerPosition)

		// Get angle and set it
		val angle = if (minResistance == maxResistance) {
			// Avoid division by 0
			0f
		} else {
			maxBend * (resistance - minResistance) / (maxResistance - minResistance)
		}
		setFlexAngle(angle)

		lastResistance = resistance
	}

	/**
	 * Sets an angle (rad) about the X axis
	 */
	fun setFlexAngle(angle: Float) {
		// Sets the rotation of the tracker by the angle about a given axis depending on
		// the tracker's TrackerPosition
		when (tracker.trackerPosition) {
			// Left fingers and right shoulder
			TrackerPosition.LEFT_THUMB_PROXIMAL, TrackerPosition.LEFT_THUMB_INTERMEDIATE,
			TrackerPosition.LEFT_THUMB_DISTAL, TrackerPosition.LEFT_INDEX_PROXIMAL,
			TrackerPosition.LEFT_INDEX_INTERMEDIATE, TrackerPosition.LEFT_INDEX_DISTAL,
			TrackerPosition.LEFT_MIDDLE_PROXIMAL, TrackerPosition.LEFT_MIDDLE_INTERMEDIATE,
			TrackerPosition.LEFT_MIDDLE_DISTAL, TrackerPosition.LEFT_RING_PROXIMAL,
			TrackerPosition.LEFT_RING_INTERMEDIATE, TrackerPosition.LEFT_RING_DISTAL,
			TrackerPosition.LEFT_LITTLE_PROXIMAL, TrackerPosition.LEFT_LITTLE_INTERMEDIATE,
			TrackerPosition.LEFT_LITTLE_DISTAL, TrackerPosition.RIGHT_SHOULDER,
			-> tracker.setRotation(EulerAngles(EulerOrder.YZX, 0f, 0f, angle).toQuaternion())

			// Right fingers and left shoulder
			TrackerPosition.RIGHT_THUMB_PROXIMAL, TrackerPosition.RIGHT_THUMB_INTERMEDIATE,
			TrackerPosition.RIGHT_THUMB_DISTAL, TrackerPosition.RIGHT_INDEX_PROXIMAL,
			TrackerPosition.RIGHT_INDEX_INTERMEDIATE, TrackerPosition.RIGHT_INDEX_DISTAL,
			TrackerPosition.RIGHT_MIDDLE_PROXIMAL, TrackerPosition.RIGHT_MIDDLE_INTERMEDIATE,
			TrackerPosition.RIGHT_MIDDLE_DISTAL, TrackerPosition.RIGHT_RING_PROXIMAL,
			TrackerPosition.RIGHT_RING_INTERMEDIATE, TrackerPosition.RIGHT_RING_DISTAL,
			TrackerPosition.RIGHT_LITTLE_PROXIMAL, TrackerPosition.RIGHT_LITTLE_INTERMEDIATE,
			TrackerPosition.RIGHT_LITTLE_DISTAL, TrackerPosition.LEFT_SHOULDER,
			-> tracker.setRotation(EulerAngles(EulerOrder.YZX, 0f, 0f, -angle).toQuaternion())

			// Default to X axis (pitch)
			else -> tracker.setRotation(EulerAngles(EulerOrder.YZX, angle, 0f, 0f).toQuaternion())
		}
	}

	/**
	 * Gets the max angle for a TrackerPosition
	 */
	private fun getMaxAngleForTrackerPosition(trackerPosition: TrackerPosition?): Float {
		if (trackerPosition == null) return FastMath.PI // 180 degrees

		return when (trackerPosition) {
			// 270 degrees
			TrackerPosition.LEFT_THUMB_DISTAL, TrackerPosition.LEFT_INDEX_DISTAL,
			TrackerPosition.LEFT_MIDDLE_DISTAL, TrackerPosition.LEFT_RING_DISTAL,
			TrackerPosition.LEFT_LITTLE_DISTAL, TrackerPosition.RIGHT_THUMB_DISTAL,
			TrackerPosition.RIGHT_INDEX_DISTAL, TrackerPosition.RIGHT_MIDDLE_DISTAL,
			TrackerPosition.RIGHT_RING_DISTAL, TrackerPosition.RIGHT_LITTLE_DISTAL,
			-> FastMath.PI + FastMath.HALF_PI

			// 180 degrees
			TrackerPosition.LEFT_THUMB_INTERMEDIATE, TrackerPosition.LEFT_INDEX_INTERMEDIATE,
			TrackerPosition.LEFT_MIDDLE_INTERMEDIATE, TrackerPosition.LEFT_RING_INTERMEDIATE,
			TrackerPosition.LEFT_LITTLE_INTERMEDIATE, TrackerPosition.RIGHT_THUMB_INTERMEDIATE,
			TrackerPosition.RIGHT_INDEX_INTERMEDIATE, TrackerPosition.RIGHT_MIDDLE_INTERMEDIATE,
			TrackerPosition.RIGHT_RING_INTERMEDIATE, TrackerPosition.RIGHT_LITTLE_INTERMEDIATE,
			-> FastMath.PI

			// 90 degrees
			TrackerPosition.LEFT_THUMB_PROXIMAL, TrackerPosition.LEFT_INDEX_PROXIMAL,
			TrackerPosition.LEFT_MIDDLE_PROXIMAL, TrackerPosition.LEFT_RING_PROXIMAL,
			TrackerPosition.LEFT_LITTLE_PROXIMAL, TrackerPosition.RIGHT_THUMB_PROXIMAL,
			TrackerPosition.RIGHT_INDEX_PROXIMAL, TrackerPosition.RIGHT_MIDDLE_PROXIMAL,
			TrackerPosition.RIGHT_RING_PROXIMAL, TrackerPosition.RIGHT_LITTLE_PROXIMAL,
			-> FastMath.HALF_PI

			// 45 degrees
			TrackerPosition.LEFT_SHOULDER, TrackerPosition.RIGHT_SHOULDER -> FastMath.QUARTER_PI

			// 135 degrees
			else -> FastMath.HALF_PI + FastMath.QUARTER_PI
		}
	}
}
