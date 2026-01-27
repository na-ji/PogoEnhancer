args = $(foreach a,$($(subst -,_,$1)_args),$(if $(value $a),$a="$($a)"))

NDKPATH?=/opt/android-sdk/ndk/latest/ndk-build_internal

all:
	@/opt/android-sdk/ndk/latest/ndk-build -B

clean:
	@rm -rf obj libs
