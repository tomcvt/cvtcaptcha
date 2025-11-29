$APP_VERSION = mvn help:evaluate -Dexpression=project.version -q -DforceStdout

Write-Host "Building Docker image with version $APP_VERSION"

docker-compose build --build-arg APP_VERSION=$APP_VERSION

docker-compose up -d