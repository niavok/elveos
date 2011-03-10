package com.bloatit.model;

import com.bloatit.data.DaoBankTransaction;
import com.bloatit.data.DaoBatch;
import com.bloatit.data.DaoBug;
import com.bloatit.data.DaoComment;
import com.bloatit.data.DaoContribution;
import com.bloatit.data.DaoFeature;
import com.bloatit.data.DaoDescription;
import com.bloatit.data.DaoExternalAccount;
import com.bloatit.data.DaoFileMetadata;
import com.bloatit.data.DaoTeam;
import com.bloatit.data.DaoHighlightFeature;
import com.bloatit.data.DaoIdentifiable;
import com.bloatit.data.DaoInternalAccount;
import com.bloatit.data.DaoJoinTeamInvitation;
import com.bloatit.data.DaoKudos;
import com.bloatit.data.DaoKudosable;
import com.bloatit.data.DaoMember;
import com.bloatit.data.DaoOffer;
import com.bloatit.data.DaoSoftware;
import com.bloatit.data.DaoRelease;
import com.bloatit.data.DaoTransaction;
import com.bloatit.data.DaoTranslation;
import com.bloatit.data.IdentifiableInterface;
import com.bloatit.data.queries.DBRequests;

public class GenericConstructor {

    public static IdentifiableInterface create(Class<? extends IdentifiableInterface> clazz, Integer id) throws ClassNotFoundException {
        // TODO: Crash if not found
        Class<?> daoClass = getDaoClass(clazz);
        if (daoClass == null) {
            throw new ClassNotFoundException("Cannot find a dao class for the class " + clazz);
        }
        return ((DaoIdentifiable) DBRequests.getById(daoClass, id)).accept(new DataVisitorConstructor());
    }

    public static Class<?> getDaoClass(Class<? extends IdentifiableInterface> clazz) {
        if (clazz.equals(ExternalAccount.class)) {
            return DaoExternalAccount.class;
        }
        if (clazz.equals(InternalAccount.class)) {
            return DaoInternalAccount.class;
        }
        if (clazz.equals(Member.class)) {
            return DaoMember.class;
        }
        if (clazz.equals(BankTransaction.class)) {
            return DaoBankTransaction.class;
        }
        if (clazz.equals(Batch.class)) {
            return DaoBatch.class;
        }
        if (clazz.equals(Description.class)) {
            return DaoDescription.class;
        }
        if (clazz.equals(Team.class)) {
            return DaoTeam.class;
        }
        if (clazz.equals(HighlightFeature.class)) {
            return DaoHighlightFeature.class;
        }
        if (clazz.equals(JoinTeamInvitation.class)) {
            return DaoJoinTeamInvitation.class;
        }
        if (clazz.equals(Software.class)) {
            return DaoSoftware.class;
        }
        if (clazz.equals(Transaction.class)) {
            return DaoTransaction.class;
        }
        if (clazz.equals(Bug.class)) {
            return DaoBug.class;
        }
        if (clazz.equals(Contribution.class)) {
            return DaoContribution.class;
        }
        if (clazz.equals(FileMetadata.class)) {
            return DaoFileMetadata.class;
        }
        if (clazz.equals(Kudos.class)) {
            return DaoKudos.class;
        }
        if (clazz.equals(Comment.class)) {
            return DaoComment.class;
        }
        if (clazz.equals(Feature.class)) {
            return DaoFeature.class;
        }
        if (clazz.equals(Offer.class)) {
            return DaoOffer.class;
        }
        if (clazz.equals(Translation.class)) {
            return DaoTranslation.class;
        }
        if (clazz.equals(Release.class)) {
            return DaoRelease.class;
        }
        if (clazz.equals(Kudosable.class) || clazz.equals(KudosableInterface.class)) {
            return DaoKudosable.class;
        }
        return null;
    }
}
