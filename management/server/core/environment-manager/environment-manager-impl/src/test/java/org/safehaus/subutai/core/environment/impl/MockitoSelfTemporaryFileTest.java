package org.safehaus.subutai.core.environment.impl;


import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;


/**
 * Created by bahadyr on 9/28/14.
 */
@RunWith( MockitoJUnitRunner.class )
public class MockitoSelfTemporaryFileTest
{
    LinkedList mockedList;


    @Before
    public void setUp() throws Exception
    {
        mockedList = mock( LinkedList.class );
    }


    /**
     * Verify behaviour
     */
    @Test
    public void testName() throws Exception
    {

        mockedList.add( "one" );
        mockedList.clear();

        verify( mockedList ).add( "one" );
        verify( mockedList ).clear();
    }


    @Test
    public void testStubbing()
    {
        when( mockedList.get( 0 ) ).thenReturn( "first" );
        when( mockedList.get( 1 ) ).thenThrow( new RuntimeException() );

        System.out.println( mockedList.get( 0 ) );
        //        System.out.println(mockedList.get( 1 ));
        System.out.println( mockedList.get( 999 ) );

        verify( mockedList ).get( 0 );
    }


    @Test
    public void testArgumentMatcher()
    {

        when( mockedList.get( anyInt() ) ).thenReturn( "element" );
        //        when( mockedList.contains( argThat( isValid() ) ) ).thenReturn( "element" );
        //        verify( mockedList ).get( anyInt() );
    }


    @Test
    public void testVerifyExactNumberOfInvocations() throws Exception
    {
        mockedList.add( "once" );
        mockedList.add( "once" );
        verify( mockedList, times( 2 ) ).add( "once" );
        verify( mockedList, never() ).add( "never" );
        verify( mockedList, atLeast( 2 ) ).add( "once" );
        verify( mockedList, atMost( 2 ) ).add( "once" );
        verify( mockedList, atLeastOnce() ).add( "once" );
    }


    @Test
    public void testStubbingVoidMethodsWithExceptions()
    {
        doThrow( new RuntimeException() ).when( mockedList ).clear();
        //        mockedList.clear();
    }


    @Test
    public void testVerificationInOrder()
    {
        List singleMock = mock( List.class );
        singleMock.add( "was added first" );
        singleMock.add( "was added second" );

        InOrder inOrder = inOrder( singleMock );
    }


    private InOrder inOrder( final List singleMock )
    {
        return null;
    }


    @Test
    public void testMakeSureInteractionsNeverHappenedOnMock()
    {
        List mockOne = mock( List.class );
        List mockTwo = mock( List.class );
        List mockThree = mock( List.class );

        mockOne.add( "one" );
        verify( mockOne ).add( "one" );
        verify( mockOne, never() ).add( "two" );

        verifyZeroInteractions( mockTwo, mockThree );
    }


    @Test
    public void testFindingRedundantInvocations()
    {
        List mockedList = mock( List.class );
        mockedList.add( "one" );
        mockedList.add( "two" );

        verify( mockedList ).add( "one" );
        //        verifyNoMoreInteractions( mockedList );
    }


    @Mock
    Map mockMap;


    @Test
    public void testMockMap()
    {
        mockMap.put( "key", "value" );
        verify( mockMap ).put( "key", "value" );
    }


    @Mock
    TestClass testClass;


    @Test
    public void testStubbingConsecutiveCalls()
    {
        TestClass mock = mock( TestClass.class );
        //        when( mock.someMethod( "some arg" ) ).thenThrow( new RuntimeException() ).thenReturn( "foo" );
        when( mock.someMethod( "some arg" ) ).thenReturn( "one", "two", "three" );

        mock.someMethod( "some arg" );
        System.out.println( mock.someMethod( "some arg" ) );
        System.out.println( mock.someMethod( "some arg" ) );
    }


    @Test
    public void testStubbingWithCallbacks()
    {
        when( testClass.someMethod( anyString() ) ).thenAnswer( new Answer()
        {
            @Override
            public Object answer( final InvocationOnMock invocationOnMock ) throws Throwable
            {
                Object[] args = invocationOnMock.getArguments();
                Object mock = invocationOnMock.getMock();
                return "Called with arguments: " + args;
            }
        } );

        System.out.println( testClass.someMethod( "one" ) );
    }


    @Test
    public void testCapturingArguments()
    {

        ArgumentCaptor<TestClass> argument = ArgumentCaptor.forClass( TestClass.class );
        verify( testClass ).someMethod( argument.capture() );
        //        assertEquals("Bahadyr", argument.getValue().someMethod( "Bahadyr" ));
    }
}


class TestClass
{
    public String someMethod( final String s )
    {
        return "some test";
    }


    public void someMethod( final TestClass capture )
    {

    }
}
