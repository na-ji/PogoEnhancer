package com.mad.pogoenhancer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import com.mad.pogoenhancer.utils.ShellHelper;
import com.mad.shared.location.LocationOverwriteMode;
import com.mad.shared.utils.Runlevel;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

public class Constants {
    public final static String LOGTAG = "PogoEnhancerJ";
    private static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    public final static String NEARBY_STOPS_GPX = "Nearby stops";

    private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();
    private static final double[] CP_MULTIPLIERS = {
            0, 0.094, 0.16639787, 0.21573247, 0.25572005, 0.29024988, 0.3210876, 0.34921268,
            0.3752356, 0.39956728, 0.4225, 0.44310755, 0.4627984, 0.48168495, 0.49985844,
            0.51739395, 0.5343543, 0.5507927, 0.5667545, 0.5822789, 0.5974, 0.6121573,
            0.6265671, 0.64065295, 0.65443563, 0.667934, 0.6811649, 0.69414365, 0.7068842,
            0.7193991, 0.7317, 0.7377695, 0.74378943, 0.74976104, 0.7556855, 0.76156384,
            0.76739717, 0.7731865, 0.77893275, 0.784637, 0.7903, 0.7953, 0.8003, 0.8053,
            0.8103, 0.8153, 0.8203, 0.8253, 0.8303, 0.8353, 0.8403, 0.8453, 0.8503,
            0.8553, 0.8603, 0.8653
    };
    private static SecureRandom rnd = new SecureRandom();

    public static String randomString(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();
    }

