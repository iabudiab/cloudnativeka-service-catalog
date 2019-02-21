package de.iabudiab.cloudnativeka.showcase;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import de.iabudiab.cloudnativeka.showcase.reko.DetectedLabel;
import de.iabudiab.cloudnativeka.showcase.reko.RekognitionService;

@RestController
public class UploadController {

	@Autowired
	private RekognitionService service;

	@PostMapping(path = "/upload")
	public List<DetectedLabel> upload(@RequestBody ImageUpload upload) {
		return service.process(upload);
	}
}
