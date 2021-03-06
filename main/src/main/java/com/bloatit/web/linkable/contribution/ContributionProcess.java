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
package com.bloatit.web.linkable.contribution;

import java.math.BigDecimal;

import javax.mail.IllegalWriteException;

import com.bloatit.data.exceptions.NotEnoughMoneyException;
import com.bloatit.framework.exceptions.highlevel.ShallNotPassException;
import com.bloatit.framework.webprocessor.annotations.ParamContainer;
import com.bloatit.framework.webprocessor.annotations.RequestParam;
import com.bloatit.framework.webprocessor.context.Context;
import com.bloatit.framework.webprocessor.url.Url;
import com.bloatit.model.Feature;
import com.bloatit.model.feature.FeatureManager;
import com.bloatit.model.right.UnauthorizedOperationException;
import com.bloatit.web.linkable.invoice.ModifyInvoicingContactProcess;
import com.bloatit.web.linkable.master.WebProcess;
import com.bloatit.web.linkable.money.PaymentProcess;
import com.bloatit.web.linkable.process.AccountProcess;
import com.bloatit.web.url.CheckContributePageUrl;
import com.bloatit.web.url.ContributeActionUrl;
import com.bloatit.web.url.ContributePageUrl;
import com.bloatit.web.url.ContributionProcessUrl;
import com.bloatit.web.url.StaticCheckContributionPageUrl;

@ParamContainer("contribution/process")
public class ContributionProcess extends AccountProcess {

    @RequestParam
    private Feature feature;

    private BigDecimal amount = new BigDecimal("0");
    private String comment = "";

    private volatile boolean contributionDone = false;

    public ContributionProcess(final ContributionProcessUrl url) {
        super(url);
        feature = url.getFeature();
    }

    public synchronized String getComment() {
        return comment;
    }

    public synchronized BigDecimal getAmount() {
        return amount;
    }

    public synchronized Feature getFeature() {
        return feature;
    }

    public synchronized void setAmount(final BigDecimal amount) throws IllegalWriteException {
        if (isLocked()) {
            throw new IllegalWriteException();
        }
        this.amount = amount;
    }

    public synchronized void setComment(final String comment) throws IllegalWriteException {
        if (isLocked()) {
            throw new IllegalWriteException();
        }
        this.comment = comment;
    }

    @Override
    protected Url doProcess() {
        return new ContributePageUrl(this);
    }

    @Override
    public void doDoLoad() {
        feature = FeatureManager.getFeatureById(feature.getId());
    }

    @Override
    public synchronized Url notifyChildClosed(final WebProcess subProcess) {
        if (subProcess instanceof PaymentProcess) {
            final PaymentProcess subPro = (PaymentProcess) subProcess;
            if (subPro.isSuccessful()) {
                // Redirects to the contribution action which will perform the
                // actual contribution
                return new ContributeActionUrl(Context.getSession().getShortKey(), this);
            }
            unlock();
            if (subPro.hasBadParams()) {
                return new StaticCheckContributionPageUrl(this);
            } else {
                return new CheckContributePageUrl(this);
            }
        } else if (subProcess instanceof ModifyInvoicingContactProcess) {
            return new CheckContributePageUrl(this);
        }
        return null;
    }

    public synchronized void doContribute() throws NotEnoughMoneyException {
        if (contributionDone) {
            return;
        }

        try {
            getFeature().addContribution(getAmount(), getComment());
            contributionDone = true;
            session.notifyGood(Context.tr("Thank you for contributing {0} on this feature.", Context.getLocalizator()
                                                                                                    .getCurrency(getAmount())
                                                                                                    .getSimpleEuroString()));
        } catch (final UnauthorizedOperationException e) {
            throw new ShallNotPassException("No right to make a contribution");
        }
    }

    @Override
    public synchronized void update(final WebProcess subProcess) {
        if (subProcess instanceof PaymentProcess) {
            final PaymentProcess subPro = (PaymentProcess) subProcess;
            if (subPro.isSuccessful()) {
                try {
                    doContribute();
                } catch (final NotEnoughMoneyException e) {
                    // Do nothing.
                }
            }
        }
        super.update(this);
    }

}
