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
package com.bloatit.web.linkable.invoice;

import com.bloatit.framework.exceptions.highlevel.BadProgrammerException;
import com.bloatit.framework.webprocessor.annotations.NonOptional;
import com.bloatit.framework.webprocessor.annotations.ParamContainer;
import com.bloatit.framework.webprocessor.annotations.ParamContainer.Protocol;
import com.bloatit.framework.webprocessor.annotations.RequestParam;
import com.bloatit.framework.webprocessor.annotations.tr;
import com.bloatit.framework.webprocessor.url.Url;
import com.bloatit.model.Actor;
import com.bloatit.model.Member;
import com.bloatit.model.Milestone;
import com.bloatit.model.Team;
import com.bloatit.model.managers.MemberManager;
import com.bloatit.model.managers.MilestoneManager;
import com.bloatit.model.managers.TeamManager;
import com.bloatit.model.right.UnauthorizedPrivateAccessException;
import com.bloatit.web.linkable.master.WebProcess;
import com.bloatit.web.url.ContributionInvoicingInformationsPageUrl;
import com.bloatit.web.url.ContributionInvoicingProcessUrl;
import com.bloatit.web.url.MemberPageUrl;
import com.bloatit.web.url.ModifyInvoicingContactProcessUrl;
import com.bloatit.web.url.TeamPageUrl;

@ParamContainer(value = "contribution_invoicing/process", protocol = Protocol.HTTPS)
public class ContributionInvoicingProcess extends WebProcess {

    @RequestParam
    private Actor<?> actor;

    @RequestParam(message = @tr("The milestone to invoice is ."))
    @NonOptional(@tr("The process is closed, expired, missing or invalid."))
    private Milestone milestone;

    @SuppressWarnings("unused")
    private final ContributionInvoicingProcessUrl url;
    @SuppressWarnings("unused")
    private ModifyInvoicingContactProcess invoicingContactProcess = null;

    public ContributionInvoicingProcess(final ContributionInvoicingProcessUrl url) {
        super(url);
        this.url = url;
        this.actor = url.getActor();
        this.milestone = url.getMilestone();
    }

    @Override
    protected synchronized Url doProcess() {
        try {
            if (actor.hasInvoicingContact(true)) {
                return new ContributionInvoicingInformationsPageUrl(false, this);
            } else {
                final ModifyInvoicingContactProcessUrl modifyInvoicingContactProcessUrl = new ModifyInvoicingContactProcessUrl(actor, this);
                modifyInvoicingContactProcessUrl.setNeedAllInfos(true);
                return modifyInvoicingContactProcessUrl;
            }
        } catch (final UnauthorizedPrivateAccessException e) {
            throw new BadProgrammerException("No access to invoicing informations in ContributionInvocingProcess", e);
        }
    }

    @Override
    protected synchronized Url doProcessErrors() {
        return session.getLastVisitedPage();
    }

    @Override
    public synchronized void doLoad() {
        if (getActor() instanceof Member) {
            setActor(MemberManager.getById(getActor().getId()));
        } else if (getActor() instanceof Team) {
            setActor(TeamManager.getById(getActor().getId()));
        }
        if (milestone != null) {
            milestone = MilestoneManager.getById(milestone.getId());
        }
    }

    public Actor<?> getActor() {
        return actor;
    }

    public void setActor(final Actor<?> actor) {
        this.actor = actor;
    }

    @Override
    public synchronized void addChildProcess(final WebProcess child) {
        super.addChildProcess(child);
        invoicingContactProcess = (ModifyInvoicingContactProcess) child;
    }

    public Milestone getMilestone() {
        return milestone;
    }

    @Override
    protected synchronized Url notifyChildClosed(final WebProcess subProcess) {
        if (subProcess instanceof ModifyInvoicingContactProcess) {
            return new ContributionInvoicingInformationsPageUrl(false, this);
        }
        return super.notifyChildClosed(subProcess);
    }

    @Override
    public synchronized Url close() {
        super.close();

        if (actor.isTeam()) {
            final TeamPageUrl teamPageUrl = new TeamPageUrl((Team) actor);
            teamPageUrl.setActiveTabKey("invoicing");
            return teamPageUrl;
        } else {
            final MemberPageUrl memberPageUrl = new MemberPageUrl((Member) actor);
            memberPageUrl.setActiveTabKey("invoicing");
            return memberPageUrl;
        }

    }

}
