package com.limasky.test;

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
}