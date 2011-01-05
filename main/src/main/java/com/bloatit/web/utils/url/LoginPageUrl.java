package com.bloatit.web.utils.url;

import com.bloatit.web.exceptions.RedirectException;

@SuppressWarnings("unused")
public final class LoginPageUrl extends Url {
public static String getName() { return "login"; }
public com.bloatit.web.html.pages.LoginPage createPage() throws RedirectException{ 
    return new com.bloatit.web.html.pages.LoginPage(this); }
public LoginPageUrl(Parameters params, Parameters session) {
    this();
    parseParameters(params, false);
    parseParameters(session, true);
}
public LoginPageUrl(){
    super(getName());
}


@Override 
protected void doRegister() { 
}

@Override 
public LoginPageUrl clone() { 
    LoginPageUrl other = new LoginPageUrl();
    return other;
}
}
