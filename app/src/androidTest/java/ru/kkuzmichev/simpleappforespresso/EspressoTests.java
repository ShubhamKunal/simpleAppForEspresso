package ru.kkuzmichev.simpleappforespresso;

import android.view.View;
import android.widget.ListView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.InstrumentationRegistry;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.uiautomator.UiDevice;
import io.qameta.allure.android.runners.AllureAndroidJUnit4;
import io.qameta.allure.kotlin.Allure;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import android.Manifest;
import android.os.Environment;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasData;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.Matchers.anyOf;
import org.junit.rules.TestWatcher;

@RunWith(AllureAndroidJUnit4.class)
public class EspressoTests {
    @Rule
    public TestWatcher watcher = new TestWatcher() {
        @Override
        protected void failed(Throwable e, org.junit.runner.Description description) {
            String className = description.getClassName();
            className = className.substring(className.lastIndexOf('.') + 1);
            String methodName = description.getMethodName();
            takeScreenshot(className + "#" + methodName);
        }
    };

    private void takeScreenshot(String name) {
        File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/screenshots/");
        if (!path.exists()) {
            path.mkdirs();
        }
        UiDevice device = UiDevice.getInstance(getInstrumentation());
        String filename = name + ".png";
        device.takeScreenshot(new File(path, filename));
        try {
            Allure.attachment(filename, new FileInputStream(new File(path, filename)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule
            .grant(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            );

    @Rule
    public ActivityScenarioRule<MainActivity> mActivityTestRule = new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void textCheckPositive() {
        Allure.feature("textCheckPositive");
        onView(withId(R.id.text_home)).check(ViewAssertions.matches(withText("This is home fragment")));
    }

    @Test
    public void textCheckNegative() {
        Allure.feature("textCheckNegative");
        onView(withId(R.id.text_home)).check(ViewAssertions.matches(withText("Another text")));
    }

    @Test
    public void testIntent() {
        Allure.feature("testIntent");
        Intents.init();
        try {
            openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext());
        } catch (Exception e) {
        }
        onView(anyOf(withText(R.string.action_settings), withId(R.id.action_settings))).perform(click());
        Intents.intended(hasData("https://google.com"));
    }

    @Before
    public void registerIdlingResources() { //Connect to the "counter"
        IdlingRegistry.getInstance().register(EspressoIdlingResources.idlingResource);
    }

    @After // Runs after tests
    public void unregisterIdlingResources() { //Disconnect from "counter"
        IdlingRegistry.getInstance().unregister(EspressoIdlingResources.idlingResource);
    }

    @Test
    public void testGalleryList() {
        Allure.feature("testGalleryList");
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.nav_gallery)).perform(click());
        onView(withId(R.id.recycle_view)).check(ViewAssertions.matches (CustomMatchers.withListSize(10)));
    }
}

class CustomMatchers {

    public static Matcher<View> withListSize (final int size) {
        return new TypeSafeMatcher<View>() {
            @Override public boolean matchesSafely (final View view) {
                return ((RecyclerView) view).getAdapter().getItemCount() == size;
            }

            @Override public void describeTo (final Description description) {
                description.appendText ("RecyclerView should have " + size + " items");
            }
        };
    }
}