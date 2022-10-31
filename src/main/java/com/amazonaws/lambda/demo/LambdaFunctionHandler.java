package com.amazonaws.lambda.demo;

import java.math.BigDecimal;
import java.util.Map;
import java.util.logging.Logger;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class LambdaFunctionHandler  implements RequestHandler<Map<String,String>,String> {

    private static final String NUMERATOR_KEY = "numerator";
    private static final String DENOMINATOR_KEY = "denominator";

    private static final Logger LOGGER = Logger.getLogger(LambdaFunctionHandler.class.getName());

    public String handleRequest(Map <String,String> values, Context context) {

        LOGGER.info("Handling request");

        LOGGER.info(values.toString());
        
        if(!values.containsKey(NUMERATOR_KEY)||!values.containsKey(DENOMINATOR_KEY)) {
            return "You need both numberator and denominator";
        }

        try {
            BigDecimal numerator = new BigDecimal(values.get(NUMERATOR_KEY));
            BigDecimal denominator= new BigDecimal(values.get(DENOMINATOR_KEY));
            return  numerator.divide(denominator).toString();
        } catch (Exception e) {
            return "Please provide valid values";
        }
    }
}
