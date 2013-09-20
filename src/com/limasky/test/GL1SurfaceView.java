package com.limasky.test;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;

class GL1SurfaceView extends GLSurfaceView
{
	public GL1SurfaceView( Activity activity, Context context )
	{
		super( context );

		// Create an OpenGL ES 1.0 context.
		setEGLContextClientVersion( 1 );

		// Set the Renderer for drawing on the GLSurfaceView
		setRenderer( new GL1Renderer( activity, context ) );
	}

	private static class GL1Renderer implements GLSurfaceView.Renderer
	{
		public GL1Renderer( Activity activity, Context context )
		{
			LSGL1Renderer.getInstance().context = context;
		}

		public void onDrawFrame( GL10 gl )
		{
			LSGL1Renderer.step();
		}

		public void onSurfaceChanged( GL10 gl, int width, int height )
		{
			LSGL1Renderer.init( width, height );

			// Do this here since the native side does not have gluPerspective.
			gl.glMatrixMode( GL10.GL_PROJECTION );
			gl.glLoadIdentity();
			GLU.gluPerspective( gl, 45.0f, (float) width / (float) height, 0.1f, 100.0f );
		}

		public void onSurfaceCreated( GL10 gl, EGLConfig config )
		{
			LSGL1Renderer.getInstance().gl10 = gl;
		}
	}
}
