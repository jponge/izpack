#include <windows.h>
#include <setupapi.h>

#include <jni.h>

#include "util.h"
#include "com_izforge_izpack_util_os_WinSetupAPIBase.h"

jobject g_jobj = NULL;
JavaVM *g_jvm;

// ----------------------------- Helper functions ----------------------------

UINT WINAPI MyQueueCallbackJava (
    PVOID pDefaultContext,
    UINT Notification,
    UINT Param1,
    UINT Param2)
{

  JNIEnv *lpEnv;

  jclass cls;
  jmethodID jmID;
  jstring js1, js2, js3, js4;
  jint win32err;
  UINT ret;
  INT lzRet = LZERROR_BADINHANDLE;

  // Attach thread to JVM
  g_jvm->AttachCurrentThread((void**)&lpEnv, NULL);

  cls = lpEnv->GetObjectClass(g_jobj);

  switch (Notification)
  {
    case SPFILENOTIFY_COPYERROR :
#ifdef DEBUG
    printf("(C) SPFILENOTIFY_COPYERROR callback...\n");
#endif
    jmID = lpEnv->GetMethodID(cls,
        "handleCopyError",
        "(Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;)I");
    js1 = ((PFILEPATHS)Param1)->Source==NULL?NULL:lpEnv->NewStringUTF(((PFILEPATHS)Param1)->Source);
    js2 = ((PFILEPATHS)Param1)->Target==NULL?NULL:lpEnv->NewStringUTF(((PFILEPATHS)Param1)->Target);
    win32err = ((PFILEPATHS)Param1)->Win32Error;
    js3 = lpEnv->NewStringUTF(FormatSystemErrorMessage(((PFILEPATHS)Param1)->Win32Error, "SetupQueueCopy"));
    ret = lpEnv->CallIntMethod(g_jobj, jmID, js1, js2, win32err, js3);
    break;
    case SPFILENOTIFY_DELETEERROR :
#ifdef DEBUG
    printf("(C) SPFILENOTIFY_DELETEERROR callback...\n");
#endif
    jmID = lpEnv->GetMethodID(cls,
        "handleDeleteError",
        "(Ljava/lang/String;ILjava/lang/String;)I");
    js1 = ((PFILEPATHS)Param1)->Target==NULL?NULL:lpEnv->NewStringUTF(((PFILEPATHS)Param1)->Target);
    win32err = ((PFILEPATHS)Param1)->Win32Error;
    js3 = lpEnv->NewStringUTF(FormatSystemErrorMessage(((PFILEPATHS)Param1)->Win32Error, "SetupQueueDelete"));
    ret = lpEnv->CallIntMethod(g_jobj, jmID, js1, win32err, js3);
    break;
    case SPFILENOTIFY_RENAMEERROR :
#ifdef DEBUG
    printf("(C) SPFILENOTIFY_RENAMEERROR callback...\n");
#endif
    jmID = lpEnv->GetMethodID(cls,
        "handleRenameError",
        "(Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;)I");
    js1 = ((PFILEPATHS)Param1)->Source==NULL?NULL:lpEnv->NewStringUTF(((PFILEPATHS)Param1)->Source);
    js2 = ((PFILEPATHS)Param1)->Target==NULL?NULL:lpEnv->NewStringUTF(((PFILEPATHS)Param1)->Target);
    win32err = ((PFILEPATHS)Param1)->Win32Error;
    js3 = lpEnv->NewStringUTF(FormatSystemErrorMessage(((PFILEPATHS)Param1)->Win32Error, "SetupQueueRename"));
    ret = lpEnv->CallIntMethod(g_jobj, jmID, js1, js2, win32err, js3);
    break;
    case SPFILENOTIFY_NEEDMEDIA :
    OFSTRUCT ReOpenBuf;

#ifdef DEBUG
    printf("(C) SPFILENOTIFY_NEEDMEDIA callback...\n");
#endif
    Param2 = (UINT)LocalAlloc(LMEM_ZEROINIT,
        (strlen(((PSOURCE_MEDIA)Param1)->SourcePath)
            +strlen(((PSOURCE_MEDIA)Param1)->SourceFile)
            +2)*sizeof(TCHAR));

    lstrcpy( (LPTSTR)Param2, ((PSOURCE_MEDIA)Param1)->SourcePath );
    lstrcat( (LPTSTR)Param2, "\\" );
    lstrcat( (LPTSTR)Param2, ((PSOURCE_MEDIA)Param1)->SourceFile );

#ifdef DEBUG
    printf("(C) SPFILENOTIFY_NEEDMEDIA: Checking for source file %s\n",
        (LPTSTR)Param2);
#endif

    lzRet = LZOpenFile(
        (LPTSTR)Param2,
        &ReOpenBuf,
        OF_EXIST | OF_READ
    );

    if ((lzRet != LZERROR_BADINHANDLE) && (lzRet != LZERROR_GLOBALLOC))
    {
#ifdef DEBUG
      printf("(C) SPFILENOTIFY_NEEDMEDIA: Found %s\n", Param2);
#endif
      //ret = FILEOP_SKIP;
      ret = SetupDefaultQueueCallback(pDefaultContext, Notification, Param1, Param2);
    }
    else
    {
#ifdef DEBUG
      ErrorPrint("LZOpenFile");
#endif

#ifdef DEBUG
      printf("(C) SPFILENOTIFY_NEEDMEDIA: callback to Java (%s)...\n",
          ((PSOURCE_MEDIA)Param1)->SourceFile);
#endif

      jmID = lpEnv->GetMethodID(cls,
          "handleNeedMedia",
          "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)I");

      js1 = ((PSOURCE_MEDIA)Param1)->Tagfile==NULL?NULL:lpEnv->NewStringUTF(((PSOURCE_MEDIA)Param1)->Tagfile);
      js2 = ((PSOURCE_MEDIA)Param1)->Description==NULL?NULL:lpEnv->NewStringUTF(((PSOURCE_MEDIA)Param1)->Description);
      js3 = ((PSOURCE_MEDIA)Param1)->SourcePath==NULL?NULL:lpEnv->NewStringUTF(((PSOURCE_MEDIA)Param1)->SourcePath);
      js4 = ((PSOURCE_MEDIA)Param1)->SourceFile==NULL?NULL:lpEnv->NewStringUTF(((PSOURCE_MEDIA)Param1)->SourceFile);

      ret = lpEnv->CallIntMethod(g_jobj, jmID, js1, js2, js3, js4);
    }

    LocalFree((LPVOID)Param2);
    break;
    default :
#ifdef DEBUG
    printf("(C) Unhandled notification %x, forwarding to standard handler\n", Notification);
#endif
    // Pass all other notifications through without modification
    ret = SetupDefaultQueueCallback(pDefaultContext, Notification, Param1, Param2);
  }

#ifdef DEBUG
  printf("(C) Callback handler returns code %d\n", ret);
#endif
  return ret;
}

