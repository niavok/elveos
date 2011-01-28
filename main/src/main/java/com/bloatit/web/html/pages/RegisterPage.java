/*
 * Copyright (C) 2010 BloatIt. This file is part of BloatIt. BloatIt is free software: you
 * can redistribute it and/or modify it under the terms of the GNU Affero General Public
 * License as published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version. BloatIt is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details. You should have received a copy of the GNU Affero General
 * Public License along with BloatIt. If not, see <http://www.gnu.org/licenses/>.
 */
package com.bloatit.web.html.pages;

import java.util.Map.Entry;

import com.bloatit.web.actions.RegisterAction;
import com.bloatit.web.annotations.ParamContainer;
import com.bloatit.web.annotations.RequestParam;
import com.bloatit.web.annotations.RequestParam.Role;
import com.bloatit.web.exceptions.RedirectException;
import com.bloatit.web.html.components.standard.HtmlDiv;
import com.bloatit.web.html.components.standard.HtmlTitleBlock;
import com.bloatit.web.html.components.standard.form.HtmlDropDown;
import com.bloatit.web.html.components.standard.form.HtmlForm;
import com.bloatit.web.html.components.standard.form.HtmlPasswordField;
import com.bloatit.web.html.components.standard.form.HtmlSubmit;
import com.bloatit.web.html.components.standard.form.HtmlTextField;
import com.bloatit.web.html.pages.master.Page;
import com.bloatit.web.server.Context;
import com.bloatit.web.utils.i18n.Country;
import com.bloatit.web.utils.i18n.Localizator;
import com.bloatit.web.utils.i18n.Localizator.LanguageDescriptor;
import com.bloatit.web.utils.url.RegisterActionUrl;
import com.bloatit.web.utils.url.RegisterPageUrl;

@ParamContainer("member/create")
public final class RegisterPage extends Page {

    @RequestParam(name = RegisterAction.LOGIN_CODE, defaultValue = "", role = Role.SESSION)
    private final String login;

    @RequestParam(name = RegisterAction.PASSWORD_CODE, defaultValue = "", role = Role.SESSION)
    private final String password;

    @RequestParam(name = RegisterAction.EMAIL_CODE, defaultValue = "", role = Role.SESSION)
    private final String email;

    @SuppressWarnings("unused")
    @RequestParam(name = RegisterAction.COUNTRY_CODE, defaultValue = "", role = Role.SESSION)
    private final String country;

    @SuppressWarnings("unused")
    @RequestParam(name = RegisterAction.LANGUAGE_CODE, defaultValue = "", role = Role.SESSION)
    private final String lang;

    public RegisterPage(final RegisterPageUrl url) {
        super(url);
        this.login = url.getLogin();
        this.password = url.getPassword();
        this.email = url.getEmail();
        this.country = url.getCountry();
        this.lang = url.getLang();
    }

    @Override
    protected void doCreate() throws RedirectException {

        final HtmlDiv box = new HtmlDiv("padding_box");
        {

            final HtmlTitleBlock container = new HtmlTitleBlock(Context.tr("Register"), 1);
            final HtmlForm form = new HtmlForm(new RegisterActionUrl().urlString());
            container.add(form);

            final HtmlTextField loginInput = new HtmlTextField(RegisterAction.LOGIN_CODE, Context.trc("Login (noun)", "Login"));
            loginInput.setDefaultValue(login);
            form.add(loginInput);

            final HtmlPasswordField passwordInput = new HtmlPasswordField(RegisterAction.PASSWORD_CODE, Context.tr("Password"));
            passwordInput.setDefaultValue(password);
            form.add(passwordInput);

            final HtmlTextField emailInput = new HtmlTextField(RegisterAction.EMAIL_CODE, Context.tr("Email"));
            emailInput.setDefaultValue(email);
            form.add(emailInput);

            final HtmlDropDown<Country> countryInput = new HtmlDropDown<Country>(RegisterAction.COUNTRY_CODE, Context.tr("Country"));
            for (final Country entry : Country.getAvailableCountries()) {
                countryInput.add(entry);
            }
            form.add(countryInput);

            final HtmlDropDown<LanguageDescriptor> langInput = new HtmlDropDown<LanguageDescriptor>(RegisterAction.LANGUAGE_CODE, Context.tr("Language"));
            for (final Entry<String, LanguageDescriptor> entry : Localizator.getAvailableLanguages().entrySet()) {
                langInput.add(entry.getValue());
            }
            form.add(langInput);

            final HtmlSubmit button = new HtmlSubmit(Context.tr("Submit"));
            form.add(button);

            box.add(container);
        }
        add(box);
    }

    @Override
    protected String getTitle() {
        return Context.tr("Make an offer");
    }

    @Override
    public boolean isStable() {
        return false;
    }
}
