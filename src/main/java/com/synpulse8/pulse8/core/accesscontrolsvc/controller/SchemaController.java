package com.synpulse8.pulse8.core.accesscontrolsvc.controller;

import com.synpulse8.pulse8.core.accesscontrolsvc.service.SchemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/")
public class SchemaController {

    @Autowired
    SchemaService service;

    @GetMapping(value = "/")
    public @ResponseBody
    String index() {
        return "Pulse8 Core Access Control";
    }

    @GetMapping(value = "/get-schema")
    public @ResponseBody
    String getSchema() {
        return service.getSchema();
    }

}
