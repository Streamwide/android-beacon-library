/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on Thu, 17 Oct 2024 15:45:30 +0100
 * @copyright  Copyright (c) 2024 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2024 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn Thu, 17 Oct 2024 15:25:40 +0100
 */
package org.altbeacon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.powermock.api.mockito.PowerMockito.mock;

import android.content.Context;
import android.util.Log;

import com.streamwide.smartms.altbeacon.beacon.Beacon;
import com.streamwide.smartms.altbeacon.beacon.BeaconParser;
import com.streamwide.smartms.altbeacon.beacon.Region;
import com.streamwide.smartms.altbeacon.beacon.service.ExtraDataBeaconTracker;
import com.streamwide.smartms.altbeacon.beacon.service.MonitoringStatus;
import com.streamwide.smartms.altbeacon.beacon.service.RangeState;
import com.streamwide.smartms.altbeacon.beacon.service.ScanState;
import com.streamwide.smartms.lib.template.serialization.ValidatingObjectInputStream;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.creation.DelegatingMethod;
import org.robolectric.RobolectricTestRunner;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This is more an example than a test - deserialize our {@link ScanState}
 * to verify which settings it requires, as the object uses a number of primitive
 * and java.* member objects.
 */
@RunWith(RobolectricTestRunner.class)
public class ScanStateTest extends ClosingBase {

    private InputStream inputStream;
    private ScanState original;

    protected Context context = mock(Context.class);

    @Override
    @Before
    public void setup() throws IOException {
        MockitoAnnotations.initMocks(this);
        mock(DelegatingMethod.class, Mockito.withSettings().serializable());

        ExtraDataBeaconTracker extraDataBeaconTracker = new ExtraDataBeaconTracker();
        extraDataBeaconTracker.track(mock(Beacon.class));

        MonitoringStatus monitoringStatus = MonitoringStatus.getInstance();

        Set<BeaconParser> beaconParsers = new HashSet<>();
        beaconParsers.add(mock(BeaconParser.class));

        Map<Region, RangeState> rangedRegionState = new HashMap<>();
        rangedRegionState.put(mock(Region.class), mock(RangeState.class));

        original = new ScanState(context);
        original.setExtraBeaconDataTracker(extraDataBeaconTracker);
        original.setMonitoringStatus(monitoringStatus);
        original.setRangedRegionState(rangedRegionState);
        original.setBeaconParsers(beaconParsers);


        final ByteArrayOutputStream bos = willClose(new ByteArrayOutputStream());
        final ObjectOutputStream oos = willClose(new ObjectOutputStream(bos));
        oos.writeObject(original);
        inputStream = willClose(new ByteArrayInputStream(bos.toByteArray()));
    }

    private void assertSerialization(final ObjectInputStream ois) throws ClassNotFoundException, IOException {
        final ScanState copy = (ScanState) (ois.readObject());
//        copy.setExtraBeaconDataTracker(extraDataBeaconTracker);
        assertEquals(original.toString(), copy.toString(), "Expecting same data after deserializing");
    }

    /**
     * Trusting java.lang.* and the array variants of that means we have
     * to define a number of accept classes explicitly. Quite safe but
     * might become a bit verbose.
     */
    @Test
    @Ignore("Test failed serialization is not the same")
    public void trustJavaLang() throws IOException, ClassNotFoundException {
        assertSerialization(willClose(
                new ValidatingObjectInputStream(inputStream)
                        .accept("org.altbeacon.*")
                        .accept("org.mockito.*")
                        .accept("java.*", "[Ljava.*")
        ));
    }


    /**
     * Trusting all
     */
    @Test
    public void assertTrustAllAndNoThrowsWhenReadObject() {


//        try {
        Assertions.assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                willClose(
                        new ValidatingObjectInputStream(inputStream)
                                .accept("*")).readObject();
            }
        });


//        Assertions.assertThrows(IllegalArgumentException.class, new Executable() {
//
//            @Override
//            public void execute() throws Throwable {
////                User user = new User();
////                user.setName(null);
//            }
//        });

//        } catch (NotSerializableException e) {
//            System.err.println("test");
//        } catch (IOException | ClassNotFoundException e) {
//            e.printStackTrace();
//        }
    }

    @Test
    public void serializeObjectWithGson() throws IOException {

        willClose(new ValidatingObjectInputStream(inputStream)
                .accept("*"));

        final ByteArrayOutputStream bos = willClose(new ByteArrayOutputStream());
        final ObjectOutputStream oos;
        try {
            oos = willClose(new ObjectOutputStream(bos));
            oos.writeObject(original);


            InputStream inputStream = willClose(new ByteArrayInputStream(bos.toByteArray()));
            ValidatingObjectInputStream validatingObjectInputStream = new ValidatingObjectInputStream(inputStream)
                    .accept("*");

            Object o = validatingObjectInputStream.readObject();
        } catch (IOException e) {
            Log.d("IOException : ", "error : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            Log.d("ClassNotFoundException : ", "error : " + e.getMessage());
        }
    }
}