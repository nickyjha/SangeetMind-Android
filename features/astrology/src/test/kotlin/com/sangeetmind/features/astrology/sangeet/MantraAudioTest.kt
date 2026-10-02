package com.sangeetmind.features.astrology.sangeet

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class MantraAudioTest {
    @Test fun suryaBeejHasTrack() = assertNotNull(MantraAudio.forMantra("surya_beej"))

    @Test fun unknownMantraHasNoTrack() = assertNull(MantraAudio.forMantra("generic"))

    @Test fun playsAddChantsAndStopAtMala() {
        val track = MantraTrack(rawRes = 0, chantsPerPlay = 27)
        assertEquals(27, MantraAudio.afterPlay(0, track))
        assertEquals(108, MantraAudio.afterPlay(81, track))
        assertEquals(108, MantraAudio.afterPlay(100, track))
    }
}
