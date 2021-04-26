package com.athul.springifyrestassured;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

public class TestCreateUser {

    private final String CONTEXT_PATH = "/springify";

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8080;
    }

    @Test
    void testCreateUser() {
        Map<String, Object> shippingAddress = new HashMap<>();
        shippingAddress.put("city", "Vancouver");
        shippingAddress.put("country", "Canada");
        shippingAddress.put("street", "123 Street");
        shippingAddress.put("postalCode", "123456");
        shippingAddress.put("type", "shipping");

        Map<String, Object> billingAddress = new HashMap<>();
        billingAddress.put("city", "Vancouver");
        billingAddress.put("country", "Canada");
        billingAddress.put("street", "123 Street");
        billingAddress.put("postalCode", "123456");
        billingAddress.put("type", "billing");

        List<Map<String, Object>> userAddresses = new ArrayList<>();
        userAddresses.add(shippingAddress);
        userAddresses.add(billingAddress);

        Map<String, Object> userDetails = new HashMap<>();
        userDetails.put("firstName", "John");
        userDetails.put("lastName", "Doe");
        userDetails.put("email", "athulkrishnaks1998@gmail.com");
        userDetails.put("password", "PASSWORD");
        userDetails.put("addresses", userAddresses);

        Response response = given().contentType("application/json").accept("application/json").body(userDetails)
                .when().post(CONTEXT_PATH + "/users")
                .then().statusCode(200).contentType("application/json").extract().response();

        String userId = response.jsonPath().getString("userId");
        assertNotNull(userId);
        assertTrue(userId.length() == 30);

        String bodyString = response.body().asString();
        try {
            JSONObject responseBodyJson = new JSONObject(bodyString);
            JSONArray addresses = responseBodyJson.getJSONArray("addresses");
            assertNotNull(addresses);
            assertTrue(addresses.length() == userAddresses.size());

            String addressId = addresses.getJSONObject(0).getString("addressId");
            assertNotNull(addressId);
            assertTrue(addressId.length() == 30);
        } catch (JSONException e) {
            fail(e.getMessage());
        }
    }
}
