package com.ysoft.geecon.helpers;

import com.ysoft.geecon.dto.DeviceResponse;
import com.ysoft.geecon.dto.OAuthClient;

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

    public DeviceResponse start() {
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

    public TokenEndpointCall exchangeDeviceCode() {
        return new TokenEndpointCall(client).deviceCode(deviceResponse.deviceCode());
    }
}
