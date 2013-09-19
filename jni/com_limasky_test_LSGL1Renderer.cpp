#include "com_limasky_test_LSGL1Renderer.h"
#include <jni.h>
#include "Log.h"

#include <GLES/gl.h>

#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <time.h>

#include "matrix4x4f.h"
#include "vector3f.h"

#include "MessageTranslator.h"
#include "Messages.h"

#define BUFFER_OFFSET(i) ((void*)(i))

long jumponmonsterSound = 0;
long chillSound = 0;
long collectSound = 0;
long jetpack5Sound = 0;
long snowballthrowSound = 0;

double elapsedTimeSinceLastAudio = 0.0;
double elapsedTimeSinceLastJetPackAudio = 0.0;
double elapsedTimeSinceLastThrowAudio = 0.0;
int soundToPlay = 0;

GLuint g_program;
GLuint g_a_positionHandle;
GLuint g_a_colorHandle;
GLuint g_u_mvpMatrixHandle;

double g_nowTime, g_prevTime;

const GLfloat g_vertices[] =
{
	-1.0f, -1.0f, 0.0f,
	 1.0f, -1.0f, 0.0f,
	-1.0f,  1.0f, 0.0f,
	 1.0f,  1.0f, 0.0f
};

#define NUMBER_OF_VERTICES 4
#define NUMBER_OF_COMPONENTS_PER_VERTEX 3

const GLfloat g_colors[] =
{
	1.0f, 1.0f, 1.0f, 1.0f,
	1.0f, 1.0f, 1.0f, 1.0f,
	1.0f, 1.0f, 1.0f, 1.0f,
	1.0f, 1.0f, 1.0f, 1.0f
};

#define NUMBER_OF_COLORS 4
#define NUMBER_OF_COMPONENTS_PER_COLOR 4

float g_textureCoordinates[] =
{
	0.0f, 1.0f,
	1.0f, 1.0f,
	0.0f, 0.0f,
	1.0f, 0.0f,
};

#define NUMBER_OF_TEXCOORDS 4
#define NUMBER_OF_COMPONENTS_TEXCOORDS 2

GLushort g_indices[] =
{
	0, 1, 2, 1, 3, 2
};

const GLsizeiptr vertex_size = NUMBER_OF_VERTICES*NUMBER_OF_COMPONENTS_PER_VERTEX*sizeof(GLfloat);
const GLsizeiptr color_size = NUMBER_OF_COLORS*NUMBER_OF_COMPONENTS_PER_COLOR*sizeof(GLfloat);
const GLsizeiptr texcoord_size = NUMBER_OF_TEXCOORDS*NUMBER_OF_COMPONENTS_TEXCOORDS*sizeof(GLfloat);

GLuint g_triangleVBO;
GLuint g_triangleIBO;


static void printGLString( const char *name, GLenum s )
{
	const char *v = (const char *) glGetString( s );
	LOGI( "GL %s = %s\n", name, v );
}

static double getCurrentTimeInSeconds()
{
   timespec lTimeVal;
   clock_gettime( CLOCK_MONOTONIC, &lTimeVal );
   return lTimeVal.tv_sec + (lTimeVal.tv_nsec * 1.0e-9);
}

static void checkGlError( const char* op )
{
	for( GLint error = glGetError(); error; error = glGetError() )
	{
		LOGI( "after %s() glError (0x%x)\n", op, error );
	}
}

int SendMessage( MessageIds msgId, void* msg, int target, int sender )
{
	return MessageTranslator::GetInstance().SendMessage( msgId, msg );
}

bool setupSounds()
{
	{
		AudioLoadMsg msg( "jumponmonster" );

		if( SendMessage( Msg_Load_Audio, &msg, 0, 0 ) )
		{
			jumponmonsterSound = msg.hash_id_;
		}
	}

	{
		AudioLoadMsg msg( "chill" );

		if( SendMessage( Msg_Load_Audio, &msg, 0, 0 ) )
		{
			chillSound = msg.hash_id_;
		}
	}

	{
		AudioLoadMsg msg( "collect" );

		if( SendMessage( Msg_Load_Audio, &msg, 0, 0 ) )
		{
			collectSound = msg.hash_id_;
		}
	}

	{
		AudioLoadMsg msg( "jetpack5" );

		if( SendMessage( Msg_Load_Audio, &msg, 0, 0 ) )
		{
			jetpack5Sound = msg.hash_id_;
		}
	}

	{
		AudioLoadMsg msg( "snowballthrow" );

		if( SendMessage( Msg_Load_Audio, &msg, 0, 0 ) )
		{
			snowballthrowSound = msg.hash_id_;
		}
	}

	return true;
}

