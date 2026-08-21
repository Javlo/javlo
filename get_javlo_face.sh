#!/bin/sh
#
# Installs the javlo-face module in the local Maven repository, so the build of Javlo finds
# org.javlo.face:javlo-face:1.0.0.
#
# The module is CPU only face detection : it lives in its own repository because the onnxruntime
# jar it pulls weights about 90 Mo, and Javlo works without it (detection is simply switched off
# when the jar is missing).
#
# Usage : ./get_javlo_face.sh

set -e

REPO_URL="https://github.com/Javlo/javlo-face.git"
TARGET="$HOME/javlo-face"

for tool in git mvn; do
	if ! command -v "$tool" >/dev/null 2>&1; then
		echo "$tool is needed but not on the PATH." >&2
		exit 1
	fi
done

if [ ! -e "$TARGET" ]; then
	echo "cloning $REPO_URL into $TARGET"
	git clone "$REPO_URL" "$TARGET"
elif [ -d "$TARGET/.git" ]; then
	echo "$TARGET is already there, updating it"
	git -C "$TARGET" pull --ff-only
else
	echo "$TARGET exists but is not a git repository : move it away first." >&2
	exit 1
fi

echo "building and installing javlo-face"
cd "$TARGET"
mvn install

echo
echo "org.javlo.face:javlo-face:1.0.0 is installed, the Javlo build can now use it."
