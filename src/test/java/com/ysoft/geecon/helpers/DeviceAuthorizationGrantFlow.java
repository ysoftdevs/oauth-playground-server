package com.ysoft.geecon.helpers;

import com.ysoft.geecon.dto.AccessTokenResponse;
import com.ysoft.geecon.dto.DeviceResponse;
import com.ysoft.geecon.dto.OAuthClient;

import java.io.IOException;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

public class DeviceAuthorizationGrantFlow {
    private final String deviceUrl;
    private final OAuthClient client;
    private DeviceResponse deviceResponse;

    public DeviceAuthorizationGrantFlow(String deviceUrl, OAuthClient client) {
        this.deviceUrl = deviceUrl;
        this.client = client;
    }

    public DeviceResponse start() throws IOException {
        deviceResponse = given().
                formParam("client_id", client.clientId()).
                when().post(deviceUrl)
                .then()
                .statusCode(200)
                .contentType(JSON)
                .body("device_code", is(notNullValue()))
                .body("user_code", is(notNullValue()))
                .body("verification_uri", is(notNullValue()))
                .body("interval", is(notNullValue()))
                .body("expires_in", is(notNullValue()))
                .extract().body().as(DeviceResponse.class);

        return deviceResponse;
    }

    public AccessTokenResponse exchangeDeviceCode() {
        return given()
                .formParam("grant_type", "urn:ietf:params:oauth:grant-type:device_code")
                .formParam("client_id", client.clientId())
                .formParam("device_code", deviceResponse.deviceCode())
                .when()
                .post("/auth/token")
                .then()
                .statusCode(200)
                .contentType(JSON)
                .body("token_type", is(notNullValue()))
                .body("expires_in", is(notNullValue()))
                .body("access_token", is(notNullValue()))
                .body("refresh_token", is(notNullValue()))
                .extract().as(AccessTokenResponse.class);
    }

    public String exchangeDeviceCodeError() {
        return given()
                .formParam("grant_type", "urn:ietf:params:oauth:grant-type:device_code")
                .formParam("client_id", client.clientId())
                .formParam("device_code", deviceResponse.deviceCode())
                .when()
                .post("/auth/token")
                .then()
                .statusCode(400).extract().asString();
//                .contentType(JSON)
//                .body("error", is(notNullValue()))
//                .body("error_detail", is(notNullValue()))
//                .extract().as(OAuthException.ErrorResponse.class);
    }
}
