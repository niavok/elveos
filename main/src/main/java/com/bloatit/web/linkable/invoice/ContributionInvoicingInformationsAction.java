/*
 * Copyright (C) 2010 BloatIt. This file is part of BloatIt. BloatIt is free
 * software: you can redistribute it and/or modify it under the terms of the GNU
 * Affero General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * BloatIt is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details. You should have received a copy of the GNU Affero General Public
 * License along with BloatIt. If not, see <http://www.gnu.org/licenses/>.
 */
package com.bloatit.web.linkable.invoice;

import java.util.List;

import com.bloatit.framework.exceptions.highlevel.BadProgrammerException;
import com.bloatit.framework.mails.ElveosMail;
import com.bloatit.framework.utils.PageIterable;
import com.bloatit.framework.webprocessor.annotations.NonOptional;
import com.bloatit.framework.webprocessor.annotations.Optional;
import com.bloatit.framework.webprocessor.annotations.ParamContainer;
import com.bloatit.framework.webprocessor.annotations.RequestParam;
import com.bloatit.framework.webprocessor.annotations.RequestParam.Role;
import com.bloatit.framework.webprocessor.annotations.tr;
import com.bloatit.framework.webprocessor.context.Context;
import com.bloatit.framework.webprocessor.url.Url;
import com.bloatit.model.Actor;
import com.bloatit.model.ContributionInvoice;
import com.bloatit.model.Member;
import com.bloatit.model.MilestoneContributionAmount;
import com.bloatit.model.right.UnauthorizedOperationException;
import com.bloatit.web.linkable.master.LoggedElveosAction;
import com.bloatit.web.linkable.members.MemberPage;
import com.bloatit.web.url.ContributionInvoicingInformationsActionUrl;
import com.bloatit.web.url.ContributionInvoicingInformationsPageUrl;
import com.bloatit.web.url.MemberPageUrl;

/**
 * Class that will create a new offer based on data received from a form.
 */
@ParamContainer("action/invoicing/contribution_invoicing_informations")
public final class ContributionInvoicingInformationsAction extends LoggedElveosAction {

    @RequestParam(message = @tr("The process is closed, expired, missing or invalid."))
    @NonOptional(@tr("The process is closed, expired, missing or invalid."))
    private final ContributionInvoicingProcess process;

    @RequestParam(name = "applyVAT", role = Role.POST)
    @Optional
    private final List<String> applyVAT;

    @SuppressWarnings("unused")
    @RequestParam(name = "generate", role = Role.POST)
    @Optional
    private final String generate;

    @RequestParam(name = "preview", role = Role.POST)
    @Optional
    private final String preview;

    @SuppressWarnings("unused")
    private final ContributionInvoicingInformationsActionUrl url;

    public ContributionInvoicingInformationsAction(final ContributionInvoicingInformationsActionUrl url) {
        super(url);
        this.url = url;
        this.process = url.getProcess();
        this.applyVAT = url.getApplyVAT();
        this.preview = url.getPreview();
        this.generate = url.getGenerate();
    }

    @Override
    public Url doProcessRestricted(final Member me) {

        if (preview != null) {
            // Return to previous page with the right values
            final ContributionInvoicingInformationsPageUrl contributionInvoicingInformationsPageUrl = new ContributionInvoicingInformationsPageUrl(true,
                                                                                                                                                   process);
            contributionInvoicingInformationsPageUrl.setApplyVAT(applyVAT);
            return contributionInvoicingInformationsPageUrl;
        }

        // Generate the invoices
        final PageIterable<MilestoneContributionAmount> contributionAmounts = process.getMilestone().getContributionAmounts();

        for (final MilestoneContributionAmount contributionAmount : contributionAmounts) {
            try {

                final Actor<?> author = contributionAmount.getContribution().getAuthor();
                final ContributionInvoice invoice = new ContributionInvoice(process.getActor(),
                                                                            author,
                                                                            "Contribution",
                                                                            "Contribution",
                                                                            contributionAmount.getAmount(),
                                                                            contributionAmount.getMilestone(),
                                                                            contributionAmount.getContribution(),
                                                                            applyVAT.contains(contributionAmount.getId().toString()));

                if (!author.isTeam()) {
                    final Member member = (Member) author;
                    final MemberPageUrl memberPageUrl = new MemberPageUrl(member);
                    memberPageUrl.setActiveTabKey(MemberPage.ACCOUNT_TAB);
                    final ElveosMail mail = new ElveosMail.InvoiceGenerated(contributionAmount.getMilestone().getOffer().getFeature().getTitle());
                    mail.addAttachment(invoice.getFile(), invoice.getInvoiceNumber() + ".pdf");
                    mail.sendMail(member, "invoice-generated");

                }

                // TODO: send mail to team

            } catch (final UnauthorizedOperationException e) {
                throw new BadProgrammerException("Fail create a ContributionInvoice", e);
            }

        }

        Context.getSession().notifyGood(Context.trn("{0} invoice succefully generated. You can download it in the invoicing tab",
                                                    "{0} invoices succefully generated. You can download them in the invoicing tab",
                                                    contributionAmounts.size(),
                                                    contributionAmounts.size()));

        return process.close();
    }

    @Override
    protected Url checkRightsAndEverything(final Member me) {
        return NO_ERROR;
    }

    @Override
    protected Url doProcessErrors() {
        return session.pickPreferredPage();
    }

    @Override
    protected String getRefusalReason() {
        return Context.tr("You must be logged to generate invoices.");
    }

    @Override
    protected void transmitParameters() {
    }

}
