#!/bin/bash
# kata-backend semversion script and docker image build/push script

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

DOCKER_REGISTRY_USER="${DOCKER_REGISTRY_USER:-jesalfz}"
IMAGE_BASE_NAME="kata-backend"

# ---------------------------------------------------------------------------
# 1. Version bump
# ---------------------------------------------------------------------------

CURRENT_VERSION=$(cat VERSION)
IFS='.' read -r MAJOR MINOR PATCH <<< "$CURRENT_VERSION"

if [[ "$1" == --version=* ]]; then
    NEW_VERSION="${1#*=}"
    echo "Setting version to $NEW_VERSION ..."
    echo "$NEW_VERSION" > VERSION
elif [ "$1" == "--major" ]; then
    echo "Bumping major version ..."
    NEW_VERSION="$((MAJOR + 1)).0.0"
    echo "$NEW_VERSION" > VERSION
elif [ "$1" == "--minor" ]; then
    echo "Bumping minor version ..."
    NEW_VERSION="${MAJOR}.$((MINOR + 1)).0"
    echo "$NEW_VERSION" > VERSION
elif [ "$1" == "--patch" ]; then
    echo "Bumping patch version ..."
    NEW_VERSION="${MAJOR}.${MINOR}.$((PATCH + 1))"
    echo "$NEW_VERSION" > VERSION
else
    echo "No version bump argument provided. Using existing version ..."
fi

# Sync build.gradle version with VERSION file
VERSION=$(cat VERSION)
if grep -q "^version = " build.gradle; then
    sed -i.bak "s/^version = .*/version = '${VERSION}'/" build.gradle && rm -f build.gradle.bak
    echo "Updated build.gradle version to $VERSION."
fi

echo "Current version is $VERSION"

# ---------------------------------------------------------------------------
# 2. Git metadata & tagging
# ---------------------------------------------------------------------------

GIT_COMMIT=$(git rev-parse --short HEAD)
echo "Current git commit is $GIT_COMMIT"

# Create SEMVER file
SEMVER_FILE="SEMVER"
echo "Creating semver file at $SEMVER_FILE ..."
echo "version: $VERSION" > "$SEMVER_FILE"
echo "commit: $GIT_COMMIT" >> "$SEMVER_FILE"
echo "Semver file created with version $VERSION and commit $GIT_COMMIT."

GIT_TAG="v$VERSION"
echo "Creating git tag $GIT_TAG ..."
if git rev-parse "$GIT_TAG" >/dev/null 2>&1; then
    echo "Git tag $GIT_TAG already exists locally. Skipping tag creation..."
else
    git tag -a "$GIT_TAG" -m "Release $GIT_TAG"
    echo "Git tag $GIT_TAG created."
fi

if git ls-remote --tags origin | grep -q "refs/tags/$GIT_TAG"; then
    echo "Git tag $GIT_TAG already exists on remote. Skipping push..."
else
    git push origin "$GIT_TAG" && echo "Git tag $GIT_TAG pushed." || echo "Failed to push git tag. Continuing..."
fi

# ---------------------------------------------------------------------------
# 3. Build & push Docker image
# ---------------------------------------------------------------------------

IMAGE_NAME="${IMAGE_BASE_NAME}:${VERSION}"
IMAGE_NAME_WITH_COMMIT="${IMAGE_BASE_NAME}:${VERSION}-${GIT_COMMIT}"

if docker image inspect "${DOCKER_REGISTRY_USER}/${IMAGE_NAME_WITH_COMMIT}" >/dev/null 2>&1; then
    echo "Docker image ${IMAGE_NAME_WITH_COMMIT} already exists. Skipping build..."
else
    echo "Building Docker image ${IMAGE_NAME_WITH_COMMIT} ..."
    docker build \
        --target prod \
        -t "${DOCKER_REGISTRY_USER}/${IMAGE_NAME_WITH_COMMIT}" \
        -t "${DOCKER_REGISTRY_USER}/${IMAGE_NAME}" \
        -t "${DOCKER_REGISTRY_USER}/${IMAGE_BASE_NAME}:latest" \
        .
    echo "Docker image ${IMAGE_NAME_WITH_COMMIT} built successfully."
fi

echo "Pushing Docker images to registry..."
docker push "${DOCKER_REGISTRY_USER}/${IMAGE_NAME}" || echo "Failed to push ${IMAGE_NAME}. Continuing..."
docker push "${DOCKER_REGISTRY_USER}/${IMAGE_NAME_WITH_COMMIT}" || echo "Failed to push ${IMAGE_NAME_WITH_COMMIT}. Continuing..."
docker push "${DOCKER_REGISTRY_USER}/${IMAGE_BASE_NAME}:latest" || echo "Failed to push latest tag. Continuing..."
echo "Docker push completed."
