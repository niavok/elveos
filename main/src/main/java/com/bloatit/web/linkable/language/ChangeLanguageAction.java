/*
 * Copyright (C) 2010 BloatIt.
 *
 * This file is part of BloatIt.
 *
 * BloatIt is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BloatIt is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with BloatIt. If not, see <http://www.gnu.org/licenses/>.
 */
package com.bloatit.web.linkable.language;

import java.util.Arrays;
import java.util.Locale;

import com.bloatit.framework.webprocessor.annotations.ParamContainer;
import com.bloatit.framework.webprocessor.annotations.RequestParam;
import com.bloatit.framework.webprocessor.annotations.RequestParam.Role;
import com.bloatit.framework.webprocessor.context.Context;
import com.bloatit.framework.webprocessor.url.Url;
import com.bloatit.web.linkable.master.ElveosAction;
import com.bloatit.web.url.ChangeLanguageActionUrl;
import com.bloatit.web.url.ChangeLanguagePageUrl;

@ParamContainer("language/dochange")
public final class ChangeLanguageAction extends ElveosAction {
    private final ChangeLanguageActionUrl url;

    @RequestParam(role = Role.POSTGET)
    private final String language;

    public ChangeLanguageAction(final ChangeLanguageActionUrl url) {
        super(url);
        this.url = url;
        this.language = url.getLanguage();
    }

    @Override
    protected Url doProcess() {
        if (Arrays.asList(Locale.getISOLanguages()).contains(language)) {
            final Locale l = new Locale(language);
            Context.getLocalizator().forceLanguage(l);
            return session.pickPreferredPage();
        }
        session.notifyWarning(Context.tr("Incorrect language, same player play again!"));
        return new ChangeLanguagePageUrl();
    }

    @Override
    protected Url doProcessErrors() {
        return new ChangeLanguagePageUrl();
    }

    @Override
    protected Url checkRightsAndEverything() {
        return NO_ERROR; // Nothing else to check
    }

    @Override
    protected void transmitParameters() {
        session.addParameter(url.getLanguageParameter());
    }
}
