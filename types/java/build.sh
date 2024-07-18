#!/bin/bash

set -e
set -o pipefail

HERE=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

if [[ -z "$VERSION" ]]; then
  echo "VERSION unset";
  exit 1;
fi;

npx --yes @openapitools/openapi-generator-cli generate \
  --input-spec http://127.0.0.1:8080/api-docs \
  --generator-name java \
  --output "$HERE/gen" \
  --additional-properties=invokerPackage=com.inspiretmstech.api \
  --additional-properties=apiPackage=com.inspiretmstech.api \
  --additional-properties=modelPackage=com.inspiretmstech.api.models \
  --additional-properties=library=apache-httpclient \
  --additional-properties=groupId=com.inspiretmstech \
  --additional-properties=artifactId=api \
  --additional-properties=artifactVersion="$VERSION" \
  --additional-properties=artifactUrl=https://github.com/inspire-labs-tms-tech/inspire-tms-api \
  --additional-properties=artifactDescription="An\ Open\ API\ Client\ for\ Inspire\ TMS" \
  --additional-properties=scmConnection=scm:git:git@github.com:inspire-labs-tms-tech/inspire-tms-api.git \
  --additional-properties=scmDeveloperConnection=scm:git:git@github.com:inspire-labs-tms-tech/inspire-tms-api.git \
  --additional-properties=scmUrl=https://github.com/inspire-labs-tms-tech/inspire-tms-api \
  --additional-properties=developerEmail=support@inspiretmstech.com \
  --additional-properties=developerName="Inspire\ TMS" \
  --additional-properties=developerOrganization="Inspire\ Labs,\ LLC" \
  --additional-properties=licenseName=MIT \
  --additional-properties=licenseUrl=https://github.com/inspire-labs-tms-tech/inspire-tms-api/blob/main/LICENSE
