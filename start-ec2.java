import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

import java.util.List;
import java.util.stream.Collectors;

public class StartEc2Instances {

    // Hardcoded VPC ID (same as bash script)
    private static final String VPC_ID = "vpc-0a50c2295b2293771";

    public static void main(String[] args) {

        try (Ec2Client ec2 = Ec2Client.create()) {

            System.out.println("🔍 Fetching stopped EC2 instances in VPC: " + VPC_ID + "...");

            // Build filters (VPC + stopped state)
            Filter vpcFilter = Filter.builder()
                    .name("vpc-id")
                    .values(VPC_ID)
                    .build();

            Filter stateFilter = Filter.builder()
                    .name("instance-state-name")
                    .values("stopped")
                    .build();

            DescribeInstancesRequest request = DescribeInstancesRequest.builder()
                    .filters(vpcFilter, stateFilter)
                    .build();

            DescribeInstancesResponse response = ec2.describeInstances(request);

            // Extract instance IDs
            List<String> instanceIds = response.reservations().stream()
                    .flatMap(reservation -> reservation.instances().stream())
                    .map(Instance::instanceId)
                    .collect(Collectors.toList());

            if (instanceIds.isEmpty()) {
                System.out.println("✅ No stopped instances found in VPC " + VPC_ID);
                return;
            }

            System.out.println("📌 Instances to start: " + instanceIds);

            // Start instances (non-blocking)
            StartInstancesRequest startRequest = StartInstancesRequest.builder()
                    .instanceIds(instanceIds)
                    .build();

            ec2.startInstances(startRequest);

            System.out.println("🚀 Start command sent. Instances are transitioning to 'running' state.");

        } catch (Ec2Exception e) {
            System.err.println("❌ AWS EC2 error: " + e.awsErrorDetails().errorMessage());
        }
    }
}
