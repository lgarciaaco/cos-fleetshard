#!/bin/bash

BASE=${BASE_PATH}/api/connector_mgmt/v1
CLUSTER_BASE=${BASE}/kafka_connector_clusters

for ID in "$@"
do
    curl --insecure --oauth2-bearer "$(ocm token)" -S -s -D /dev/stderr -XDELETE "${CLUSTER_BASE}"/"${ID}" | jq
done