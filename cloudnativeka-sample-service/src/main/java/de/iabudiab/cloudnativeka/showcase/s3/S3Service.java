package de.iabudiab.cloudnativeka.showcase.s3;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import de.iabudiab.cloudnativeka.showcase.ImageUpload;
import de.iabudiab.cloudnativeka.showcase.reko.DetectedLabel;
import software.amazon.awssdk.core.auth.AwsCredentials;
import software.amazon.awssdk.core.auth.StaticCredentialsProvider;
import software.amazon.awssdk.core.regions.Region;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class S3Service {

	@Value("${aws.s3.region:eu-central-1}")
	private String region;

	@Value("${aws.s3.bucket:demo}")
	private String bucket;

	@Value("${aws.s3.accessKeyId}")
	private String accessKeyId;

	@Value("${aws.s3.secretAccessKey}")
	private String secretAccessKey;

	@Value("${aws.s3.filter:pet}")
	private String filter;

	public void filterAndStore(ImageUpload upload, byte[] imageBytes, List<DetectedLabel> labels) {
		boolean match = labels.stream() //
				.filter(it -> it.getConfidence() > 95) //
				.map(DetectedLabel::getName) //
				.map(String::toLowerCase) //
				.anyMatch(it -> it.equals(filter));

		if (!match) {
			return;
		}

		AwsCredentials credentials = AwsCredentials.create(accessKeyId, secretAccessKey);
		StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);

		S3Client s3 = S3Client.builder() //
				.region(Region.of(region)) //
				.credentialsProvider(
						credentialsProvider) //
				.build();

		PutObjectRequest request = PutObjectRequest.builder() //
				.bucket(bucket) //
				.contentType(String.format("image/%s", upload.getExtension())) //
				.contentEncoding("base64") //
				.key(String.format("%s.%s", upload.getName(), upload.getExtension())).build();

		s3.putObject(request, RequestBody.of(imageBytes));
	}
}
