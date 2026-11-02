package com.mottainai.operacional;
import android.content.Intent;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.mottainai.operacional.notifications.NotificationRouter;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
/** Internal inbox routing works without FCM, notification permission or a signed-in fixture. */
@RunWith(AndroidJUnit4.class)
public class DeviceNotificationSmokeTest {
    @Test public void managerOnlyDestinationsRemainProtected(){
        assertFalse(NotificationRouter.isDestinationAllowed(NotificationRouter.Destination.POS_APPROVAL,"estoquista"));
        assertTrue(NotificationRouter.isDestinationAllowed(NotificationRouter.Destination.POS_APPROVAL,"gerente"));
        assertFalse(NotificationRouter.isDestinationAllowed(NotificationRouter.Destination.ALERT,"unknown"));
    }
    @Test public void invalidDestinationsAreNotOpened(){
        assertNull(NotificationRouter.destinationFromIntent(new Intent().putExtra(NotificationRouter.EXTRA_DESTINATION,"INVALID")));
        assertEquals(NotificationRouter.Destination.ALERT,NotificationRouter.destinationFromIntent(new Intent().putExtra(NotificationRouter.EXTRA_DESTINATION,"ALERT")));
    }
}
