package com.project.ewalet.controller;

import com.project.ewalet.model.BalanceCatalog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
public class BalanceCatalogController {

    @Autowired
    BalanceCatalogMapper balanceCatalogMapper;

    @PostMapping("/get-balance-catalog")
    public ResponseEntity getBalanceCatalog() {

        return new ResponseEntity<BalanceCatalog>(balanceCatalogMapper.getAll(), HttpStatus.OK);
    }

}
