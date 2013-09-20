package com.limasky.test;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;

public final class LSGL1Renderer
{
	public GL10 gl10;
	public Context context;
	
    private static final LSGL1Renderer INSTANCE = new LSGL1Renderer();

    private LSGL1Renderer()
    {
        if( INSTANCE != null )
        {
            throw new IllegalStateException( "Already instantiated" );
        }
    }
    
    public static LSGL1Renderer getInstance()
    {
        return INSTANCE;
    }
    
	public static native void init( int width, int height );
	public static native void step();
}
