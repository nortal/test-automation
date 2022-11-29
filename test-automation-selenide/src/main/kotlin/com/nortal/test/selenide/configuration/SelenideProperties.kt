/**
 * Copyright (c) 2022 Nortal AS
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.nortal.test.selenide.configuration

import com.codeborne.selenide.AssertionMode
import com.codeborne.selenide.FileDownloadMode
import com.codeborne.selenide.SelectorMode
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

/**
 * Selenide configuration.
 */
@ConstructorBinding
@ConfigurationProperties(prefix = "test-automation.selenide")
data class SelenideProperties(
    /**
     * Base url for open() function calls
     * Can be configured either programmatically, via selenide.properties file or by system property "-Dselenide.baseUrl=http://myhost".
     * <br></br>
     * Default value: http://localhost:8080
     */
    val baseUrl: String = "http://localhost:8080",
    /**
     * Scenario cleanup mode. Closing webdriver is slow, faster approach is clearing the data.
     */
    val scenarioCleanupMode: SelenideCleanupMode = SelenideCleanupMode.CLEAR_BROWSER_DATA,
    /**
     * Timeout in milliseconds to fail the test, if conditions still not met
     * Can be configured either programmatically, via selenide.properties file or by system property "-Dselenide.timeout=10000"
     * <br>
     * Default value: 4000 (milliseconds)
     */
    val timeout: Long = 4000,
    /**
     * Interval in milliseconds, when checking if a single element or collection elements are appeared
     * Can be configured either programmatically, via selenide.properties file or by system property "-Dselenide.pollingInterval=50"
     * <br></br>
     * Default value: 200 (milliseconds)
     */
    val pollingInterval: Long = 200,
    /**
     * If holdBrowserOpen is true, browser window stays open after running tests. It may be useful for debugging.
     * Can be configured either programmatically, via selenide.properties file or by system property "-Dselenide.holdBrowserOpen=true".
     * <br></br>
     * Default value: false.
     */
    val holdBrowserOpen: Boolean = false,


    /**
     * Should Selenide re-spawn browser if it's disappeared (hangs, broken, unexpectedly closed).
     * <br></br>
     * Can be configured either programmatically, via selenide.properties file
     * or by system property "-Dselenide.reopenBrowserOnFail=false".
     * <br></br>
     * Set this property to false if you want to disable automatic re-spawning the browser.
     * <br></br>
     * Default value: true
     */
    val reopenBrowserOnFail: Boolean = true,

    /**
     * Which browser to use.
     * Can be configured either programmatically, via selenide.properties file or by system property "-Dselenide.browser=ie".
     * Supported values: "chrome", "firefox", "ie", "opera", "edge"
     * <br></br>
     * Default value: "chrome"
     */
    val browser: BrowserEngine = BrowserEngine.CHROME,

    /**
     * Which browser version to use (for Internet Explorer).
     * Can be configured either programmatically, via selenide.properties file or by system property "-Dselenide.browserVersion=8".
     * <br></br>
     * Default value: none
     */
    val browserVersion: String? = null,

    /**
     * URL of remote web driver (in case of using Selenium Grid).
     * Can be configured either programmatically, via selenide.properties file
     * or by system property "-Dselenide.remote=http://localhost:5678/wd/hub".
     * <br></br>
     * Default value: null (Grid is not used).
     */
    val remote: String? = null,

    /**
     * The browser window size.
     * Can be configured either programmatically, via selenide.properties file or by system property "-Dselenide.browserSize=1024x768".
     * <br></br>
     * Default value: 1920x1080
     */
    val browserSize: String = "1920x1080",

    /**
     * The browser window position on screen.
     * Can be configured either programmatically, via selenide.properties file or by system property "-Dselenide.browserPosition=10x10".
     * <br></br>
     * Default value: none
     */
    val browserPosition: String? = null,

//    /**
//     * Browser capabilities.
//     * Warning: this capabilities will override capabilities were set by system properties.
//     * <br></br>
//     * Default value: new MutableCapabilities()
//     */
//    val browserCapabilities: MutableCapabilities? = Configuration.defaults.browserCapabilities()

    /**
     * Should webdriver wait until page is completely loaded.
     * Possible values: "none", "normal" and "eager".
     * <br></br>
     * Can be configured either programmatically, via selenide.properties file or by system property "-Dselenide.pageLoadStrategy=eager".
     * Default value: "normal".
     * <br></br>
     * - `normal`: return after the load event fires on the new page (it's default in Selenium webdriver);
     * - `eager`: return after DOMContentLoaded fires;
     * - `none`: return immediately
     * <br></br>
     * In some cases `eager` can bring performance boosts for the slow tests.
     * Though, we left default value `normal` because we are afraid to break users' existing tests.
     * <br></br>
     * See https://w3c.github.io/webdriver/webdriver-spec.html#dfn-page-loading-strategy
     *
     * @since 3.5
     */
    val pageLoadStrategy: String? = "normal",

    /**
     * Timeout for loading a web page (in milliseconds).
     * Default timeout in Selenium WebDriver is 300 seconds (which is incredibly long).
     * Selenide default is 30 seconds.
     *
     * @since 5.15.0
     */
    val pageLoadTimeout: Long = 30000,

    /**
     * ATTENTION! Automatic WebDriver waiting after click isn't working in case of using this feature.
     * Use clicking via JavaScript instead common element clicking.
     * This solution may be helpful for testing in Internet Explorer.
     * Can be configured either programmatically, via selenide.properties file or by system property "-Dselenide.clickViaJs=true".
     * <br></br>
     * Default value: false
     */
    val clickViaJs: Boolean = false,

    /**
     * Defines if Selenide takes screenshots on failing tests.
     * Can be configured either programmatically, via selenide.properties file or by system property "-Dselenide.screenshots=false".
     * <br></br>
     * Default value: true
     */
    val screenshots: Boolean = true,

    /**
     * Defines if Selenide saves page source on failing tests.
     * Can be configured either programmatically, via selenide.properties file or by system property "-Dselenide.savePageSource=false".
     * <br></br>
     * Default value: true
     */
    val savePageSource: Boolean = true,

    /**
     * Folder to store screenshots to.
     * Can be configured either programmatically, via selenide.properties file
     * or by system property "-Dselenide.reportsFolder=test-result/reports".
     * <br></br>
     * Default value: "build/reports/tests" (this is default for Gradle projects)
     */
    val reportsFolder: String = "build/reports/test-automation/selenide-failures",

    /**
     * Folder to store downloaded files to.
     * Can be configured either programmatically, via selenide.properties file
     * or by system property "-Dselenide.downloadsFolder=test-result/downloads".
     * <br></br>
     * Default value: "build/downloads" (this is default for Gradle projects)
     */
    val downloadsFolder: String = "build/reports/test-automation/selenide-downloads",

    /**
     * If set to true, sets value by javascript instead of using Selenium built-in "sendKey" function
     * (that is quite slow because it sends every character separately).
     * <br></br>
     * Tested on Codeborne projects - works well, speed up ~30%.
     * Some people reported 150% speedup (because sending characters one-by-one was especially
     * slow via network to Selenium Grid on cloud).
     * <br></br>
     * https://github.com/selenide/selenide/issues/135
     * Can be configured either programmatically, via selenide.properties file or by system property "-Dselenide.fastSetValue=true".
     * <br></br>
     * Default value: false
     */
    val fastSetValue: Boolean = false,

    /**
     *
     * Choose how Selenide should retrieve web elements: using default CSS or Sizzle (CSS3).
     * <br></br>
     *
     *
     * Can be configured either programmatically, via selenide.properties file or by system property "-Dselenide.selectorMode=Sizzle".
     *
     * <br></br>
     * Possible values: "CSS" or "Sizzle"
     * <br></br>
     * Default value: CSS
     *
     * @see SelectorMode
     */
    val selectorMode: SelectorMode = SelectorMode.CSS,

    /**
     *
     * Assertion mode
     *
     *
     * Can be configured either programmatically, via selenide.properties file
     * or by system property "-Dselenide.assertionMode=SOFT".
     *
     * <br></br>
     * Possible values: "STRICT" or "SOFT"
     * <br></br>
     * Default value: STRICT
     *
     * @see AssertionMode
     */
    val assertionMode: AssertionMode = AssertionMode.STRICT,

    /**
     * Defines if files are downloaded via direct HTTP or vie selenide embedded proxy server
     * Can be configured either programmatically, via selenide.properties file or by system property "-Dselenide.fileDownload=PROXY"
     * <br></br>
     * Default: HTTPGET
     */
    val fileDownload: FileDownloadMode? = FileDownloadMode.HTTPGET,

    /**
     * If Selenide should run browser through its own proxy server.
     * It allows some additional features which are not possible with plain Selenium.
     * But it's not enabled by default because sometimes it would not work (more exactly, if tests and browser and
     * executed on different machines, and "test machine" is not accessible from "browser machine"). If it's not your
     * case, I recommend to enable proxy.
     * Can be configured either programmatically, via selenide.properties file or by system property "-Dselenide.proxyEnabled=true"
     * <br></br>
     * Default: false
     */
    val proxyEnabled: Boolean = false,

    /**
     * Host of Selenide proxy server.
     * Used only if proxyEnabled == true.
     * Can be configured either programmatically, via selenide.properties file or by system property "-Dselenide.proxyHost=127.0.0.1"
     * <br></br>
     * Default: empty (meaning that Selenide will detect current machine's ip/hostname automatically)
     *
     * @see com.browserup.bup.client.ClientUtil.getConnectableAddress
     */
    val proxyHost: String? = null,

    /**
     * Port of Selenide proxy server.
     * Used only if proxyEnabled == true.
     * Can be configured either programmatically, via selenide.properties file or by system property "-Dselenide.proxyPort=8888"
     * <br></br>
     * Default: 0 (meaning that Selenide will choose a random free port on current machine)
     */
    val proxyPort: Int = 0,

    /**
     * Controls Selenide and WebDriverManager integration.
     * When integration is enabled you don't need to download and setup any browser driver executables.
     * See https://github.com/bonigarcia/webdrivermanager for WebDriverManager configuration details.
     * Can be configured either programmatically, via selenide.properties file
     * or by system property "-Dselenide.driverManagerEnabled=false"
     * <br></br>
     *
     * Default: true
     */
    val driverManagerEnabled: Boolean = true,

    /**
     *
     *
     * Whether webdriver logs should be enabled.
     *
     *
     *
     *
     * These logs may be useful for debugging some webdriver issues.
     * But in most cases they are not needed (and can take quite a lot of disk space),
     * that's why don't enable them by default.
     *
     *
     * Default: false
     * @since 5.18.0
     */
    val webdriverLogsEnabled: Boolean = false,

    /**
     * Enables the ability to run the browser in headless mode.
     * Works only for Chrome(59+) and Firefox(56+).
     * Can be configured either programmatically, via selenide.properties file or by system property "-Dselenide.headless=true"
     * <br></br>
     * Default: false
     */
    val headless: Boolean = false,

    /**
     * Sets the path to browser executable.
     * Works only for Chrome, Firefox and Opera.
     * Can be configured either programmatically, via selenide.properties file
     * or by system property "-Dselenide.browserBinary=/path/to/binary"
     */
    val browserBinary: String? = null,
)

enum class BrowserEngine {
    CHROME, EDGE, FIREFOX, OPERA, SAFARI
}