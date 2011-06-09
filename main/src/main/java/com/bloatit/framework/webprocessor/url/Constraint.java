package com.bloatit.framework.webprocessor.url;

import java.math.BigDecimal;

import com.bloatit.framework.exceptions.highlevel.BadProgrammerException;
import com.bloatit.framework.webprocessor.annotations.Message;
import com.bloatit.framework.webprocessor.annotations.MessageFormater;

public abstract class Constraint<T> {

    private final String message;

    public Constraint(final String message) {
        this.message = message;
    }

    public Message getMessage(final T value, final MessageFormater formater) {
        if (verify(value)) {
            return null;
        }
        updateFormater(formater);
        return new Message(message, formater);
    }

    public abstract boolean verify(T value);

    protected abstract void updateFormater(MessageFormater formater);

    static boolean cmp(final boolean inferior, final boolean exclusive, final int value) {
        if (inferior && exclusive) {
            return value < 0;
        }
        if (inferior && !exclusive) {
            return value <= 0;
        }
        if (!inferior && exclusive) {
            return value > 0;
        }
        if (!inferior && !exclusive) {
            return value >= 0;
        }
        throw new BadProgrammerException("I cannot be !");
    }

    public static class OptionalConstraint<V> extends Constraint<V> {

        public OptionalConstraint(final String message) {
            super(message);
        }

        @Override
        public boolean verify(final V value) {
            if (value instanceof String) {
                return !((String) value).isEmpty();
            }
            return value != null;
        }

        @Override
        protected void updateFormater(final MessageFormater formater) {
            // Nothing to do.
        }
    }

    public static class PrecisionConstraint<V extends Comparable<V>> extends Constraint<V> {
        private final int precision;

        public PrecisionConstraint(final String message, final int precision) {
            super(message);
            this.precision = precision;
        }

        @Override
        public boolean verify(final V value) {
            if (value instanceof BigDecimal) {
                return ((BigDecimal) value).stripTrailingZeros().scale() < precision;
            }
            throw new BadProgrammerException("Precision is not supported for this class.");
        }

        @Override
        protected void updateFormater(final MessageFormater formater) {
            formater.addParameter("%constraint%", String.valueOf(precision));
        }
    }

    public static abstract class MinMaxConstraint<V extends Comparable<V>> extends Constraint<V> {
        protected final boolean isExclusive;
        protected final Integer minMax;

        public MinMaxConstraint(final String message, final int minMax, final boolean isExclusive) {
            super(message);
            this.minMax = minMax;
            this.isExclusive = isExclusive;
        }

        @Override
        protected void updateFormater(final MessageFormater formater) {
            formater.addParameter("%constraint%", String.valueOf(minMax));
        }

        @Override
        public boolean verify(final V value) {
            if (value == null) {
                return true;
            }

            if (value instanceof BigDecimal) {
                return cmp(isExclusive, (new BigDecimal(minMax).compareTo((BigDecimal) value)));
            }
            if (value instanceof String) {
                return cmp(isExclusive, (minMax.compareTo(((String) value).length())));
            }
            try {
                @SuppressWarnings("unchecked") final Comparable<V> theMin = value.getClass().cast(minMax);
                return cmp(isExclusive, theMin.compareTo(value));
            } catch (final ClassCastException e) {
                throw new BadProgrammerException("Constraint not allowed on this Class.", e);
            }
        }

        public abstract boolean cmp(boolean exclusive, int value);
    }

    public static class MinConstraint<V extends Comparable<V>> extends MinMaxConstraint<V> {
        public MinConstraint(final String message, final int min, final boolean isExclusive) {
            super(message, min, isExclusive);
        }

        @Override
        public boolean cmp(final boolean exclusive, final int value) {
            return Constraint.cmp(true, exclusive, value);
        }
    }

    public static class MaxConstraint<V extends Comparable<V>> extends MinMaxConstraint<V> {
        public MaxConstraint(final String message, final int max, final boolean isExclusive) {
            super(message, max, isExclusive);
        }

        @Override
        public boolean cmp(final boolean exclusive, final int value) {
            return Constraint.cmp(false, exclusive, value);
        }
    }

    public static class LengthConstraint<V extends Comparable<V>> extends MinMaxConstraint<V> {
        public LengthConstraint(final String message, final int length) {
            super(message, length, true);
        }

        @Override
        public boolean cmp(final boolean exclusive, final int value) {
            return value == 0;
        }
    }
}
