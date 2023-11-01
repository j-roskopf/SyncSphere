#!/bin/zsh

# pre-commit file to be used in conjunction with the walgreens gradle plugin. This will be executed before commit and will execute `spotlessApply` on staged files

# Workaround for SourceTree / other Git GUI users (https://community.atlassian.com/t5/Bitbucket-questions/SourceTree-Hook-failing-because-paths-don-t-seem-to-be-set/qaq-p/274792)
source ~/.zshrc

trap 'git stash drop; [ -f "$GIT_STASH_FILE" ] && rm "$GIT_STASH_FILE"; exit 1' ERR

set -e

echo "Running formatter..."

GIT_STASH_FILE="stash.patch"

# Stash unstaged changes
git diff > "$GIT_STASH_FILE"

# add the patch so it is not stashed
git add "$GIT_STASH_FILE"

# stash untracked files
git stash -k

# apply spotless and check formatting
./gradlew spotlessApply --daemon
./gradlew detektAll --daemon
./gradlew :build-logic:convention:spotlessApply --daemon

# re-add any changes that spotless created
git add -u

# store the last exit code
RESULT=$?

if test -f "$GIT_STASH_FILE";
then
  echo "$GIT_STASH_FILE has been found"

    # apply
    git apply stash.patch --allow-empty

    # delete the patch and re-add that to the index
    rm -f stash.patch
    git add stash.patch
else
    echo "$GIT_STASH_FILE has not been found"
fi

# delete the WIP stash
git stash drop

# return the exit code
exit $RESULT