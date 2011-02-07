package com.bloatit.model;

import java.util.EnumSet;

import com.bloatit.common.Log;
import com.bloatit.data.DaoKudosable;
import com.bloatit.data.DaoKudosable.State;
import com.bloatit.data.DaoUserContent;
import com.bloatit.framework.exceptions.UnauthorizedOperationException;
import com.bloatit.framework.exceptions.UnauthorizedOperationException.SpecialCode;
import com.bloatit.model.right.KudosableRight;
import com.bloatit.model.right.RightManager.Action;

public abstract class Kudosable<T extends DaoKudosable> extends UserContent<T> {

    private static final int TURN_VALID = KudosableConfiguration.getDefaultTurnValid();
    private static final int TURN_REJECTED = KudosableConfiguration.getDefaultTurnRejected();
    private static final int TURN_HIDDEN = KudosableConfiguration.getDefaultTurnHidden();
    private static final int TURN_PENDING = KudosableConfiguration.getDefaultTurnPending();

    private static final int MIN_INFLUENCE_TO_UNKUDOS = KudosableConfiguration.getMinInfluenceToUnkudos();
    private static final int MIN_INFLUENCE_TO_KUDOS = KudosableConfiguration.getMinInfluenceToKudos();

    public Kudosable(final T dao) {
        super(dao);
    }

    public EnumSet<SpecialCode> canKudos() {
        return canKudos(1);
    }

    public EnumSet<SpecialCode> canUnkudos() {
        return canKudos(-1);
    }

    public final void unkudos() throws UnauthorizedOperationException {
        addKudos(-1);
        notifyKudos(false);
    }

    public final void kudos() throws UnauthorizedOperationException {
        addKudos(1);
        notifyKudos(true);
    }

    private final EnumSet<SpecialCode> canKudos(int signe) {
        EnumSet<SpecialCode> errors = EnumSet.noneOf(SpecialCode.class);

        // See if we can kudos.
        if (!new KudosableRight.Kudos().canAccess(calculateRole(this), Action.WRITE)) {
            errors.add(SpecialCode.NOTHING_SPECIAL);
        }

        if (getAuthTokenUnprotected() == null){
            errors.add(SpecialCode.AUTHENTICATION_NEEDED);
        }

        // Only one kudos per person
        if (getDao().hasKudosed(getAuthTokenUnprotected().getMember().getDao())) {
            errors.add(SpecialCode.ALREADY_KUDOSED);
        }

        // Make sure we are in the right position
        final Member member = getAuthTokenUnprotected().getMember();
        final int influence = member.calculateInfluence();
        if (signe == -1 && influence < MIN_INFLUENCE_TO_UNKUDOS) {
            errors.add(SpecialCode.INFLUENCE_LOW_ON_UNKUDOS);
        }
        if (signe == 1 && influence < MIN_INFLUENCE_TO_KUDOS) {
            errors.add(SpecialCode.INFLUENCE_LOW_ON_KUDOS);
        }
        return errors;
    }

    public final State getState() {
        return getDao().getState();
    }

    @Override
    protected final DaoUserContent getDaoUserContent() {
        return getDao();
    }

    private void addKudos(final int signe) throws UnauthorizedOperationException {
        EnumSet<SpecialCode> canKudos = canKudos(signe);
        if (!canKudos.isEmpty()) {
            throw new UnauthorizedOperationException(canKudos.iterator().next());
        }
        // Make sure we are in the right position
        final Member member = getAuthToken().getMember();
        final int influence = member.calculateInfluence();

        if (influence > 0) {
            getAuthor().addToKarma(influence);
            calculateNewState(getDao().addKudos(member.getDao(), signe * influence));
        }
    }

    private void calculateNewState(final int newPop) {
        switch (getState()) {
        case PENDING:
            if (newPop >= turnValid()) {
                setState(State.VALIDATED);
                notifyValid();
            } else if (newPop <= turnHidden() && newPop > turnRejected()) {
                setState(State.HIDDEN);
                notifyHidden();
            } else if (newPop <= turnRejected()) {
                setState(State.REJECTED);
                notifyRejected();
            }
            // NO BREAK IT IS OK !!
        case VALIDATED:
            if (newPop <= turnPending()) {
                setState(State.PENDING);
                notifyPending();
            }
            break;
        case HIDDEN:
        case REJECTED:
            if (newPop >= turnPending() && newPop < turnValid()) {
                setState(State.PENDING);
                notifyPending();
            } else if (newPop >= turnValid()) {
                setState(State.VALIDATED);
                notifyValid();
            } else if (newPop <= turnHidden() && newPop > turnRejected()) {
                setState(State.VALIDATED);
                notifyValid();
            } else if (newPop <= turnRejected()) {
                setState(State.REJECTED);
                notifyRejected();
            }
            break;
        default:
            assert false;
            break;
        }
    }

    private void setState(final State newState) {
        Log.model().info("Kudosable: " + getId() + " change from state: " + this.getState() + ", to: " + newState);
        getDao().setState(newState);
    }

    /**
     * You can redefine me if you want to customize the state calculation limits. Default
     * value is {@value #TURN_PENDING}.
     *
     * @return The popularity to for a Kudosable to reach to turn to {@link State#PENDING}
     *         state.
     */
    protected int turnPending() {
        return TURN_PENDING;
    }

    /**
     * You can redefine me if you want to customize the state calculation limits. Default
     * value is {@value #TURN_VALID}.
     *
     * @return The popularity to for a Kudosable to reach to turn to
     *         {@link State#VALIDATED} state.
     */
    protected int turnValid() {
        return TURN_VALID;
    }

    /**
     * You can redefine me if you want to customize the state calculation limits. Default
     * value is {@value #TURN_REJECTED}.
     *
     * @return The popularity to for a Kudosable to reach to turn to
     *         {@link State#REJECTED} state.
     */
    protected int turnRejected() {
        return TURN_REJECTED;
    }

    /**
     * You can redefine me if you want to customize the state calculation limits. Default
     * value is {@value #TURN_HIDDEN}.
     *
     * @return The popularity to for a Kudosable to reach to turn to {@link State#HIDDEN}
     *         state.
     */
    protected int turnHidden() {
        return TURN_HIDDEN;
    }

    /**
     * This method is called each time this Kudosable is kudosed.
     *
     * @param positif true if it is a kudos false if it is an unKudos.
     */
    protected void notifyKudos(final boolean positif) {
        // Implement me if you wish
    }

    /**
     * This method is called each time this Kudosable change its state to valid.
     */
    protected void notifyValid() {
        // Implement me if you wish
    }

    /**
     * This method is called each time this Kudosable change its state to pending.
     */
    protected void notifyPending() {
        // Implement me if you wish
    }

    /**
     * This method is called each time this Kudosable change its state to rejected.
     */
    protected void notifyRejected() {
        // Implement me if you wish
    }

    /**
     * This method is called each time this Kudosable change its state to Hidden.
     */
    protected void notifyHidden() {
        // Implement me if you wish
    }

    public final int getPopularity() {
        return getDao().getPopularity();
    }

}