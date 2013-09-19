To assist my ADT builds, I put this in my .bash_profile script and launch eclipse from a bash shell:

# Android NDK Dev...
export ADT_PATH=/Applications/adt-bundle-mac-x86_64-20130729
export ANDROID_SDK=$ADT_PATH/sdk
export ANDROID_NDK=/Users/kharris/Android/android-ndk-r9
export PATH=$ADT_PATH/eclipse/Eclipse.app/Contents/MacOS:$ANDROID_SDK/tools:$ANDROID_SDK/platform-tools:$ANDROID_NDK:$PATH

TEST