UINT WINAPI MyQueueCallback (
    PVOID pDefaultContext,
    UINT Notification,
    UINT Param1,
    UINT Param2)
{
  switch (Notification)
  {
    case SPFILENOTIFY_COPYERROR :
#ifdef DEBUG
    printf("SPFILENOTIFY_COPYERROR: A copy error occured, aborting.\n");
#endif
    return FILEOP_ABORT;
    case SPFILENOTIFY_DELETEERROR :
    // Skip any file delete errors
#ifdef DEBUG
    printf("SPFILENOTIFY_DELETEERROR: A delete error occured, skipping file.\n");
#endif
    return FILEOP_SKIP;
    case SPFILENOTIFY_RENAMEERROR :
#ifdef DEBUG
    printf("SPFILENOTIFY_RENAMEERROR: A rename error occured, aborting.\n");
#endif
    return FILEOP_ABORT;
    case SPFILENOTIFY_NEEDMEDIA :
#ifdef DEBUG
    printf("SPFILENOTIFY_NEEDMEDIA: Error with the source file %s, aborting.\n",
        ((PSOURCE_MEDIA)Param1)->SourceFile);
#endif
    return FILEOP_ABORT;
    default :
    // Pass all other notifications through without modification
    return SetupDefaultQueueCallback(pDefaultContext, Notification, Param1, Param2);
  }
}

// ------------------- Implementation of native functions --------------------

JNIEXPORT jint JNICALL Java_com_izforge_izpack_util_os_WinSetupAPIBase_SetupOpenFileQueue
(JNIEnv *env, jobject out, jobject msghandler)
{
  HSPFILEQ FileQueue;

  if (msghandler!=NULL)
  // Make object a Global Reference
  g_jobj = env->NewGlobalRef(msghandler);

  // Get Java VM
  env->GetJavaVM(&g_jvm);

#ifdef DEBUG
  printf("(C) Opening new file queue...\n");
#endif
  FileQueue = SetupOpenFileQueue();

  if(!FileQueue || (FileQueue == INVALID_HANDLE_VALUE))
  {
#ifdef DEBUG
    ErrorPrint("SetupOpenFileQueue");
#endif
    ThrowIOExceptionLastError(env, "SetupOpenFileQueue");
  }

#ifdef DEBUG
  printf("(C) Opened file queue (%x).\n", FileQueue);
#endif
  return (jint)FileQueue;
};

