#ifndef _MESSAGES_H_
#define _MESSAGES_H_

#include <string.h>

#define MSG_SUCCESS 1
#define MSG_FAILURE 0

enum MessageIds
{
	Msg_Load_Audio,
	Msg_Play_Audio
};

// Based on the djb2 algorithm found here:
// http://www.cse.yorku.ca/~oz/hash.html
static unsigned long hash( unsigned char *str )
{
	unsigned long hash = 5381;
	int c;

	while ( (c = *str++) )
		hash = ((hash << 5) + hash) + c; /* hash * 33 + c */

	return hash;
}

struct AudioLoadMsg
{
    AudioLoadMsg( const char* file_name ) :
    	file_name_( file_name )
    {
// TODO: How long of a string can we hash with this?
		unsigned char unsigned_str[100];
		strcpy( (char*)unsigned_str, file_name_ );

		hash_id_ = hash( unsigned_str );
    }

    const char* file_name_;
    long hash_id_;
};

struct AudioPlayMsg
{
	AudioPlayMsg( long hash_id, float volume, int loop ) :
		hash_id_( hash_id ),
		volume_( volume ),
		loop_( loop )
	{}

    long hash_id_;
    float volume_;
    int loop_;
};

#endif // _MESSAGES_H_
