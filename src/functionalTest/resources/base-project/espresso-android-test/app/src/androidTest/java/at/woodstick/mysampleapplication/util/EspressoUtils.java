package at.woodstick.mysampleapplication.util;

import android.app.Activity;
import android.app.Instrumentation;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.action.ViewActions;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.isInternal;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.core.IsNot.not;

/**
 * APP
 */
public final class EspressoUtils {

    public static ViewInteraction onViewWithId(int id) {
        return onView(withId(id));
    }

    /**
     * Closes soft keyboard {@link ViewActions#closeSoftKeyboard()}
     * @param id the resource id
     * @param textToType the text to type
     * @return
     */
    public static ViewInteraction enterText(int id, String textToType) {
        return enterText(id, textToType, true);
    }

    public static ViewInteraction enterText(int id, String textToType, boolean closeKeyboard) {
        ViewInteraction interaction = onView(withId(id)).perform(typeText(textToType));

        if(closeKeyboard) {
            interaction.perform(closeSoftKeyboard());
        }

        return interaction;
    }

    public static ViewInteraction clickButton(int id) {
        return onView(withId(id)).perform(click());
    }

    /**
     * By default Espresso Intents does not stub any Intents. Stubbing needs to be setup before
     * every test run. In this case all internal Intents will be blocked.
     */
    public static void stubAllInternalIntents() {
        intending(isInternal()).respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, null));
    }

    /**
     * By default Espresso Intents does not stub any Intents. Stubbing needs to be setup before
     * every test run. In this case all external Intents will be blocked.
     */
    public static void stubAllExternalIntents() {
        intending(not(isInternal())).respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, null));
    }
}