JNIEXPORT void JNICALL Java_com_izforge_izpack_util_os_WinSetupAPIBase_SetupCloseFileQueue
(JNIEnv *env, jobject out, jint queuehandle)
{
#ifdef DEBUG
  printf("(C) Closing file queue (%x)...\n", queuehandle);
#endif
  SetupCloseFileQueue((HSPFILEQ)queuehandle);

  if (g_jobj!=NULL)
  env->DeleteGlobalRef(g_jobj);

  return;
};

JNIEXPORT void JNICALL Java_com_izforge_izpack_util_os_WinSetupAPIBase_SetupQueueCopy
(JNIEnv *env, jobject out, jint queuehandle,
    jstring sourcerootpath, jstring sourcepath, jstring sourcefilename,
    jstring sourcedescription, jstring sourcetagfile,
    jstring targetdirectory, jstring targetfilename, jint copystyle)
{
  BOOL result;

  const char *str_sourcerootpath = sourcerootpath==NULL?NULL:env->GetStringUTFChars(sourcerootpath, 0);
  const char *str_sourcepath = sourcepath==NULL?NULL:env->GetStringUTFChars(sourcepath, 0);
  const char *str_sourcefilename = sourcefilename==NULL?NULL:env->GetStringUTFChars(sourcefilename, 0);
  const char *str_sourcedescription = sourcedescription==NULL?NULL:env->GetStringUTFChars(sourcedescription, 0);
  const char *str_sourcetagfile = sourcetagfile==NULL?NULL:env->GetStringUTFChars(sourcetagfile, 0);
  const char *str_targetdirectory = targetdirectory==NULL?NULL:env->GetStringUTFChars(targetdirectory, 0);
  const char *str_targetfilename = targetfilename==NULL?NULL:env->GetStringUTFChars(targetfilename, 0);

  result = SetupQueueCopy((HSPFILEQ)queuehandle,
      str_sourcerootpath, str_sourcepath, str_sourcefilename,
      str_sourcedescription, str_sourcetagfile,
      str_targetdirectory, str_targetfilename,
      (DWORD)copystyle);

  DWORD dw = GetLastError();

  env->ReleaseStringUTFChars(sourcerootpath, str_sourcerootpath);
  env->ReleaseStringUTFChars(sourcepath, str_sourcepath);
  env->ReleaseStringUTFChars(sourcefilename, str_sourcefilename);
  env->ReleaseStringUTFChars(sourcedescription, str_sourcedescription);
  env->ReleaseStringUTFChars(sourcetagfile, str_sourcetagfile);
  env->ReleaseStringUTFChars(targetdirectory, str_targetdirectory);
  env->ReleaseStringUTFChars(targetfilename, str_targetfilename);

  if(!result)
  {
#ifdef DEBUG
    ErrorPrint("SetupQueueCopy");
#endif
    ThrowIOExceptionSystemError(env, dw, "SetupQueueCopy");
  }

};

JNIEXPORT void JNICALL Java_com_izforge_izpack_util_os_WinSetupAPIBase_SetupQueueDelete
(JNIEnv *env, jobject out, jint queuehandle, jstring pathpart1, jstring pathpart2)
{
  BOOL result;

  const char *str1 = pathpart1==NULL?NULL:env->GetStringUTFChars(pathpart1, 0);
  const char *str2 = pathpart2==NULL?NULL:env->GetStringUTFChars(pathpart2, 0);

  result = SetupQueueDelete((HSPFILEQ)queuehandle, str1, str2);

  DWORD dw = GetLastError();

  env->ReleaseStringUTFChars(pathpart1, str1);
  env->ReleaseStringUTFChars(pathpart2, str2);

  if(!result)
  {
#ifdef DEBUG
    ErrorPrint("SetupQueueDelete");
#endif
    ThrowIOExceptionSystemError(env, dw, "SetupQueueDelete");
  }
};

