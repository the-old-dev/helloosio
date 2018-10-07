/*
 * Copyright 2016-2017 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.openshift.booster.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.objects.Cursor;
import org.dizitart.no2.objects.ObjectRepository;

@RestController
public class GreetingController {

    private final GreetingProperties properties;
    private String errors;
    private Nitrite db;
    private ObjectRepository<Greeting> repository;
    
     Logger logger = LoggerFactory.getLogger(GreetingController.class);
    
    @Autowired
    public GreetingController(GreetingProperties properties) {
        this.properties = properties;
        this.errors ="Errors:\n";
        initialize();
    }

    @RequestMapping("/api/greeting")
    public Greeting greeting(@RequestParam(value="name", defaultValue="World") String name) {
        String message = String.format(properties.getMessage(), name);
        Greeting greeting = new Greeting(message, errors);
        
        try{
            repository.insert(greeting);
            logger.info("NitriteDB: Repository insert happend");
        } catch (Exception e) {
            this.errors = errors + e.toString();
            logger.error(e.toString());
        }   
        
        return greeting;
    }
    
    @RequestMapping("/api/greetings")
    public Cursor<Greeting> greetings() {
        return  repository.find();
    }

    private void initialize() {
        
        logger.info("NitriteDB: starting ...");
        
        try {
            
            // Initialize DB
            db = Nitrite.builder()
                .compressed()
                .filePath("test.db")
                .openOrCreate("user", "password");
                
            logger.info("NitriteDB: started -" + db);
                   
            // Initialize an Object Repository
            repository = db.getRepository(Greeting.class);
            logger.info("NitriteDB: Repository created");
            
        } catch (Exception e) {
            this.errors = errors + e.toString();
            logger.error(e.toString());
        }    

    }
    
}