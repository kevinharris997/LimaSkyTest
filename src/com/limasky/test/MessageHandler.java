package com.limasky.test;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.opengl.GLUtils;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

// This is the Java-side singleton for managing the native-side MessageTranslator 
// singleton. Its primary goal is to initialize and destroy the native-side 
// singleton in response to Android life-cycle events and to dispatch requests 
// to objects in the UI thread via Java Handler Messages.
public final class MessageHandler
{
	private Handler mHandler;
	
	public static final int MSG_SUCCESS = 1;
	public static final int MSG_FAILURE = 0;

    static class MessageIds
    {
        public static final int Msg_Load_Audio = 0;
        public static final int Msg_Play_Audio = 1;
    }

    private static final MessageHandler INSTANCE = new MessageHandler();

    private MessageHandler()
    {
        if( INSTANCE != null )
        {
            throw new IllegalStateException( "Already instantiated" );
        }
        
		mHandler = new Handler() {
			
			@Override
			public void handleMessage( Message msg ) {
				  
				//Log.i( "MessageHandler Handler", "handleMessage" );

				switch( msg.what )
				{
					case MessageHandler.MessageIds.Msg_Load_Audio:
					{
						Bundle b = msg.getData();
					    long hashId = b.getLong( "hashId" );
					    String fileName = b.getString( "fileName" );
					    
					    LSAudioManager.getInstance().loadSound( hashId, fileName );
					}
					break;
	
					case MessageHandler.MessageIds.Msg_Play_Audio:
					{
						Bundle b = msg.getData();
					    long hashId = b.getLong( "hashId" );
					    float volume = b.getFloat( "volume" );
					    int loop = b.getInt( "loop" );
					    
					    LSAudioManager.getInstance().playSound( hashId, volume, loop );
					}
					break;
					
					default:
						Log.e( "MessageHandler Handler", "Unknown message id " +  String.valueOf(msg.what) + " passed!" );
						break;
				}
			    
			    super.handleMessage( msg );
			}
		};
    }

    public static MessageHandler getInstance()
    {
        return INSTANCE;
    }

	public native void initNative();
	public native void destroyNative();
	
	// These functions always execute in the context of the rendering thread,
	// so we must send a Message that we can catch back in the context of the 
	// UI thread before we can request any work to be done.
	
	public void loadSound( long hashId, String fileName )
	{
		Message msg = mHandler.obtainMessage();
        
        msg.what = MessageIds.Msg_Load_Audio;

        Bundle b = new Bundle();
        b.putLong( "hashId", hashId );
        b.putString( "fileName", fileName );
        msg.setData( b );

        mHandler.sendMessage( msg );
	}
	
	public void playSound( long hashId, float volume, int loop )
	{
        Message msg = mHandler.obtainMessage();
        
        msg.what = MessageIds.Msg_Play_Audio;
        		
        Bundle b = new Bundle();
        b.putLong( "hashId", hashId );
        b.putFloat( "volume", volume );
        b.putInt( "loop", loop );
        msg.setData( b );

        mHandler.sendMessage( msg );
	}
	
	public int generateScoremarker( String playerName )
	{
		// The method is already in the context of the OpenGL rendering thread, so
		// just pull the cached OpenGL interface and app Context and use them to 
		// render the passed player name into a score marker texture.
		
		GL10 gl = LSGL1Renderer.getInstance().gl10;
		Context context = LSGL1Renderer.getInstance().context;

		// Create an empty, mutable bitmap
		Bitmap bitmap = Bitmap.createBitmap( 256, 256, Bitmap.Config.ARGB_4444 );
		// Get a canvas to paint over the bitmap
		Canvas canvas = new Canvas( bitmap );
		bitmap.eraseColor( 0 );

		// Get a background image from resources
		// note the image format must match the bitmap format
		Drawable background = context.getResources().getDrawable( R.drawable.android );
		background.setBounds( 0, 0, 256, 256 );
		background.draw( canvas ); // Draw the background to our bitmap

		// Draw the text
		Typeface tf = Typeface.create( "Helvetica", Typeface.NORMAL );
		
		Paint textPaint = new Paint();
		textPaint.setTypeface( tf );
		textPaint.setTextSize( 32 );
		textPaint.setAntiAlias( true );
		textPaint.setARGB( 255, 0, 0, 0 );
		canvas.drawText( playerName, 40, 50, textPaint );

		int[] textureIds = new int[1];
		
		// Generate one texture pointer...
		gl.glGenTextures( 1, textureIds, 0 );
		// ...and bind it to our array
		gl.glBindTexture( GL10.GL_TEXTURE_2D, textureIds[0] );
		
		// Create Nearest Filtered Texture
		gl.glTexParameterf( GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST );
		gl.glTexParameterf( GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR );
		
		// Different possible texture parameters, e.g. GL10.GL_CLAMP_TO_EDGE
		gl.glTexParameterf( GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT );
		gl.glTexParameterf( GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT );
		
		// Use the Android GLUtils to specify a two-dimensional texture
		// image from our bitmap
		GLUtils.texImage2D( GL10.GL_TEXTURE_2D, 0, bitmap, 0 );
		
		// Clean up
		bitmap.recycle();
		
		return textureIds[0];
	}
}