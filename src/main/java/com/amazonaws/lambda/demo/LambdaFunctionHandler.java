package com.amazonaws.lambda.demo;

import java.math.BigDecimal;
import java.util.Map;
import java.util.logging.Logger;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.lambda.demo.entity.Response;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.CreatePlatformEndpointRequest;
import com.amazonaws.services.sns.model.CreatePlatformEndpointResult;
import com.amazonaws.services.sns.model.DeleteEndpointRequest;
import com.amazonaws.services.sns.model.DeleteEndpointResult;
import com.amazonaws.services.sns.model.DeletePlatformApplicationRequest;
import com.amazonaws.services.sns.model.DeletePlatformApplicationResult;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;

import software.amazon.awssdk.services.sns.SnsClient;

import software.amazon.awssdk.services.sns.model.SnsException;


import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;


public class LambdaFunctionHandler implements RequestHandler<Map<String, String>, String> {

	private static final String USER_KEY = "user";
	private static final String TOKEN_KEY = "token";
	private static final String ARN_KEY = "arn";

	private DynamoDB dynamoDb;
	private String DYNAMODB_TABLE_NAME = "MobileTokens";
	private Regions REGION = Regions.US_EAST_1;

	private static final Logger LOGGER = Logger.getLogger(LambdaFunctionHandler.class.getName());

	public String handleRequest(Map<String, String> values, Context context) {

		LOGGER.info("Handling request");

		LOGGER.info(values.toString());

		this.initDynamoDbClient();

		

		if (!values.containsKey(USER_KEY) || !values.containsKey(TOKEN_KEY) || !values.containsKey(ARN_KEY)) {
			return "Faltan parametros de validacion";
		}

		String token = values.get(TOKEN_KEY);
		String user = values.get(USER_KEY);
		String platformApplicationArn = values.get(ARN_KEY);
		
		
		
		Table table =this.dynamoDb.getTable(DYNAMODB_TABLE_NAME);
		
		
		Item item=new Item();

		Item itemsearch = table.getItem("user",user,"arn",platformApplicationArn);
		
		AmazonSNS client = AmazonSNSClientBuilder.standard().build();
		
		
		if(itemsearch!=null) {
			LOGGER.info("Delete request Endpoint");
			DeleteEndpointResult resultd = deleteEndpoint(client, itemsearch.getString("arnendpoint"));
			table.deleteItem("user",user,"arn",platformApplicationArn);
		}
		
		LOGGER.info("Create request Endpoint");
		CreatePlatformEndpointResult resultc = createEndpoint(client, token, platformApplicationArn);
		item.withString("arnendpoint", resultc.getEndpointArn());
		item.withString("token", token);
		item.withString("user", user);
		item.withString("arn", platformApplicationArn);
		
		
		this.dynamoDb.getTable(DYNAMODB_TABLE_NAME).putItem(item);

		 
		Gson gson = new GsonBuilder()
		       .disableHtmlEscaping()
		       .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
		       .setPrettyPrinting()
		       .serializeNulls()
		       .create();		
		
		Response res=new Response();
		res.setRespose("Success");
		
		return gson.toJson(res);

	}

	public static CreatePlatformEndpointResult createEndpoint(AmazonSNS client, String token,
			String platformApplicationArn) {

		CreatePlatformEndpointResult result = null;

		try {
			CreatePlatformEndpointRequest platformEndpointRequest = new CreatePlatformEndpointRequest();
			platformEndpointRequest.setPlatformApplicationArn(platformApplicationArn);
			platformEndpointRequest.setToken(token);

			result = client.createPlatformEndpoint(platformEndpointRequest);

			LOGGER.info("Amazon Push reg result: " + result);

		} catch (SnsException e) {
			LOGGER.info("Amazon fail Push reg result: " + e.awsErrorDetails().errorMessage());
		}
		return result;
	}

	public static DeleteEndpointResult deleteEndpoint(AmazonSNS client, String endPointArn) {

		DeleteEndpointResult result = null;

		try {

			DeleteEndpointRequest deletePlatformApplicationRequest = new DeleteEndpointRequest();
			deletePlatformApplicationRequest.setEndpointArn(endPointArn);

			result = client.deleteEndpoint(deletePlatformApplicationRequest);

		} catch (SnsException e) {
			LOGGER.info("Amazon fail Push reg result: " + e.awsErrorDetails().errorMessage());
		}
		return result;
	}

	private void initDynamoDbClient() {
		AmazonDynamoDBClient client = new AmazonDynamoDBClient();

		client.setRegion(Region.getRegion(REGION));
		this.dynamoDb = new DynamoDB(client);
	}

}
