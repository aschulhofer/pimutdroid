package at.woodstick.mysampleapplication.espresso;

import android.content.Intent;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import at.woodstick.mysampleapplication.DisplayMessageActivity;
import at.woodstick.mysampleapplication.GSampleActivity;
import at.woodstick.mysampleapplication.R;

import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static at.woodstick.mysampleapplication.util.EspressoUtils.onViewWithId;

/**
 *
 */
@RunWith(AndroidJUnit4.class)
public class DisplayMessageActivityTest {

    private static final String TEXT_TO_BE_TYPED = "asd ASD1234";

    @Rule
    public ActivityTestRule<DisplayMessageActivity> launchActivityRule = new ActivityTestRule<>(DisplayMessageActivity.class, false, false);

    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {

    }

    @Test
    public void typeText_click_InitiatesActivity() {
        Intent messageIntent = new Intent();
        messageIntent.putExtra(GSampleActivity.EXTRA_MESSAGE, TEXT_TO_BE_TYPED);

        launchActivityRule.launchActivity(messageIntent);

        onViewWithId(R.id.textView).check(matches(withText(TEXT_TO_BE_TYPED)));
    }
}
