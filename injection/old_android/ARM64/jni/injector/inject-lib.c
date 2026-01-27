/*
 * Compile with:
 *
 * /Users/frida/Buildbot/frida-android/build/build/ndk-android-arm64/bin/aarch64-linux-android-clang -fPIC -DANDROID -fPIE -ffunction-sections -fdata-sections -Wall -Os -pipe -g3 frida-core-example.c -o frida-core-example -L. -lfrida-core -llog -ldl -lm -llog -ldl -lm -ldl -ldl -llog -lm -Wl,--export-dynamic -fuse-ld=gold -Wl,--gc-sections,-z,noexecstack,-z,relro,-z,now -L/Users/frida/Buildbot/frida-android/build/build/frida-android-arm64/lib -L/Users/frida/Buildbot/frida-android/build/build/sdk-android-arm64/lib
 *
 * Visit www.frida.re to learn more about Frida.
 */

#include "frida-core.h"

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <sys/un.h>
#include <sys/stat.h>
// #include "instr.h"

#define BUFFER_SIZE 32768


static GMainLoop * loop = NULL;

int
main (int argc,
      char * argv[])
{
  guint target_pid;
  GError * error = NULL;
  int result = 0;
  FridaInjector * injector;
  guint id;
  frida_init ();

  if (argc != 4 || (target_pid = atoi (argv[1])) == 0)
  {
    // g_printerr ("Usage: %s <pid> <pathToLib>\n", argv[0]);
    exit(2);
  }
  loop = g_main_loop_new (NULL, TRUE);
  //check if user is active sub
  // char *orig = argv[2];
  char* origCall = (char*) malloc(1 + strlen(argv[2]));
  strcpy(origCall, argv[2]);

  // curl_easy_setopt(easyhandle, CURLOPT_SSL_VERIFYPEER, 0L); //remove CA check
      // curl_easy_setopt(easyhandle, CURLOPT_SSL_VERIFYHOST, 0L);
  error = NULL;

  // char* path = "/data/data/com.nianticlabs.pokemongo/lib/";
  // char * fullPath = (char *) malloc(1 + strlen(path)+ strlen(argv[3]) );
  // strcpy(fullPath, path);
  // strcat(fullPath, argv[3]);

  // g_printerr("Patching SELinux\n");
  frida_selinux_patch_policy();
  // _frida_setfilecon(path, "u:object_r:frida_file:s0");

  //g_printerr("Creating injector\n");

  injector = frida_injector_new();
  //g_printerr("%s\n", origCall);
  //g_printerr("%s\n", argv[3]);
  id = frida_injector_inject_library_file_sync (injector, target_pid, argv[3], "_Z15proto_hook_mainPKcPi", origCall, NULL, &error);
  free(origCall);
  // free(fullPath);
  if (error != NULL)
  {
    fprintf (stderr, "%s\n", error->message);
    g_error_free (error);

    result = 1;
  }

  frida_injector_close_sync (injector, NULL, &error);
  if (error != NULL)
  {
    fprintf (stderr, "%s\n", error->message);
    g_error_free (error);

    result = 32;
  }
  g_object_unref (injector);

  frida_deinit ();

  exit(result);
}
