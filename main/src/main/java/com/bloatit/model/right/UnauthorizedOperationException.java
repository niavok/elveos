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
package com.bloatit.model.right;

import com.bloatit.model.JoinTeamInvitation;

/**
 * This exception is thrown when you try to access a property without the having
 * right.
 * <p>
 * An {@link UnauthorizedOperationException} should give you some informations
 * about why you failed to access a property. To do so, tree different
 * informations can be available:
 * <li>The role in which the user was when trying to access the property.</li>
 * <li>The action the user try to do.</li>
 * <li>A special code for every possible special access error.</li>
 * </p>
 */
public class UnauthorizedOperationException extends Exception {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -3668632178618592431L;

    /**
     * A specialCode is the return code describing the.
     * {@link UnauthorizedOperationException}.
     */
    public enum SpecialCode {
        /**
         * You try to access a method that require authentication without
         * authenticating the object.
         * 
         * @see RestrictedObject#authenticate(com.bloatit.model.right.AuthenticatedUserToken)
         */
        AUTHENTICATION_NEEDED,

        /**
         * No special code. See the roles/action.
         */
        NOTHING_SPECIAL,

        /**
         * You try to (un)kudos a kudosable that has already been (un)kudosed by
         * you.
         */
        ALREADY_VOTED,

        /**
         * You try to (un)kudos a locked kudosable.
         */
        KUDOSABLE_LOCKED,

        /** Influence too low to unkudos a kudosable. */
        INFLUENCE_LOW_ON_VOTE_DOWN,

        /** Influence too low to kudos a kudosable. */
        INFLUENCE_LOW_ON_VOTE_UP,

        /**
         * You try to add somebody in a team, but this team is not public. You
         * have to use {@link JoinTeamInvitation} object to join a non public
         * team.
         */
        TEAM_NOT_PUBLIC,

        /**
         * You try to contribute in the name of a team without having the BANK
         * right.
         */
        TEAM_CONTRIBUTION_WITHOUT_BANK,

        /**
         * You try to accept/refuse an invitation, but you are not the receiver.
         */
        INVITATION_RECIEVER_MISMATCH,

        /**
         * You try to send an invitation, but you are have not the right.
         */
        INVITATION_SEND_NO_RIGHT,

        /**
         * You try to delete a feature, but you are not the current developer.
         */
        NON_DEVELOPER_CANCEL_FEATURE,

        /**
         * You try to finish a feature, but you are not the current developer.
         */
        NON_DEVELOPER_FINISHED_FEATURE,

        /**
         * You create an object and insert it with different person. For example
         * Tom create an Offer and Yo insert it in the feature.
         */
        CREATOR_INSERTOR_MISMATCH,

        /** You try to vote but you are the author. */
        OWNED_BY_ME,

        /**
         * You tried to do an action reserved for the admin.
         */
        ADMIN_ONLY,

        /**
         * You tried to remove a member from a team with having the right
         */
        TEAM_PROMOTE_RIGHT_MISSING,

        /**
         * You tried to modify team without having the right to
         */
        TEAM_MISSING_MODIFY_RIGHT,

        /**
         * You tried to vote on an offer and you do not have contributed on the
         * corresponding feature
         */
        YOU_HAVE_TO_CONTRIBUTE_TO_VOTE_ON_OFFER

    }

    /** The action. */
    private final Action action;

    /** The code. */
    private final SpecialCode code;

    /**
     * Instantiates a new unauthorized operation exception.
     * 
     * @param action the action when trying to access a property.
     * @param code the code describing more precisely what went wrong.
     */
    private UnauthorizedOperationException(final Action action, final SpecialCode code) {
        super();
        this.action = action;
        this.code = code;
    }

    /**
     * Instantiates a new unauthorized operation exception.
     * 
     * @param action the action when trying to access a property.
     */
    public UnauthorizedOperationException(final Action action) {
        this(action, SpecialCode.NOTHING_SPECIAL);
    }

    /**
     * Instantiates a new unauthorized operation exception.
     * 
     * @param code the code describing more precisely what went wrong.
     */
    public UnauthorizedOperationException(final SpecialCode code) {
        super();
        this.action = null;
        this.code = code;
    }

    /**
     * Gets the action.
     * 
     * @return the action that has been forbidden.
     */
    public final Action getAction() {
        return action;
    }

    /**
     * Gets the code.
     * 
     * @return the code describing what went wrong.
     */
    public final SpecialCode getCode() {
        return code;
    }

}
