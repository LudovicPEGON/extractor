package com.everteam.extractor.cucumber.stepdefs;

import com.everteam.extractor.ExtractorApp;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.ResultActions;

import org.springframework.boot.test.context.SpringBootTest;

@WebAppConfiguration
@SpringBootTest
@ContextConfiguration(classes = ExtractorApp.class)
public abstract class StepDefs {

    protected ResultActions actions;

}
