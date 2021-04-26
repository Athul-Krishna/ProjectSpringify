package com.athul.springifyrestassured;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.FixMethodOrder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runners.MethodSorters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UsersWebServiceEndpointTest {

    private final String CONTEXT_PATH = "/springify";
    private final String EMAIL = "athulkrishnaks1998@gmail.com";
    private final String JSON = "application/json";
    private static String authorizationHeader;
    private static String userId;
    private static List<Map<String, String>> addresses;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8080;
    }

//    Test User Login endpoint
    @Test
    void a() {
        Map<String, String> loginDetails = new HashMap<>();
        loginDetails.put("email", EMAIL);
        loginDetails.put("password", "PASSWORD");

        Response response = given().contentType(JSON).accept(JSON).body(loginDetails)
                .when().post(CONTEXT_PATH + "/users/login")
                .then().statusCode(200).extract().response();

        authorizationHeader = response.header("Authorization");
        userId = response.header("UserId");
        assertNotNull(authorizationHeader);
        assertNotNull(userId);
    }

//    Test Get User endpoint
    @Test
    void b() {
        Response response = given().pathParam("id", userId)
                .header("Authorization", authorizationHeader).accept(JSON)
                .when().get(CONTEXT_PATH + "/users/{id}")
                .then().statusCode(200).contentType(JSON).extract().response();

        String userPublicId = response.jsonPath().getString("userId");
        String userEmail = response.jsonPath().getString("email");
        String firstName = response.jsonPath().getString("firstName");
        String lastName = response.jsonPath().getString("lastName");
        addresses = response.jsonPath().getList("addresses");
        String addressId = addresses.get(0).get("addressId");

        assertNotNull(userPublicId);
        assertNotNull(userEmail);
        assertNotNull(firstName);
        assertNotNull(lastName);
        assertEquals(EMAIL, userEmail);
        assertTrue(addresses.size() == 2);
        assertTrue(addressId.length() == 30);
    }

//    Test Update User endpoint
    @Test
    void c() {
        Map<String, Object> userDetails = new HashMap<>();
        userDetails.put("firstName", "Jean");
        userDetails.put("lastName", "Doe");

        Response response = given().contentType(JSON).accept(JSON).body(userDetails)
                .header("Authorization", authorizationHeader).pathParam("id", userId)
                .when().put(CONTEXT_PATH + "/users/{id}")
                .then().statusCode(200).contentType(JSON).extract().response();

        String firstName = response.jsonPath().getString("firstName");
        String lastName = response.jsonPath().getString("lastName");
        List<Map<String, String>> storedAddresses = response.jsonPath().getList("addresses");

        assertEquals("Jean", firstName);
        assertEquals("Doe", lastName);
        assertNotNull(storedAddresses);
        assertTrue(addresses.size() == storedAddresses.size());
        assertEquals(addresses.get(0).get("street"), storedAddresses.get(0).get("street"));
    }

//    Test Delete User endpoint
    @Test
    void d() {
        Response response = given().accept(JSON)
                .header("Authorization", authorizationHeader).pathParam("id", userId)
                .when().delete(CONTEXT_PATH + "/users/{id}")
                .then().statusCode(200).contentType(JSON).extract().response();

        String operationResult = response.jsonPath().getString("operationResult");
        assertEquals("SUCCESS", operationResult);
    }
}
