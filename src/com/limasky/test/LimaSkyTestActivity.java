package com.limasky.test;

import android.app.Activity;
import android.os.Bundle;

public class LimaSkyTestActivity extends Activity
{
	static
	{
		// Load the C++ based Doodle Jump code as a library!
		System.loadLibrary( "doodlejump" );
	}
	
	GL1SurfaceView mView;
	
	LSGL1Renderer mGL2RendererLib;

	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		
		mView = new GL1SurfaceView( this, getApplication() );
		setContentView( mView );
		
		mGL2RendererLib = new LSGL1Renderer();

		LSAudioManager.getInstance().init( this.getBaseContext() );
		MessageHandler.getInstance().initNative();
	}

	@Override
	protected void onDestroy()
	{
		MessageHandler.getInstance().destroyNative();
		LSAudioManager.getInstance().destroy();
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
		
        // The following call pauses the rendering thread.
        // If your OpenGL application is memory intensive,
        // you should consider de-allocating objects that
        // consume significant memory here.
		mView.onPause();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		
        // The following call resumes a paused rendering thread.
        // If you de-allocated graphic objects for onPause()
        // this is a good place to re-allocate them.
		mView.onResume();
	}
}
