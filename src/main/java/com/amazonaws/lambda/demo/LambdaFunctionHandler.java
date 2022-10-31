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

	private static final String NUMERATOR_KEY = "numerator";
	private static final String DENOMINATOR_KEY = "denominator";

	private static final Logger LOGGER = Logger.getLogger(LambdaFunctionHandler.class.getName());

	public String handleRequest(Map<String, String> values, Context context) {

		LOGGER.info("Handling request");

		LOGGER.info(values.toString());


		String token = "cwPfsZnPSMaNmzGF4Aggfi:APA91bHr0pDM-BTdVUvkn0BXoe1qLnmAEIW1llHNEhf7saTNE-fKiJ-OB9aU4JD0wp8BSeJHmtrwVMk8owyjhw510hJ1Xirq6STdBxiOoZHkc1WCXqGnZKjf3UDA5yKty4mzlc14VYTm";
		String platformApplicationArn = "arn:aws:sns:us-east-1:951069153692:app/GCM/Coffe-shop";

		AmazonSNS client = AmazonSNSClientBuilder.standard().build();


		LOGGER.info("Create request");
		
		
		CreatePlatformEndpointRequest platformEndpointRequest = new CreatePlatformEndpointRequest();
		platformEndpointRequest.setPlatformApplicationArn(platformApplicationArn);
		platformEndpointRequest.setToken(token);

		CreatePlatformEndpointResult result = client.createPlatformEndpoint(platformEndpointRequest);
		LOGGER.info("Amazon Push reg result: " + result);
		
		return "Amazon Push reg result: " + result;

	}



}
