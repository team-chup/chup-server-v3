#!/bin/bash
INPUT=$(cat)
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name')

[[ "$TOOL_NAME" == "Edit" || "$TOOL_NAME" == "Write" ]] || exit 0

FILE_PATH=$(echo "$INPUT" | jq -r '.tool_input.file_path // empty')
CWD=$(echo "$INPUT" | jq -r '.cwd // empty')

[[ "$FILE_PATH" == *.kt || "$FILE_PATH" == *.java ]] || exit 0
[[ "$FILE_PATH" == */test/* ]] && exit 0
FILE_NAME=$(basename "$FILE_PATH")
[[ "$FILE_NAME" == *ServiceImpl.kt || "$FILE_NAME" == *ServiceImpl.java ]] || exit 0

if [[ "$FILE_PATH" == /* ]]; then
    FILE_ABS="$FILE_PATH"
elif [[ -n "$CWD" ]]; then
    FILE_ABS="$CWD/$FILE_PATH"
else
    exit 0
fi

PROJECT_ROOT=$(git -C "$(dirname "$FILE_ABS")" rev-parse --show-toplevel 2>/dev/null)
[[ -z "$PROJECT_ROOT" ]] && PROJECT_ROOT="$CWD"
[[ -z "$PROJECT_ROOT" ]] && exit 0

MODULE_DIR="${FILE_ABS%%/src/*}"
[[ "$MODULE_DIR" == "$FILE_ABS" ]] && exit 0
[[ -d "$MODULE_DIR/src/test" ]] || exit 0

# 모노레포(apps/server 등) 대응: 모듈에서 위로 올라가며 가장 가까운 gradlew를 찾아 그 디렉터리를 Gradle 루트로 쓴다
GRADLE_ROOT=""
DIR="$MODULE_DIR"
while :; do
    if [[ -x "$DIR/gradlew" ]]; then
        GRADLE_ROOT="$DIR"
        break
    fi
    PARENT=$(dirname "$DIR")
    [[ "$PARENT" == "$DIR" ]] && break
    DIR="$PARENT"
done

if [[ -n "$GRADLE_ROOT" ]]; then
    GRADLE_CMD="$GRADLE_ROOT/gradlew"
elif command -v gradle >/dev/null 2>&1; then
    GRADLE_CMD="gradle"
    GRADLE_ROOT="$PROJECT_ROOT"
else
    echo "[Hook] gradlew/gradle 를 찾지 못해 테스트를 건너뜁니다." >&2
    exit 0
fi

REL="${MODULE_DIR#$GRADLE_ROOT}"
REL="${REL#/}"
if [[ -z "$REL" ]]; then
    TEST_TASK=":test"
else
    TEST_TASK=":${REL//\//:}:test"
fi

BASE="${FILE_NAME%.*}"
TEST_CLASS="${BASE%Impl}Test"
echo "[Hook] Running $TEST_TASK --tests $TEST_CLASS ..." >&2
TEST_OUTPUT=$(cd "$GRADLE_ROOT" &&"$GRADLE_CMD" "$TEST_TASK" --tests "$TEST_CLASS" 2>&1)
TEST_EXIT=$?
TAIL=$(echo "$TEST_OUTPUT" | tail -5)
if [[ $TEST_EXIT -ne 0 ]]; then
    echo "[Hook] Test FAILED ($TEST_TASK). Last 5 lines:"
    echo "$TAIL"
    echo "Tests failed after editing $FILE_NAME. Consider running test-fixer agent."
else
    echo "[Hook] Tests passed ($TEST_TASK). Last 5 lines:"
    echo "$TAIL"
fi
exit 0