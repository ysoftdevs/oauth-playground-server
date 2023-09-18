package com.ysoft.geecon.helpers;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.FormElement;

import java.io.IOException;

public class LoginScreen {

    private final FormElement form;

    public LoginScreen(Document doc) {
        this.form = doc.expectForm("form");
        ;
    }

    public Document submit(String username, String password) throws IOException {
        form.getElementsByAttributeValue("name", "username").val(username);
        form.getElementsByAttributeValue("name", "password").val(password);

        return form.submit().post();
    }

    public ConsentScreen submitCorrect(String username, String password) throws IOException {
        Document posted = submit(username, password);
        return new ConsentScreen(posted);
    }

    public LoginScreen submitWrong(String username, String password) throws IOException {
        Document posted = submit(username, password);
        return new LoginScreen(posted);
    }
}
