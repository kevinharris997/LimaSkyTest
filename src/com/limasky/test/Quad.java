package com.limasky.test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.opengl.GLUtils;

public class Quad
{
	//
	// (-1,1)   2--3  (1,1)
	//          |\ |
	//          | \|
	// (-1,-1)  0--1  (-1,1)
	//
	
	// Our quad's vertices.
	private float vertices[] =
	{
		-1.0f, -1.0f, 0.0f, 
		 1.0f, -1.0f, 0.0f, 
		-1.0f,  1.0f, 0.0f, 
		 1.0f,  1.0f, 0.0f
	};

	// The order, in which, we would like to connect our vertices.
	private short[] indices =
	{
		0, 1, 2, 1, 3, 2
	};

	// The colors mapped to the vertices.
	float[] colors =
	{
//		1.0f, 0.0f, 0.0f, 1.0f, // vertex 0 is red
//		0.0f, 1.0f, 0.0f, 1.0f, // vertex 1 is green
//		0.0f, 0.0f, 1.0f, 1.0f, // vertex 2 is blue
//		1.0f, 1.0f, 1.0f, 1.0f  // vertex 3 is white
			
		1.0f, 1.0f, 1.0f, 1.0f, // vertex 0 is white
		1.0f, 1.0f, 1.0f, 1.0f, // vertex 1 is white
		1.0f, 1.0f, 1.0f, 1.0f, // vertex 2 is white
		1.0f, 1.0f, 1.0f, 1.0f  // vertex 3 is white
	};

	// Texture coordinates for the vertices
	float textureCoordinates[] =
	{
		0.0f, 1.0f, 
		1.0f, 1.0f, 
		0.0f, 0.0f, 
		1.0f, 0.0f,
	};

	// Our vertex buffer.
	private FloatBuffer vertexBuffer;

	// Our index buffer.
	private ShortBuffer indexBuffer;

	// Our color buffer.
	private FloatBuffer colorBuffer;

	// Our UV texture buffer.
	private FloatBuffer textureBuffer;

	// Our texture id.
	private int textureId = -1;

	// The bitmap we want to load as a texture.
	private Bitmap bitmap;

	// Indicates if we need to load the texture.
	private boolean shouldLoadTexture = false;

	public Quad()
	{
		// A float is 4 bytes, therefore we multiply the number of vertices with 4.
		ByteBuffer vbb = ByteBuffer.allocateDirect( vertices.length * 4 );
		vbb.order( ByteOrder.nativeOrder() );
		vertexBuffer = vbb.asFloatBuffer();
		vertexBuffer.put( vertices );
		vertexBuffer.position( 0 );

		// A short is 2 bytes, therefore we multiply the number of vertices with 2.
		ByteBuffer ibb = ByteBuffer.allocateDirect( indices.length * 2 );
		ibb.order( ByteOrder.nativeOrder() );
		indexBuffer = ibb.asShortBuffer();
		indexBuffer.put( indices );
		indexBuffer.position( 0 );

		// A float has 4 bytes, colors (RGBA) * 4 bytes
		ByteBuffer cbb = ByteBuffer.allocateDirect( colors.length * 4 );
		cbb.order( ByteOrder.nativeOrder() );
		colorBuffer = cbb.asFloatBuffer();
		colorBuffer.put( colors );
		colorBuffer.position( 0 );

		// A float has 4 bytes, therefore we multiply the number of vertices with 4.
		ByteBuffer tbb = ByteBuffer.allocateDirect( textureCoordinates.length * 4 );
		tbb.order( ByteOrder.nativeOrder() );
		textureBuffer = tbb.asFloatBuffer();
		textureBuffer.put( textureCoordinates );
		textureBuffer.position( 0 );
	}

	/**
	 * This function draws our quad on screen.
	 * 
	 * @param gl
	 */
	public void draw( GL10 gl )
	{
		// Counter-clockwise winding.
		gl.glFrontFace( GL10.GL_CCW );
		// Enable face culling.
		gl.glEnable( GL10.GL_CULL_FACE );
		// What faces to remove with the face culling.
		gl.glCullFace( GL10.GL_BACK );

		// Enable vertex arrays and set the location and data format of our array of vertices.
		gl.glEnableClientState( GL10.GL_VERTEX_ARRAY );
		gl.glVertexPointer( 3, GL10.GL_FLOAT, 0, vertexBuffer );

		// Enable color arrays and set the location and data format of our array of colors.
		gl.glEnableClientState( GL10.GL_COLOR_ARRAY );
		gl.glColorPointer( 4, GL10.GL_FLOAT, 0, colorBuffer );

		if( shouldLoadTexture )
		{
			loadGLTexture( gl );
			shouldLoadTexture = false;
		}

		if( textureId != -1 && textureBuffer != null )
		{
			gl.glEnable( GL10.GL_TEXTURE_2D );

			// Enable texture coord arrays and set the location and data format of our array of texture coords.
			gl.glEnableClientState( GL10.GL_TEXTURE_COORD_ARRAY );
			gl.glTexCoordPointer( 2, GL10.GL_FLOAT, 0, textureBuffer );
			
			gl.glBindTexture( GL10.GL_TEXTURE_2D, textureId );
		}

		// Draw our quad as 2 triangles.
		gl.glDrawElements( GL10.GL_TRIANGLES, indices.length, GL10.GL_UNSIGNED_SHORT, indexBuffer );

		// Disable the vertices and color buffers.
		gl.glDisableClientState( GL10.GL_VERTEX_ARRAY );
		gl.glDisableClientState( GL10.GL_COLOR_ARRAY );
		
		// Disable face culling.
		gl.glDisable( GL10.GL_CULL_FACE );

		if( textureId != -1 && textureBuffer != null )
		{
			gl.glDisable( GL10.GL_TEXTURE_2D );
			
			// Disable the texture coords buffer.
			gl.glDisableClientState( GL10.GL_TEXTURE_COORD_ARRAY );
		}
	}

	/**
	 * Set the bitmap to load into a texture.
	 * 
	 * @param bitmap
	 */
	public void loadBitmap( Bitmap bitmap )
	{
		this.bitmap = bitmap;
		shouldLoadTexture = true;
	}

	/**
	 * Loads the texture.
	 * 
	 * @param gl
	 */
	private void loadGLTexture( GL10 gl )
	{
		// Generate one texture ID.
		int[] textures = new int[1];
		gl.glGenTextures( 1, textures, 0 );
		textureId = textures[0];

		// Now, bind it to our array
		gl.glBindTexture( GL10.GL_TEXTURE_2D, textureId );

		// Create a Linear Filtered Texture.
		gl.glTexParameterf( GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR );
		gl.glTexParameterf( GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR );

		// Set texture parameters to GL_CLAMP_TO_EDGE.
		gl.glTexParameterf( GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE );
		gl.glTexParameterf( GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE );

		// Use the Android GLUtils to specify a two-dimensional texture image from our bitmap
		GLUtils.texImage2D( GL10.GL_TEXTURE_2D, 0, bitmap, 0 );
		
// TODO: Double check this!
bitmap.recycle();
		
	}
}
