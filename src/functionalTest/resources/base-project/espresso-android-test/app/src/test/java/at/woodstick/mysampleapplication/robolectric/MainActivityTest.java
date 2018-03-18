package at.woodstick.mysampleapplication.robolectric;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Resources;
import android.widget.Button;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;

import at.woodstick.mysampleapplication.GSampleActivity;
import at.woodstick.mysampleapplication.MainActivity;
import at.woodstick.mysampleapplication.R;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * If encountering error
 *
 * No such manifest file: build\intermediates\bundles\debug\AndroidManifest.xml
 * android.content.res.Resources$NotFoundException: Resource ID #0x7f020052
 *
 * Try:
 * Run > Edit Configurations.. > Defaults > Android JUnit
 * Working Directory: $MODULE_DIR$
 */
@RunWith(RobolectricTestRunner.class)
public class MainActivityTest {

    private MainActivity mainActivity;
    private Resources mainActivityResources;

    @Before
    public void setUp() {
        mainActivity = Robolectric.setupActivity(MainActivity.class);
        mainActivityResources = mainActivity.getResources();
    }

    @Test
    @Config(qualifiers = "de")
    public void gsampleStartButton_germanText_shouldBeCorrect() {
        Button gsampleButton = getViewById(R.id.start_gsample_button);

        String actualGSampleButtonText = (String)gsampleButton.getText();
        String expectedGSampleButtonText = mainActivityResources.getString(R.string.button_start_gsample);

        assertThat(actualGSampleButtonText, is("GSample starten"));
        assertThat(actualGSampleButtonText, is(expectedGSampleButtonText));
    }

    @Test
    public void gsampleStartButton_click_shouldStartCorrectIntent() {
        Button gsampleButton = getViewById(R.id.start_gsample_button);
        gsampleButton.performClick();

        Intent actualIntent = ShadowApplication.getInstance().getNextStartedActivity();
        Intent expectedIntent = new Intent(mainActivity, GSampleActivity.class);
        ComponentName expectedComponent = new ComponentName(mainActivity, GSampleActivity.class);

        assertThat(actualIntent.getComponent().getPackageName(), is(expectedIntent.getComponent().getPackageName()));
        assertThat(actualIntent.getComponent().getClassName(), is(expectedIntent.getComponent().getClassName()));

        assertThat(actualIntent.getComponent(), is(expectedComponent));
        assertThat(actualIntent.getComponent().getPackageName(), is(mainActivity.getPackageName()));
        assertThat(actualIntent.getComponent().getClassName(), is(GSampleActivity.class.getName()));
    }

    public <T> T getViewById(int viewId) {
        return getViewById(mainActivity, viewId);
    }

    public static <T> T getViewById(Activity activity, int viewId) {
        return (T) activity.findViewById(viewId);
    }
}
