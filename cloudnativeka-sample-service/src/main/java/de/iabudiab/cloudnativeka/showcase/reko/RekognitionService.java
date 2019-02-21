package de.iabudiab.cloudnativeka.showcase.reko;

import static java.util.stream.Collectors.toList;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import de.iabudiab.cloudnativeka.showcase.ImageUpload;
import de.iabudiab.cloudnativeka.showcase.s3.S3Service;
import software.amazon.awssdk.core.auth.AwsCredentials;
import software.amazon.awssdk.core.auth.StaticCredentialsProvider;
import software.amazon.awssdk.core.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsRequest;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsResponse;
import software.amazon.awssdk.services.rekognition.model.Image;

@Service
public class RekognitionService {

	@Autowired
	private S3Service s3Service;

	@Value("${aws.reko.region:eu-west-1}")
	private String region;

	@Value("${aws.reko.accessKeyId}")
	private String accessKeyId;

	@Value("${aws.reko.secretAccessKey}")
	private String secretAccessKey;

	public List<DetectedLabel> process(ImageUpload upload) {
		AwsCredentials credentials = AwsCredentials.create(accessKeyId, secretAccessKey);
		StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);

		RekognitionClient rekognition = RekognitionClient.builder() //
				.region(Region.of(region)) //
				.credentialsProvider(credentialsProvider) //
				.build();

		byte[] imageBytes = Base64.getDecoder().decode(upload.getData());
		ByteBuffer byteBuffer = ByteBuffer.wrap(imageBytes);
		Image image = Image.builder().bytes(byteBuffer).build();

		DetectLabelsRequest request = DetectLabelsRequest.builder().image(image).build();

		DetectLabelsResponse response = rekognition.detectLabels(request);

		List<DetectedLabel> labels = response.labels().stream() //
				.map(DetectedLabel::new) //
				.collect(toList());

		s3Service.filterAndStore(upload, imageBytes, labels);

		return labels;
	}
}
