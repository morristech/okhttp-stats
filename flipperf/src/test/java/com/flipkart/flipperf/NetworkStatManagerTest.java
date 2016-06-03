package com.flipkart.flipperf;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.HandlerThread;

import com.flipkart.flipperf.newlib.handler.OnResponseReceivedListener;
import com.flipkart.flipperf.newlib.handler.PersistentStatsHandler;
import com.flipkart.flipperf.newlib.model.RequestStats;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.internal.ShadowExtractor;
import org.robolectric.shadows.ShadowConnectivityManager;
import org.robolectric.shadows.ShadowNetworkInfo;

import java.io.IOException;
import java.net.SocketTimeoutException;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by anirudh.r on 13/05/16 at 12:28 AM.
 * Test for {@link PersistentStatsHandler}
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class NetworkStatManagerTest {

    /**
     * Test for {@link PersistentStatsHandler#addListener(OnResponseReceivedListener)}
     *
     * @throws Exception
     */
    @Test
    public void testAddListener() throws Exception {
        ConnectivityManager connectivityManager = (ConnectivityManager) RuntimeEnvironment.application.getSystemService(Context.CONNECTIVITY_SERVICE);
        ShadowConnectivityManager shadowConnectivityManager = (ShadowConnectivityManager) ShadowExtractor.extract(connectivityManager);
        ShadowNetworkInfo shadowOfActiveNetworkInfo = (ShadowNetworkInfo) ShadowExtractor.extract(connectivityManager.getActiveNetworkInfo());
        shadowOfActiveNetworkInfo.setConnectionType(ConnectivityManager.TYPE_WIFI);

        shadowConnectivityManager.setNetworkInfo(ConnectivityManager.TYPE_WIFI, connectivityManager.getActiveNetworkInfo());

        OnResponseReceivedListener onResponseReceivedListener = mock(OnResponseReceivedListener.class);

        HandlerThread handlerThread = new HandlerThread("Test");
        handlerThread.start();
        Handler handler = new Handler(handlerThread.getLooper());
        PersistentStatsHandler networkStatManager = new PersistentStatsHandler(RuntimeEnvironment.application, handler, shadowConnectivityManager.getActiveNetworkInfo());
        networkStatManager.addListener(onResponseReceivedListener);

        //assert size is 1
        Assert.assertTrue(networkStatManager.getOnResponseReceivedListenerList().size() == 1);
    }

    /**
     * Test for {@link PersistentStatsHandler#removeListener(OnResponseReceivedListener)}
     *
     * @throws Exception
     */
    @Test
    public void testRemoveListener() throws Exception {
        ConnectivityManager connectivityManager = (ConnectivityManager) RuntimeEnvironment.application.getSystemService(Context.CONNECTIVITY_SERVICE);

        OnResponseReceivedListener onResponseReceivedListener = mock(OnResponseReceivedListener.class);


        HandlerThread handlerThread = new HandlerThread("Test");
        handlerThread.start();
        Handler handler = new Handler(handlerThread.getLooper());
        PersistentStatsHandler networkStatManager = new PersistentStatsHandler(RuntimeEnvironment.application, handler);
        networkStatManager.addListener(onResponseReceivedListener);

        //assert size is 1
        Assert.assertTrue(networkStatManager.getOnResponseReceivedListenerList().size() == 1);

        networkStatManager.removeListener(onResponseReceivedListener);

        //assert size is 0
        Assert.assertTrue(networkStatManager.getOnResponseReceivedListenerList().size() == 0);
    }

    /**
     * Test for {@link PersistentStatsHandler#onResponseReceived(RequestStats)}
     *
     * @throws Exception
     */
    @Test
    public void testOnResponseReceived() throws Exception {
        ConnectivityManager connectivityManager = (ConnectivityManager) RuntimeEnvironment.application.getSystemService(Context.CONNECTIVITY_SERVICE);

        OnResponseReceivedListener onResponseReceivedListener = mock(OnResponseReceivedListener.class);
        OnResponseReceivedListener onResponseReceivedListener1 = mock(OnResponseReceivedListener.class);


        HandlerThread handlerThread = new HandlerThread("Test");
        handlerThread.start();
        Handler handler = new Handler(handlerThread.getLooper());
        PersistentStatsHandler networkStatManager = new PersistentStatsHandler(RuntimeEnvironment.application, handler);
        networkStatManager.addListener(onResponseReceivedListener);

        //assert size is 1
        Assert.assertTrue(networkStatManager.getOnResponseReceivedListenerList().size() == 1);

        RequestStats requestStats = new RequestStats(1);
        networkStatManager.onResponseReceived(requestStats);

        //verify onResponseReceived gets called once
        verify(onResponseReceivedListener, times(1)).onResponseSuccess(any(NetworkInfo.class), eq(requestStats));
        reset(onResponseReceivedListener);

        //adding another listener
        networkStatManager.addListener(onResponseReceivedListener1);

        //assert size is 2
        Assert.assertTrue(networkStatManager.getOnResponseReceivedListenerList().size() == 2);
        networkStatManager.onResponseReceived(requestStats);

        //verify onResponseReceived of 1st listener gets called once
        verify(onResponseReceivedListener, times(1)).onResponseSuccess(any(NetworkInfo.class), eq(requestStats));
        //verify onResponseReceived of 2nd listener gets called once
        verify(onResponseReceivedListener1, times(1)).onResponseSuccess(any(NetworkInfo.class), eq(requestStats));
    }

    /**
     * Test for {@link PersistentStatsHandler#onHttpExchangeError(RequestStats, IOException)}
     *
     * @throws Exception
     */
    @Test
    public void testOnHttpExchangeError() throws Exception {
        ConnectivityManager connectivityManager = (ConnectivityManager) RuntimeEnvironment.application.getSystemService(Context.CONNECTIVITY_SERVICE);

        OnResponseReceivedListener onResponseReceivedListener = mock(OnResponseReceivedListener.class);
        OnResponseReceivedListener onResponseReceivedListener1 = mock(OnResponseReceivedListener.class);

        HandlerThread handlerThread = new HandlerThread("Test");
        handlerThread.start();
        Handler handler = new Handler(handlerThread.getLooper());
        PersistentStatsHandler networkStatManager = new PersistentStatsHandler(RuntimeEnvironment.application, handler);
        networkStatManager.addListener(onResponseReceivedListener);

        //assert size is 1
        Assert.assertTrue(networkStatManager.getOnResponseReceivedListenerList().size() == 1);

        RequestStats requestStats = new RequestStats(1);
        networkStatManager.onHttpExchangeError(requestStats, new IOException(""));

        //verify onHttpErrorReceived gets called once
        verify(onResponseReceivedListener, times(1)).onResponseError(any(NetworkInfo.class), eq(requestStats), any(IOException.class));

        //adding another listener
        networkStatManager.addListener(onResponseReceivedListener1);
        reset(onResponseReceivedListener);

        //assert size is 2
        Assert.assertTrue(networkStatManager.getOnResponseReceivedListenerList().size() == 2);
        networkStatManager.onHttpExchangeError(requestStats, new IOException(""));

        verify(onResponseReceivedListener, times(1)).onResponseError(any(NetworkInfo.class), eq(requestStats), any(IOException.class));
        verify(onResponseReceivedListener1, times(1)).onResponseError(any(NetworkInfo.class), eq(requestStats), any(IOException.class));

    }

    /**
     * Test for number of {@link OnResponseReceivedListener} in {@link PersistentStatsHandler}
     *
     * @throws Exception
     */
    @Test
    public void testOnInputStreamError() throws Exception {
        ConnectivityManager connectivityManager = (ConnectivityManager) RuntimeEnvironment.application.getSystemService(Context.CONNECTIVITY_SERVICE);

        OnResponseReceivedListener onResponseReceivedListener = mock(OnResponseReceivedListener.class);
        OnResponseReceivedListener onResponseReceivedListener1 = mock(OnResponseReceivedListener.class);

        HandlerThread handlerThread = new HandlerThread("Test");
        handlerThread.start();
        Handler handler = new Handler(handlerThread.getLooper());
        PersistentStatsHandler networkStatManager = new PersistentStatsHandler(RuntimeEnvironment.application, handler);
        networkStatManager.addListener(onResponseReceivedListener);

        //assert size is 1
        Assert.assertTrue(networkStatManager.getOnResponseReceivedListenerList().size() == 1);

        RequestStats requestStats = new RequestStats(1);
        networkStatManager.onResponseInputStreamError(requestStats, new SocketTimeoutException());

        //verify onInputStreamReadError gets called once
        reset(onResponseReceivedListener);

        //adding another listener
        networkStatManager.addListener(onResponseReceivedListener1);

        //assert size is 2
        Assert.assertTrue(networkStatManager.getOnResponseReceivedListenerList().size() == 2);

    }

}
