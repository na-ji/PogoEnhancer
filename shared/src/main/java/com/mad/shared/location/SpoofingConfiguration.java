package com.mad.shared.location;

public class SpoofingConfiguration {
    public boolean spoofingEnabled = false;
    public SpoofingMethod spoofingMethod = SpoofingMethod.MOCK;
    public LocationOverwriteMode overwriteMode = LocationOverwriteMode.COMMON;
    public boolean enableSuspendedMocking = false;
    public boolean resetGplayServices = false;
    public boolean resetAgpsContinuoursly = false;
    public boolean resetAgpsOnce = false;
    public boolean overwriteFused = false;
}
