package com.applitools.quickstarts;

import com.applitools.eyes.*;
//import com.applitools.eyes.BatchInfo;
//import com.applitools.eyes.RectangleSize;
//import com.applitools.eyes.TestResultsSummary;
import com.applitools.eyes.selenium.*;
//import com.applitools.eyes.selenium.BrowserType;
//import com.applitools.eyes.selenium.Configuration;
//import com.applitools.eyes.selenium.Eyes;
import com.applitools.eyes.selenium.fluent.Target;
import com.applitools.eyes.visualgrid.model.*;
//import com.applitools.eyes.visualgrid.model.DeviceName;
//import com.applitools.eyes.visualgrid.model.ScreenOrientation;
import com.applitools.eyes.visualgrid.services.VisualGridRunner;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

/**
 * Unit test for simple App.
 */
public class AppTest {

	public static void main(String[] args) {
		// BaseURL of the website
		String baseURL = System.getenv("BASEURL");
		if (baseURL == null) {
			baseURL = "https://demo.applitools.com";
			//baseURL = "https://demo.applitools.com/index_v2.html";
			// ⭐️ Note to see visual bugs, run the test using the above URL for the 1st run.
			// but then change the above URL to https://demo.applitools.com/index_v2.html
			// (for the 2nd run)
		}
		
		// Use or not-use the Ultrafast Grid
		boolean useGrid = false; //default to not using the Ultrafast Grid (override with env var)
		String env = System.getenv("USE_GRID");
		if (env != null) {
			useGrid = return Boolean.parseBoolean(env);
		}
		
		// Create a new chrome web driver
		WebDriver webDriver = new ChromeDriver(new ChromeOptions().setHeadless(getCI()));

		// Create Grid or Local (aka Classic) runner
		EyesRunner runner = null;
		if (useGrid) {
			// Create a runner with concurrency of 10
			runner = new VisualGridRunner(10);
		} else {
			runner = new ClassicRunner();
		}
			
		// Create Eyes object with the runner.
		Eyes eyes = new Eyes(runner);
		setUp(eyes, useGrid); // setup Applitools Configuration

		try {
			simpleTest(baseURL, webDriver, eyes);
		} finally {
			tearDown(webDriver, runner);
		}

	}

	public static boolean getCI() {
		String env = System.getenv("CI");
		return Boolean.parseBoolean(env);
	}

	public static void setUp(Eyes eyes, boolean useGrid) {

		// Initialize eyes Configuration
		Configuration config = new Configuration();

		// You can get your api key from the Applitools dashboard and set as environment variable or GitHub secret
		config.setApiKey(System.getenv("APPLITOOLS_API_KEY"));

		if (useGrid) {
			// create a new batch info instance and set it to the configuration
			config.setBatch(new BatchInfo("Demo Batch - Selenium for Java - Ultrafast"));

			// Add browsers with different viewports
			config.addBrowser(800, 600, BrowserType.CHROME);
			config.addBrowser(700, 500, BrowserType.FIREFOX);
			config.addBrowser(1600, 1200, BrowserType.IE_11);
			config.addBrowser(1024, 768, BrowserType.EDGE_CHROMIUM);
			config.addBrowser(800, 600, BrowserType.SAFARI);

			// Add mobile emulation devices in Portrait mode
			config.addDeviceEmulation(DeviceName.iPhone_X, ScreenOrientation.PORTRAIT);
			config.addDeviceEmulation(DeviceName.Pixel_2, ScreenOrientation.PORTRAIT);
		} else {
			// create a new batch info instance and set it to the configuration
			config.setBatch(new BatchInfo("Demo Batch - Selenium for Java - Local"));
		}
		
		// Advanced Configuration properties -- see documentation for details
		/* TODO - add and document this stuff
					.setIgnoreDisplacements(true)
					.setMatchLevel(MatchLevel.STRICT);
					.setHideScrollbars(true)
					fullscreen
					wait before screenshots
					send dom
					use dom
					root cause analysis
					*/
		// Set the configuration object to eyes
		eyes.setConfiguration(config);

	}

	public static void simpleTest(String baseURL, WebDriver webDriver, Eyes eyes) {

		try {

			// Navigate to the url we want to test
			webDriver.get(baseURL);

			// Call Open on eyes to initialize a test session
			eyes.open(webDriver, "Demo App - Selenium for Java", "Smoke Test - Selenium for Java", new RectangleSize(800, 600));

			// check the login page with fluent api, see more info here
			// https://applitools.com/docs/topics/sdk/the-eyes-sdk-check-fluent-api.html
			eyes.check(Target.window().fully().withName("Login page"));

			webDriver.findElement(By.id("log-in")).click();

			// Check the app page
			eyes.check(Target.window().fully().withName("App page"));

			// Call Close on eyes to let the server know it should display the results
			eyes.closeAsync();

		} finally  {
			eyes.abortAsync();
		}

	}

	private static void tearDown(WebDriver webDriver, EyesRunner runner) {
		// Close the browser
		webDriver.quit();

		// find visual differences
		TestResultsSummary allTestResults = runner.getAllTestResults(true);
		System.out.println(allTestResults);
	}

}
