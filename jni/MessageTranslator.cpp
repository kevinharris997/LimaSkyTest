#include "MessageTranslator.h"
#include "Messages.h"
#include "Log.h"

#include <jni.h>

static jclass message_handler_class;

void MessageTranslator::Init( JNIEnv* env, jobject obj )
{
	//LOGI( "MessageTranslator::init called!" );

	SetJVM( env );

	//
	// Get access to the related JAVA class...
	//

	message_handler_class = (jclass)env->NewGlobalRef( env->FindClass( "com/limasky/test/MessageHandler" ) );

	if( message_handler_class == 0 )
	{
		LOGE( "MessageTranslator::init FindClass failed for MessageTranslator!" );
		return;
	}

	//
	// Get access to the related methods...
	//

	// MessageHandler.loadSound
	obj_load_sound_ = env->NewGlobalRef(obj);

	method_load_sound_ = env->GetMethodID( message_handler_class, "loadSound", "(JLjava/lang/String;)V" );

	if( method_load_sound_ == 0 )
	{
		LOGE( "MessageTranslator::init GetMethodID failed for loadSound!" );
		return;
	}

	// MessageHandler.playSound

	obj_play_sound_ = env->NewGlobalRef(obj);

	method_play_sound_ = env->GetMethodID( message_handler_class, "playSound", "(JFI)V" );

	if( method_play_sound_ == 0 )
	{
		LOGE( "MessageTranslator::init GetMethodID failed for playSound!" );
		return;
	}

	// MessageHandler.generateScoremarker

	obj_generate_scoremarker_ = env->NewGlobalRef(obj);

	method_generate_scoremarker_ = env->GetMethodID( message_handler_class, "generateScoremarker", "(Ljava/lang/String;)I" );

	if( method_generate_scoremarker_ == 0 )
	{
		LOGE( "MessageTranslator::init GetMethodID failed for generateScoremarker!" );
		return;
	}
}

void MessageTranslator::Destroy( JNIEnv* env )
{
	//LOGI( "MessageTranslator::destroy called!" );

	env->DeleteGlobalRef( obj_load_sound_ );
	env->DeleteGlobalRef( obj_play_sound_ );
	env->DeleteGlobalRef( obj_generate_scoremarker_ );

	env->DeleteGlobalRef( message_handler_class );
}

int MessageTranslator::SendMessage( MessageIds msg_id, void* msg, int target, int sender )
{
	int returnCode = MSG_FAILURE;

	switch( msg_id )
	{
		case Msg_Load_Audio:
		{
			AudioLoadMsg* audioLoadMsg = static_cast<AudioLoadMsg*>(msg);

			returnCode = LoadSound( audioLoadMsg->file_name_, audioLoadMsg->hash_id_ );
		}
		break;

		case Msg_Play_Audio:
		{
			AudioPlayMsg* audioPlayMsg = static_cast<AudioPlayMsg*>(msg);
			returnCode = PlaySound( audioPlayMsg->hash_id_, audioPlayMsg->volume_, audioPlayMsg->loop_ );
		}
		break;

		case Msg_Generate_Scoremarker:
		{
			GenerateScoremarkerMsg* generateScoremarker = static_cast<GenerateScoremarkerMsg*>(msg);
			generateScoremarker->texture_id_ = GenerateScoremarker( generateScoremarker->player_name_ );

			if( generateScoremarker->texture_id_ != 0 )
				returnCode = MSG_SUCCESS;
		}
		break;

		default:
			LOGI( "MessageTranslator::sendMessage unknown msg_id passed!" );
			break;
	}

	return returnCode;
}

bool MessageTranslator::SetJVM( JNIEnv* env )
{
	if( jvm_ == 0 )
	{
		jint rs = env->GetJavaVM( &jvm_ );

		if( rs != JNI_OK )
		{
			LOGE( "MessageTranslator::setJVM GetJavaVM failed!" );
			return false;
		}
	}

	return true;
}

int MessageTranslator::LoadSound( const char* file_name, long hash_id )
{
	//LOGI( "MessageTranslator::loadSound called!" );

	if( !jvm_ )
	{
		LOGE( "MessageTranslator::loadSound JVM NULL!" );
		return MSG_FAILURE;
	}

	JNIEnv* env;
	jint rs = jvm_->AttachCurrentThread( &env, 0 );

	if( rs != JNI_OK )
	{
		LOGE( "MessageTranslator::loadSound AttachCurrentThread failed!" );
		return MSG_FAILURE;
	}

	jvalue jv1;
	jv1.j = hash_id;

	jstring jstr_file_name = env->NewStringUTF( file_name );

	env->CallVoidMethod( obj_load_sound_, method_load_sound_, jv1, jstr_file_name );

	env->DeleteLocalRef( jstr_file_name );

	return MSG_SUCCESS;
}

int MessageTranslator::PlaySound( long id, float volume, int loop )
{
	//LOGI( "MessageTranslator::playSound called!" );

	if( !jvm_ )
	{
		LOGE( "MessageTranslator::playSound JVM NULL!" );
		return MSG_FAILURE;
	}

	JNIEnv* env;
	jint rs = jvm_->AttachCurrentThread( &env, 0 );

	if( rs != JNI_OK )
	{
		LOGE( "MessageTranslator::playSound AttachCurrentThread failed!" );
		return MSG_FAILURE;
	}

	jvalue jv1;
	jv1.j = id;

// TODO: Why can't I pass this float the way the docs say to?!
	//jvalue jv2;
	//jv2.f = volume;

	env->CallVoidMethod( obj_play_sound_, method_play_sound_, jv1, volume, loop );

	return MSG_SUCCESS;
}

int MessageTranslator::GenerateScoremarker( const char* player_name )
{
	LOGI( "MessageTranslator::GenerateScoremarker called!" );

	if( !jvm_ )
	{
		LOGE( "MessageTranslator::GenerateScoremarker JVM NULL!" );
		return MSG_FAILURE;
	}

	JNIEnv* env;
	jint rs = jvm_->AttachCurrentThread( &env, 0 );

	if( rs != JNI_OK )
	{
		LOGE( "MessageTranslator::GenerateScoremarker AttachCurrentThread failed!" );
		return MSG_FAILURE;
	}

	jstring jstr_player_name = env->NewStringUTF( player_name );

	//jint paramInt = (jint) env->CallIntMethod( obj_generate_scoremarker_, method_generate_scoremarker_, jstr_player_name );
	jint paramInt = (jint) env->CallIntMethod( obj_generate_scoremarker_, method_generate_scoremarker_, jstr_player_name );

	env->DeleteLocalRef( jstr_player_name );

	return (int)paramInt;
}
