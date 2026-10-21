#!/usr/bin/env bash
# Deploy an image tag on the server: log in to ECR, pull, restart, clean up
# Usage: bash infra/deploy.sh <image-tag>
set -euo pipefail

TAG="${1:-latest}"
REGISTRY="851725432663.dkr.ecr.ca-central-1.amazonaws.com"
cd "$(dirname "$0")/.."

# Log Docker in to ECR using the server's IAM role
/snap/bin/aws ecr get-login-password --region ca-central-1 | docker login --username AWS --password-stdin "$REGISTRY"

# Pull the new image and restart with it
export APP_TAG="$TAG"
docker compose pull app
docker compose up -d

# Remove old, unused images to free disk space
docker image prune -f

echo "Deployed $TAG"
