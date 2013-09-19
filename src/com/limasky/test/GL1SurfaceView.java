package com.limasky.test;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLUtils;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.limasky.test.Quad;

import com.limasky.test.R;

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
		private Quad quad;
		private float angle = 0;
		private Context context;

		public GL1Renderer( Activity activity, Context context )
		{
			this.context = context;

			// Initialize our quad.
			quad = new Quad();

			// Load a texture.
			// quad.loadBitmap( BitmapFactory.decodeResource(
			// activity.getResources(), R.drawable.android ) );
		}

		public void onDrawFrame( GL10 gl )
		{

			// Clears the screen and depth buffer.
			gl.glClear( GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT );

			gl.glLoadIdentity();
			gl.glRotatef( angle, 0, 0, 1 );
			gl.glTranslatef( 0, 0, -6 );

			// Draw our quad.
			quad.draw( gl );

			// Increase the angle.
			angle++;
			
LSGL1Renderer.step();
		}

		public void onSurfaceChanged( GL10 gl, int width, int height )
		{
LSGL1Renderer.init( width, height );

			//gl.glViewport( 0, 0, width, height );

			gl.glMatrixMode( GL10.GL_PROJECTION );
			gl.glLoadIdentity();
			GLU.gluPerspective( gl, 45.0f, (float) width / (float) height, 0.1f, 100.0f );

			gl.glMatrixMode( GL10.GL_MODELVIEW );
			gl.glLoadIdentity();
		}

		public void onSurfaceCreated( GL10 gl, EGLConfig config )
		{
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
			canvas.drawText( "PlayerName", 40, 50, textPaint );

			// int[] textures = new int[1];
			//
			// // Generate one texture pointer...
			// gl.glGenTextures( 1, textures, 0 );
			// // ...and bind it to our array
			// gl.glBindTexture( GL10.GL_TEXTURE_2D, textures[0] );
			//
			// // Create Nearest Filtered Texture
			// gl.glTexParameterf( GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST );
			// gl.glTexParameterf( GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR );
			//
			// // Different possible texture parameters, e.g. GL10.GL_CLAMP_TO_EDGE
			// gl.glTexParameterf( GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT );
			// gl.glTexParameterf( GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT );
			//
			// // Use the Android GLUtils to specify a two-dimensional texture
			// // image from our bitmap
			// GLUtils.texImage2D( GL10.GL_TEXTURE_2D, 0, bitmap, 0 );
			//
			// // Clean up
			// bitmap.recycle();
			
quad.loadBitmap( bitmap );


		}
	}
}
