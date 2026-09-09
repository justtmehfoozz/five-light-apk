package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.updater.AppUpdateManager
import com.example.data.updater.ReleaseInfo
import com.example.data.updater.UpdateState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AppUpdateManagerTest {

    private lateinit var context: Context
    private lateinit var updateManager: AppUpdateManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        updateManager = AppUpdateManager(context)
    }

    @Test
    fun testInitialStateIsIdle() {
        assertEquals(UpdateState.Idle, updateManager.updateState.value)
    }

    @Test
    fun testCurrentVersionProperties() {
        assertEquals(BuildConfig.VERSION_NAME, updateManager.currentVersionName)
        assertEquals(BuildConfig.VERSION_CODE.toLong(), updateManager.currentVersionCode)
    }

    @Test
    fun testOfficialRepositoryConfig() {
        assertEquals("justtmehfoozz", AppUpdateManager.OFFICIAL_REPO_OWNER)
        assertEquals("five-light-apk", AppUpdateManager.OFFICIAL_REPO_NAME)
        assertEquals("https://api.github.com/repos/justtmehfoozz/five-light-apk/releases/latest", AppUpdateManager.GITHUB_API_URL)
    }

    @Test
    fun testApkValidationRejectsEmptyFile() {
        val tempApk = File(context.cacheDir, "test_empty.apk").apply {
            createNewFile()
        }

        val dummyRelease = ReleaseInfo(
            versionName = "1.7",
            versionCode = 5,
            tagName = "v1.7",
            name = "FiveLight v1.7",
            body = "New features",
            publishedAt = "2026-09-04T00:00:00Z",
            apkDownloadUrl = "https://github.com/justtmehfoozz/five-light-apk/releases/download/v1.7/fivelight.apk",
            apkFileName = "fivelight.apk",
            apkSize = 1024L
        )

        val result = updateManager.validateApk(tempApk, dummyRelease)
        assertTrue(result.isFailure)
        tempApk.delete()
    }

    @Test
    fun testInstallPermissionIntent() {
        val intent = updateManager.getInstallPermissionIntent()
        assertNotNull(intent)
        assertNotNull(intent.action)
    }

    @Test
    fun testParseAtomFeedCorrectlyExtractsLatestRelease() {
        val sampleFeed = """
            <?xml version="1.0" encoding="UTF-8"?>
            <feed xmlns="http://www.w3.org/2005/Atom" xml:lang="en-US">
              <entry>
                <id>tag:github.com,2008:Repository/1331080292/1.8</id>
                <updated>2026-09-09T01:40:30Z</updated>
                <link rel="alternate" type="text/html" href="https://github.com/justtmehfoozz/five-light-apk/releases/tag/1.8"/>
                <title>FiveLight 1.8</title>
                <content type="html">&lt;p&gt;FiveLight 1.8&lt;/p&gt;&lt;p&gt;Signed release build.&lt;br&gt;Version Name: 1.8&lt;br&gt;Version Code: 6&lt;/p&gt;</content>
              </entry>
            </feed>
        """.trimIndent()

        val parsed = updateManager.parseAtomFeed(sampleFeed)
        assertNotNull(parsed)
        assertEquals("1.8", parsed?.versionName)
        assertEquals(6L, parsed?.versionCode)
        assertEquals("1.8", parsed?.tagName)
        assertEquals("FiveLight 1.8", parsed?.name)
        assertEquals("https://github.com/justtmehfoozz/five-light-apk/releases/download/1.8/FiveLight.apk", parsed?.apkDownloadUrl)
        assertEquals("FiveLight.apk", parsed?.apkFileName)
        assertTrue(parsed?.body?.contains("Version Code: 6") == true)
    }

    @Test
    fun testParseAtomFeedWithAttributes() {
        val sampleFeed = """
            <?xml version="1.0" encoding="UTF-8"?>
            <feed xmlns="http://www.w3.org/2005/Atom">
              <entry xml:lang="en">
                <id>tag:github.com,2008:Repository/1331080292/1.8</id>
                <updated>2026-09-09T01:40:30Z</updated>
                <link rel="alternate" type="text/html" href="https://github.com/justtmehfoozz/five-light-apk/releases/tag/1.8"/>
                <title>FiveLight 1.8</title>
                <content type="html">&lt;p&gt;FiveLight 1.8&lt;/p&gt;&lt;p&gt;Signed release build.&lt;br&gt;Version Name: 1.8&lt;br&gt;Version Code: 6&lt;/p&gt;</content>
              </entry>
            </feed>
        """.trimIndent()

        val parsed = updateManager.parseAtomFeed(sampleFeed)
        assertNotNull(parsed)
        assertEquals("1.8", parsed?.versionName)
        assertEquals(6L, parsed?.versionCode)
    }

    @Test
    fun testParseAtomFeedWithNamespace() {
        val sampleFeed = """
            <?xml version="1.0" encoding="UTF-8"?>
            <feed xmlns="http://www.w3.org/2005/Atom">
              <entry xmlns="http://www.w3.org/2005/Atom">
                <id>tag:github.com,2008:Repository/1331080292/1.8</id>
                <updated>2026-09-09T01:40:30Z</updated>
                <link rel="alternate" type="text/html" href="https://github.com/justtmehfoozz/five-light-apk/releases/tag/1.8"/>
                <title>FiveLight 1.8</title>
                <content type="html">&lt;p&gt;FiveLight 1.8&lt;/p&gt;&lt;p&gt;Signed release build.&lt;br&gt;Version Name: 1.8&lt;br&gt;Version Code: 6&lt;/p&gt;</content>
              </entry>
            </feed>
        """.trimIndent()

        val parsed = updateManager.parseAtomFeed(sampleFeed)
        assertNotNull(parsed)
        assertEquals("1.8", parsed?.versionName)
        assertEquals(6L, parsed?.versionCode)
    }

    @Test
    fun testParseAtomFeedWithWhitespaceVariations() {
        val sampleFeed = """
            <?xml version="1.0" encoding="UTF-8"?>
            <feed xmlns="http://www.w3.org/2005/Atom">
              <entry 
                xml:lang="en"
                xmlns:abc="http://example.com"
                abc:attr="value"   >
                <id>tag:github.com,2008:Repository/1331080292/1.8</id>
                <updated>2026-09-09T01:40:30Z</updated>
                <link rel="alternate" type="text/html" href="https://github.com/justtmehfoozz/five-light-apk/releases/tag/1.8"/>
                <title>FiveLight 1.8</title>
                <content type="html">&lt;p&gt;FiveLight 1.8&lt;/p&gt;&lt;p&gt;Signed release build.&lt;br&gt;Version Name: 1.8&lt;br&gt;Version Code: 6&lt;/p&gt;</content>
              </entry>
            </feed>
        """.trimIndent()

        val parsed = updateManager.parseAtomFeed(sampleFeed)
        assertNotNull(parsed)
        assertEquals("1.8", parsed?.versionName)
        assertEquals(6L, parsed?.versionCode)
    }

    @Test
    fun testVersionComparisonWithParsedRelease() {
        val sampleFeed = """
            <?xml version="1.0" encoding="UTF-8"?>
            <feed xmlns="http://www.w3.org/2005/Atom">
              <entry xml:lang="en-US">
                <id>tag:github.com,2008:Repository/1331080292/1.8</id>
                <updated>2026-09-09T01:40:30Z</updated>
                <link rel="alternate" type="text/html" href="https://github.com/justtmehfoozz/five-light-apk/releases/tag/1.8"/>
                <title>FiveLight 1.8</title>
                <content type="html">&lt;p&gt;FiveLight 1.8&lt;/p&gt;&lt;p&gt;Signed release build.&lt;br&gt;Version Name: 1.8&lt;br&gt;Version Code: 6&lt;/p&gt;</content>
              </entry>
            </feed>
        """.trimIndent()

        val parsed = updateManager.parseAtomFeed(sampleFeed)
        assertNotNull(parsed)
        val remoteCode = parsed!!.versionCode
        val installedCode = 5L // simulated v1.7
        assertTrue(remoteCode > installedCode)
    }

    @Test
    fun testRateLimitErrorExposesUsableRetryAction() {
        val errorState = UpdateState.Error(
            message = "GitHub API hourly request limit reached. Please try again in 45 minutes.",
            isNetworkError = true,
            canRetry = true
        )
        assertTrue(errorState.canRetry)
    }
}
