#ifndef _MessageTranslator_H_
#define _MessageTranslator_H_

#include <jni.h>
#include "Messages.h"

// This is the native-side singleton for processing messages in an Android
// specific manner. The basic goal is to have certain tasks such as playing
// audio and networking done in Java instead of C++ where the tasks can be
// executed in a safer more generic platform way.
class MessageTranslator
{
public:

	static MessageTranslator& GetInstance()
	{
		static MessageTranslator instance; // Guaranteed to be destroyed.

		return instance;
	}

	int SendMessage( MessageIds msg_id, void* msg, int target = 0, int sender = 0 );

	void Init( JNIEnv* env, jobject obj );
	void Destroy( JNIEnv* env );

private:

	MessageTranslator() {};

	// Don't forget to declare these two. You want to make sure they
	// are unaccessible otherwise you may accidently get copies of
	// your singleton appearing.
	MessageTranslator(MessageTranslator const&); // Don't Implement
	void operator=(MessageTranslator const&); // Don't implement

	bool SetJVM( JNIEnv* env );

	//
	// Audio...
	//

	int LoadSound( const char* file_name, long hash_id );
	int PlaySound( long id, float volume, int loop );

	JavaVM* jvm_;

	jobject obj_load_sound_;
	jmethodID method_load_sound_;

	jobject obj_play_sound_;
	jmethodID method_play_sound_;

	jobject obj_generate_scoremarker_;
	jmethodID method_generate_scoremarker_;

	//
	// Score Marker Texture Generation...
	//

	int GenerateScoremarker( const char* player_name );
};

#endif // _MessageTranslator_H_
