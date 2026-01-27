function onReportLocationSetLocationFalse(targetClassMethod) {
	var delim = targetClassMethod.lastIndexOf(".");
	if (delim === -1) return;
	var targetClass = targetClassMethod.slice(0, delim)
	var targetMethod = targetClassMethod.slice(delim + 1, targetClassMethod.length)
	var hook = Java.use(targetClass);
	var overloadCount = hook[targetMethod].overloads.length;
	for (var i = 0; i < overloadCount; i++) {
		hook[targetMethod].overloads[i].implementation = function() {
            console.log("onReportLocationSetLocationFalse")
			if(arguments[0] !== null) {
				arguments[0].setIsFromMockProvider(false);
			}
			this[targetMethod].apply(this, arguments);
			return;
		}
	}
}

function isMockSetLocationMockFalse(targetClassMethod) {
	var delim = targetClassMethod.lastIndexOf(".");
	if (delim === -1) return;
	var targetClass = targetClassMethod.slice(0, delim)
	var targetMethod = targetClassMethod.slice(delim + 1, targetClassMethod.length)
	var hook = Java.use(targetClass);
	var overloadCount = hook[targetMethod].overloads.length;
	for (var i = 0; i < overloadCount; i++) {
		hook[targetMethod].overloads[i].implementation = function() {
            console.log("isMockSetLocationMockFalse")
			if(arguments[0] !== null) {
				arguments[0].setIsFromMockProvider(false);
			}
			this[targetMethod].apply(this, arguments);
			return;
		}
	}
}

function setLocationResultMockFalse(targetClassMethod) {
	var delim = targetClassMethod.lastIndexOf(".");
	if (delim === -1) return;
	var targetClass = targetClassMethod.slice(0, delim)
	var targetMethod = targetClassMethod.slice(delim + 1, targetClassMethod.length)
	var hook = Java.use(targetClass);
	var overloadCount = hook[targetMethod].overloads.length;
	for (var i = 0; i < overloadCount; i++) {
		hook[targetMethod].overloads[i].implementation = function() {
            console.log("setLocationResultMockFalse")
			if(arguments[0] !== null) {
				arguments[0].setIsFromMockProvider(false);
			}
			this[targetMethod].apply(this, arguments);
			return;
		}
	}
}

function setLocationResultMockFalseAndroidNew(targetClassMethod) {
	var delim = targetClassMethod.lastIndexOf(".");
	if (delim === -1) return;
	var targetClass = targetClassMethod.slice(0, delim)
	var targetMethod = targetClassMethod.slice(delim + 1, targetClassMethod.length)
	var hook = Java.use(targetClass);
	console.log(targetMethod)
	var overloadCount = hook[targetMethod].overloads.length;
	for (var i = 0; i < overloadCount; i++) {
		hook[targetMethod].overloads[i].implementation = function() {
            console.log("setLocationResultMockFalseAndroidNew")
			if(arguments[0] !== null) {
				arguments[0].setMock(false);
			}
			this[targetMethod].apply(this, arguments);
			return;
		}
	}
}

function overwriteReturnFalse(targetClassMethod) {
	var delim = targetClassMethod.lastIndexOf(".");
	if (delim === -1) return;
	var targetClass = targetClassMethod.slice(0, delim)
	var targetMethod = targetClassMethod.slice(delim + 1, targetClassMethod.length)
	var hook = Java.use(targetClass);
	var overloadCount = hook[targetMethod].overloads.length;
	for (var i = 0; i < overloadCount; i++) {
		hook[targetMethod].overloads[i].implementation = function() {
            console.log("overwriteReturnFalse")
			return false;
		}
	}
}

setTimeout(function() {
    console.log("Injecting...")
	if(Java.available) {
		Java.perform(function() {
			var ver = Java.use('android.os.Build$VERSION');
			var sdk = ver.SDK_INT.value;
			if (sdk >= 31) { // Android 12
                // https://android.googlesource.com/platform/frameworks/base/+/refs/heads/android13-s3-release/services/core/java/com/android/server/location/provider/LocationProviderManager.java#1789
                // https://android.googlesource.com/platform/frameworks/base/+/refs/heads/android12-release/services/core/java/com/android/server/location/provider/LocationProviderManager.java#1675
                setLocationResultMockFalseAndroidNew("com.android.server.location.provider.LocationProviderManager.setLastLocation");
                overwriteReturnFalse("android.location.Location.isMock");
                setLocationResultMockFalseAndroidNew("com.android.server.location.provider.LocationProviderManager.injectLastLocation");
			} else if (sdk > 29) {
				onReportLocationSetLocationFalse("com.android.server.location.LocationManagerService$LocationProviderManager.onReportLocation");
				onReportLocationSetLocationFalse("com.android.server.LocationManagerService.handleLocationChangedLocked");
				onReportLocationSetLocationFalse("com.android.server.LocationManagerService.handleLocationChanged");
			} else if (sdk == 29) {
				onReportLocationSetLocationFalse("com.android.server.LocationManagerService$LocationProvider.onReportLocation");
				onReportLocationSetLocationFalse("com.android.server.LocationManagerService.handleLocationChangedLocked");
			} else {
				onReportLocationSetLocationFalse("com.android.server.LocationManagerService.reportLocation");
				onReportLocationSetLocationFalse("com.android.server.LocationManagerService.handleLocationChanged");
				onReportLocationSetLocationFalse("com.android.server.LocationManagerService.handleLocationChangedLocked");
			}
		});
	}
}, 0);