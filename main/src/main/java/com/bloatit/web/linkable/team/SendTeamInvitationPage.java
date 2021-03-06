//
// Copyright (c) 2011 Linkeos.
//
// This file is part of Elveos.org.
// Elveos.org is free software: you can redistribute it and/or modify it
// under the terms of the GNU General Public License as published by the
// Free Software Foundation, either version 3 of the License, or (at your
// option) any later version.
//
// Elveos.org is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
// more details.
// You should have received a copy of the GNU General Public License along
// with Elveos.org. If not, see http://www.gnu.org/licenses/.
//
package com.bloatit.web.linkable.team;

import static com.bloatit.framework.webprocessor.context.Context.tr;

import com.bloatit.framework.exceptions.lowlevel.RedirectException;
import com.bloatit.framework.webprocessor.annotations.NonOptional;
import com.bloatit.framework.webprocessor.annotations.ParamContainer;
import com.bloatit.framework.webprocessor.annotations.RequestParam;
import com.bloatit.framework.webprocessor.annotations.RequestParam.Role;
import com.bloatit.framework.webprocessor.annotations.tr;
import com.bloatit.framework.webprocessor.components.HtmlDiv;
import com.bloatit.framework.webprocessor.components.form.FieldData;
import com.bloatit.framework.webprocessor.components.form.HtmlDropDown;
import com.bloatit.framework.webprocessor.components.form.HtmlSubmit;
import com.bloatit.framework.webprocessor.components.meta.HtmlElement;
import com.bloatit.framework.webprocessor.context.Context;
import com.bloatit.model.Member;
import com.bloatit.model.Team;
import com.bloatit.model.managers.MemberManager;
import com.bloatit.web.components.HtmlElveosForm;
import com.bloatit.web.linkable.master.Breadcrumb;
import com.bloatit.web.linkable.master.LoggedElveosPage;
import com.bloatit.web.linkable.master.sidebar.TwoColumnLayout;
import com.bloatit.web.url.SendTeamInvitationActionUrl;
import com.bloatit.web.url.SendTeamInvitationPageUrl;

/**
 * <p>
 * A page to send invitations to teams
 * </p>
 */
@ParamContainer("teams/%team%/sendinvitation")
public class SendTeamInvitationPage extends LoggedElveosPage {
    private final SendTeamInvitationPageUrl url;

    @RequestParam(role = Role.PAGENAME, message = @tr("I cannot find the team number: ''%value%''."))
    @NonOptional(@tr("You have to specify a team number."))
    private final Team team;

    public SendTeamInvitationPage(final SendTeamInvitationPageUrl url) {
        super(url);
        this.url = url;
        this.team = url.getTeam();
    }

    @Override
    public HtmlElement createRestrictedContent(final Member me) throws RedirectException {
        final TwoColumnLayout layout = new TwoColumnLayout(true, url);
        final HtmlDiv left = new HtmlDiv();
        layout.addLeft(left);

        final SendTeamInvitationActionUrl target = new SendTeamInvitationActionUrl(getSession().getShortKey(), team);
        final HtmlElveosForm form = new HtmlElveosForm(target.urlString());
        left.add(form);
        final FieldData fieldData = target.getReceiverParameter().pickFieldData();
        final HtmlDropDown receiverInput = new HtmlDropDown(fieldData.getName(), Context.tr("Select a member"));
        form.add(receiverInput);
        for (final Member m : MemberManager.getAll()) {
            if (!m.equals(me)) {
                receiverInput.addDropDownElement(m.getId().toString(), m.getDisplayName());
            }
        }
        form.addSubmit(new HtmlSubmit(Context.tr("Submit")));
        return layout;
    }

    @Override
    public String getRefusalReason() {
        return Context.tr("Your must be logged to send team invitations");
    }

    @Override
    protected String createPageTitle() {
        return Context.tr("Send team invitations");
    }

    @Override
    public boolean isStable() {
        return false;
    }

    @Override
    protected Breadcrumb createBreadcrumb(final Member member) {
        return SendTeamInvitationPage.generateBreadcrumb(team);
    }

    private static Breadcrumb generateBreadcrumb(final Team team) {
        final Breadcrumb breadcrumb = TeamPage.generateBreadcrumb(team);
        breadcrumb.pushLink(new SendTeamInvitationPageUrl(team).getHtmlLink(tr("Send team invitation")));
        return breadcrumb;
    }
}