JNIEXPORT void JNICALL Java_com_izforge_izpack_util_os_WinSetupAPIBase_SetupQueueRename
(JNIEnv *env, jobject out, jint queuehandle,
    jstring sourcepath, jstring sourcefilename,
    jstring targetpath, jstring targetfilename)
{
  BOOL result;

  const char *str_sourcepath = sourcepath==NULL?NULL:env->GetStringUTFChars(sourcepath, 0);
  const char *str_sourcefilename = sourcefilename==NULL?NULL:env->GetStringUTFChars(sourcefilename, 0);
  const char *str_targetpath = targetpath==NULL?NULL:env->GetStringUTFChars(targetpath, 0);
  const char *str_targetfilename = targetfilename==NULL?NULL:env->GetStringUTFChars(targetfilename, 0);

  result = SetupQueueRename((HSPFILEQ)queuehandle,
      str_sourcepath, str_sourcefilename,
      str_targetpath, str_targetfilename);

  DWORD dw = GetLastError();

  env->ReleaseStringUTFChars(sourcepath, str_sourcepath);
  env->ReleaseStringUTFChars(sourcefilename, str_sourcefilename);
  env->ReleaseStringUTFChars(targetpath, str_targetpath);
  env->ReleaseStringUTFChars(targetfilename, str_targetfilename);

  if(!result)
  {
#ifdef DEBUG
    ErrorPrint("SetupQueueRename");
#endif
    ThrowIOExceptionSystemError(env, dw, "SetupQueueRename");
  }
};

JNIEXPORT jboolean JNICALL Java_com_izforge_izpack_util_os_WinSetupAPIBase_SetupCommitFileQueue
(JNIEnv *env, jobject out, jint queuehandle)
{
  PVOID lpCallbackContext;
  PSP_FILE_CALLBACK msghandler;
  BOOL result;

  lpCallbackContext = SetupInitDefaultQueueCallback( NULL );

#ifdef DEBUG
  printf("(C) Comitting file queue (%x)...\n", queuehandle);
#endif

  if (g_jobj!=NULL)
  {
    SetupDefaultQueueCallback(lpCallbackContext, 0, 0, 0);
    msghandler = (PSP_FILE_CALLBACK)MyQueueCallbackJava;
  }
  else
  {
    msghandler = SetupDefaultQueueCallback;
  }

  result = SetupCommitFileQueue(NULL,
      (HSPFILEQ)queuehandle,
      (PSP_FILE_CALLBACK)msghandler,
      (PVOID)lpCallbackContext);

  if(!result)
  {
#ifdef DEBUG
    ErrorPrint("SetupCommitFileQueue");
#endif
    ThrowIOExceptionLastError(env, "SetupCommitFileQueue");
  }
  /*
  else
  {
  INT reboot = SetupPromptReboot( (HSPFILEQ)queuehandle, NULL, TRUE);
  printf("(C) SetupPromptReboot returned %d\n", reboot);
  if ((reboot & SPFILEQ_FILE_IN_USE) != 0) {
  printf("(Java) At least one file was in use during the queue commit process and there are delayed file operations pending.");
  //printf("(Java) Rebooting...");
  //InitiateSystemShutdown(null, this.getClass().toString(), 30, true, true);
  }
  if ((reboot & SPFILEQ_REBOOT_RECOMMENDED) != 0) {
  printf("(Java) The system should be rebooted.");
  //printf("(Java) Rebooting...");
  //InitiateSystemShutdown(null, this.getClass().toString(), 30, true, true);
  }
  if ((reboot & SPFILEQ_REBOOT_IN_PROGRESS) != 0)
  printf("(Java) System shutdown is already in progress.");
  }
  */

  SetupTermDefaultQueueCallback( lpCallbackContext );

  return result;
};

JNIEXPORT jint JNICALL Java_com_izforge_izpack_util_os_WinSetupAPIBase_SetupPromptReboot
(JNIEnv *env, jobject out, jint queuehandle, jboolean scanonly)
{
  INT result;

  result = SetupPromptReboot((HSPFILEQ)queuehandle, NULL, scanonly);
  if (result == -1)
  {
#ifdef DEBUG
    ErrorPrint("SetupPromptReboot");
#endif
    ThrowIOExceptionLastError(env, "SetupPromptReboot");
  }
#ifdef DEBUG
  else
  if (scanonly)
  {
    printf("(C) SetupPromptReboot returned %x\n", result);
    if ((result & SPFILEQ_FILE_IN_USE) != 0)
    printf("(C) At least one file was in use during the queue commit process and there are delayed file operations pending.\n");
    if ((result & SPFILEQ_REBOOT_RECOMMENDED) != 0)
    printf("(C) The system should be rebooted.\n");
    if ((result & SPFILEQ_REBOOT_IN_PROGRESS) != 0)
    printf("(C) System shutdown is in progress.\n");
  }
#endif

  return result;
};
