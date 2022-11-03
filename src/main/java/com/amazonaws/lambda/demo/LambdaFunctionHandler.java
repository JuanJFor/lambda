package com.amazonaws.lambda.demo;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.CreatePlatformEndpointRequest;
import com.amazonaws.services.sns.model.CreatePlatformEndpointResult;
import com.amazonaws.services.sns.model.DeleteEndpointRequest;
import com.amazonaws.services.sns.model.DeleteEndpointResult;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.SubscribeResult;
import com.amazonaws.services.sns.model.UnsubscribeResult;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import software.amazon.awssdk.services.sns.model.SnsException;

public class LambdaFunctionHandler implements RequestHandler<Map<String, String>, String> {

	private static final String USER_KEY = "user";
	private static final String TOKEN_KEY = "token";
	private static final String ARN_KEY = "arn";
	private static final String ARN_TOPIC_KEY = "arntopic";

	private DynamoDB dynamoDb;
	private String DYNAMODB_TABLE_NAME = "MobileTokens";
	private Regions REGION = Regions.US_EAST_1;

	private static final Logger LOGGER = Logger.getLogger(LambdaFunctionHandler.class.getName());

	public String handleRequest(Map<String, String> values, Context context) {

		LOGGER.info("Handling request");

		LOGGER.info(values.toString());

		this.initDynamoDbClient();

		if (!values.containsKey(USER_KEY) || !values.containsKey(TOKEN_KEY) || !values.containsKey(ARN_KEY)
				|| !values.containsKey(ARN_TOPIC_KEY)) {
			return "Faltan parametros de validacion";
		}

		String token = values.get(TOKEN_KEY);
		String user = values.get(USER_KEY);
		String platformApplicationArn = values.get(ARN_KEY);
		String topicArn = values.get(ARN_TOPIC_KEY);

		Table table = this.dynamoDb.getTable(DYNAMODB_TABLE_NAME);

		Item item = new Item();

		Item itemsearch = table.getItem("user", user, "arn", platformApplicationArn);

		AmazonSNS client = AmazonSNSClientBuilder.standard().build();

		if (itemsearch != null) {
			LOGGER.info("Delete request Endpoint");
			DeleteEndpointResult resultd = deleteEndpoint(client, itemsearch.getString("arnendpoint"), topicArn,
					itemsearch.getString("arntopicendpoint"));
			table.deleteItem("user", user, "arn", platformApplicationArn);
		}

		LOGGER.info("Create request Endpoint");
		Map map = createEndpoint(client, token, platformApplicationArn, topicArn);
		item.withString("arnendpoint", ((CreatePlatformEndpointResult) map.get("resultEndpoint")).getEndpointArn());
		item.withString(TOKEN_KEY, token);
		item.withString(USER_KEY, user);
		item.withString(ARN_KEY, platformApplicationArn);
		item.withString(ARN_TOPIC_KEY, topicArn);
		item.withString("arntopicendpoint", ((SubscribeResult) map.get("resultsubcirbe")).getSubscriptionArn());

		this.dynamoDb.getTable(DYNAMODB_TABLE_NAME).putItem(item);

		Gson gson = new Gson();

		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("response", "Success");

		return gson.toJson(jsonObject);

	}

	public static Map createEndpoint(AmazonSNS client, String token, String platformApplicationArn, String topicArn) {

		Map map = new HashMap();

		try {
			CreatePlatformEndpointRequest platformEndpointRequest = new CreatePlatformEndpointRequest();
			platformEndpointRequest.setPlatformApplicationArn(platformApplicationArn);
			platformEndpointRequest.setToken(token);

			CreatePlatformEndpointResult resultEndpoint = client.createPlatformEndpoint(platformEndpointRequest);
			map.put("resultEndpoint", resultEndpoint);
			LOGGER.info("Amazon Create Endpoint reg result: " + resultEndpoint);

			SubscribeResult resultsubcirbe = client.subscribe(topicArn, "application", resultEndpoint.getEndpointArn());
			LOGGER.info("Amazon Subscribre Topic: " + resultsubcirbe);
			map.put("resultsubcirbe", resultsubcirbe);

		} catch (SnsException e) {
			LOGGER.info("Amazon fail Push reg result: " + e.awsErrorDetails().errorMessage());
		}
		return map;
	}

	public static DeleteEndpointResult deleteEndpoint(AmazonSNS client, String endPointArn, String topicArn,
			String arntopicendpoint) {

		DeleteEndpointResult resultEndpoint = null;

		try {

			DeleteEndpointRequest deletePlatformApplicationRequest = new DeleteEndpointRequest();
			deletePlatformApplicationRequest.setEndpointArn(endPointArn);

			resultEndpoint = client.deleteEndpoint(deletePlatformApplicationRequest);

			LOGGER.info("Amazon Delete Endpoint reg result: " + resultEndpoint);

			UnsubscribeResult resultsubcirbe = client.unsubscribe(arntopicendpoint);
			LOGGER.info("Amazon Delete Suscription reg result: " + resultsubcirbe);

		} catch (SnsException e) {
			LOGGER.info("Amazon fail Delete reg result: " + e.awsErrorDetails().errorMessage());
		}
		return resultEndpoint;
	}

	private void initDynamoDbClient() {
		AmazonDynamoDBClient client = new AmazonDynamoDBClient();

		client.setRegion(Region.getRegion(REGION));
		this.dynamoDb = new DynamoDB(client);
	}

}