    public static boolean isArm64System() {
        ArrayList<String> supportedAbis = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String[] temp = Build.SUPPORTED_ABIS;
            supportedAbis.addAll(Arrays.asList(temp));
        } else {
            supportedAbis.add(Build.CPU_ABI);
            supportedAbis.add(Build.CPU_ABI2);
        }
        for (String supportedAbi : supportedAbis) {
            if (supportedAbi.toLowerCase().contains("64")) {
                return true;
            }
        }
        return false;
    }

    public static String hexadecimal(String input) throws UnsupportedEncodingException {
        if (input == null) throw new NullPointerException();
        return asHex(input.getBytes(StandardCharsets.UTF_8));
    }

    public static String asHex(byte[] buf) {
        char[] chars = new char[2 * buf.length];
        for (int i = 0; i < buf.length; ++i) {
            chars[2 * i] = HEX_CHARS[(buf[i] & 0xF0) >>> 4];
            chars[2 * i + 1] = HEX_CHARS[buf[i] & 0x0F];
        }
        return new String(chars);
    }

    public static int getLevelByMultiplier(double multiplier) {
        int level;
        double toleranceDelta = 1.0E-7;
        for (level = 0; level < CP_MULTIPLIERS.length; level++) {
            if (CP_MULTIPLIERS[level] >= multiplier - toleranceDelta) {
                return level;
            }
        }
        return level;
    }

    public interface GEO {
        int EARTH_RADIUS = 6371000;
    }

    public interface REPACKAGE_SPECIFICS {
        String BUSYBOX_PATH = "/sbin/.core/busybox";
    }

    public interface ACTION {
    }

    public interface NOTIFICATION_ID {
        int FOREGROUND_SERVICE = 101;
    }

    public static LocationOverwriteMode getLocationOverwriteMode(SharedPreferences preferences) {
        String overwriteMethod = preferences.getString(SHAREDPERFERENCES_KEYS.LOCATION_OVERWRITE_METHOD,
                DEFAULT_VALUES.LOCATION_OVERWRITE_METHOD);
        if (overwriteMethod.equals("Minimal")) {
            return LocationOverwriteMode.MINIMAL;
        } else if (overwriteMethod.equals("Common")) {
            return LocationOverwriteMode.COMMON;
        } else if (overwriteMethod.toLowerCase().equals("overkill")){
            return LocationOverwriteMode.OVERKILL;
        } else {
            return LocationOverwriteMode.INDIRECT;
        }
    }

    public interface SHAREDPERFERENCES_KEYS {
        String POST_DESTINATION = "post_destination";
        String DEVICE_ID = "auth_id";
        String LIBFILENAME = "libfilename";
        String IV_OVERLAY_ENABLED = "iv_overlay_enabled";
        String INJECT_AFTER_SECONDS = "preference_inject_after_seconds";
        String FULL_DAEMON = "full_daemon";
        String POST_ORIGIN = "post_origin";
        String OOM_ADJ_ENABLED = "switch_enable_oomadj";
        String USE_ARM64_INJECTOR = "switch_use_arm64";
        String DISABLE_NOTIFICATION = "switch_disable_notifications";
        String SHOW_HEADSUP = "switch_popup_notifications";
        String LAST_PID_INJECTED = "last_pid_injected";
        String LAST_TIME_INJECTED = "last_time_injected";
        String LATEST_VERSION_KNOWN = "latest_version_known";
        String INTENTIONAL_STOP = "intentional_stop";
        String AUTH_ENABLED = "switch_enable_auth_header";
        String AUTH_USERNAME = "auth_username";
        String AUTH_PASSWORD = "auth_password";
        String DISABLE_EXTERNAL_COMMUNICATION = "switch_disable_external_communication";
        String CALL_SETENFORCE = "switch_setenforce";
        String BOOT_DELAY = "boot_delay";
        String LAST_SYSTEM_PID_INJECTED = "last_sys_inj";
        String ENABLE_SYSTEM_PATCHING = "switch_enable_mock_location_patch";
        String LAST_SYSTEM_PATCH_TIMESTAMP = "last_system_patch_timestamp";
        String SEND_RAW_PROTOS = "switch_send_raw_protos";

        String OVERLAY_JOYSTICK_PREOPEN = "overlay_joystick_preopen";
        String OVERLAY_NEARBY_PREOPEN = "overlay_nearby_preopen";
        String OVERLAY_COOLDOWN_PREOPEN = "overlay_cooldown_preopen";
        String OVERLAY_INCIDENT_PREOPEN = "overlay_incident_preopen";
        String OVERLAY_GYM_PREOPEN = "overlay_gym_preopen";


        String RESET_GOOGLE_PLAY_SERVICES = "reset_google_play_services";
        String RESET_AGPS_CONTINUOUSLY = "reset_agps_continuously";
        String RESET_AGPS_ONCE = "reset_agps_once";
        String LAST_LOCATION_LATITUDE = "last_location_latitude";
        String LAST_LOCATION_LONGITUDE = "last_location_longitude";
        String LAST_LOCATION_ALTITUDE = "last_location_altitude";
        String SPOOFING_ENABLED = "enable_spoofing";
        String APP_SYSTEMIZED = "app_systemized";
        String SPOOFING_METHOD = "spoofing_method";
        String REPLACE_CP_WITH_IV_PERCENTAGE = "replace_cp_with_iv_percentage";
        String SPOOF_SPEED_RUN_KMPH = "spoof_walk_speed_kmph";
        String ENHANCED_CAPTURE = "enhanced_capture";
        String MON_NOTIFICATION_IDS_NEARBY = "mon_notification_ids_nearby";
        String MON_NOT_TRANSFERED_IDS = "mon_not_transfered_ids";
        String MON_NOT_RUN_IDS = "mon_not_run_ids";
        String AUTOTRANSFER_SWITCH_NOT_TRANSFER = "autotransfer_switch_not_transfer";
        String MON_HIDE_IDS_NEARBY = "mon_hide_ids_nearby";
        String FAST_CATCH_OPTION = "fast_catch_option";
        String ENABLE_AUTOSPIN = "enable_autospin";
        String SAVE_LAST_USED_BALL = "save_last_used_ball";
        String USE_NANNY = "use_nanny";
        String PINAP_MODE = "pinap_mode";
        String TELEPORTED_AT = "teleported_at";
        String DISTANCE_TRAVELLED = "distance_travelled";

        String LAST_OVERLAY_X_MAIN = "last_overlay_x_main";
        String LAST_OVERLAY_Y_MAIN = "last_overlay_y_main";
        String LAST_OVERLAY_X_JOYSTICK = "last_overlay_x_joystick";
        String LAST_OVERLAY_Y_JOYSTICK = "last_overlay_y_joystick";
        String LAST_OVERLAY_X_IV = "last_overlay_x_iv";
        String LAST_OVERLAY_Y_IV = "last_overlay_y_iv";
        String LAST_OVERLAY_X_NEARBY = "last_overlay_x_nearby";
        String LAST_OVERLAY_Y_NEARBY = "last_overlay_y_nearby";
        String LAST_OVERLAY_X_INCIDENT = "last_overlay_x_incident";
        String LAST_OVERLAY_Y_INCIDENT = "last_overlay_y_incident";
        String LAST_OVERLAY_X_COOLDOWN = "last_overlay_x_cooldown";
        String LAST_OVERLAY_Y_COOLDOWN = "last_overlay_y_cooldown";
        String SUSPENDED_MOCKING = "suspended_mocking";
        String LOCATION_OVERWRITE_METHOD = "location_overwrite_method";
        String LOCATION_OVERWRITE_FUSED = "overwrite_fused";
        String ENABLE_SKIP_ENCOUNTER_INTRO = "skip_encounter_intro";
        String EASY_CATCH_OPTION = "easy_catch_option";
        String ENABLE_AUTORUN = "enable_autorun";
        String ENABLE_AUTOTRANSFER = "enable_autotransfer";
        String ENABLE_AUTOENCOUNTER = "enable_autoencounter";
        String ENABLE_PEPLUS_AUTOSPIN = "enable_peplus_autospin";
        String ENABLE_PEPLUS_AUTOCATCH = "enable_peplus_autocatch";
        String ENABLE_PEPLUS_AUTOFEED = "enable_peplus_autofeed";

        String COOLDOWN_LAT = "cooldown_lat";
        String COOLDOWN_LNG = "cooldown_lng";
        String COOLDOWN_TIME = "cooldown_time";

        String AUTORUN_MIN_IV = "autorun_min_iv";
        String GPX_ROUTES = "gpx_routes";
        String OVERLAY_SCALING = "overlay_scaling";
        String IV_TOAST_DURATION = "iv_toast_duration";
        String TELEPORT_DEFAULT_OPTION = "teleport_default_option";
        String OVERLAY_SHOW_IV_ELEMENT = "overlay_show_iv_element";
        String OVERLAY_NEARBY_NON_SPOOFING_CLICK_COPY = "overlay_nearby_non_spoofing_click_copy";
        String REPLACE_ENCOUNTER_NAMES = "replace_encounter_name";
        String OVERLAY_SHOW_XSXL = "overlay_show_xsxl";
        String OVERLAY_SHOW_MOVESET = "overlay_show_moveset";
        String OVERLAY_SHOW_SPECIALMON = "overlay_show_specialmon";
        String OVERLAY_SHOW_GENDER = "overlay_show_gender";
        String OVERLAY_SHOW_LVL = "overlay_show_lvl";
        String OVERLAY_SHOW_IV_VALUES = "overlay_show_iv_values";
        String OVERLAY_SHOW_NAME = "overlay_show_name";
        String OVERLAY_SHOW_IV_PERCENTAGE = "overlay_show_iv_percentage";
        String UNLOCK_FPS = "unlock_fps";
        String SKIP_EVOLVE_ANIMATION = "skip_evolve_animation";
        String TOGGLE_INJECTION_DETECTION = "toggle_injection_detection";
        String SPEEDUP_GIFT_OPENING = "speedup_gift_opening";

        String INCIDENT_DISPLAY_GRUNTS = "incident_display_grunts";
        String INCIDENT_DISPLAY_LEADERS = "incident_display_leaders";
        String INCIDENT_DISPLAY_GIOVANNI = "incident_display_giovanni";

        String ENABLE_MASSTRANSFER = "masstranser";
        String ENABLE_KEEP_ENCOUNTER_UI = "keep_enc_ui";
        String DISABLE_GRUNTS = "disable_grunts";
        String HIDE_MON_ON_MAP = "hide_mon_on_map";
        String INCREASE_VISIBILITY = "increase_visibility";

        String LAST_OVERLAY_X_INJECTION_SETTINGS = "last_overlay_x_injection_settings";
        String LAST_OVERLAY_Y_INJECTION_SETTINGS = "last_overlay_y_injection_settings";
        String LAST_OVERLAY_X_MAP_SETTINGS = "last_overlay_x_map_settings";
        String LAST_OVERLAY_Y_MAP_SETTINGS = "last_overlay_y_map_settings";

        String SHOW_NEARBY_GYM_FRACTION_NONE = "show_nearby_gym_fraction_none";
        String SHOW_NEARBY_GYM_FRACTION_RED = "show_nearby_gym_fraction_red";
        String SHOW_NEARBY_GYM_FRACTION_BLUE = "show_nearby_gym_fraction_blue";
        String SHOW_NEARBY_GYM_FRACTION_YELLOW = "show_nearby_gym_fraction_yellow";
        String SHOW_NEARBY_GYM_WITH_RAID_LEVEL_NONE = "show_nearby_gym_with_raid_level_none";
        String SHOW_NEARBY_GYM_WITH_RAID_LEVEL_1 = "show_nearby_gym_with_raid_level_1";
        String SHOW_NEARBY_GYM_WITH_RAID_LEVEL_2 = "show_nearby_gym_with_raid_level_2";
        String SHOW_NEARBY_GYM_WITH_RAID_LEVEL_3 = "show_nearby_gym_with_raid_level_3";
        String SHOW_NEARBY_GYM_WITH_RAID_LEVEL_4 = "show_nearby_gym_with_raid_level_4";
        String SHOW_NEARBY_GYM_WITH_RAID_LEVEL_5 = "show_nearby_gym_with_raid_level_5";
        String LAST_OVERLAY_X_GYM = "last_overlay_x_gym";
        String LAST_OVERLAY_Y_GYM = "last_overlay_y_gym";

        int[] supportedItemIdsForInventoryCleaning = {1, 2, 3, 101, 102, 103, 104, 201, 202, 701, 703, 705, 706, 708, 1101, 1102, 1103, 1104, 1105, 1201, 1202, 1301, 1404, 301, 401};
        int[] wildmonTyp = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18};


        String ACTIVE_INVENTORY_MANAGEMENT = "activate_inventory_management";

    }

    public interface DEFAULT_VALUES {
        String DEFAULT_POST_DESTINATION = "/";
        String DEFAULT_DEVICE_ID = null;
        String LIBFILENAME = "libd.so";
        boolean IV_OVERLAY_ENABLED = false;
        boolean SWITCH_ENABLE_WATCHDOG = false;
        String INJECT_AFTER_SECONDS = "120";
        boolean FULL_DAEMON = false;
        String POST_ORIGIN = "";
        boolean OOM_ADJ_ENABLED = false;
        boolean USE_ARM64_INJECTOR = false;
        boolean DISABLE_NOTIFICATION = false;
        boolean SHOW_HEADSUP = false;
        int LAST_PID_INJECTED = -1;
        long LAST_TIME_INJECTED = 0L;
        String LATEST_VERSION_KNOWN = "";
        boolean INTENTIONAL_STOP = true;
        boolean AUTH_ENABLED = false;
        String AUTH_USERNAME = "";
        String AUTH_PASSWORD = "";
        boolean DISABLE_EXTERNAL_COMMUNICATION = false;
        boolean CALL_SETENFORCE = true;
        long BOOT_DELAY = 0L;
        int LAST_SYSTEM_PID_INJECTED = -1;
        boolean ENABLE_SYSTEM_PATCHING = false;
        long LAST_SYSTEM_PATCH_TIMESTAMP = 0L;
        boolean SEND_RAW_PROTOS = false;

        boolean RESET_GOOGLE_PLAY_SERVICES = false;
        boolean RESET_AGPS_CONTINUOUSLY = false;
        boolean RESET_AGPS_ONCE = false;
        Long LAST_LOCATION_LATITUDE = 49L;
        Long LAST_LOCATION_LONGITUDE = 9L;
        Long LAST_LOCATION_ALTITUDE = 150L;
        boolean SPOOFING_ENABLED = false;
        String SPOOFING_METHOD = "mock";
        boolean REPLACE_CP_WITH_IV_PERCENTAGE = false;
        String SPOOF_SPEED_RUN_KMPH = "30.0";
        String ENHANCED_CAPTURE = "Disabled";
        Set<String> MON_NOTIFICATION_IDS_NEARBY = new HashSet<String>();
        Set<String> MON_NOT_TRANSFERED_IDS = new HashSet<String>();
        Set<String> MON_NOT_RUN_IDS = new HashSet<String>();
        Set<String> MON_HIDE_IDS_NEARBY = new HashSet<String>();
        String FAST_CATCH_OPTION = "None";
        String EASY_CATCH_OPTION = "None";
        boolean AUTOTRANSFER_SWITCH_NOT_TRANSFER = false;
        boolean OVERLAY_JOYSTICK_PREOPEN = false;
        boolean OVERLAY_NEARBY_PREOPEN = false;
        boolean OVERLAY_COOLDOWN_PREOPEN = false;
        boolean OVERLAY_INCIDENT_PREOPEN = false;
        boolean OVERLAY_GYM_PREOPEN = false;

        boolean ENABLE_AUTOSPIN = false;
        boolean SAVE_LAST_USED_BALL = false;
        boolean USE_NANNY = false;
        boolean PINAP_MODE = false;
        long TELEPORTED_AT = 0;
        float DISTANCE_TRAVELLED = 0; // just loose some precision god damn
        int LAST_OVERLAY_X_MAIN = 100;
        int LAST_OVERLAY_Y_MAIN = 100;
        int LAST_OVERLAY_X_JOYSTICK = 200;
        int LAST_OVERLAY_Y_JOYSTICK = 200;
        int LAST_OVERLAY_X_IV = 200;
        int LAST_OVERLAY_Y_IV = 200;
        int LAST_OVERLAY_X_NEARBY = 300;
        int LAST_OVERLAY_Y_NEARBY = 300;
        int LAST_OVERLAY_X_COOLDOWN = 300;
        int LAST_OVERLAY_Y_COOLDOWN = 300;
        int LAST_OVERLAY_X_INCIDENT = 300;
        int LAST_OVERLAY_Y_INCIDENT = 300;
        int LAST_OVERLAY_X_INJECTION_SETTINGS = 300;
        int LAST_OVERLAY_Y_INJECTION_SETTINGS = 300;
        int LAST_OVERLAY_X_MAP_SETTINGS = 300;
        int LAST_OVERLAY_Y_MAP_SETTINGS = 300;
        int LAST_OVERLAY_X_GYM = 300;
        int LAST_OVERLAY_Y_GYM = 300;
        boolean SUSPENDED_MOCKING = true;
        String LOCATION_OVERWRITE_METHOD = "Minimal";
        boolean LOCATION_OVERWRITE_FUSED = false;
        boolean ENABLE_SKIP_ENCOUNTER_INTRO = false;
        boolean ENABLE_AUTORUN = false;
        boolean ENABLE_AUTOTRANSFER = false;
        boolean ENABLE_AUTOENCOUNTER = false;
        boolean ENABLE_PEPLUS_AUTOSPIN = false;
        boolean ENABLE_PEPLUS_AUTOCATCH = false;
        boolean ENABLE_PEPLUS_AUTOFEED = false;

        String COOLDOWN_LAT = "0.0";
        String COOLDOWN_LNG = "0.0";
        long COOLDOWN_TIME = 0;

        String AUTORUN_MIN_IV = "0";
        String GPX_ROUTES = "";
        int OVERLAY_SCALING = 50;
        boolean OVERLAY_SHOW_IV_ELEMENT = true;
        String IV_TOAST_DURATION = "2s";
        String TELEPORT_DEFAULT_OPTION = "Ask every time";
        boolean OVERLAY_NEARBY_NON_SPOOFING_CLICK_COPY = false;
        boolean REPLACE_ENCOUNTER_NAMES = false;
        boolean OVERLAY_SHOW_XSXL = false;
        boolean OVERLAY_SHOW_MOVESET = false;
        boolean OVERLAY_SHOW_SPECIALMON = false;
        boolean OVERLAY_SHOW_GENDER = false;
        boolean OVERLAY_SHOW_LVL = false;
        boolean OVERLAY_SHOW_IV_VALUES = false;
        boolean OVERLAY_SHOW_NAME = false;
        boolean OVERLAY_SHOW_IV_PERCENTAGE = false;
        boolean UNLOCK_FPS = false;
        boolean SKIP_EVOLVE_ANIMATION = false;
        boolean TOGGLE_INJECTION_DETECTION = false;
        boolean SPEEDUP_GIFT_OPENING = false;

        boolean INCIDENT_DISPLAY_GRUNTS = true;
        boolean INCIDENT_DISPLAY_LEADERS = true;
        boolean INCIDENT_DISPLAY_GIOVANNI = true;

        boolean SHOW_NEARBY_GYM_FRACTION_NONE = true;
        boolean SHOW_NEARBY_GYM_FRACTION_RED = true;
        boolean SHOW_NEARBY_GYM_FRACTION_BLUE = true;
        boolean SHOW_NEARBY_GYM_FRACTION_YELLOW = true;
        boolean SHOW_NEARBY_GYM_WITH_RAID_LEVEL_NONE = true;
        boolean SHOW_NEARBY_GYM_WITH_RAID_LEVEL_1 = true;
        boolean SHOW_NEARBY_GYM_WITH_RAID_LEVEL_2 = true;
        boolean SHOW_NEARBY_GYM_WITH_RAID_LEVEL_3 = true;
        boolean SHOW_NEARBY_GYM_WITH_RAID_LEVEL_4 = true;
        boolean SHOW_NEARBY_GYM_WITH_RAID_LEVEL_5 = true;

        boolean ENABLE_MASSTRANSFER = false;
        boolean ENABLE_KEEP_ENCOUNTER_UI = false;
        boolean DISABLE_GRUNTS = false;
        boolean HIDE_MON_ON_MAP = false;
        boolean INCREASE_VISIBILITY = false;

        // inventory
        boolean ACTIVE_INVENTORY_MANAGEMENT = false;
        String SEMI_INFINITE_INVENTORY_KEEP_AMOUNT = "9999";
        boolean SHOW_WILDMON = true;
    }

    private static long calculateTotalCooldown(double distanceTravelledKilometers) {
        if (distanceTravelledKilometers >= 1335) {
            return 120 * 60;
        } else if (distanceTravelledKilometers >= 1300) {
            return 117 * 60;
        } else if (distanceTravelledKilometers >= 1220) {
            return 111 * 60;
        } else if (distanceTravelledKilometers >= 1180) {
            return 109 * 60;
        } else if (distanceTravelledKilometers >= 1100) {
            return 104 * 60;
        } else if (distanceTravelledKilometers >= 1020) {
            return 102 * 60;
        } else if (distanceTravelledKilometers >= 1007) {
            return 98 * 60;
        } else if (distanceTravelledKilometers >= 950) {
            return 95 * 60;
        } else if (distanceTravelledKilometers >= 900) {
            return 91 * 60;
        } else if (distanceTravelledKilometers >= 850) {
            return 90 * 60;
        } else if (distanceTravelledKilometers >= 800) {
            return 88 * 60;
        } else if (distanceTravelledKilometers >= 750) {
            return 82 * 60;
        } else if (distanceTravelledKilometers >= 700) {
            return 77 * 60;
        } else if (distanceTravelledKilometers >= 650) {
            return 74 * 60;
        } else if (distanceTravelledKilometers >= 600) {
            return 70 * 60;
        } else if (distanceTravelledKilometers >= 550) {
            return 66 * 60;
        } else if (distanceTravelledKilometers >= 500) {
            return 62 * 60;
        } else if (distanceTravelledKilometers >= 450) {
            return 58 * 60;
        } else if (distanceTravelledKilometers >= 400) {
            return 54 * 60;
        } else if (distanceTravelledKilometers >= 350) {
            return 50 * 60;
        } else if (distanceTravelledKilometers >= 325) {
            return 48 * 60;
        } else if (distanceTravelledKilometers >= 300) {
            return 46 * 60;
        } else if (distanceTravelledKilometers >= 250) {
            return 41 * 60;
        } else if (distanceTravelledKilometers >= 200) {
            return 37 * 60;
        } else if (distanceTravelledKilometers >= 175) {
            return 34 * 60;
        } else if (distanceTravelledKilometers >= 150) {
            return 32 * 60;
        } else if (distanceTravelledKilometers >= 125) {
            return 29 * 60;
        } else if (distanceTravelledKilometers >= 100) {
            return 26 * 60;
        } else if (distanceTravelledKilometers >= 90) {
            return 25 * 60;
        } else if (distanceTravelledKilometers >= 80) {
            return 24 * 60;
        } else if (distanceTravelledKilometers >= 70) {
            return 23 * 60;
        } else if (distanceTravelledKilometers >= 60) {
            return 21 * 60;
        } else if (distanceTravelledKilometers >= 50) {
            return 20 * 60;
        } else if (distanceTravelledKilometers >= 40) {
            return 19 * 60;
        } else if (distanceTravelledKilometers >= 35) {
            return 18 * 60;
        } else if (distanceTravelledKilometers >= 30) {
            return 17 * 60;
        } else if (distanceTravelledKilometers >= 25) {
            return 15 * 60;
        } else if (distanceTravelledKilometers >= 20) {
            return 12 * 60;
        } else if (distanceTravelledKilometers >= 15) {
            return 9 * 60;
        } else if (distanceTravelledKilometers >= 10) {
            return 7 * 60;
        } else if (distanceTravelledKilometers >= 8) {
            return 5 * 60;
        } else if (distanceTravelledKilometers >= 5) {
            return 4 * 60;
        } else if (distanceTravelledKilometers >= 3) {
            return 3 * 60;
        } else if (distanceTravelledKilometers >= 2) {
            return 2 * 60;
        } else if (distanceTravelledKilometers >= 1) {
            return 60;
        } else {
            return 0;
        }
    }

    public static String cooldownOverAt(long lastAction, double distanceKilometers) {
        long currentTime = System.currentTimeMillis() / 1000;
        long overAtSeconds = currentTime + calculateRemainingCooldown(lastAction, distanceKilometers);

        return unixSecondsToTimeString(overAtSeconds);
    }

    public static String unixSecondsToTimeString(long seconds) {
        if (seconds < 0) {
            return "";
        }
        TimeZone tz = TimeZone.getDefault();
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        df.setTimeZone(tz);
        return df.format(new Date(seconds * 1000));
    }

    public static long calculateRemainingCooldown(long lastAction, double distanceKilometers) {
        long totalCdInSeconds = calculateTotalCooldown(distanceKilometers);
        long currentTime = System.currentTimeMillis() / 1000;

        long remaining = (lastAction + totalCdInSeconds) - currentTime;
        return remaining >= 0 ? remaining : 0;
    }

    public static boolean is64BitPogoenhancerInstalled(Context context) {
        String libPath = context.getPackageCodePath().replace("base.apk",
                "lib/");
        ShellHelper shellHelper = new ShellHelper();
        ArrayList<String> strings = shellHelper.runCommand(Runlevel.sh, "ls " + libPath);
        for (String libFolder : strings) {
            if (libFolder.contains("64")) {
                return true;
            }
        }

        return false;
    }
}