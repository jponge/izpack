#include <stdio.h>
#include <windows.h>

#include <jni.h>

LPTSTR FormatSystemErrorMessage(DWORD dw, LPTSTR lpszFunction) {

  LPVOID lpMsgBuf;
  LPVOID lpDisplayBuf;

  FormatMessage(FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM,
      NULL, dw, MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT), (LPTSTR)
          &lpMsgBuf, 0, NULL);

  lpDisplayBuf = LocalAlloc(LMEM_ZEROINIT, (strlen((const char *)lpMsgBuf)
      +strlen(lpszFunction)+40) *sizeof(TCHAR));

  wsprintf((char *)lpDisplayBuf, TEXT("%s returned with code %d: %s"),
      lpszFunction, dw, lpMsgBuf);

  LocalFree(lpMsgBuf);
  return (char *)lpDisplayBuf;

}

LPTSTR FormatLastSystemErrorMessage(LPTSTR lpszFunction) {
  return FormatSystemErrorMessage(GetLastError(), lpszFunction);
}

void ErrorPrint(LPTSTR lpszFunction) {
  DWORD dw = GetLastError();

  LPVOID lpMsgBuf;

  FormatMessage(FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM,
      NULL, dw, MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT), (LPTSTR)
          &lpMsgBuf, 0, NULL);

  printf("(C) %s returned with code %d: %s", lpszFunction, dw,
      (LPTSTR)lpMsgBuf);

  LocalFree(lpMsgBuf);
}

void ErrorMessageBox(LPTSTR lpszFunction) {
  LPVOID lpMsgBuf;
  LPVOID lpDisplayBuf;
  DWORD dw = GetLastError();

  FormatMessage(FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM,
      NULL, dw, MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT), (LPTSTR)
          &lpMsgBuf, 0, NULL);

  lpDisplayBuf = LocalAlloc(LMEM_ZEROINIT, (strlen((const char *)lpMsgBuf)
      +strlen(lpszFunction)+40)*sizeof(TCHAR));
  wsprintf((char *)lpDisplayBuf, TEXT("%s returned with code %d: %s"),
      lpszFunction, dw, lpMsgBuf);
  MessageBox(NULL, (const char *)lpDisplayBuf, TEXT("Error"), MB_OK);

  LocalFree(lpMsgBuf);
  LocalFree(lpDisplayBuf);
  //ExitProcess(dw);
}

void ThrowException(JNIEnv *env, LPTSTR type, LPTSTR message) {
  if (env->ExceptionOccurred())
    return;
  jclass newExcCls = env->FindClass(type);
  if (newExcCls)
    env->ThrowNew(newExcCls, message);
}

void ThrowExceptionLastError(JNIEnv *env, LPTSTR type, LPTSTR lpszFunction) {
  LPTSTR message = FormatLastSystemErrorMessage(lpszFunction);
  ThrowException(env, type, message);
  if (message != NULL)
    LocalFree(message);
}

void ThrowExceptionSystemError(JNIEnv *env, LPTSTR type, DWORD dw, LPTSTR lpszFunction) {
  LPTSTR message = FormatSystemErrorMessage(dw, lpszFunction);
  ThrowException(env, type, message);
  if (message != NULL)
    LocalFree(message);
}

void ThrowIOExceptionLastError(JNIEnv *env, LPTSTR lpszFunction) {
  ThrowExceptionLastError(env, "java/io/IOException", lpszFunction);
}

void ThrowIOExceptionSystemError(JNIEnv *env, DWORD dw, LPTSTR lpszFunction) {
  ThrowExceptionSystemError(env, "java/io/IOException", dw, lpszFunction);
}
