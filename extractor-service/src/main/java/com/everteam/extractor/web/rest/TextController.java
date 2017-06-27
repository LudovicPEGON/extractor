package com.everteam.extractor.web.rest;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.everteam.extractor.domain.TextData;
import com.everteam.extractor.service.TextService;

/**
 * Controller class responsible for handling requests of extractor application.
 * 
 * @author arpita
 *
 */
@RestController
@RequestMapping("/api/text")
public class TextController {

	@Autowired
	private TextService textService;

	@RequestMapping(value = "/uri", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public TextData getText(@RequestParam String uri) throws MalformedURLException, IOException, URISyntaxException {
		return textService.getText(uri);
	}

	@RequestMapping(value = "/binary", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public TextData getText(@RequestPart("file") MultipartFile file){
		try {
			return textService.getText(file);
		} catch (IOException e) {
			return null;
		}
	}
}
