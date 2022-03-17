/*
 * Copyright 2021 European Commission
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package eu.europa.ec.digit.leos.pilot.export.controller;

import eu.europa.ec.digit.leos.pilot.export.exception.LeosDocumentException;
import eu.europa.ec.digit.leos.pilot.export.model.ConvertDocumentInput;
import eu.europa.ec.digit.leos.pilot.export.service.LeosDocumentService;
import eu.europa.ec.digit.leos.pilot.export.util.ConstantsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class LeosDocumentApiController {

    private static final Logger LOG = LoggerFactory.getLogger(LeosDocumentApiController.class);

    private final LeosDocumentService leosDocumentService;

    public LeosDocumentApiController(LeosDocumentService leosDocumentService) {
        this.leosDocumentService = leosDocumentService;
    }

    private ResponseEntity<Object> buildErrorResponse(String message, Exception e, HttpStatus httpStatus) {
        return buildErrorResponse(message, e, httpStatus, false);
    }

    private ResponseEntity<Object> buildErrorResponse(String message, Exception e, HttpStatus httpStatus, boolean isSevere) {
        if (isSevere) {
            LOG.error(message, e);
        } else {
            LOG.info(message, e);
        }
        return new ResponseEntity<>(message + ": " + e.getMessage(), httpStatus);
    }

    private ResponseEntity<Object> buildValidZipResponse(byte[] outputFile) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(
                ContentDisposition
                        .builder("attachment")
                        .filename("result_" + System.currentTimeMillis() + ".zip")
                        .build());
        headers.setContentType(MediaType.valueOf(ConstantsUtil.APPLICATION_ZIP_VALUE));
        headers.setContentLength(outputFile.length);
        return new ResponseEntity<>(outputFile, headers, HttpStatus.OK);
    }

    @RequestMapping(value = "/getRenditions", method = RequestMethod.POST, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public ResponseEntity<Object> getRenditions(@RequestParam MultipartFile inputFile,
                                                    @RequestParam(required = false, defaultValue = "false") boolean isWithAnnotations) {
        try {
            final ConvertDocumentInput convertDocumentInput = leosDocumentService.createDocumentInput(
                    inputFile,
                    isWithAnnotations
            );
            byte[] convertDocumentOutput = leosDocumentService.getRenditions(convertDocumentInput);
            return buildValidZipResponse(convertDocumentOutput);
        } catch (LeosDocumentException e) {
            return buildErrorResponse("Issue processing the document", e, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return buildErrorResponse("Error found while processing the document", e, HttpStatus.INTERNAL_SERVER_ERROR, true);
        }
    }

    @RequestMapping("/test")
    public String test() { return "Test RESTful service"; }
}
