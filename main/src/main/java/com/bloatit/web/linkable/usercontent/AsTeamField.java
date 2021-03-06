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
package com.bloatit.web.linkable.usercontent;

import com.bloatit.data.DaoTeamRight.UserTeamRight;
import com.bloatit.framework.exceptions.highlevel.ShallNotPassException;
import com.bloatit.framework.utils.PageIterable;
import com.bloatit.framework.webprocessor.components.PlaceHolderElement;
import com.bloatit.framework.webprocessor.components.form.FieldData;
import com.bloatit.framework.webprocessor.components.form.HtmlDropDown;
import com.bloatit.framework.webprocessor.context.Context;
import com.bloatit.model.Member;
import com.bloatit.model.Team;
import com.bloatit.model.right.UnauthorizedOperationException;
import com.bloatit.web.url.UserContentActionUrl;

public class AsTeamField extends PlaceHolderElement {

    private HtmlDropDown teamInput;

    private final boolean showSomething;

    public AsTeamField(final UserContentActionUrl targetUrl, final Member me, final UserTeamRight right, final String label, final String comment) {
        super();
        if (me != null) {
            try {
                final PageIterable<Team> teams = me.getTeams();
                final FieldData teamData = targetUrl.getTeamParameter().pickFieldData();
                teamInput = new HtmlDropDown(teamData.getName());
                getTeamInput().addErrorMessages(teamData.getErrorMessages());
                // getTeamInput().setComment(comment);
                getTeamInput().addDropDownElement("", Context.tr("Myself"));
                int nbTeam = 0;
                for (final Team team : teams) {
                    if (team.getUserTeamRight(me).contains(right)) {
                        getTeamInput().addDropDownElement(team.getId().toString(), team.getDisplayName());
                        nbTeam++;
                    }
                }
                getTeamInput().setDefaultValue(teamData.getSuggestedValue());
                if (nbTeam > 0) {
                    showSomething = true;
                    add(getTeamInput());
                } else {
                    showSomething = false;
                }
            } catch (final UnauthorizedOperationException e) {
                Context.getSession().notifyError(Context.tr("An error prevented us from displaying you some information. Please notify us."));
                throw new ShallNotPassException("Can't access current user teams (I checked before tho)", e);
            }
        } else {
            showSomething = false;
        }
    }

    public final boolean showSomething() {
        return showSomething;
    }

    public HtmlDropDown getTeamInput() {
        return teamInput;
    }

}
