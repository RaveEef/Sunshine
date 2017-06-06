package com.example.android.sunshine.app.data;

import android.test.AndroidTestCase;

/**
 * Created by evast on 9-5-2017.
 */
@SuppressWarnings("deprecation")
public class TestPractice extends AndroidTestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testThatDemonstratedAssertions() throws Throwable{
        int a = 5;
        int b = 3;
        int c = 5;
        int d = 10;

        assertEquals("X should be equal", a, c);
        assertTrue("Y should be true", d > a);
        assertFalse("Z should be false", a == b);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
