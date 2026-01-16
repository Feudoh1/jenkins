#!/bin/bash
# Script to start all EC2 instances in a specific VPC (for Jenkins pipeline use)

#set -euo pipefail

# Hardcoded VPC ID (update this value to your target VPC)
VPC_ID="vpc-0a50c2295b2293771"

echo "🔍 Fetching stopped EC2 instances in VPC: $VPC_ID..."

# Fetch stopped instances in the VPC
INSTANCE_IDS=$(aws ec2 describe-instances \
  --filters "Name=vpc-id,Values=$VPC_ID" "Name=instance-state-name,Values=stopped" \
  --query "Reservations[].Instances[].InstanceId" \
  --output text)

if [ -z "$INSTANCE_IDS" ]; then
  echo "✅ No stopped instances found in VPC $VPC_ID"
  exit 0
fi

echo "📌 Instances to start: $INSTANCE_IDS"

# Start the instances (non-blocking)
aws ec2 start-instances --instance-ids $INSTANCE_IDS

echo "🚀 Start command sent. Instances are transitioning to 'running' state."
