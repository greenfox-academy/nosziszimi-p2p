package com.greenfox.nori.peertopeerchatapp.controller;

import com.greenfox.nori.peertopeerchatapp.model.Client;
import com.greenfox.nori.peertopeerchatapp.model.IncomingJSON;
import com.greenfox.nori.peertopeerchatapp.model.Message;
import com.greenfox.nori.peertopeerchatapp.model.StatusError;
import com.greenfox.nori.peertopeerchatapp.model.StatusOk;
import com.greenfox.nori.peertopeerchatapp.service.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

/**
 * Created by Nóra on 2017. 05. 22..
 */
@org.springframework.web.bind.annotation.RestController
@CrossOrigin("*")
public class RestController {

  @Autowired
  Service service;

  @ExceptionHandler()
  public StatusError exception(){
    return new StatusError("error", "");
  }

  @PostMapping("/api/message/receive")
  public ResponseEntity<?> receiveMessage(@RequestBody IncomingJSON incoming) {
    String missingField = "";
    Message message = incoming.getMessage();
    if (message.getId() == 0) {
      missingField += "message.id, ";
    }
    if (message.getUsername() == null) {
      missingField += "message.username, ";
    }
    if(message.getText() == null) {
      missingField += "message.text, ";
    }
    if(message.getTimestamp() == null) {
      missingField += "message.timestamp, ";
    }
    if(incoming.getClient().getId() == null) {
    missingField += "client.id";
    }

    if(missingField.length() != 0) {
      StatusError error = new StatusError
              ("error", "Missing field(s): " + missingField);
      return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    } else {
      StatusOk statusOk = new StatusOk("ok");
      if (!incoming.getClient().getId().equals(System.getenv("CHAT_APP_UNIQUE_ID"))
              && !service.repeatedMessage(incoming.getMessage())) {
        service.saveMessage(message);
        RestTemplate restTemplate = new RestTemplate();
        //String text = incoming.getMessage().getText();
        //text += "-Nóri-";
        //incoming.getMessage().setText(text);
        restTemplate.postForObject("https://peertopeerchatapp.herokuapp.com/api/message/receive"
                , incoming, StatusOk.class);
        restTemplate.postForObject(System.getenv("CHAT_APP_PEER_ADDRESS")
                , incoming, StatusOk.class);
      }
      return new ResponseEntity<>(statusOk, HttpStatus.OK);
    }
  }
}
