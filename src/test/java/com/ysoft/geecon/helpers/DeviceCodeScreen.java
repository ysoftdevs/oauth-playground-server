package com.ysoft.geecon.helpers;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.FormElement;

import java.io.IOException;
import java.net.URI;

public class DeviceCodeScreen {

    private final FormElement form;

    public DeviceCodeScreen(URI verificationUrl) throws IOException {
        this.form = Jsoup.connect(verificationUrl.toString()).get().expectForm("form");
    }

    public LoginScreen enterCode(String code) throws IOException {
        form.getElementsByAttributeValue("name", "code").val(code);
        Document login = form.submit().post();
        return new LoginScreen(login);
    }
}
