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
package com.bloatit.web.linkable;

import java.util.EnumSet;

import com.bloatit.framework.exceptions.highlevel.ShallNotPassException;
import com.bloatit.framework.webprocessor.annotations.NonOptional;
import com.bloatit.framework.webprocessor.annotations.ParamContainer;
import com.bloatit.framework.webprocessor.annotations.RequestParam;
import com.bloatit.framework.webprocessor.annotations.tr;
import com.bloatit.framework.webprocessor.context.Context;
import com.bloatit.framework.webprocessor.url.Url;
import com.bloatit.model.KudosableInterface;
import com.bloatit.model.Member;
import com.bloatit.model.right.UnauthorizedOperationException;
import com.bloatit.model.right.UnauthorizedOperationException.SpecialCode;
import com.bloatit.web.linkable.master.LoggedElveosAction;
import com.bloatit.web.url.PopularityVoteActionUrl;

/**
 * A response to a form used to assess any <code>kudosable</code> on the bloatit
 * website
 */
@ParamContainer("popularity/vote")
public final class PopularityVoteAction extends LoggedElveosAction {

    private static final String TARGET_KUDOSABLE = "targetKudosable";
    private static final String VOTE_UP = "voteUp";

    @NonOptional(@tr("Nothing to vote on."))
    @RequestParam(name = TARGET_KUDOSABLE)
    private final KudosableInterface targetKudosable;

    @RequestParam(name = VOTE_UP)
    private final Boolean voteUp;

    // Keep it for consistency
    @SuppressWarnings("unused")
    private final PopularityVoteActionUrl url;

    public PopularityVoteAction(final PopularityVoteActionUrl url) {
        super(url);
        this.url = url;
        this.targetKudosable = url.getTargetKudosable();
        this.voteUp = url.getVoteUp();
    }

    @Override
    public Url doProcessRestricted(final Member me) {
        try {
            if (voteUp) {
                final EnumSet<SpecialCode> canVote = targetKudosable.canVoteUp();

                if (canVote.isEmpty()) {
                    final int weight = targetKudosable.voteUp();
                    session.notifyGood(Context.tr("Vote up applied: {0}.", weight));
                } else {
                    analyseErrors(canVote);
                }
            } else {
                final EnumSet<SpecialCode> canVote = targetKudosable.canVoteDown();

                if (canVote.isEmpty()) {
                    final int weight = targetKudosable.voteDown();
                    session.notifyGood(Context.tr("Vote down applied: {0}.", weight));
                } else {
                    analyseErrors(canVote);
                }
            }
        } catch (final UnauthorizedOperationException e) {
            Context.getSession().notifyError(Context.tr("Error voting on a component. Please notify us."));
            throw new ShallNotPassException("Error voting", e);
        }

        return session.pickPreferredPage();
    }

    @Override
    protected Url checkRightsAndEverything(final Member me) {

        return null;
    }

    private void analyseErrors(final EnumSet<SpecialCode> canVote) {
        boolean foundSomthing = false;
        if (canVote.contains(SpecialCode.ALREADY_VOTED)) {
            session.notifyWarning(Context.tr("You already voted on that."));
            foundSomthing = true;
        }
        if (canVote.contains(SpecialCode.INFLUENCE_LOW_ON_VOTE_UP)) {
            session.notifyWarning(Context.tr("You have a too low reputation to vote up that."));
            foundSomthing = true;
        }
        if (canVote.contains(SpecialCode.INFLUENCE_LOW_ON_VOTE_DOWN)) {
            session.notifyWarning(Context.tr("You have a too low reputation to vote down that."));
            foundSomthing = true;
        }
        if (canVote.contains(SpecialCode.OWNED_BY_ME)) {
            session.notifyWarning(Context.tr("You can't vote for yourself!"));
            foundSomthing = true;
        }
        if (canVote.contains(SpecialCode.YOU_HAVE_TO_CONTRIBUTE_TO_VOTE_ON_OFFER)) {
            session.notifyWarning(Context.tr("You have to contribute to vote for this offer!"));
            foundSomthing = true;
        }

        if (!foundSomthing) {
            session.notifyWarning(Context.tr("For an unknown reason you cannot vote here."));
        }
    }

    @Override
    protected Url doProcessErrors() {
        return session.pickPreferredPage();
    }

    @Override
    protected String getRefusalReason() {
        return "You must be logged to kudo";
    }

    @Override
    protected void transmitParameters() {
        // Nothing to save
    }

}
