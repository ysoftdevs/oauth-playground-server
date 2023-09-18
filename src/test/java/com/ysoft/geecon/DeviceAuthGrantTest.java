package com.ysoft.geecon;

import com.ysoft.geecon.dto.DeviceResponse;
import com.ysoft.geecon.dto.OAuthClient;
import com.ysoft.geecon.dto.User;
import com.ysoft.geecon.repo.ClientsRepo;
import com.ysoft.geecon.repo.UsersRepo;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.path.xml.XmlPath;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class DeviceAuthGrantTest {
    @Inject
    ClientsRepo clientsRepo;
    @Inject
    UsersRepo usersRepo;


    @Test
    public void deviceAuthGrant_invalidCode() {
        given().formParam("code", "somecode").
                when().post("/auth/device-login").
                then().statusCode(404);
    }

    @Test
    public void deviceAuthGrant() {
        clientsRepo.register(new OAuthClient("myclient", "", null, null));
        usersRepo.register(new User("bob", "password"));

        DeviceResponse deviceResponse = given().
                formParam("client_id", "myclient").
                when().post("/auth/device")
                .then()
                .statusCode(200)
                .contentType(JSON)
                .body("device_code", is(notNullValue()))
                .body("user_code", is(notNullValue()))
                .body("verification_uri", is(notNullValue()))
                .body("interval", is(notNullValue()))
                .body("expires_in", is(notNullValue()))
                .extract().body().as(DeviceResponse.class);

        String sessionId = given().formParam("code", deviceResponse.userCode()).
                when().post("/auth/device-login").
                then().statusCode(200)
                .extract().body().xmlPath(XmlPath.CompatibilityMode.HTML)
                .getString("html.body.div.form.input");

        String body = given().
                formParam("sessionId", sessionId).
                formParam("username", "bob").
                formParam("password", "password").
                when().
                post("auth")
                .then().statusCode(200).extract().body().asString();

        assertEquals("aaa", body);

    }

}