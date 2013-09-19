package com.limasky.test;

import java.lang.reflect.Field;
import java.util.HashMap;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;

// This Java-side singleton will manage the loading and playing of audio via 
// JNI based requests from the native-side MessageTranslator.
public final class LSAudioManager
{
	private SoundPool mSoundPool;
	private AudioManager  mAudioManager;
	private HashMap<Long,Integer> mSoundPoolMap;
	
	private Context mContext;

    private static final LSAudioManager INSTANCE = new LSAudioManager();
    
    public static final int MAX_SIMULTANEOUS_STREAMS = 4;

    private LSAudioManager()
    {
        if( INSTANCE != null )
        {
            throw new IllegalStateException( "Already instantiated" );
        }
    }

    public static LSAudioManager getInstance()
    {
        return INSTANCE;
    }
    
    public static int getResId(String variableName, Class<?> c)
    {
        Field field = null;
        int resId = 0;
        try {
            field = c.getField(variableName);
            try {
                resId = field.getInt(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return resId;
    }

	public void init( Context context )
    {
		mContext = context;
		
        // Set up our audio player.
        mSoundPool = new SoundPool( MAX_SIMULTANEOUS_STREAMS, AudioManager.STREAM_MUSIC, 0 );
        mAudioManager = (AudioManager) context.getSystemService( Context.AUDIO_SERVICE );
        mSoundPoolMap = new HashMap<Long,Integer>();
    }

	public void destroy()
    {
		mSoundPool.release();
		mSoundPool = null;
		mSoundPoolMap.clear();
		mAudioManager.unloadSoundEffects();
    }

	public void loadSound( long hashId, String fileName )
	{
        //Log.i( "loadSound", "C++ wants Java to load a sound!" );
        //Log.i( "fileName = ", fileName );
        //Log.i( "hashId = ", String.valueOf(hashId) );
        
        // Find the resource id that matches the file name passed.
        int resourceId = getResId( fileName, R.raw.class );

        if( !mSoundPoolMap.containsKey( hashId ) )
        {
            // Load the sound and map the sound id created by SoundPool to the 
            // hash id created by the Native side.
            int soundPoolId = mSoundPool.load( mContext, resourceId, 1 );
            
            mSoundPoolMap.put( hashId, soundPoolId );
        }
        else
        {
			Log.e( "loadSound", "Sound pool failed to load the sound file '" + fileName + "'. The hash id " + String.valueOf(hashId) + " has already been used." );
        }
    }
	
	public void playSound( long hashId, float volume, int loop )
	{
        //Log.i( "playSound", "C++ wants Java to play a sound!" );
        //Log.i( "hashId = ", String.valueOf(hashId) );
        //Log.i( "volume = ", String.valueOf(volume) );
        //Log.i( "loop = ", String.valueOf(loop) );
		
        float streamVolume = mAudioManager.getStreamVolume( AudioManager.STREAM_MUSIC ) * volume;
		streamVolume = streamVolume / mAudioManager.getStreamMaxVolume( AudioManager.STREAM_MUSIC );
		
		//Log.i( "streamVolume = ", String.valueOf(streamVolume) );
		
		int streamId = 0;
		
		try
		{
			streamId = mSoundPool.play( mSoundPoolMap.get(hashId), streamVolume, streamVolume, 1, loop, 1f );
		}
		catch( NullPointerException e )
		{
			Log.e( "playSound", "Sound pool map returned a NULL for hash id: " + String.valueOf(hashId) );
		}
        
        if( streamId == 0 )
        {
        	Log.e( "playSound", "Sound pool failed to play sound for hash id: " + String.valueOf(hashId) );
        }
    }
}