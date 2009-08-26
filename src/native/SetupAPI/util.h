#include <windows.h>
#include <jni.h>

LPTSTR FormatSystemErrorMessage(DWORD dw, LPTSTR lpszFunction);
LPTSTR FormatLastSystemErrorMessage(LPTSTR lpszFunction);

void ErrorMessageBox(LPTSTR lpszFunction);
void ErrorPrint(LPTSTR lpszFunction);

void ThrowException(JNIEnv *env, LPTSTR type, LPTSTR message);
void ThrowExceptionLastError(JNIEnv *env, LPTSTR type, LPTSTR lpszFunction);
void ThrowExceptionSystemError(JNIEnv *env, LPTSTR type, DWORD dw, LPTSTR lpszFunction);
void ThrowIOExceptionLastError(JNIEnv *env, LPTSTR lpszFunction);
void ThrowIOExceptionSystemError(JNIEnv *env, DWORD dw, LPTSTR lpszFunction);
