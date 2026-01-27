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
#include <sys/types.h>
#include <sys/un.h>
#include <sys/stat.h>
// #include "instr.h"

#define BUFFER_SIZE 32768


int socket(int domain, int type, int protocol);
static void on_message (FridaScript * script, const gchar * message, GBytes * data, gpointer user_data);
static void on_signal (int signo);
static gboolean stop (gpointer user_data);

static GMainLoop * loop = NULL;

extern int _frida_setfilecon(const char *path, const char *con);

int
main (int argc,
      char * argv[])
{
  int result = 0;
  int target_pid;
  GError * error;
  guint id;
  // fprintf(stderr, "Entry...\n", 0);
  if (argc != 3 || (target_pid = atoi (argv[1])) == 0)
  {
    // g_printerr ("Usage: %s <pid> <pathToLib>\n", argv[0]);
    exit(2);
  }

  // curl_easy_setopt(easyhandle, CURLOPT_SSL_VERIFYPEER, 0L); //remove CA check
      // curl_easy_setopt(easyhandle, CURLOPT_SSL_VERIFYHOST, 0L);
  error = NULL;

  // char* path = "/data/data/com.nianticlabs.pokemongo/lib/";
  // char * fullPath = (char *) malloc(1 + strlen(path)+ strlen(argv[3]) );
  // strcpy(fullPath, path);
  // strcat(fullPath, argv[3]);
  //printf("Init...\n");
  FridaDeviceManager * manager;
  FridaDeviceList * devices;
  gint num_devices, i;
  FridaDevice * local_device;
  FridaSession * session;

  frida_init ();
  frida_selinux_patch_policy();
  // _frida_setfilecon(path, "u:object_r:frida_file:s0");

  manager = frida_device_manager_new ();
  devices = frida_device_manager_enumerate_devices_sync (manager, NULL, &error);
  g_assert (error == NULL);
  local_device = NULL;
  num_devices = frida_device_list_size (devices);
  for (i = 0; i != num_devices; i++)
  {
    FridaDevice * device = frida_device_list_get (devices, i);

    // g_print ("[*] Found device: \"%s\"\n", frida_device_get_name (device));

    if (frida_device_get_dtype (device) == FRIDA_DEVICE_TYPE_LOCAL)
      local_device = g_object_ref (device);

    g_object_unref (device);
  }

  g_assert (local_device != NULL);

  frida_unref (devices);
  devices = NULL;

  session = frida_device_attach_sync (local_device, target_pid, FRIDA_REALM_NATIVE, NULL, &error);
  if (error == NULL)
  {
    FridaScript * script;

    //g_print ("[*] Attached\n");
    FridaScriptOptions * options;
    options = frida_script_options_new ();
    frida_script_options_set_name (options, "example");
    //frida_script_options_set_runtime (options, FRIDA_SCRIPT_RUNTIME_V8);
    //g_print ("[*] Attaching with options\n");

    script = frida_session_create_script_sync (session,
                                               "function onReportLocationSetLocationFalse(targetClassMethod) {\n"
                                               "var delim = targetClassMethod.lastIndexOf(\".\");\n"
                                               "if (delim === -1) return;\n"
                                               "var targetClass = targetClassMethod.slice(0, delim)\n"
                                               "var targetMethod = targetClassMethod.slice(delim + 1, targetClassMethod.length)\n"
                                               "var hook = Java.use(targetClass);\n"
                                               "var overloadCount = hook[targetMethod].overloads.length;\n"
                                               "for (var i = 0; i < overloadCount; i++) {\n"
                                               "hook[targetMethod].overloads[i].implementation = function() {\n"
                                               "if(arguments[0] !== null) {\n"
                                               "arguments[0].setIsFromMockProvider(false);\n"
                                               "}\n"
                                               "this[targetMethod].apply(this, arguments);\n"
                                               "return;\n"
                                               "}\n"
                                               "}\n"
                                               "}\n"
                                               "\n"
                                               "function isMockSetLocationMockFalse(targetClassMethod) {\n"
                                               "var delim = targetClassMethod.lastIndexOf(\".\");\n"
                                               "if (delim === -1) return;\n"
                                               "var targetClass = targetClassMethod.slice(0, delim)\n"
                                               "var targetMethod = targetClassMethod.slice(delim + 1, targetClassMethod.length)\n"
                                               "var hook = Java.use(targetClass);\n"
                                               "var overloadCount = hook[targetMethod].overloads.length;\n"
                                               "for (var i = 0; i < overloadCount; i++) {\n"
                                               "hook[targetMethod].overloads[i].implementation = function() {\n"
                                               "if(arguments[0] !== null) {\n"
                                               "arguments[0].setIsFromMockProvider(false);\n"
                                               "}\n"
                                               "this[targetMethod].apply(this, arguments);\n"
                                               "return;\n"
                                               "}\n"
                                               "}\n"
                                               "}\n"
                                               "\n"
                                               "function setLocationResultMockFalse(targetClassMethod) {\n"
                                               "var delim = targetClassMethod.lastIndexOf(\".\");\n"
                                               "if (delim === -1) return;\n"
                                               "var targetClass = targetClassMethod.slice(0, delim)\n"
                                               "var targetMethod = targetClassMethod.slice(delim + 1, targetClassMethod.length)\n"
                                               "var hook = Java.use(targetClass);\n"
                                               "var overloadCount = hook[targetMethod].overloads.length;\n"
                                               "for (var i = 0; i < overloadCount; i++) {\n"
                                               "hook[targetMethod].overloads[i].implementation = function() {\n"
                                               "if(arguments[0] !== null) {\n"
                                               "arguments[0].setIsFromMockProvider(false);\n"
                                               "}\n"
                                               "this[targetMethod].apply(this, arguments);\n"
                                               "return;\n"
                                               "}\n"
                                               "}\n"
                                               "}\n"
                                               "\n"
                                               "function setLocationResultMockFalseAndroidNew(targetClassMethod) {\n"
                                               "var delim = targetClassMethod.lastIndexOf(\".\");\n"
                                               "if (delim === -1) return;\n"
                                               "var targetClass = targetClassMethod.slice(0, delim)\n"
                                               "var targetMethod = targetClassMethod.slice(delim + 1, targetClassMethod.length)\n"
                                               "var hook = Java.use(targetClass);\n"
                                               "var overloadCount = hook[targetMethod].overloads.length;\n"
                                               "for (var i = 0; i < overloadCount; i++) {\n"
                                               "hook[targetMethod].overloads[i].implementation = function() {\n"
                                               "if(arguments[0] !== null) {\n"
                                               "arguments[0].setMock(false);\n"
                                               "}\n"
                                               "this[targetMethod].apply(this, arguments);\n"
                                               "return;\n"
                                               "}\n"
                                               "}\n"
                                               "}\n"
                                               "\n"
                                               "function overwriteReturnFalse(targetClassMethod) {\n"
                                               "var delim = targetClassMethod.lastIndexOf(\".\");\n"
                                               "if (delim === -1) return;\n"
                                               "var targetClass = targetClassMethod.slice(0, delim)\n"
                                               "var targetMethod = targetClassMethod.slice(delim + 1, targetClassMethod.length)\n"
                                               "var hook = Java.use(targetClass);\n"
                                               "var overloadCount = hook[targetMethod].overloads.length;\n"
                                               "for (var i = 0; i < overloadCount; i++) {\n"
                                               "hook[targetMethod].overloads[i].implementation = function() {\n"
                                               "return false;\n"
                                               "}\n"
                                               "}\n"
                                               "}\n"
                                               "\n"
                                               "setTimeout(function() {\n"
                                               "if(Java.available) {\n"
                                               "Java.perform(function() {\n"
                                               "var ver = Java.use('android.os.Build$VERSION');\n"
                                               "var sdk = ver.SDK_INT.value;\n"
                                               "if (sdk >= 31) {\n"
                                               "setLocationResultMockFalseAndroidNew(\"com.android.server.location.provider.LocationProviderManager.setLastLocation\");\n"
                                               "overwriteReturnFalse(\"android.location.Location.isMock\");\n"
                                               "setLocationResultMockFalseAndroidNew(\"com.android.server.location.provider.LocationProviderManager.injectLastLocation\");\n"
                                               "} else if (sdk > 29) {\n"
                                               "onReportLocationSetLocationFalse(\"com.android.server.location.LocationManagerService$LocationProviderManager.onReportLocation\");\n"
                                               "onReportLocationSetLocationFalse(\"com.android.server.LocationManagerService.handleLocationChangedLocked\");\n"
                                               "onReportLocationSetLocationFalse(\"com.android.server.LocationManagerService.handleLocationChanged\");\n"
                                               "} else if (sdk == 29) {\n"
                                               "onReportLocationSetLocationFalse(\"com.android.server.LocationManagerService$LocationProvider.onReportLocation\");\n"
                                               "onReportLocationSetLocationFalse(\"com.android.server.LocationManagerService.handleLocationChangedLocked\");\n"
                                               "} else {\n"
                                               "onReportLocationSetLocationFalse(\"com.android.server.LocationManagerService.reportLocation\");\n"
                                               "onReportLocationSetLocationFalse(\"com.android.server.LocationManagerService.handleLocationChanged\");\n"
                                               "onReportLocationSetLocationFalse(\"com.android.server.LocationManagerService.handleLocationChangedLocked\");\n"
                                               "}\n"
                                               "});\n"
                                               "}\n"
                                               "}, 0);\n",
    options, NULL, &error);
    //g_print ("[*] Asserting error\n");
    g_clear_object (&options);
    g_assert (error == NULL);
    /*if (error != NULL) {
      g_printerr ("Failed to create script: %s\n", error->message);
      return 1;
    }*/

    // g_signal_connect (script, "message", G_CALLBACK (on_message), NULL);
    //g_print ("[*] Script loading\n");

    frida_script_load_sync (script, NULL, &error);
    g_assert (error == NULL);
    // g_print ("[*] Script loaded\n");
    //
    // if (g_main_loop_is_running (loop))
    //   g_main_loop_run (loop);
    //
    // g_print ("[*] Stopped\n");

    frida_script_eternalize_sync (script, NULL, &error);


    // frida_session_detach_sync (session);
    // frida_unref (session);
  } else {
    // g_printerr ("Failed to attach: %s\n", error->message);
    g_error_free (error);
  }
  // g_print("Unref the device\n");
  frida_unref (local_device);
  // g_print("closing manager\n");
  frida_device_manager_close_sync (manager, NULL, &error);
  // g_print("unref the manager\n");
  frida_unref (manager);
  // g_print ("[*] Closed\n");
  return 0;
}


static void
on_message (FridaScript * script,
            const gchar * message,
            GBytes * data,
            gpointer user_data)
{
  JsonParser * parser;
  JsonObject * root;
  const gchar * type;

  parser = json_parser_new ();
  json_parser_load_from_data (parser, message, -1, NULL);
  root = json_node_get_object (json_parser_get_root (parser));

  type = json_object_get_string_member (root, "type");
  if (strcmp (type, "log") == 0)
  {
    const gchar * log_message;

    log_message = json_object_get_string_member (root, "payload");
    // g_print ("%s\n", log_message);
  }
  else
  {
    // g_print ("on_message: %s\n", message);
  }

  g_object_unref (parser);
}

static void
on_signal (int signo)
{
  g_idle_add (stop, NULL);
}

static gboolean
stop (gpointer user_data)
{
  g_main_loop_quit (loop);

  return FALSE;
}
