package myproject;

import java.util.List;

import com.pulumi.Pulumi;
import com.pulumi.asset.FileAsset;
import com.pulumi.aws.s3.Bucket;
import com.pulumi.aws.s3.BucketArgs;
import com.pulumi.aws.s3.BucketObject;
import com.pulumi.aws.s3.BucketObjectArgs;
import com.pulumi.aws.s3.BucketOwnershipControls;
import com.pulumi.aws.s3.BucketOwnershipControlsArgs;
import com.pulumi.aws.s3.BucketPublicAccessBlock;
import com.pulumi.aws.s3.BucketPublicAccessBlockArgs;
import com.pulumi.aws.s3.inputs.BucketOwnershipControlsRuleArgs;
import com.pulumi.aws.s3.inputs.BucketWebsiteArgs;
import com.pulumi.resources.CustomResourceOptions;

public class App {
    public static void main(String[] args) {
        Pulumi.run(ctx -> {
            var bucket = new Bucket("my-bucket", BucketArgs.builder()
                    .website(BucketWebsiteArgs.builder()
                            .indexDocument("index.html")
                            .errorDocument("error.html")
                            .build())
                    .build());

            var ownershipControls = new BucketOwnershipControls("ownershipControls",
                    BucketOwnershipControlsArgs.builder()
                            .bucket(bucket.id())
                            .rule(BucketOwnershipControlsRuleArgs.builder()
                                    .objectOwnership("ObjectWriter")
                                    .build())
                            .build());

            var publicAccessBlock = new BucketPublicAccessBlock("publicAccessBlock",
                    BucketPublicAccessBlockArgs.builder()
                            .bucket(bucket.id())
                            .blockPublicAcls(false)
                            .build());

        List.of("index.html", "error.html").forEach(file -> {
            BucketObject bucketObject = new BucketObject(file, BucketObjectArgs.builder()
                    .bucket(bucket.id())
                    .source(new FileAsset("./" + file))
                    .contentType("text/html")
                    .acl("public-read")
                    .build(),
                    CustomResourceOptions.builder()
                            .dependsOn(
                                    publicAccessBlock,
                                    ownershipControls)
                            .build());
            // Use the bucketObject instance here if needed
        });

            ctx.export("bucketName", bucket.bucket());
            ctx.export("bucketEndpoint", bucket.websiteEndpoint().applyValue(
                websiteEndpoint -> String.format("http://%s", websiteEndpoint)));
        });
    }
}
