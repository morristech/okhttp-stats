package com.flipkart.flipperf;

import com.flipkart.flipperf.newlib.response.DefaultResponseHandler;
import com.flipkart.flipperf.newlib.response.ResponseHandler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by anirudh.r on 05/05/16 at 7:53 PM.
 * Test for {@link DefaultResponseHandler}
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class DefaultResponseHandlerTest {

    /**
     * Test for {@link ResponseHandler#onEOF()}
     */
    @Test
    public void testOnEOF() {

        DefaultResponseHandler.ResponseCallback responseCallback = mock(DefaultResponseHandler.ResponseCallback.class);
        DefaultResponseHandler defaultResponseHandler = new DefaultResponseHandler(responseCallback);
        defaultResponseHandler.onRead(10);
        defaultResponseHandler.onRead(10);
        defaultResponseHandler.onEOF();
        //verify that responseDataReceived gets called once
        verify(responseCallback,times(1)).onEOF(20);
    }


}