bool setupGraphics( int w, int h )
{
	LOGI( "setupGraphics(%d, %d)", w, h );

	printGLString( "Version", GL_VERSION );
	printGLString( "Vendor", GL_VENDOR );
	printGLString( "Renderer", GL_RENDERER );
	printGLString( "Extensions", GL_EXTENSIONS );

	glGenBuffers( 1, &g_triangleVBO );
	glBindBuffer( GL_ARRAY_BUFFER, g_triangleVBO );
	glBufferData( GL_ARRAY_BUFFER, vertex_size + color_size + texcoord_size, 0, GL_STATIC_DRAW );
	glBufferSubData( GL_ARRAY_BUFFER, 0, vertex_size, g_vertices ); // Start at index 0, to length of vertex_size.
	glBufferSubData( GL_ARRAY_BUFFER, vertex_size, color_size, g_colors ); // Append color data to vertex data.
	glBufferSubData( GL_ARRAY_BUFFER, vertex_size + color_size, texcoord_size, g_textureCoordinates ); // Append texcoord data to vertex+color data.

	glGenBuffers( 1, &g_triangleIBO );
	glBindBuffer( GL_ELEMENT_ARRAY_BUFFER, g_triangleIBO );
	glBufferData( GL_ELEMENT_ARRAY_BUFFER, sizeof(GLushort)*6, g_indices, GL_STATIC_DRAW );

	glViewport( 0, 0, w, h );
	checkGlError( "glViewport" );

	g_nowTime = getCurrentTimeInSeconds();
	g_prevTime = g_nowTime;

	return true;
}

void renderFrame()
{
	g_nowTime = getCurrentTimeInSeconds();
	double elapsed = g_nowTime - g_prevTime;




//	// Crappy test code!
//
//	// Play a long sound that other sounds must play simultaneous with.
//	elapsedTimeSinceLastJetPackAudio += elapsed;
//
//	if( elapsedTimeSinceLastJetPackAudio > 5.0 )
//	{
//		AudioPlayMsg msg( jetpack5Sound, 0.5f, 0 );
//		SendMessage( Msg_Play_Audio, &msg, 0, 0 );
//
//		elapsedTimeSinceLastJetPackAudio = 0;
//	}
//
//	// Play a very short sound that plays very often!
//	elapsedTimeSinceLastThrowAudio += elapsed;
//
//	if( elapsedTimeSinceLastThrowAudio > 0.5 )
//	{
//		AudioPlayMsg msg( snowballthrowSound, 1.0f, 0 );
//		SendMessage( Msg_Play_Audio, &msg, 0, 0 );
//
//		elapsedTimeSinceLastThrowAudio = 0;
//	}
//
//	// Play a few other sounds on top of the long and short and long sound.
//	elapsedTimeSinceLastAudio += elapsed;
//
//	if( elapsedTimeSinceLastAudio > 2.0 )
//	{
//		if( soundToPlay == 0 && jumponmonsterSound != 0 )
//		{
//			AudioPlayMsg msg( jumponmonsterSound, 1.0f, 0 );
//			SendMessage( Msg_Play_Audio, &msg, 0, 0 );
//		}
//		else if( soundToPlay == 1 && chillSound != 0 )
//		{
//			AudioPlayMsg msg( chillSound, 1.0f, 0 );
//			SendMessage( Msg_Play_Audio, &msg, 0, 0 );
//		}
//		else if( soundToPlay == 2 && collectSound != 0 )
//		{
//			AudioPlayMsg msg( collectSound, 1.0f, 0 );
//			SendMessage( Msg_Play_Audio, &msg, 0, 0 );
//		}
//
//		++soundToPlay;
//
//		if( soundToPlay > 2 )
//			soundToPlay = 0;
//
//		elapsedTimeSinceLastAudio = 0;
//	}





//	glClearColor( 0.0f, 0.0f, 0.0f, 1.0f );
//	checkGlError( "glClearColor" );
//	glClear( GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT );
//	checkGlError( "glClear" );

	static float rotationY = 0.0f;
	rotationY += (elapsed * 50.0f);

	if( rotationY > 360.0f )
		rotationY = 0.0f;

	glMatrixMode( GL_MODELVIEW );
    glLoadIdentity();
    glTranslatef( 0, 0, -4 );
    glRotatef( rotationY, 0.0f, 1.0f, 0.0f );


	glEnable( GL_TEXTURE_2D );

	glBindBuffer( GL_ARRAY_BUFFER, g_triangleVBO );

	glEnableClientState( GL_VERTEX_ARRAY );
	glEnableClientState( GL_COLOR_ARRAY );
	glEnableClientState( GL_TEXTURE_COORD_ARRAY );

	glVertexPointer( 3, GL_FLOAT, 0, (GLvoid*)((char*)NULL) );
	glColorPointer( 4, GL_FLOAT, 0, (GLvoid*)((char*)NULL+vertex_size) );
	glTexCoordPointer( 2, GL_FLOAT, 0, (GLvoid*)((char*)NULL+vertex_size+color_size) );

	glBindBuffer( GL_ELEMENT_ARRAY_BUFFER, g_triangleIBO );
	glDrawElements( GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, BUFFER_OFFSET(0) );

    glDisableClientState( GL_VERTEX_ARRAY );
    glDisableClientState( GL_COLOR_ARRAY );
    glDisableClientState( GL_TEXTURE_COORD_ARRAY );
    glBindBuffer( GL_ARRAY_BUFFER, 0 );
    glBindBuffer( GL_ELEMENT_ARRAY_BUFFER, 0 );

	g_prevTime = g_nowTime;
}

JNIEXPORT void JNICALL Java_com_limasky_test_LSGL1Renderer_init(JNIEnv *env, jclass jcls, jint width, jint height)
{
	setupSounds();
	setupGraphics( width, height );
}

JNIEXPORT void JNICALL Java_com_limasky_test_LSGL1Renderer_step(JNIEnv *, jclass)
{
	// For this sample, the Java code drives the native rendering.
	renderFrame();
}
