package com.amazonaws.lambda.demo;

import java.math.BigDecimal;
import java.util.Map;
import java.util.logging.Logger;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.CreatePlatformEndpointRequest;
import com.amazonaws.services.sns.model.CreatePlatformEndpointResult;

import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;

import software.amazon.awssdk.services.sns.model.SnsException;

public class LambdaFunctionHandler implements RequestHandler<Map<String, String>, String> {

	private static final String USER_KEY = "user";
	private static final String TOKEN_KEY = "token";
	private static final String ARN_KEY = "arn";

	private static final Logger LOGGER = Logger.getLogger(LambdaFunctionHandler.class.getName());

	public String handleRequest(Map<String, String> values, Context context) {

		LOGGER.info("Handling request");

		LOGGER.info(values.toString());
		
        if(!values.containsKey(USER_KEY)||!values.containsKey(TOKEN_KEY)||!values.containsKey(ARN_KEY)) {
            return "Faltan parametros de validacion";
        }
		


		String token = values.get(TOKEN_KEY);
		String user=values.get(USER_KEY);
		String platformApplicationArn = values.get(ARN_KEY);

		AmazonSNS client = AmazonSNSClientBuilder.standard().build();


		LOGGER.info("Create request");
		
		
		createEndpoint(client, token, platformApplicationArn);
		
		return "Success";

	}
	
	
	
    public static String createEndpoint(AmazonSNS client, String token, String platformApplicationArn){

    	String resultmsg = null;

        try {
    		CreatePlatformEndpointRequest platformEndpointRequest = new CreatePlatformEndpointRequest();
    		platformEndpointRequest.setPlatformApplicationArn(platformApplicationArn);
    		platformEndpointRequest.setToken(token);

    		CreatePlatformEndpointResult result = client.createPlatformEndpoint(platformEndpointRequest);
    		LOGGER.info("Amazon Push reg result: " + result);
    		
    		resultmsg="Amazon Push reg result: " + result;
    		
        } catch ( SnsException e) {
        	resultmsg="Amazon fail Push reg result: " + e.awsErrorDetails().errorMessage();
        }
        return resultmsg;
    }



}
