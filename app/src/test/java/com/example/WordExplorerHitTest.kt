package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.util.QuranData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WordExplorerHitTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        QuranData.ensureDataLoaded(context)
    }

    @Test
    fun testWordRetrievalInVerseWithPauseMarks() {
        // Verse 2:2 has tokens: ['ذَٰلِكَ', 'ٱلْكِتَٰبُ', 'لَا', 'رَيْبَ', 'ۛ', 'فِيهِ', 'ۛ', 'هُدًۭى', 'لِّلْمُتَّقِينَ']
        // Token 0: ذَٰلِكَ
        val w0 = QuranData.getWordInfo(context, 2, 2, 0)
        assertNotNull(w0)
        assertEquals("ذَٰلِكَ", w0?.exactArabic)

        // Token 1: ٱلْكِتَٰبُ
        val w1 = QuranData.getWordInfo(context, 2, 2, 1)
        assertNotNull(w1)
        assertEquals("ٱلْكِتَٰبُ", w1?.exactArabic)

        // Token 2: لَا
        val w2 = QuranData.getWordInfo(context, 2, 2, 2)
        assertNotNull(w2)
        assertEquals("لَا", w2?.exactArabic)

        // Token 3: رَيْبَ
        val w3 = QuranData.getWordInfo(context, 2, 2, 3)
        assertNotNull(w3)
        assertEquals("رَيْبَ", w3?.exactArabic)

        // Token 4: ۛ (pause mark -> should be null, not 'فِيهِ' or 'هُدًۭى')
        val w4 = QuranData.getWordInfo(context, 2, 2, 4)
        assertNull(w4)

        // Token 5: فِيهِ (must be exactly 'فِيهِ', NOT shifted to 'هُدًۭى')
        val w5 = QuranData.getWordInfo(context, 2, 2, 5)
        assertNotNull(w5)
        assertEquals("فِيهِ", w5?.exactArabic)

        // Token 6: ۛ (pause mark -> null)
        val w6 = QuranData.getWordInfo(context, 2, 2, 6)
        assertNull(w6)

        // Token 7: هُدًۭى (must be exactly 'هُدًۭى', NOT null or shifted)
        val w7 = QuranData.getWordInfo(context, 2, 2, 7)
        assertNotNull(w7)
        assertEquals("هُدًۭى", w7?.exactArabic)

        // Token 8: لِّلْمُتَّقِينَ (must be exactly 'لِّلْمُتَّقِينَ')
        val w8 = QuranData.getWordInfo(context, 2, 2, 8)
        assertNotNull(w8)
        assertEquals("لِّلْمُتَّقِينَ", w8?.exactArabic)
    }

    @Test
    fun testAyatAlKursiWordMapping() {
        // Verse 2:255 (Ayat al-Kursi) has 58 whitespace tokens including pause marks
        // Token 0: ٱللَّهُ
        val w0 = QuranData.getWordInfo(context, 2, 255, 0)
        assertNotNull(w0)
        assertEquals("ٱللَّهُ", w0?.exactArabic)

        // Token 6: ٱلْقَيُّومُ
        val w6 = QuranData.getWordInfo(context, 2, 255, 6)
        assertNotNull(w6)
        assertEquals("ٱلْقَيُّومُ", w6?.exactArabic)

        // Token 7: ۚ (pause mark)
        val w7 = QuranData.getWordInfo(context, 2, 255, 7)
        assertNull(w7)

        // Token 8: لَا (after pause mark -> must be 'لَا', not shifted!)
        val w8 = QuranData.getWordInfo(context, 2, 255, 8)
        assertNotNull(w8)
        assertEquals("لَا", w8?.exactArabic)

        // Token 57 (last word): ٱلْعَظِيمُ
        val w57 = QuranData.getWordInfo(context, 2, 255, 57)
        assertNotNull(w57)
        assertEquals("ٱلْعَظِيمُ", w57?.exactArabic)
    }

    @Test
    fun testAlFatihahMapping() {
        val w0 = QuranData.getWordInfo(context, 1, 1, 0)
        assertNotNull(w0)
        assertEquals("بِسْمِ", w0?.exactArabic)

        val w3 = QuranData.getWordInfo(context, 1, 1, 3)
        assertNotNull(w3)
        assertEquals("ٱلرَّحِيمِ", w3?.exactArabic)
    }

    @Test
    fun testWhitespaceAndNonWordTokenRejection() {
        // Verse 2:2 tokens:
        // 0: ذَٰلِكَ (word)
        // 1: ٱلْكِتَٰبُ (word)
        // 2: لَا (word)
        // 3: رَيْبَ (word)
        // 4: ۛ (pause mark -> null)
        // 5: فِيهِ (word)
        // 6: ۛ (pause mark -> null)
        // 7: هُدًۭى (word)
        // 8: لِّلْمُتَّقِينَ (word)
        // 9: out of bounds -> null
        assertNull(QuranData.getWordInfo(context, 2, 2, 4))
        assertNull(QuranData.getWordInfo(context, 2, 2, 6))
        assertNull(QuranData.getWordInfo(context, 2, 2, 9))
        assertNull(QuranData.getWordInfo(context, 2, 2, -1))
        assertNull(QuranData.getWordInfo(context, 2, 2, 100))
    }
}
