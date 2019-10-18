package com.encorsa.wandr

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before
import java.io.IOException

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    //@Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.encorsa.wandr", appContext.packageName)
    }
    @Test
    fun formatIntegers(){
        val inserted: Int = 46
        val updated: Int = 45
        val deleted: Int = 44

        assertEquals("46 Inserted, 45 Updated, 44 Deleted", "%s Inserted, %s Updated, %s Deleted".format(inserted, updated, deleted))
    }
}

