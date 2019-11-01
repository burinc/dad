.PHONY: native-image circleci test clean

PLATFORM = $(shell uname -s)
ifeq ($(PLATFORM), Darwin)
	GRAAL_EXTRA_OPTION :=
else
	GRAAL_EXTRA_OPTION := "--static"
endif

target/oton.jar:
	lein uberjar

oton: target/oton.jar
	$(GRAALVM_HOME)/bin/native-image \
		-jar target/oton.jar \
		-H:Name=oton \
		-H:+ReportExceptionStackTraces \
		-J-Dclojure.spec.skip-macros=true \
		-J-Dclojure.compiler.direct-linking=true \
		"-H:IncludeResources=command/.*" \
		"-H:IncludeResources=version.txt" \
		--initialize-at-build-time  \
		-H:Log=registerResource: \
		--verbose \
		--no-fallback \
		--no-server \
		$(GRAAL_EXTRA_OPTION) \
		"-J-Xmx3g"

native-image: clean oton

circleci:
	circleci local execute --job debian

test:
	lein test

clean:
	lein clean
	\rm -f oton
