#include "com_limasky_test_MessageHandler.h"
#include <jni.h>

#include "MessageTranslator.h"

JNIEXPORT void JNICALL Java_com_limasky_test_MessageHandler_initNative(JNIEnv *env, jobject obj)
{
	MessageTranslator::GetInstance().Init( env, obj );
}

JNIEXPORT void JNICALL Java_com_limasky_test_MessageHandler_destroyNative(JNIEnv *env, jobject)
{
	MessageTranslator::GetInstance().Destroy( env );
}
