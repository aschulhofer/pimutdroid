package at.woodstick.mysampleapplication.espresso;

import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import at.woodstick.mysampleapplication.GSampleActivity;
import at.woodstick.mysampleapplication.R;

import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.ComponentNameMatchers.hasShortClassName;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static android.support.test.espresso.intent.matcher.IntentMatchers.toPackage;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static at.woodstick.mysampleapplication.util.EspressoUtils.clickButton;
import static at.woodstick.mysampleapplication.util.EspressoUtils.enterText;
import static at.woodstick.mysampleapplication.util.EspressoUtils.onViewWithId;
import static org.hamcrest.core.AllOf.allOf;

/**
 *
 */
@RunWith(AndroidJUnit4.class)
public class GSampleActivityTest {

    private static final String PACKAGE_NAME = "at.woodstick.mysampleapplication";
    private static final String ACTIVITY_CLASS_SHORTNAME_DISPLAYMESSAGE = ".DisplayMessageActivity";

    private static final String TEXT_TO_BE_TYPED = "asd ASD1234";

    @Rule
    public IntentsTestRule<GSampleActivity> intentRule = new IntentsTestRule<>(GSampleActivity.class);

    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {

    }

    @Test
    public void typeText_click_InitiatesActivity() {
        enterText(R.id.editText, TEXT_TO_BE_TYPED);

        clickButton(R.id.button_send_message);

        onViewWithId(R.id.messageInputTextView).check(doesNotExist());

        intended(
            allOf(
                hasComponent(hasShortClassName(ACTIVITY_CLASS_SHORTNAME_DISPLAYMESSAGE)),
                toPackage(PACKAGE_NAME),
                hasExtra(GSampleActivity.EXTRA_MESSAGE, TEXT_TO_BE_TYPED)
            )
        );
    }

    @Test
    public void typeNoText_click_InitiatesActivity() {
        clickButton(R.id.button_send_message);

        onViewWithId(R.id.messageInputTextView).check(matches(isDisplayed()));
    }
}
