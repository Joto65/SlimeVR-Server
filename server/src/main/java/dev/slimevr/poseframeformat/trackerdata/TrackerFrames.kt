package dev.slimevr.poseframeformat.trackerdata

import dev.slimevr.VRServer
import dev.slimevr.poseframeformat.trackerdata.TrackerFrame.Companion.fromTracker
import dev.slimevr.tracking.trackers.Tracker
import dev.slimevr.tracking.trackers.TrackerStatus
import io.eiren.util.collections.FastList

data class TrackerFrames(val name: String = "", val frames: FastList<TrackerFrame?>) {

	constructor(name: String = "", initialCapacity: Int = 5) : this(name, FastList<TrackerFrame?>(initialCapacity))
	constructor(baseTracker: Tracker, frames: FastList<TrackerFrame?>) : this(baseTracker.name, frames)
	constructor(baseTracker: Tracker, initialCapacity: Int = 5) : this(baseTracker, FastList<TrackerFrame?>(initialCapacity))

	fun addFrameFromTracker(index: Int, tracker: Tracker): TrackerFrame? {
		val trackerFrame = fromTracker(tracker)
		frames.add(index, trackerFrame)
		return trackerFrame
	}

	fun addFrameFromTracker(tracker: Tracker): TrackerFrame? {
		val trackerFrame = fromTracker(tracker)
		frames.add(trackerFrame)
		return trackerFrame
	}

	fun tryGetFrame(index: Int): TrackerFrame? {
		return if (index < 0 || index >= frames.size) null else frames[index]
	}

	fun tryGetFirstNotNullFrame(): TrackerFrame? {
		return frames.firstOrNull { frame -> frame != null }
	}

	fun toTracker(): Tracker {
		val firstFrame = tryGetFirstNotNullFrame() ?: TrackerFrame.empty
		val tracker = Tracker(
			device = null,
			id = VRServer.getNextLocalTrackerId(),
			name = name,
			trackerPosition = firstFrame.tryGetTrackerPosition(),
			hasPosition = firstFrame.hasPosition(),
			hasRotation = firstFrame.hasRotation(),
			hasAcceleration = firstFrame.hasAcceleration(),
			isInternal = true,
			isComputed = true
		)

		tracker.status = TrackerStatus.OK

		return tracker
	}
}