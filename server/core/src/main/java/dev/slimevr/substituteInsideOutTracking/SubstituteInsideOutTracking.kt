package dev.slimevr.substituteInsideOutTracking

import dev.slimevr.tracking.processor.skeleton.HumanSkeleton
import dev.slimevr.tracking.trackers.TrackerRole
import dev.slimevr.util.ann.VRServerThread
import dev.slimevr.tracking.trackers.Tracker
import io.github.axisangles.ktmath.Vector3

class SubstituteInsideOutTracking(
	private val skeleton: HumanSkeleton,
) {
	private var isEnabled: Boolean = false

	@VRServerThread
	fun setSubstituteInsideOutTrackingEnabled(value: Boolean) {
		if(value) {
			isEnabled = true
		}
	}

	fun update(){
	if(!isEnabled) {
		return
	}
	var leftHandTracker: Tracker? = null
	var rightHandTracker: Tracker? = null
	for(tracker in skeleton.humanPoseManager.server!!.allTrackers) {
		if(tracker.trackerPosition?.trackerRole == TrackerRole.LEFT_CONTROLLER && tracker.hasPosition && tracker.position != Vector3(0f,0f,0f)) { /** to-do: determine if controller is in headset view, instead of 0,0,0 **/
			leftHandTracker = tracker
		}
		else if(tracker.trackerPosition?.trackerRole == TrackerRole.RIGHT_CONTROLLER && tracker.hasPosition && tracker.position != Vector3(0f,0f,0f)) { /** to-do: determine if controller is in headset view, instead of 0,0,0 **/
			rightHandTracker = tracker
		}
		/** to-do: add skeletal hand tracking input checks **/
		else if(leftHandTracker != null && tracker.trackerPosition?.trackerRole == TrackerRole.LEFT_HAND && tracker.hasPosition && tracker.position != Vector3(0f,0f,0f)) {
			leftHandTracker = tracker
		}
		else if(rightHandTracker != null && tracker.trackerPosition?.trackerRole == TrackerRole.RIGHT_HAND && tracker.hasPosition && tracker.position != Vector3(0f,0f,0f)) {
			rightHandTracker = tracker
		}
	}
		/** hand trackers override controller tracking, needs testing if this is sufficient to override hand trackers with controller tracking **/
		skeleton.humanPoseManager.skeleton.computedLeftHandTracker = leftHandTracker
		skeleton.humanPoseManager.skeleton.computedRightHandTracker = rightHandTracker
	}

}
