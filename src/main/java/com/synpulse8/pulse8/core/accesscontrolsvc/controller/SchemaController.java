package com.synpulse8.pulse8.core.accesscontrolsvc.controller;

import com.synpulse8.pulse8.core.accesscontrolsvc.service.SchemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.net.http.HttpHeaders;

@RestController
@RequestMapping(value = "/")
public class SchemaController {

    @Autowired
    SchemaService service;

    @GetMapping(value = "/")
    public @ResponseBody
    String index(@RequestHeader("X-Consumer-Custom-ID") String userid) {
        return "Pulse8 Core Access Control User: " + userid;
    }

    @GetMapping(value = "/get-schema")
    public @ResponseBody
    String getSchema() {
        return service.getSchema();
    }

}
